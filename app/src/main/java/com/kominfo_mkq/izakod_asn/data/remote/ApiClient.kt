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

    const val BASE_URL = "http://192.168.110.236:3000/"
    const val API_KEY = "f26d27b0b8a01f0390767155e17745e2"

    private const val ENTAGO_BASE_URL = "https://entago.merauke.go.id/"
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
            android.util.Log.d("ApiClient", "   $name: $value")
        }
        android.util.Log.d("ApiClient", "========================================")

        chain.proceed(request)
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url

        val base = BASE_URL.toHttpUrlOrNull()
        val isNextJs = base != null && url.host == base.host && url.port == base.port
        val isLoginRequest = url.encodedPath.endsWith("/api/login")

        if (!isNextJs || isLoginRequest) return@Interceptor chain.proceed(original)

        val token = TokenStore.getToken()
        if (token.isNullOrBlank()) return@Interceptor chain.proceed(original)

        val newReq = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        android.util.Log.d("ApiClient", "JWT token (len) = ${token.length} for ${original.url}")

        chain.proceed(newReq)
    }

    private fun persistTokens(accessToken: String, refreshToken: String) {
        TokenStore.setToken(accessToken)
        TokenStore.setRefreshToken(refreshToken)

        AppContextHolder.get()?.let { context ->
            val prefs = UserPreferences(context)
            prefs.setMobileJwtToken(accessToken)
            prefs.setRefreshToken(refreshToken)
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(urlLoggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
            .authenticator { _, response ->
                if (responseCount(response) >= 2) return@authenticator null
                if (response.request.url.encodedPath.endsWith("/api/login")) return@authenticator null

                val failedToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")
                    ?.trim()

                val refreshToken = TokenStore.getRefreshToken()?.trim()
                if (refreshToken.isNullOrEmpty()) return@authenticator null

                val refreshClient = OkHttpClient.Builder()
                    .addInterceptor { refreshChain ->
                        val refreshRequest = refreshChain.request().newBuilder()
                            .header("X-Api-Key", API_KEY)
                            .header("EabsenApiKey", API_KEY)
                            .header("Content-Type", "application/json")
                            .build()
                        refreshChain.proceed(refreshRequest)
                    }
                    .build()

                val refreshApi = Retrofit.Builder()
                    .baseUrl(ENTAGO_BASE_URL)
                    .client(refreshClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(EabsenCoreApiService::class.java)

                return@authenticator synchronized(refreshLock) {
                    val latestToken = TokenStore.getToken()?.trim()
                    if (!latestToken.isNullOrEmpty() && latestToken != failedToken) {
                        return@synchronized response.request.newBuilder()
                            .header("Authorization", "Bearer $latestToken")
                            .build()
                    }

                    try {
                        val refreshResponse = runBlocking {
                            refreshApi.refreshToken(RefreshTokenRequest(refreshToken))
                        }

                        if (!refreshResponse.isSuccessful || refreshResponse.body()?.success != true) {
                            android.util.Log.e("ApiClient", "Refresh token gagal: ${refreshResponse.code()}")
                            TokenStore.setToken(null)
                            TokenStore.setRefreshToken(null)
                            return@synchronized null
                        }

                        val newData = refreshResponse.body()?.data
                        val newToken = newData?.token?.trim().orEmpty()
                        val newRefreshToken = newData?.refreshToken?.trim().orEmpty()

                        if (newToken.isEmpty() || newRefreshToken.isEmpty()) {
                            android.util.Log.e("ApiClient", "Refresh response tidak lengkap")
                            TokenStore.setToken(null)
                            TokenStore.setRefreshToken(null)
                            return@synchronized null
                        }

                        persistTokens(newToken, newRefreshToken)

                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    } catch (e: Exception) {
                        android.util.Log.e("ApiClient", "Refresh fatal: ${e.message}", e)
                        null
                    }
                }
            }
            .addInterceptor(loggingInterceptor)
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

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}
