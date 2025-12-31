package com.kominfo_mkq.izakod_asn.data.remote

import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanResponse
import com.kominfo_mkq.izakod_asn.data.model.EabsenLoginResponse
import com.kominfo_mkq.izakod_asn.data.model.KategoriListResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanListResponse
import com.kominfo_mkq.izakod_asn.data.model.LoginRequest
import com.kominfo_mkq.izakod_asn.data.model.PegawaiData
import com.kominfo_mkq.izakod_asn.data.model.StatistikBulananResponse
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service untuk eAbsen endpoints
 */
interface EabsenApiService {

    /**
     * Login ke eAbsen API
     * POST /api/login
     */
    //@POST("/api/login") ke server izakod-asn dulu
    @POST("https://dev.api.eabsen.merauke.go.id/api/user/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<EabsenLoginResponse>

    /**
     * Fetch pegawai data by PIN
     * GET /api/pegawai/{pin}
     */
    @GET("api/pegawai/pin/{pin}")
    suspend fun getPegawai(
        @Path("pin") pin: String,
    ): Response<PegawaiData>

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
     * Create new laporan kegiatan
     * POST /api/laporan-kegiatan
     */
    @POST("api/laporan-kegiatan")
    suspend fun createLaporan(
        @Body request: CreateLaporanRequest
    ): Response<CreateLaporanResponse>

    /**
     * ✅ NEW: Get list kategori kegiatan
     * GET /api/kategori?is_active=1
     */
    @GET("api/kategori")
    suspend fun getKategoriList(
        @Query("is_active") isActive: Int = 1
    ): Response<KategoriListResponse>

    /**
     * Create new laporan kegiatan with pegawai_id in query
     * POST /api/laporan-kegiatan?pegawai_id={pegawai_id}
     */
    @POST("api/laporan-kegiatan")
    suspend fun createLaporan(
        @Body request: CreateLaporanRequest,
        @Query("pegawai_id") pegawaiId: Int? = null,
        @Query("pin") pin: String? = null  // ✅ Add PIN parameter
    ): Response<CreateLaporanResponse>

    /**
     * Get all laporan kegiatan
     * GET /api/laporan-kegiatan
     */
    @GET("api/laporan-kegiatan")
    suspend fun getLaporanList(
        @Query("pegawai_id") pegawaiId: Int? = null
    ): Response<LaporanListResponse>

    /**
     * Get laporan detail by ID
     * GET /api/laporan/{id}?pegawai_id={pegawai_id}
     */
    @GET("api/laporan-kegiatan/{id}")
    suspend fun getLaporanDetail(
        @Path("id") laporanId: Int,
        @Query("pegawai_id") pegawaiId: Int? = null
    ): Response<LaporanDetailResponse>

    /**
     * Update laporan kegiatan
     * PUT /api/laporan/{id}?pegawai_id={pegawai_id}
     */
    @PUT("api/laporan-kegiatan/{id}")
    suspend fun updateLaporan(
        @Path("id") laporanId: Int,
        @Body request: UpdateLaporanRequest,
        @Query("pegawai_id") pegawaiId: Int? = null
    ): Response<UpdateLaporanResponse>
}