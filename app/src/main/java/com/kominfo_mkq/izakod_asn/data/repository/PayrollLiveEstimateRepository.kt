package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.PayrollLiveEstimateResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.remote.EabsenRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class PayrollLiveEstimateRepository {

    private val izakodApiService = ApiClient.eabsenApiService
    private val entagoApiService = EabsenRetrofitClient.apiService

    suspend fun getTppEstimasiBerjalan(
        tahun: Int,
        bulan: Int
    ): ApiResponse<PayrollLiveEstimateResponse> = getEstimasiBerjalan(tahun, bulan, EstimateType.TPP)

    suspend fun getGajiEstimasiBerjalan(
        tahun: Int,
        bulan: Int
    ): ApiResponse<PayrollLiveEstimateResponse> = getEstimasiBerjalan(tahun, bulan, EstimateType.GAJI)

    private suspend fun getEstimasiBerjalan(
        tahun: Int,
        bulan: Int,
        type: EstimateType
    ): ApiResponse<PayrollLiveEstimateResponse> = withContext(Dispatchers.IO) {
        try {
            val snapshotResponse = entagoApiService.getPayrollAbsensiLiveSnapshot(
                tahun = tahun,
                bulan = bulan
            )
            val snapshotBody = snapshotResponse.body()

            if (!snapshotResponse.isSuccessful || snapshotBody == null) {
                return@withContext ApiResponse(
                    success = false,
                    error = parseJsonError(
                        raw = snapshotResponse.errorBody()?.string(),
                        fallback = "Gagal memuat snapshot absensi live E-NTAGO (${snapshotResponse.code()})"
                    )
                )
            }

            val estimateResponse = when (type) {
                EstimateType.TPP -> izakodApiService.getTppEstimasiBerjalan(snapshotBody)
                EstimateType.GAJI -> izakodApiService.getGajiNonAsnEstimasiBerjalan(snapshotBody)
            }

            if (estimateResponse.isSuccessful) {
                val body = estimateResponse.body()
                if (body?.success == true) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Estimasi berjalan belum tersedia"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = parseEstimateError(estimateResponse)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(
                success = false,
                error = e.message ?: "Gagal memuat estimasi berjalan"
            )
        }
    }

    private fun parseEstimateError(response: Response<PayrollLiveEstimateResponse>): String {
        return parseJsonError(
            raw = response.errorBody()?.string(),
            fallback = "Gagal memuat estimasi berjalan (${response.code()})"
        )
    }

    private fun parseJsonError(raw: String?, fallback: String): String {
        val parsedMessage = try {
            val json = JSONObject(raw ?: "{}")
            json.optString("message").ifBlank {
                json.optString("response")
            }.ifBlank {
                json.optString("error")
            }
        } catch (_: Exception) {
            ""
        }

        return parsedMessage.ifBlank { fallback }
    }

    private enum class EstimateType {
        TPP,
        GAJI
    }
}
