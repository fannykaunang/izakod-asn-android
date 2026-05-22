package com.kominfo_mkq.izakod_asn.data.remote

import com.kominfo_mkq.izakod_asn.data.local.AppContextHolder
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // const val BASE_URL = "http://192.168.110.236:3001/"
    const val BASE_URL = "https://izakod-asn.merauke.go.id/"
    const val API_KEY = "f26d27b0b8a01f0390767155e17745e2"

    private val refreshLock = Any()

    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        val newRequest = if (url.contains("entago.merauke.go.id")) {
            originalRequest.newBuilder()
                .addHeader("X-Api-Key", API_KEY)
                .addHeader("EabsenApiKey", API_KEY)
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val urlLoggingInterceptor = Interceptor { chain ->
        val request = chain.request()

        android.util.Log.d("ApiClient", "========================================")
        android.util.Log.d("ApiClient", "REQUEST URL: ${request.url}")
        android.util.Log.d("ApiClient", "Method: ${request.method}")
        android.util.Log.d("ApiClient", "Headers:")
        request.headers.forEach { (name, value) ->
            android.util.Log.d("ApiClient", "   $name: ${redactHeaderValue(name, value)}")
        }
        android.util.Log.d("ApiClient", "========================================")

        chain.proceed(request)
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url

        if (!isIzakodNextJsRequest(url) || shouldSkipMobileAuth(url)) {
            return@Interceptor chain.proceed(original)
        }

        val token = TokenStore.getToken()
        if (token.isNullOrBlank()) return@Interceptor chain.proceed(original)

        val newReq = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        chain.proceed(newReq)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
            redactHeader("X-Izakod-Mobile-Token")
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(urlLoggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator { _, response -> refreshIzakodMobileToken(response) }
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val eabsenApiService: EabsenApiService by lazy {
        retrofit.create(EabsenApiService::class.java)
    }

    fun executeAuthorized(request: Request): Response {
        return okHttpClient.newCall(request).execute()
    }

    private fun refreshIzakodMobileToken(response: Response): Request? {
        val url = response.request.url
        if (!isIzakodNextJsRequest(url)) return null
        if (responseCount(response) >= 2) return null
        if (shouldSkipMobileAuth(url)) return null

        val context = AppContextHolder.get() ?: return null
        val prefs = UserPreferences(context)
        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        return synchronized(refreshLock) {
            val latestToken = TokenStore.getToken() ?: prefs.getMobileJwtToken()
            if (!latestToken.isNullOrBlank() && latestToken != failedToken) {
                return@synchronized response.request.newBuilder()
                    .header("Authorization", "Bearer $latestToken")
                    .build()
            }

            val refreshToken = TokenStore.getRefreshToken() ?: prefs.getRefreshToken()
            if (refreshToken.isNullOrBlank()) return@synchronized null

            try {
                val refreshApi = createMobileRefreshApi()
                val refreshResponse = runBlocking {
                    refreshApi.refreshMobileToken(RefreshTokenRequest(refreshToken))
                }

                if (!refreshResponse.isSuccessful || refreshResponse.body()?.success != true) {
                    android.util.Log.w(
                        "ApiClient",
                        "Refresh token IZAKOD gagal: ${refreshResponse.code()}"
                    )
                    clearMobileTokens(prefs)
                    return@synchronized null
                }

                val newData = refreshResponse.body()?.data
                val newToken = newData?.token?.trim().orEmpty()
                val newRefreshToken = newData?.refreshToken?.trim().orEmpty()

                if (newToken.isEmpty() || newRefreshToken.isEmpty()) {
                    android.util.Log.w("ApiClient", "Refresh token IZAKOD response tidak lengkap")
                    clearMobileTokens(prefs)
                    return@synchronized null
                }

                prefs.setMobileJwtToken(newToken)
                prefs.setRefreshToken(newRefreshToken)
                TokenStore.setToken(newToken)
                TokenStore.setRefreshToken(newRefreshToken)

                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } catch (e: Exception) {
                android.util.Log.w("ApiClient", "Refresh token IZAKOD error: ${e.message}")
                null
            }
        }
    }

    private fun createMobileRefreshApi(): EabsenApiService {
        val refreshClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EabsenApiService::class.java)
    }

    private fun clearMobileTokens(prefs: UserPreferences) {
        prefs.setMobileJwtToken(null)
        prefs.setRefreshToken(null)
        TokenStore.setToken(null)
        TokenStore.setRefreshToken(null)
    }

    private fun isIzakodNextJsRequest(url: HttpUrl): Boolean {
        val base = BASE_URL.toHttpUrlOrNull() ?: return false
        return url.host == base.host && url.port == base.port
    }

    private fun shouldSkipMobileAuth(url: HttpUrl): Boolean {
        val path = url.encodedPath
        return path.endsWith("/api/login") ||
            path.endsWith("/api/mobile/token") ||
            path.endsWith("/api/mobile/refresh")
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    private fun redactHeaderValue(name: String, value: String): String {
        return when (name.lowercase()) {
            "authorization",
            "cookie",
            "set-cookie",
            "x-izakod-mobile-token" -> "[redacted]"
            else -> value
        }
    }
}
