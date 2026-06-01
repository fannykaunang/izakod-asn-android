package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.TppMeResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class TppRepository {

    private val apiService = ApiClient.eabsenApiService
    private val liveEstimateRepository = PayrollLiveEstimateRepository()

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
                    ApiResponse(
                        success = true,
                        data = mergeLiveEstimate(body, tahun, bulan)
                    )
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat TPP Saya"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = parseErrorMessage(response, "Gagal memuat TPP Saya")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    private suspend fun mergeLiveEstimate(
        body: TppMeResponse,
        tahun: Int?,
        bulan: Int?
    ): TppMeResponse {
        val currentData = body.data ?: return body
        if (tahun == null || bulan == null) return body

        val estimateResponse = liveEstimateRepository.getTppEstimasiBerjalan(
            tahun = tahun,
            bulan = bulan
        )
        val liveData = estimateResponse.data?.data
        val liveNominal = liveData?.nominalTpp ?: return body

        val mergedNominal = liveNominal.copy(
            label = liveNominal.label ?: liveData.label
        )

        return body.copy(
            data = currentData.copy(
                nominalTpp = mergedNominal
            )
        )
    }

    private fun parseErrorMessage(
        response: Response<TppMeResponse>,
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
                "NON_ASN_NOT_ELIGIBLE_FOR_TPP" -> "Pegawai Honorer/Kontrak menggunakan menu Gaji Saya."
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
