package com.kominfo_mkq.izakod_asn.data.repository

import android.util.Log
import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeData
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnPerhitungan
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeResponse
import com.kominfo_mkq.izakod_asn.data.model.PayrollDisplay
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
        if (currentData.hasOfficialPayroll()) {
            Log.d(
                TAG,
                "mergeLiveEstimate skipped: official payroll already available " +
                    "status=${currentData.perhitungan?.status}, display=${currentData.displayPayroll?.status}"
            )
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
                    perhitungan = ssoCalculation,
                    displayPayroll = ssoCalculation.toEstimateDisplayPayroll("sso_payroll_estimate_cache")
                )
            )
        }

        Log.d(TAG, "mergeLiveEstimate SSO cache miss, trying live estimate endpoint")
        val estimateResponse = liveEstimateRepository.getGajiEstimasiBerjalan(
            tahun = tahun,
            bulan = bulan
        )
        val liveData = estimateResponse.data?.data
        val liveCalculation = liveData?.nominalGaji
        Log.d(
            TAG,
            "mergeLiveEstimate live response: success=${estimateResponse.success}, " +
                "available=${liveData?.available}, " +
                "hasNominal=${liveCalculation != null}, total=${liveCalculation?.totalDibayar}, " +
                "status=${liveCalculation?.status}, error=${estimateResponse.error}"
        )
        if (!estimateResponse.success || liveCalculation == null || liveData?.available == false) {
            val reason = estimateResponse.error
                ?: liveData?.message
                ?: "Estimasi berjalan belum tersedia"
            return body.withEstimateUnavailableMessage(reason)
        }

        return body.copy(
            data = currentData.copy(
                perhitungan = liveCalculation,
                displayPayroll = liveCalculation.toEstimateDisplayPayroll(
                    liveData.source ?: "entago_live_snapshot"
                )
            )
        )
    }

    private fun GajiNonAsnMeData.hasOfficialPayroll(): Boolean {
        val displayStatus = displayPayroll?.status?.lowercase()
        if (displayStatus == "resmi") return true

        val status = perhitungan?.status?.lowercase()
        return perhitungan != null && status in OFFICIAL_GAJI_STATUSES
    }

    private fun GajiNonAsnMeResponse.withEstimateUnavailableMessage(
        reason: String
    ): GajiNonAsnMeResponse {
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

    private fun GajiNonAsnPerhitungan.toEstimateDisplayPayroll(source: String): PayrollDisplay {
        return PayrollDisplay(
            status = "estimasi",
            nominal = totalDibayar,
            source = source,
            label = "Estimasi berjalan",
            badge = "ESTIMASI",
            message = "Nominal ini masih estimasi berjalan dan dapat berubah sampai OPD memfinalkan perhitungan.",
            isFinal = false,
            detailStatus = status
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
        private val OFFICIAL_GAJI_STATUSES = setOf(
            "dihitung",
            "diajukan_opd",
            "diverifikasi_opd",
            "final_opd",
            "dibayar",
            "arsip"
        )
    }
}
