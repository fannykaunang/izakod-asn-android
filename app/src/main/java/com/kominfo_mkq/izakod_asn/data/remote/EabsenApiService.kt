package com.kominfo_mkq.izakod_asn.data.remote

import com.google.gson.JsonObject
import com.kominfo_mkq.izakod_asn.data.model.AiPanduanRequest
import com.kominfo_mkq.izakod_asn.data.model.AiPanduanResponse
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiListResponse
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiResponse
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanListResponse
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanMutationResponse
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanRequest
import com.kominfo_mkq.izakod_asn.data.model.BasicActionResponse
import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanResponse
import com.kominfo_mkq.izakod_asn.data.model.CreatePenilaianKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.model.CreateReminderRequest
import com.kominfo_mkq.izakod_asn.data.model.CreateReminderResponse
import com.kominfo_mkq.izakod_asn.data.model.DashboardOverviewResponse
import com.kominfo_mkq.izakod_asn.data.model.DeleteReminderResponse
import com.kominfo_mkq.izakod_asn.data.model.EntagoLoginRequest
import com.kominfo_mkq.izakod_asn.data.model.EntagoLoginResponse
import com.kominfo_mkq.izakod_asn.data.model.FcmRegisterRequest
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeResponse
import com.kominfo_mkq.izakod_asn.data.model.KategoriListResponse
import com.kominfo_mkq.izakod_asn.data.model.KandidatBawahanResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatanSettingsResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanCetakResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanListResponse
import com.kominfo_mkq.izakod_asn.data.model.MobileSsoExchangeRequest
import com.kominfo_mkq.izakod_asn.data.model.MobileSsoExchangeResponse
import com.kominfo_mkq.izakod_asn.data.model.MobileTokenRequest
import com.kominfo_mkq.izakod_asn.data.model.MobileTokenResponse
import com.kominfo_mkq.izakod_asn.data.model.NotifikasiResponse
import com.kominfo_mkq.izakod_asn.data.model.PayrollLiveEstimateResponse
import com.kominfo_mkq.izakod_asn.data.model.PegawaiData
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfileResponse
import com.kominfo_mkq.izakod_asn.data.model.PenilaianBelumDibuatResponse
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaListResponse
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaHistoryResponse
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaListResponse
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.model.RealisasiLinkLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.RealisasiLinkedLaporanResponse
import com.kominfo_mkq.izakod_asn.data.model.RefreshTokenRequest
import com.kominfo_mkq.izakod_asn.data.model.ReminderListResponse
import com.kominfo_mkq.izakod_asn.data.model.StatistikBulananResponse
import com.kominfo_mkq.izakod_asn.data.model.StatistikHarianResponse
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaHistoryResponse
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaListResponse
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaMutationResponse
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaReviewRequest
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateRequest
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateResponse
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanResponse
import com.kominfo_mkq.izakod_asn.data.model.TppMeResponse
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanResponse
import com.kominfo_mkq.izakod_asn.data.model.UpdatePenilaianKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.model.VerifikasiLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.VerifikasiLaporanResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API Service untuk eAbsen endpoints
 */
interface EabsenApiService {

    /**
     * Login ke eAbsen API
     * POST /api/login
     */
    @POST("api/login")
    suspend fun login(
        @Body request: EntagoLoginRequest
    ): Response<EntagoLoginResponse>

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
     * Get statistik harian
     * GET /api/statistik/harian
     */
    @GET("api/statistik/harian")
    suspend fun getStatistikHarian(
        @Query("skpdid") skpdid: Int? = null,
        @Query("pegawai_id") pegawaiId: Int? = null
    ): Response<StatistikHarianResponse>

    @GET("api/dashboard")
    suspend fun getDashboardOverview(
        @Query("tahun") tahun: Int? = null,
        @Query("bulan") bulan: Int? = null
    ): Response<DashboardOverviewResponse>

    @GET("api/mobile/profile/me")
    suspend fun getMobileProfile(): Response<PegawaiProfileResponse>

    @POST("api/ai/panduan")
    suspend fun askAiPanduan(
        @Body request: AiPanduanRequest
    ): Response<AiPanduanResponse>

    @GET("api/tpp/me")
    suspend fun getTppSaya(
        @Query("tahun") tahun: Int? = null,
        @Query("bulan") bulan: Int? = null
    ): Response<TppMeResponse>

