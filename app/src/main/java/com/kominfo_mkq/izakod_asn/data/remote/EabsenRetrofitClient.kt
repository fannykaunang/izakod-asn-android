package com.kominfo_mkq.izakod_asn.data.remote

import com.kominfo_mkq.izakod_asn.data.local.AppContextHolder
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.RefreshTokenRequest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object EabsenRetrofitClient {

    private const val EABSEN_BASE_URL = "https://entago.merauke.go.id/"
    private const val API_KEY = "f26d27b0b8a01f0390767155e17745e2"
    private val refreshLock = Any()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val headerInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
            .header("X-Api-Key", API_KEY)
            .header("EabsenApiKey", API_KEY)
            .method(originalRequest.method, originalRequest.body)

        val token = AppContextHolder.get()?.let { context ->
            UserPreferences(context).getEntagoAccessToken()
        }
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        chain.proceed(builder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(headerInterceptor)
        .authenticator { _, response ->
            if (responseCount(response) >= 2) return@authenticator null
            if (response.request.url.encodedPath.endsWith("/api/auth/refresh")) return@authenticator null

            val failedToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()

            val refreshToken = AppContextHolder.get()?.let { context ->
                UserPreferences(context).getEntagoRefreshToken()
            }
            if (refreshToken.isNullOrBlank()) return@authenticator null

            val refreshClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("X-Api-Key", API_KEY)
                        .header("EabsenApiKey", API_KEY)
                        .header("Content-Type", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val refreshApi = Retrofit.Builder()
                .baseUrl(EABSEN_BASE_URL)
                .client(refreshClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(EabsenCoreApiService::class.java)

            return@authenticator synchronized(refreshLock) {
                val latestToken = AppContextHolder.get()?.let { context ->
                    UserPreferences(context).getEntagoAccessToken()
                }?.trim()
                if (!latestToken.isNullOrBlank() && latestToken != failedToken) {
                    return@synchronized response.request.newBuilder()
                        .header("Authorization", "Bearer $latestToken")
                        .build()
                }

                try {
                    val refreshResponse = runBlocking {
                        refreshApi.refreshToken(RefreshTokenRequest(refreshToken))
                    }

                    if (!refreshResponse.isSuccessful || refreshResponse.body()?.success != true) {
                        android.util.Log.e(
                            "EabsenRetrofitClient",
                            "Refresh token gagal: ${refreshResponse.code()}"
                        )
                        AppContextHolder.get()?.let { context ->
                            val prefs = UserPreferences(context)
                            prefs.setEntagoAccessToken(null)
                            prefs.setEntagoRefreshToken(null)
                        }
                        return@synchronized null
                    }

                    val newData = refreshResponse.body()?.data
                    val newToken = newData?.token?.trim().orEmpty()
                    val newRefreshToken = newData?.refreshToken?.trim().orEmpty()

                    if (newToken.isEmpty() || newRefreshToken.isEmpty()) {
                        android.util.Log.e("EabsenRetrofitClient", "Refresh response tidak lengkap")
                        AppContextHolder.get()?.let { context ->
                            val prefs = UserPreferences(context)
                            prefs.setEntagoAccessToken(null)
                            prefs.setEntagoRefreshToken(null)
                        }
                        return@synchronized null
                    }

                    AppContextHolder.get()?.let { context ->
                        val prefs = UserPreferences(context)
                        prefs.setEntagoAccessToken(newToken)
                        prefs.setEntagoRefreshToken(newRefreshToken)
                    }

                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } catch (e: Exception) {
                    android.util.Log.e("EabsenRetrofitClient", "Refresh fatal: ${e.message}", e)
                    null
                }
            }
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EABSEN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: EabsenCoreApiService by lazy {
        retrofit.create(EabsenCoreApiService::class.java)
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
