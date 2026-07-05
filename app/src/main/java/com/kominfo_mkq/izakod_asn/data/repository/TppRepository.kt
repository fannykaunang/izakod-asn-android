package com.kominfo_mkq.izakod_asn.data.repository

import android.util.Log
import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.PayrollDisplay
import com.kominfo_mkq.izakod_asn.data.model.TppMeData
import com.kominfo_mkq.izakod_asn.data.model.TppMeResponse
import com.kominfo_mkq.izakod_asn.data.model.TppNominal
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
        if (currentData.hasOfficialPayroll()) {
            Log.d(
                TAG,
                "mergeLiveEstimate skipped: official payroll already available " +
                    "status=${currentData.nominalTpp?.status}, display=${currentData.displayPayroll?.status}"
            )
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
                    nominalTpp = ssoNominal,
                    displayPayroll = ssoNominal.toEstimateDisplayPayroll("sso_payroll_estimate_cache")
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
                "available=${liveData?.available}, " +
                "hasNominal=${liveNominal != null}, estimasi=${liveNominal?.estimasiDiterima}, " +
                "totalDibayar=${liveNominal?.totalDibayar}, status=${liveNominal?.status}, " +
                "error=${estimateResponse.error}"
        )
        if (!estimateResponse.success || liveNominal == null || liveData?.available == false) {
            val reason = estimateResponse.error
                ?: liveData?.message
                ?: "Estimasi berjalan belum tersedia"
            return body.withEstimateUnavailableMessage(reason)
        }

        val mergedNominal = liveNominal.copy(
            label = liveNominal.label ?: liveData.label
        )

        return body.copy(
            data = currentData.copy(
                nominalTpp = mergedNominal,
                displayPayroll = mergedNominal.toEstimateDisplayPayroll(liveData?.source ?: "entago_live_snapshot")
            )
        )
    }

    private fun TppMeData.hasOfficialPayroll(): Boolean {
        val displayStatus = displayPayroll?.status?.lowercase()
        if (displayStatus == "resmi") return true

        val status = nominalTpp?.status?.lowercase()
        return nominalTpp != null && status in OFFICIAL_TPP_STATUSES
    }

    private fun TppMeResponse.withEstimateUnavailableMessage(
        reason: String
    ): TppMeResponse {
        val currentData = data ?: return this
        val currentDisplay = currentData.displayPayroll
        val cleanedReason = reason.trim().trimEnd('.')
        val message = when {
            cleanedReason.isBlank() ->
                "Data resmi belum dihitung. Estimasi berjalan belum bisa dimuat saat ini."
            cleanedReason.contains("sesi", ignoreCase = true) ||
                cleanedReason.contains("token", ignoreCase = true) ||
                cleanedReason.contains("belum tersedia di perangkat", ignoreCase = true) ->
                "Data resmi belum dihitung. Estimasi berjalan belum bisa dimuat karena sesi E-NTAGO perlu diperbarui."
            else ->
                "Data resmi belum dihitung. Estimasi berjalan belum bisa dimuat: $cleanedReason."
        }

        return copy(
            data = currentData.copy(
                displayPayroll = (currentDisplay ?: PayrollDisplay(
                    status = "belum",
                    nominal = null,
                    source = "estimasi_berjalan_unavailable",
                    label = "Belum dihitung",
                    badge = "BELUM",
                    isFinal = false,
                    detailStatus = null
                )).copy(
                    status = currentDisplay?.status ?: "belum",
                    badge = currentDisplay?.badge ?: "BELUM",
                    message = message
                )
            )
        )
    }

    private fun TppNominal.toEstimateDisplayPayroll(source: String): PayrollDisplay {
        val nominal = estimasiDiterima.takeIf { it > 0.0 }
            ?: totalDibayar.takeIf { it > 0.0 }
            ?: totalNetto.takeIf { it > 0.0 }

        return PayrollDisplay(
            status = "estimasi",
            nominal = nominal,
            source = source,
            label = label?.takeIf { it.isNotBlank() } ?: "Estimasi berjalan",
            badge = "ESTIMASI",
            message = "Nominal ini masih estimasi berjalan dan dapat berubah sampai OPD memfinalkan perhitungan.",
            isFinal = false,
            detailStatus = status
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
        private val OFFICIAL_TPP_STATUSES = setOf(
            "dihitung",
            "diajukan_opd",
            "diverifikasi_opd",
            "final_opd",
            "dibayar",
            "arsip"
        )
    }
}
