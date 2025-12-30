package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.EabsenLoginResponse
import com.kominfo_mkq.izakod_asn.data.model.LoginRequest
import com.kominfo_mkq.izakod_asn.data.model.PegawaiData
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository untuk handle authentication dan API calls
 */
class AuthRepository {

    private val apiService = ApiClient.eabsenApiService

    /**
     * Login ke eAbsen API
     *
     * @param email Email atau username
     * @param password Password
     * @return ApiResponse dengan EabsenLoginResponse
     */
    suspend fun login(
        email: String,
        password: String
    ): ApiResponse<EabsenLoginResponse> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(email = email, pwd = password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.result == 1) {
                    // ✅ Login berhasil, sekarang fetch pegawai_id by PIN
                    val pin = body.pin

                    try {
                        // Fetch pegawai data from Next.js
                        val pegawaiResponse = apiService.getPegawai(pin)

                        if (pegawaiResponse.isSuccessful && pegawaiResponse.body() != null) {
                            val pegawaiData = pegawaiResponse.body()!!

                            // ✅ Return login response dengan pegawai_id
                            val updatedResponse = body.copy(
                                pegawaiId = pegawaiData.pegawaiId
                            )

                            return@withContext ApiResponse(
                                success = true,
                                data = updatedResponse
                            )
                        } else {
                            // Pegawai not found, return without pegawai_id
                            return@withContext ApiResponse(
                                success = true,
                                data = body.copy(pegawaiId = null)
                            )
                        }
                    } catch (e: Exception) {
                        // Fetch pegawai failed, continue without pegawai_id
                        e.printStackTrace()
                        return@withContext ApiResponse(
                            success = true,
                            data = body.copy(pegawaiId = null)
                        )
                    }
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.response ?: "Login gagal"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Login gagal: ${response.code()} ${response.message()}"
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
     * Fetch data pegawai by PIN
     *
     * @param pin PIN pegawai
     * @return ApiResponse dengan PegawaiData
     */
    suspend fun getPegawaiData(pin: String): ApiResponse<PegawaiData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPegawai(pin)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    ApiResponse(
                        success = true,
                        data = body
                    )
                } else {
                    ApiResponse(
                        success = false,
                        error = "Data pegawai tidak ditemukan"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Gagal mengambil data pegawai: ${response.code()}"
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