package com.kominfo_mkq.izakod_asn.data.remote

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit API Client Configuration
 */
object ApiClient {

    // Base URL untuk Next.js server
    private const val BASE_URL = "http://192.168.110.236:3000/"
    const val API_KEY = "zkENw7654FBWHmNupvi2BbcXxhPHvF"

    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .addHeader("EabsenApiKey", API_KEY)
            .build()
        chain.proceed(newRequest)
    }

    /**
     * Simple CookieJar to store session cookies
     */
    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    /**
     * OkHttpClient dengan interceptor untuk logging dan cookie handling
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .cookieJar(cookieJar)  // Enable cookie storage
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * IZAKOD API Service instance (for authenticated endpoints)
     */
    val eabsenApiService: EabsenApiService by lazy {
        retrofit.create(EabsenApiService::class.java)
    }

    /**
     * Clear all stored cookies (call on logout)
     */
    fun clearCookies() {
        cookieJar.clearAll()
    }
}

/**
 * Network Configuration Helper
 */
object NetworkConfig {

    /**
     * Menentukan base URL berdasarkan environment
     */
    fun getBaseUrl(isDevelopment: Boolean = true): String {
        return if (isDevelopment) {
            // Development - gunakan IP lokal
            "http://192.168.110.236:3000/"
        } else {
            // Production - ganti dengan domain production
            "https://izakod.merauke.go.id/"
        }
    }

    /**
     * Check apakah menggunakan emulator atau device fisik
     */
    fun isEmulator(): Boolean {
        return (android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk" == android.os.Build.PRODUCT)
    }
}

/**
 * Extension function untuk CookieJar
 */
private fun CookieJar.clearAll() {
    // Implementation depends on your CookieJar
    // Add if needed
}