    @GET("api/non-asn/gaji/me")
    suspend fun getGajiNonAsnSaya(
        @Query("tahun") tahun: Int? = null,
        @Query("bulan") bulan: Int? = null
    ): Response<GajiNonAsnMeResponse>

    @POST("api/tpp/me/estimasi-berjalan")
    suspend fun getTppEstimasiBerjalan(
        @Body snapshot: JsonObject
    ): Response<PayrollLiveEstimateResponse>

    @POST("api/non-asn/gaji/me/estimasi-berjalan")
    suspend fun getGajiNonAsnEstimasiBerjalan(
        @Body snapshot: JsonObject
    ): Response<PayrollLiveEstimateResponse>

    /**
     * ✅ NEW: Get list kategori kegiatan
     * GET /api/kategori?is_active=1
     */
    @GET("api/kategori")
    suspend fun getKategoriList(
        @Query("is_active") isActive: Int = 1
    ): Response<KategoriListResponse>

    @GET("api/laporan-kegiatan/settings")
    suspend fun getLaporanKegiatanSettings(
        @Query("pegawai_id") pegawaiId: Int? = null
    ): Response<LaporanKegiatanSettingsResponse>

    /**
     * Create new laporan kegiatan with pegawai_id in query
     * POST /api/laporan-kegiatan?pegawai_id={pegawai_id}
     */
    @POST("api/laporan-kegiatan")
    suspend fun createLaporan(
        @Body request: CreateLaporanRequest,
        @Query("pegawai_id") pegawaiId: Int? = null,
        @Query("pin") pin: String? = null,  // ✅ Add PIN parameter
        @Header("X-Entago-Access-Token") entagoAccessToken: String? = null
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
        @Query("pegawai_id") pegawaiId: Int? = null,
        @Query("pin") pin: String? = null,
        @Header("X-Entago-Access-Token") entagoAccessToken: String? = null
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

    @PATCH("api/notifikasi/{id}/read")
    suspend fun markNotificationAsRead(
        @Path("id") notificationId: Int
    ): Response<BasicActionResponse>

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

    @GET("api/atasan-pegawai/bawahan-saya")
    suspend fun getBawahanSaya(): Response<AtasanPegawaiListResponse>

    @GET("api/atasan-pegawai/kandidat-bawahan")
    suspend fun getKandidatBawahan(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 100
    ): Response<KandidatBawahanResponse>

    @GET("api/atasan-pegawai/usulan")
    suspend fun getAtasanPegawaiUsulan(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 100
    ): Response<AtasanPegawaiUsulanListResponse>

    @POST("api/atasan-pegawai/usulan")
    suspend fun createAtasanPegawaiUsulan(
        @Body request: AtasanPegawaiUsulanRequest
    ): Response<AtasanPegawaiUsulanMutationResponse>

    @PATCH("api/atasan-pegawai/usulan/{id}")
    suspend fun updateAtasanPegawaiUsulan(
        @Path("id") id: Int,
        @Body request: AtasanPegawaiUsulanRequest
    ): Response<AtasanPegawaiUsulanMutationResponse>

    @POST("api/atasan-pegawai/usulan/{id}/submit")
    suspend fun submitAtasanPegawaiUsulan(
        @Path("id") id: Int,
        @Body request: AtasanPegawaiUsulanRequest
    ): Response<AtasanPegawaiUsulanMutationResponse>

    @POST("api/atasan-pegawai/usulan/{id}/cancel")
    suspend fun cancelAtasanPegawaiUsulan(
        @Path("id") id: Int
    ): Response<AtasanPegawaiUsulanMutationResponse>

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

    @POST("api/mobile/refresh")
    suspend fun refreshMobileToken(
        @Body request: RefreshTokenRequest
    ): Response<MobileTokenResponse>

    @POST("api/mobile/sso-exchange")
    suspend fun exchangeMobileSsoTicket(
        @Body request: MobileSsoExchangeRequest
    ): Response<MobileSsoExchangeResponse>

    @POST("api/mobile/fcm/register")
    suspend fun registerFcmToken(
        @Body request: FcmRegisterRequest
    ): Response<BasicActionResponse>

    @GET("api/target-kinerja")
    suspend fun getTargetKinerjaList(
        @Query("tahun") tahun: Int? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("status") status: String? = null,
        @Query("pegawai_id") pegawaiId: Int? = null
    ): Response<TargetKinerjaListResponse>

    @GET("api/target-kinerja/{id}")
    suspend fun getTargetKinerjaDetail(
        @Path("id") targetId: Int
    ): Response<TargetKinerjaDetailResponse>

    @GET("api/target-kinerja/{id}/history")
    suspend fun getTargetKinerjaHistory(
        @Path("id") targetId: Int
    ): Response<TargetKinerjaHistoryResponse>

    @POST("api/target-kinerja")
    suspend fun createTargetKinerja(
        @Body request: TargetKinerjaRequest
    ): Response<TargetKinerjaMutationResponse>

    @PUT("api/target-kinerja/{id}")
    suspend fun updateTargetKinerja(
        @Path("id") targetId: Int,
        @Body request: TargetKinerjaRequest
    ): Response<TargetKinerjaDetailResponse>

    @DELETE("api/target-kinerja/{id}")
    suspend fun deleteTargetKinerja(
        @Path("id") targetId: Int
    ): Response<TargetKinerjaMutationResponse>

    @POST("api/target-kinerja/{id}/submit")
    suspend fun submitTargetKinerja(
        @Path("id") targetId: Int
    ): Response<TargetKinerjaDetailResponse>

    @POST("api/target-kinerja/{id}/review")
    suspend fun reviewTargetKinerja(
        @Path("id") targetId: Int,
        @Body request: TargetKinerjaReviewRequest
    ): Response<TargetKinerjaDetailResponse>

    @GET("api/realisasi-kinerja")
    suspend fun getRealisasiKinerjaList(
        @Query("target_kinerja_id") targetKinerjaId: Int? = null,
        @Query("pegawai_id") pegawaiId: Int? = null,
        @Query("tahun") tahun: Int? = null,
        @Query("bulan") bulan: Int? = null
    ): Response<RealisasiKinerjaListResponse>

    @GET("api/realisasi-kinerja/{id}/history")
    suspend fun getRealisasiKinerjaHistory(
        @Path("id") realisasiId: Int
    ): Response<RealisasiKinerjaHistoryResponse>

    @POST("api/realisasi-kinerja")
    suspend fun saveRealisasiKinerja(
        @Body request: RealisasiKinerjaRequest
    ): Response<RealisasiKinerjaDetailResponse>

    @GET("api/realisasi-kinerja/{id}/laporan")
    suspend fun getRealisasiLinkedLaporan(
        @Path("id") realisasiId: Int
    ): Response<RealisasiLinkedLaporanResponse>

    @POST("api/realisasi-kinerja/{id}/laporan")
    suspend fun linkLaporanToRealisasi(
        @Path("id") realisasiId: Int,
        @Body request: RealisasiLinkLaporanRequest
    ): Response<BasicActionResponse>

    @DELETE("api/realisasi-kinerja/{id}/laporan/{laporanId}")
    suspend fun unlinkLaporanFromRealisasi(
        @Path("id") realisasiId: Int,
        @Path("laporanId") laporanId: Int
    ): Response<BasicActionResponse>

    @GET("api/penilaian-kinerja")
    suspend fun getPenilaianKinerjaList(
        @Query("tahun") tahun: Int? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("status_finalisasi") statusFinalisasi: String? = null,
        @Query("pegawai_id") pegawaiId: Int? = null
    ): Response<PenilaianKinerjaListResponse>

    @GET("api/penilaian-kinerja/belum-dibuat")
    suspend fun getPenilaianBelumDibuat(
        @Query("tahun") tahun: Int? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("pegawai_id") pegawaiId: Int? = null
    ): Response<PenilaianBelumDibuatResponse>

    @GET("api/penilaian-kinerja/{id}")
    suspend fun getPenilaianKinerjaDetail(
        @Path("id") assessmentId: Int
    ): Response<PenilaianKinerjaDetailResponse>

    @POST("api/penilaian-kinerja")
    suspend fun createPenilaianKinerja(
        @Body request: CreatePenilaianKinerjaRequest
    ): Response<PenilaianKinerjaDetailResponse>

    @PUT("api/penilaian-kinerja/{id}")
    suspend fun updatePenilaianKinerja(
        @Path("id") assessmentId: Int,
        @Body request: UpdatePenilaianKinerjaRequest
    ): Response<PenilaianKinerjaDetailResponse>

    @POST("api/penilaian-kinerja/{id}/finalisasi")
    suspend fun finalisasiPenilaianKinerja(
        @Path("id") assessmentId: Int
    ): Response<PenilaianKinerjaDetailResponse>
}
