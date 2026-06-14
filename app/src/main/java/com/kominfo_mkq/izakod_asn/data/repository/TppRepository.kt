package com.kominfo_mkq.izakod_asn.data.repository

import android.util.Log
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
    private val ssoEstimateCacheRepository = SsoPayrollEstimateCacheRepository()

    suspend fun getTppSaya(
        tahun: Int? = null,
        bulan: Int? = null
    ): ApiResponse<TppMeResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "getTppSaya request: tahun=$tahun, bulan=$bulan")
            val response = apiService.getTppSaya(
                tahun = tahun,
                bulan = bulan
            )

            if (response.isSuccessful) {
                val body = response.body()
                Log.d(
                    TAG,
                    "getTppSaya official response: success=${body?.success}, hasData=${body?.data != null}, " +
                        "hasNominal=${body?.data?.nominalTpp != null}, " +
                        "officialEstimasi=${body?.data?.nominalTpp?.estimasiDiterima}, " +
                        "officialTotalDibayar=${body?.data?.nominalTpp?.totalDibayar}, " +
                        "officialStatus=${body?.data?.nominalTpp?.status}"
                )
                if (body != null && body.success) {
                    val merged = mergeLiveEstimate(body, tahun, bulan)
                    Log.d(
                        TAG,
                        "getTppSaya merged response: hasData=${merged.data != null}, " +
                            "hasNominal=${merged.data?.nominalTpp != null}, " +
                            "mergedEstimasi=${merged.data?.nominalTpp?.estimasiDiterima}, " +
                            "mergedTotalDibayar=${merged.data?.nominalTpp?.totalDibayar}, " +
                            "mergedStatus=${merged.data?.nominalTpp?.status}"
                    )
                    ApiResponse(
                        success = true,
                        data = merged
                    )
                } else {
                    val error = body?.message ?: "Gagal memuat TPP Saya"
                    ssoFallbackResponse(tahun, bulan, error) ?: ApiResponse(
                        success = false,
                        error = error
                    )
                }
            } else {
                Log.w(TAG, "getTppSaya failed HTTP: code=${response.code()}, message=${response.message()}")
                val error = parseErrorMessage(response, "Gagal memuat TPP Saya")
                ssoFallbackResponse(tahun, bulan, error) ?: ApiResponse(
                    success = false,
                    error = error
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "getTppSaya exception: ${e.message}", e)
            val error = e.message ?: "Network error"
            ssoFallbackResponse(tahun, bulan, error) ?: ApiResponse(success = false, error = error)
        }
    }

    private fun ssoFallbackResponse(
        tahun: Int?,
        bulan: Int?,
        reason: String
    ): ApiResponse<TppMeResponse>? {
        if (tahun == null || bulan == null) return null

        val fallback = ssoEstimateCacheRepository.getTppFallbackResponse(
            tahun = tahun,
            bulan = bulan
        ) ?: return null

        Log.w(TAG, "getTppSaya uses SSO fallback after official detail failure: $reason")
        return ApiResponse(
            success = true,
            data = fallback
        )
    }

    private suspend fun mergeLiveEstimate(
        body: TppMeResponse,
        tahun: Int?,
        bulan: Int?
    ): TppMeResponse {
        val currentData = body.data ?: run {
            Log.d(TAG, "mergeLiveEstimate skipped: body data is null")
            return body
        }
        if (tahun == null || bulan == null) {
            Log.d(TAG, "mergeLiveEstimate skipped: period is null")
            return body
        }

        val ssoNominal = ssoEstimateCacheRepository.getTppEstimate(
            tahun = tahun,
            bulan = bulan
        )
        if (ssoNominal != null) {
            Log.d(
                TAG,
                "mergeLiveEstimate uses SSO cache: estimasi=${ssoNominal.estimasiDiterima}, " +
                    "totalDibayar=${ssoNominal.totalDibayar}, status=${ssoNominal.status}"
            )
            return body.copy(
                data = currentData.copy(
                    nominalTpp = ssoNominal
                )
            )
        }

        Log.d(TAG, "mergeLiveEstimate SSO cache miss, trying live estimate endpoint")
        val estimateResponse = liveEstimateRepository.getTppEstimasiBerjalan(
            tahun = tahun,
            bulan = bulan
        )
        val liveData = estimateResponse.data?.data
        val liveNominal = liveData?.nominalTpp
        Log.d(
            TAG,
            "mergeLiveEstimate live response: success=${estimateResponse.success}, " +
                "hasNominal=${liveNominal != null}, estimasi=${liveNominal?.estimasiDiterima}, " +
                "totalDibayar=${liveNominal?.totalDibayar}, status=${liveNominal?.status}, " +
                "error=${estimateResponse.error}"
        )
        if (liveNominal == null) return body

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

    private companion object {
        private const val TAG = "IZAKOD_TPP_REPO"
    }
}
