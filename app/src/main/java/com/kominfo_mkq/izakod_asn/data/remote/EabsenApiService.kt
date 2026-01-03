package com.kominfo_mkq.izakod_asn.data.remote

import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiResponse
import com.kominfo_mkq.izakod_asn.data.model.BasicActionResponse
import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanResponse
import com.kominfo_mkq.izakod_asn.data.model.CreateReminderRequest
import com.kominfo_mkq.izakod_asn.data.model.CreateReminderResponse
import com.kominfo_mkq.izakod_asn.data.model.DeleteReminderResponse
import com.kominfo_mkq.izakod_asn.data.model.EabsenLoginResponse
import com.kominfo_mkq.izakod_asn.data.model.FcmRegisterRequest
import com.kominfo_mkq.izakod_asn.data.model.KategoriListResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanCetakResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanListResponse
import com.kominfo_mkq.izakod_asn.data.model.LoginRequest
import com.kominfo_mkq.izakod_asn.data.model.MobileTokenRequest
import com.kominfo_mkq.izakod_asn.data.model.MobileTokenResponse
import com.kominfo_mkq.izakod_asn.data.model.NotifikasiResponse
import com.kominfo_mkq.izakod_asn.data.model.PegawaiData
import com.kominfo_mkq.izakod_asn.data.model.ReminderListResponse
import com.kominfo_mkq.izakod_asn.data.model.StatistikBulananResponse
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateRequest
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateResponse
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanResponse
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanResponse
import com.kominfo_mkq.izakod_asn.data.model.VerifikasiLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.VerifikasiLaporanResponse
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

    /**
     * Get template kegiatan list
     * GET /api/template-kegiatan
     */
    @GET("api/template-kegiatan")
    suspend fun getTemplateKegiatan(
        @Query("pegawai_id") pegawaiId: Int? = null,
        @Query("kategori_id") kategoriId: Int? = null,
        @Query("is_public") isPublic: Int? = null,
        @Query("unit_kerja") unitKerja: String? = null
    ): Response<TemplateKegiatanResponse>

    /**
     * Verify laporan (Terima, Revisi, atau Tolak)
     * POST /api/laporan-kegiatan/{id}/verifikasi
     */
    @POST("api/laporan-kegiatan/{id}/verifikasi")
    suspend fun verifikasiLaporan(
        @Path("id") laporanId: Int,
        @Body request: VerifikasiLaporanRequest,
        @Query("pegawai_id") pegawaiId: Int?
    ): Response<VerifikasiLaporanResponse>

    @GET("api/laporan-kegiatan/cetak")
    suspend fun getLaporanCetakBulanan(
        @Query("pegawai_id") pegawai_id: Int?,
        @Query("pegawaiId") pegawaiId: Int,
        @Query("tahun") tahun: Int,
        @Query("bulan") bulan: Int
    ): Response<LaporanCetakResponse>

    /**
     * Get notifications for logged-in user
     * GET /api/notifikasi
     */
    @GET("api/notifikasi")
    suspend fun getNotifications(
        @Query("pegawai_id") pegawai_id: Int?,
    ): Response<NotifikasiResponse>

    /**
     * Get reminder list
     * GET /api/reminder
     */
    @GET("api/reminder")
    suspend fun getReminders(
        @Query("pegawai_id") pegawai_id: Int?,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("tipe") tipe: String? = null
    ): Response<ReminderListResponse>

    /**
     * Create reminder
     * POST /api/reminder
     */
    @POST("api/reminder")
    suspend fun createReminder(
        @Query("pegawai_id") pegawai_id: Int?,
        @Body request: CreateReminderRequest
    ): Response<CreateReminderResponse>

    /**
     * Delete reminder
     * DELETE /api/reminder/{id}
     */
    @DELETE("api/reminder/{id}")
    suspend fun deleteReminder(
        @Path("id") reminderId: Int,
        @Query("pegawai_id") pegawaiId: Int
    ): Response<DeleteReminderResponse>

    /**
     * Get data atasan pegawai by bawahan
     * GET /api/atasan-pegawai/by-bawahan/{pegawaiId}
     */
    @GET("api/atasan-pegawai/by-bawahan/{pegawaiId}")
    suspend fun getAtasanPegawaiByBawahan(
        @Path("pegawaiId") pegawaiId: Int
    ): Response<AtasanPegawaiResponse>

    // CREATE
    @POST("api/template-kegiatan")
    suspend fun createTemplateKegiatan(
        @Body request: TemplateKegiatanCreateRequest
    ): Response<TemplateKegiatanCreateResponse>

    // UPDATE
    @PUT("api/template-kegiatan/{id}")
    suspend fun updateTemplateKegiatan(
        @Path("id") templateId: Int,
        @Body request: TemplateKegiatanCreateRequest
    ): Response<BasicActionResponse>

    // DELETE
    @DELETE("api/template-kegiatan/{id}")
    suspend fun deleteTemplateKegiatan(
        @Path("id") templateId: Int
    ): Response<BasicActionResponse>

    @POST("api/mobile/token")
    suspend fun getMobileToken(
        @Body request: MobileTokenRequest
    ): Response<MobileTokenResponse>

    @POST("api/mobile/fcm/register")
    suspend fun registerFcmToken(
        @Body request: FcmRegisterRequest
    ): Response<BasicActionResponse>
}