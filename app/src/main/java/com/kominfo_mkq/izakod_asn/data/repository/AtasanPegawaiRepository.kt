package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiData
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanItem
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanMutationResponse
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanRequest
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanVerifyRequest
import com.kominfo_mkq.izakod_asn.data.model.KandidatBawahanItem
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class AtasanPegawaiRepository {

    private val apiService = ApiClient.eabsenApiService

    suspend fun getBawahanSaya(): ApiResponse<List<AtasanPegawaiData>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBawahanSaya()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        ApiResponse(success = true, data = body.data)
                    } else {
                        ApiResponse(
                            success = false,
                            error = body?.message ?: "Gagal memuat daftar bawahan"
                        )
                    }
                } else {
                    ApiResponse(success = false, error = response.errorMessage())
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message ?: "Koneksi bermasalah")
            }
        }

    suspend fun getKandidatBawahan(
        search: String? = null,
        limit: Int = 100
    ): ApiResponse<List<KandidatBawahanItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getKandidatBawahan(search = search, limit = limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    ApiResponse(success = true, data = body.data)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat kandidat bawahan"
                    )
                }
            } else {
                ApiResponse(success = false, error = response.errorMessage())
            }
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun getUsulan(
        status: String? = null,
        limit: Int = 100
    ): ApiResponse<List<AtasanPegawaiUsulanItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAtasanPegawaiUsulan(status = status, limit = limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    ApiResponse(success = true, data = body.data)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat usulan atasan-bawahan"
                    )
                }
            } else {
                ApiResponse(success = false, error = response.errorMessage())
            }
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun createUsulan(
        request: AtasanPegawaiUsulanRequest
    ): ApiResponse<AtasanPegawaiUsulanMutationResponse> = withContext(Dispatchers.IO) {
        try {
            handleMutation(apiService.createAtasanPegawaiUsulan(request))
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun updateUsulan(
        id: Int,
        request: AtasanPegawaiUsulanRequest
    ): ApiResponse<AtasanPegawaiUsulanMutationResponse> = withContext(Dispatchers.IO) {
        try {
            handleMutation(apiService.updateAtasanPegawaiUsulan(id, request))
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun submitUsulan(
        id: Int,
        request: AtasanPegawaiUsulanRequest
    ): ApiResponse<AtasanPegawaiUsulanMutationResponse> = withContext(Dispatchers.IO) {
        try {
            handleMutation(apiService.submitAtasanPegawaiUsulan(id, request))
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message ?: "Koneksi bermasalah")
        }
    }

    suspend fun cancelUsulan(id: Int): ApiResponse<AtasanPegawaiUsulanMutationResponse> =
        withContext(Dispatchers.IO) {
            try {
                handleMutation(apiService.cancelAtasanPegawaiUsulan(id))
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message ?: "Koneksi bermasalah")
            }
        }

    suspend fun verifyUsulan(
        id: Int,
        request: AtasanPegawaiUsulanVerifyRequest
    ): ApiResponse<AtasanPegawaiUsulanMutationResponse> = withContext(Dispatchers.IO) {
        try {
            handleMutation(apiService.verifyAtasanPegawaiUsulan(id, request))
        } catch (e: Exception) {
            ApiResponse(success = false, error = e.message ?: "Koneksi bermasalah")
        }
    }

    private fun handleMutation(
        response: Response<AtasanPegawaiUsulanMutationResponse>
    ): ApiResponse<AtasanPegawaiUsulanMutationResponse> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                ApiResponse(success = true, data = body)
            } else {
                ApiResponse(
                    success = false,
                    error = body?.message ?: "Gagal menyimpan usulan atasan-bawahan"
                )
            }
        } else {
            ApiResponse(success = false, error = response.errorMessage())
        }
    }

    private fun Response<*>.errorMessage(): String {
        val fallback = "Error: ${code()} ${message()}".trim()
        val raw = runCatching { errorBody()?.string() }.getOrNull().orEmpty()
        if (raw.isBlank()) return fallback

        return runCatching {
            JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
        }.getOrNull() ?: fallback
    }
}
