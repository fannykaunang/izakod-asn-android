// network/MobileApi.kt
package com.kominfo_mkq.izakod_asn.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object MobileApi {
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // ganti base url kamu
    private const val BASE_URL = "https://your-domain.com"

    fun registerFcmToken(jwt: String, bodyJson: String): Boolean {
        val req = Request.Builder()
            .url("$BASE_URL/api/mobile/fcm/register")
            .post(bodyJson.toRequestBody(JSON))
            .addHeader("Authorization", "Bearer $jwt")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(req).execute().use { res ->
            return res.isSuccessful
        }
    }

    fun pushTest(jwt: String, bodyJson: String): Boolean {
        val req = Request.Builder()
            .url("$BASE_URL/api/mobile/fcm/test")
            .post(bodyJson.toRequestBody(JSON))
            .addHeader("Authorization", "Bearer $jwt")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(req).execute().use { res ->
            return res.isSuccessful
        }
    }
}
