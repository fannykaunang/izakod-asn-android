package com.kominfo_mkq.izakod_asn.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object EabsenRetrofitClient {

    private const val EABSEN_BASE_URL = "https://dev.api.eabsen.merauke.go.id/"
    private const val API_KEY = "zkENw7654FBWHmNupvi2BbcXxhPHvF" // Ganti dengan API Key yang sebenarnya

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 1. Buat Header Interceptor
    private val headerInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithHeader = originalRequest.newBuilder()
            .header("EabsenApiKey", API_KEY) // Menambahkan header ke setiap request
            .method(originalRequest.method, originalRequest.body)
            .build()
        chain.proceed(requestWithHeader)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(headerInterceptor) // 2. Daftarkan Interceptor di sini
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
}
