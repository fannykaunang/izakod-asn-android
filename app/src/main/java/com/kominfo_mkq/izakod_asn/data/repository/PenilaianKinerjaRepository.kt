package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.CreatePenilaianKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaListResponse
import com.kominfo_mkq.izakod_asn.data.model.UpdatePenilaianKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PenilaianKinerjaRepository {

    private val apiService = ApiClient.eabsenApiService

    suspend fun getPenilaianKinerjaList(
        tahun: Int? = null,
        bulan: Int? = null,
        statusFinalisasi: String? = null,
        pegawaiId: Int? = null
    ): ApiResponse<PenilaianKinerjaListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPenilaianKinerjaList(
                tahun = tahun,
                bulan = bulan,
                statusFinalisasi = statusFinalisasi,
                pegawaiId = pegawaiId
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat penilaian kinerja"
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

    suspend fun getPenilaianKinerjaDetail(
        assessmentId: Int
    ): ApiResponse<PenilaianKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPenilaianKinerjaDetail(assessmentId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat detail penilaian kinerja"
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

    suspend fun createDraftPenilaianKinerja(
        tahun: Int,
        bulan: Int,
        pegawaiId: Int? = null,
        catatan: String? = null
    ): ApiResponse<PenilaianKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPenilaianKinerja(
                CreatePenilaianKinerjaRequest(
                    pegawaiId = pegawaiId,
                    tahun = tahun,
                    bulan = bulan,
                    catatan = catatan
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal membuat draft penilaian"
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

    suspend fun updatePenilaianKinerja(
        assessmentId: Int,
        nilaiTarget: Double?,
        nilaiRealisasi: Double?,
        nilaiAkhir: Double?,
        predikat: String?,
        catatan: String?,
        statusFinalisasi: String = "review"
    ): ApiResponse<PenilaianKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updatePenilaianKinerja(
                assessmentId = assessmentId,
                request = UpdatePenilaianKinerjaRequest(
                    nilaiTarget = nilaiTarget,
                    nilaiRealisasi = nilaiRealisasi,
                    nilaiAkhir = nilaiAkhir,
                    predikat = predikat,
                    catatan = catatan,
                    statusFinalisasi = statusFinalisasi
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal menyimpan penilaian kinerja"
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

    suspend fun finalisasiPenilaianKinerja(
        assessmentId: Int
    ): ApiResponse<PenilaianKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.finalisasiPenilaianKinerja(assessmentId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memfinalkan penilaian kinerja"
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
