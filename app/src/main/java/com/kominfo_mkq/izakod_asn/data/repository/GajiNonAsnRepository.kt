package com.kominfo_mkq.izakod_asn.data.repository

import android.util.Log
import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class GajiNonAsnRepository {

    private val apiService = ApiClient.eabsenApiService
    private val liveEstimateRepository = PayrollLiveEstimateRepository()
    private val ssoEstimateCacheRepository = SsoPayrollEstimateCacheRepository()

    suspend fun getGajiSaya(
        tahun: Int? = null,
        bulan: Int? = null
    ): ApiResponse<GajiNonAsnMeResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "getGajiSaya request: tahun=$tahun, bulan=$bulan")
            val response = apiService.getGajiNonAsnSaya(
                tahun = tahun,
                bulan = bulan
            )

            if (response.isSuccessful) {
                val body = response.body()
                Log.d(
                    TAG,
                    "getGajiSaya official response: success=${body?.success}, hasData=${body?.data != null}, " +
                        "hasCalculation=${body?.data?.perhitungan != null}, " +
                        "officialTotal=${body?.data?.perhitungan?.totalDibayar}, " +
                        "officialStatus=${body?.data?.perhitungan?.status}"
                )
                if (body != null && body.success) {
                    val merged = mergeLiveEstimate(body, tahun, bulan)
                    Log.d(
                        TAG,
                        "getGajiSaya merged response: hasData=${merged.data != null}, " +
                            "hasCalculation=${merged.data?.perhitungan != null}, " +
                            "mergedTotal=${merged.data?.perhitungan?.totalDibayar}, " +
                            "mergedStatus=${merged.data?.perhitungan?.status}"
                    )
                    ApiResponse(
                        success = true,
                        data = merged
                    )
                } else {
                    val error = body?.message ?: "Gagal memuat Gaji Saya"
                    ssoFallbackResponse(tahun, bulan, error) ?: ApiResponse(
                        success = false,
                        error = error
                    )
                }
            } else {
                Log.w(TAG, "getGajiSaya failed HTTP: code=${response.code()}, message=${response.message()}")
                val error = parseErrorMessage(response, "Gagal memuat Gaji Saya")
                ssoFallbackResponse(tahun, bulan, error) ?: ApiResponse(
                    success = false,
                    error = error
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "getGajiSaya exception: ${e.message}", e)
            val error = e.message ?: "Network error"
            ssoFallbackResponse(tahun, bulan, error) ?: ApiResponse(success = false, error = error)
        }
    }

    private fun ssoFallbackResponse(
        tahun: Int?,
        bulan: Int?,
        reason: String
    ): ApiResponse<GajiNonAsnMeResponse>? {
        if (tahun == null || bulan == null) return null

        val fallback = ssoEstimateCacheRepository.getGajiFallbackResponse(
            tahun = tahun,
            bulan = bulan
        ) ?: return null

        Log.w(TAG, "getGajiSaya uses SSO fallback after official detail failure: $reason")
        return ApiResponse(
            success = true,
            data = fallback
        )
    }

    private suspend fun mergeLiveEstimate(
        body: GajiNonAsnMeResponse,
        tahun: Int?,
        bulan: Int?
    ): GajiNonAsnMeResponse {
        val currentData = body.data ?: run {
            Log.d(TAG, "mergeLiveEstimate skipped: body data is null")
            return body
        }
        if (tahun == null || bulan == null) {
            Log.d(TAG, "mergeLiveEstimate skipped: period is null")
            return body
        }

        val ssoCalculation = ssoEstimateCacheRepository.getGajiEstimate(
            tahun = tahun,
            bulan = bulan
        )
        if (ssoCalculation != null) {
            Log.d(
                TAG,
                "mergeLiveEstimate uses SSO cache: total=${ssoCalculation.totalDibayar}, " +
                    "status=${ssoCalculation.status}, potongan=${ssoCalculation.totalPotongan}"
            )
            return body.copy(
                data = currentData.copy(
                    perhitungan = ssoCalculation
                )
            )
        }

        Log.d(TAG, "mergeLiveEstimate SSO cache miss, trying live estimate endpoint")
        val estimateResponse = liveEstimateRepository.getGajiEstimasiBerjalan(
            tahun = tahun,
            bulan = bulan
        )
        val liveCalculation = estimateResponse.data?.data?.nominalGaji
        Log.d(
            TAG,
            "mergeLiveEstimate live response: success=${estimateResponse.success}, " +
                "hasNominal=${liveCalculation != null}, total=${liveCalculation?.totalDibayar}, " +
                "status=${liveCalculation?.status}, error=${estimateResponse.error}"
        )
        if (liveCalculation == null) return body

        return body.copy(
            data = currentData.copy(
                perhitungan = liveCalculation
            )
        )
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

    private companion object {
        private const val TAG = "IZAKOD_GAJI_REPO"
    }
}
