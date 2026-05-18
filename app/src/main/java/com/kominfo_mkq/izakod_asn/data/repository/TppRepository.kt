package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.TppMeResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TppRepository {

    private val apiService = ApiClient.eabsenApiService

    suspend fun getTppSaya(
        tahun: Int? = null,
        bulan: Int? = null
    ): ApiResponse<TppMeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTppSaya(
                tahun = tahun,
                bulan = bulan
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat TPP Saya"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }
}
