package com.kominfo_mkq.izakod_asn.data.remote

import com.kominfo_mkq.izakod_asn.data.model.EntagoLoginRequest
import com.kominfo_mkq.izakod_asn.data.model.EntagoLoginResponse
import com.kominfo_mkq.izakod_asn.data.model.PegawaiData
import com.kominfo_mkq.izakod_asn.data.model.StatistikBulananResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API Service untuk IZAKOD-ASN API
 */
interface IzakodApiService {

    /**
     * Login ke eAbsen API
     * POST /api/login
     */
    @POST("api/login")
    suspend fun login(
        @Body request: EntagoLoginRequest
    ): Response<EntagoLoginResponse>

    /**
     * Get statistik bulanan
     * GET /api/statistik/bulanan
     *
     * Query parameters:
     * - skpdid: Filter by SKPD ID (admin only)
     * - pegawai_id: Filter by Pegawai ID (admin only)
     * - bulan: Filter by month (1-12)
     * - tahun: Filter by year
     */
    @GET("api/statistik/bulanan")
    suspend fun getStatistikBulanan(
        @Query("skpdid") skpdid: Int? = null,
        @Query("pegawai_id") pegawaiId: Int? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): Response<StatistikBulananResponse>

    /**
     * Fetch pegawai data by PIN
     * GET /api/pegawai/{pin}
     */
    @GET("api/pegawai/{pin}")
    suspend fun getPegawai(
        @Path("pin") pin: String
    ): Response<PegawaiData>
}
