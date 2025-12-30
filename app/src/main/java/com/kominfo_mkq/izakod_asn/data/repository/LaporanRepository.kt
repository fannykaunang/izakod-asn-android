package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanResponse
import com.kominfo_mkq.izakod_asn.data.model.KategoriListResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository untuk Laporan Kegiatan
 */
class LaporanRepository {

    private val apiService = ApiClient.eabsenApiService

    /**
     * Get list kategori kegiatan
     */
    suspend fun getKategoriList(): ApiResponse<KategoriListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getKategoriList(isActive = 1)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.success) {
                    ApiResponse(
                        success = true,
                        data = body
                    )
                } else {
                    ApiResponse(
                        success = false,
                        error = "Gagal memuat kategori"
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
            ApiResponse(
                success = false,
                error = e.message ?: "Network error"
            )
        }
    }

    /**
     * Create new laporan kegiatan
     */
    suspend fun createLaporan(
        request: CreateLaporanRequest
    ): ApiResponse<CreateLaporanResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createLaporan(request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.success) {
                    ApiResponse(
                        success = true,
                        data = body
                    )
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal membuat laporan"
                    )
                }
            } else {
                // Parse error body
                val errorBody = response.errorBody()?.string()
                val requiresAttendance = errorBody?.contains("requiresAttendance") == true

                ApiResponse(
                    success = false,
                    error = if (requiresAttendance) {
                        "Anda belum absen pada tanggal yang dipilih. Silakan absen terlebih dahulu."
                    } else {
                        "Error: ${response.code()} ${response.message()}"
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(
                success = false,
                error = e.message ?: "Network error"
            )
        }
    }
}