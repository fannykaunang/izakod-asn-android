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

object ApiClient {

    const val BASE_URL = "http://192.168.110.236:3000/"
    const val API_KEY = "zkENw7654FBWHmNupvi2BbcXxhPHvF"

    // ✅ FIXED: Only add API key for Next.js requests, not ASP.NET
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // ✅ Only add API key if NOT calling ASP.NET server
        val newRequest = if (url.contains("dev.api.eabsen.merauke.go.id")) {
            originalRequest.newBuilder()
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

        // ✅ Log the complete URL being called
        android.util.Log.d("ApiClient", "========================================")
        android.util.Log.d("ApiClient", "🌐 REQUEST URL: ${request.url}")
        android.util.Log.d("ApiClient", "📍 Method: ${request.method}")
        android.util.Log.d("ApiClient", "🔑 Headers:")
        request.headers.forEach { (name, value) ->
            android.util.Log.d("ApiClient", "   $name: $value")
        }
        android.util.Log.d("ApiClient", "========================================")

        chain.proceed(request)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(urlLoggingInterceptor)
            .addInterceptor(apiKeyInterceptor)
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

    fun clearCookies() {
        (okHttpClient.cookieJar as? CookieJar)?.let {
            // Clear implementation if needed
        }
    }
}

object NetworkConfig {
    fun getBaseUrl(isDevelopment: Boolean = true): String {
        return if (isDevelopment) {
            "http://192.168.110.236:3000/"
        } else {
            "https://izakod.merauke.go.id/"
        }
    }

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