package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.PengumumanHighlightItem
import com.kominfo_mkq.izakod_asn.data.model.PengumumanReadDetail
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class PengumumanRepository {

    private val apiService = ApiClient.eabsenApiService

    suspend fun getHighlights(
        limit: Int = 5
    ): ApiResponse<List<PengumumanHighlightItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPengumumanHighlights(limit = limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    ApiResponse(success = true, data = body.data)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat pengumuman"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = response.errorMessage("Gagal memuat pengumuman")
                )
            }
        } catch (error: Exception) {
            ApiResponse(
                success = false,
                error = error.message ?: "Koneksi bermasalah saat memuat pengumuman"
            )
        }
    }

    suspend fun getReadDetail(
        id: Int
    ): ApiResponse<PengumumanReadDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPengumumanReadDetail(id)
            if (response.isSuccessful) {
                val body = response.body()
                val detail = body?.data
                if (body?.success == true && detail != null) {
                    ApiResponse(success = true, data = detail)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Pengumuman tidak ditemukan"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = response.errorMessage("Gagal memuat detail pengumuman")
                )
            }
        } catch (error: Exception) {
            ApiResponse(
                success = false,
                error = error.message ?: "Koneksi bermasalah saat memuat detail pengumuman"
            )
        }
    }

    private fun Response<*>.errorMessage(fallback: String): String {
        val raw = runCatching { errorBody()?.string() }.getOrNull().orEmpty()
        if (raw.isBlank()) return "$fallback: ${code()} ${message()}".trim()

        return runCatching {
            JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
        }.getOrNull() ?: "$fallback: ${code()} ${message()}".trim()
    }
}
