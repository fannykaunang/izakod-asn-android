package com.kominfo_mkq.izakod_asn.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit API Client Configuration
 */
object ApiClient_old {

    // PENTING: Untuk Android Emulator, gunakan 10.0.2.2 untuk akses localhost
    // Untuk device fisik, gunakan IP komputer (contoh: 192.168.1.100)

    // Base URL untuk API Dev Laptop
    //private const val BASE_URL = "http://192.168.110.236:3000/api/"  // Localhost untuk emulator
    private const val BASE_URL = "https://dev.api.eabsen.merauke.go.id/api/"  // Localhost untuk emulator


    // private const val BASE_URL = "http://192.168.1.100/api/"  // Untuk device fisik
    // private const val BASE_URL = "http://10.10.10.188/api/"  // Production server

    // API Key untuk eAbsen
    const val API_KEY = "zkENw7654FBWHmNupvi2BbcXxhPHvF"  // Ganti dengan API key yang benar

    /**
     * OkHttpClient dengan interceptor untuk logging
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
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
     * API Service instance
     */
    val apiService: EabsenApiService by lazy {
        retrofit.create(EabsenApiService::class.java)
    }
}

/**
 * Network Configuration Helper
 */
object NetworkConfig_old {

    /**
     * Menentukan base URL berdasarkan environment
     *
     * Development:
     * - Emulator: 10.0.2.2
     * - Genymotion: 10.0.3.2
     * - Device fisik: IP lokal komputer (cek dengan ipconfig/ifconfig)
     *
     * Production:
     * - Server URL: http://10.10.10.188/api/
     */
    fun getBaseUrl(isDevelopment: Boolean = true): String {
        return if (isDevelopment) {
            // Development - gunakan localhost
            "http://10.0.2.2/api/"  // Default untuk Android Emulator
        } else {
            // Production
            "http://10.10.10.188/api/"
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