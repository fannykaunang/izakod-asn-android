package com.kominfo_mkq.izakod_asn.data.remote

import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url

        // hanya untuk Next.js base url
        val base = BASE_URL.toHttpUrlOrNull()
        val isNextJs =
            base != null &&
                    url.host == base.host &&
                    url.port == base.port

        if (!isNextJs) return@Interceptor chain.proceed(original)

        val token = TokenStore.getToken()
        if (token.isNullOrBlank()) return@Interceptor chain.proceed(original)

        val newReq = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        android.util.Log.d("ApiClient", "JWT token (len) = ${token.length} for ${original.url}")

        chain.proceed(newReq)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
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
}