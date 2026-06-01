package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class GajiNonAsnRepository {

    private val apiService = ApiClient.eabsenApiService

    suspend fun getGajiSaya(
        tahun: Int? = null,
        bulan: Int? = null
    ): ApiResponse<GajiNonAsnMeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getGajiNonAsnSaya(
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
                        error = body?.message ?: "Gagal memuat Gaji Saya"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = parseErrorMessage(response, "Gagal memuat Gaji Saya")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    private fun parseErrorMessage(
        response: Response<GajiNonAsnMeResponse>,
        fallback: String
    ): String {
        val raw = response.errorBody()?.string()
        val parsedMessage = try {
            val json = JSONObject(raw ?: "{}")
            val code = json.optString("code")
            val message = json.optString("message").ifBlank {
                json.optString("response")
            }

            when (code) {
                "ASN_USES_TPP" -> "Pegawai ASN/PPPK menggunakan menu TPP Saya."
                else -> message
            }
        } catch (_: Exception) {
            ""
        }

        return parsedMessage.ifBlank {
            "$fallback: ${response.code()} ${response.message()}"
        }
    }
}
