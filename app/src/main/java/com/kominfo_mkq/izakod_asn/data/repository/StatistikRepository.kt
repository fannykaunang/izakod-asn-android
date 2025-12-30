package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.StatistikBulananResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository untuk handle statistik API calls
 */
class StatistikRepository {

    private val apiService = ApiClient.eabsenApiService

    companion object {
        // ✅ Simpan pegawai_id sebagai static variable
        private var currentPegawaiId: Int? = null
        private var currentPin: String? = null

        /**
         * Set pegawai_id after successful login
         * Call from anywhere: StatistikRepository.setUserData(id, pin)
         */
        fun setUserData(pegawaiId: Int?, pin: String?) {
            currentPegawaiId = pegawaiId
            currentPin = pin
        }

        /**
         * Get stored pegawai_id
         */
        fun getPegawaiId(): Int? = currentPegawaiId

        /**
         * Get stored pin
         */
        fun getPin(): String? = currentPin

        /**
         * Clear stored data on logout
         */
        fun clearData() {
            currentPegawaiId = null
            currentPin = null
        }
    }

    /**
     * Set pegawai_id after successful login
     */
    fun setPegawaiId(pegawaiId: Int?, pin: String?) {
        currentPegawaiId = pegawaiId
        currentPin = pin
    }

    /**
     * Get statistik bulanan
     *
     * @param skpdid Filter by SKPD ID (optional, admin only)
     * @param pegawaiId Filter by Pegawai ID (optional, admin only)
     * @param bulan Filter by month (optional, 1-12)
     * @param tahun Filter by year (optional)
     * @return ApiResponse dengan StatistikBulananResponse
     */
    suspend fun getStatistikBulanan(
        skpdid: Int? = null,
        pegawaiId: Int? = null,
        bulan: Int? = null,
        tahun: Int? = null
    ): ApiResponse<StatistikBulananResponse> = withContext(Dispatchers.IO) {
        try {
            val effectivePegawaiId = pegawaiId ?: currentPegawaiId

            val response = apiService.getStatistikBulanan(
                skpdid = skpdid,
                pegawaiId = effectivePegawaiId,
                bulan = bulan,
                tahun = tahun
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.success) {
                    ApiResponse(
                        success = true,
                        data = body
                    )
                } else {
                    ApiResponse(
                        success = false,
                        error = "Gagal memuat data statistik"
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
            ApiResponse(
                success = false,
                error = e.message ?: "Network error"
            )
        }
    }

    /**
     * Clear stored data on logout
     */
    fun clearData() {
        currentPegawaiId = null
        currentPin = null
    }
}