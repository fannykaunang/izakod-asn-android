package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model untuk API statistik harian
 * Endpoint: GET /api/statistik/harian
 */
data class StatistikHarianResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: StatistikHarianData
)

data class StatistikHarianData(
    @SerializedName("metrics")
    val metrics: DailyMetricsData,

    @SerializedName("timeSeries")
    val timeSeries: List<DailyTimeSeriesItem>,

    @SerializedName("filters")
    val filters: DailyFiltersData,

    @SerializedName("isAdmin")
    val isAdmin: Boolean
)

data class DailyMetricsData(
    @SerializedName("jumlah_diverifikasi")
    val jumlahDiverifikasi: String,

    @SerializedName("jumlah_pending")
    val jumlahPending: String,

    @SerializedName("jumlah_ditolak")
    val jumlahDitolak: String,

    @SerializedName("avg_produktivitas")
    val avgProduktivitas: String,

    @SerializedName("rata_rata_rating")
    val rataRataRating: String,

    @SerializedName("total_durasi")
    val totalDurasi: String
)

data class DailyTimeSeriesItem(
    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("jumlah_kegiatan")
    val jumlahKegiatan: String
)

data class DailyFiltersData(
    @SerializedName("skpdList")
    val skpdList: List<SkpdItem> = emptyList(),

    @SerializedName("pegawaiList")
    val pegawaiList: List<PegawaiItem> = emptyList()
)

/**
 * Response model untuk API statistik bulanan
 * Endpoint: GET /api/statistik/bulanan
 */
data class StatistikBulananResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: StatistikData
)

data class StatistikData(
    @SerializedName("metrics")
    val metrics: MetricsData,

    @SerializedName("timeSeries")
    val timeSeries: List<TimeSeriesItem>,

    @SerializedName("filters")
    val filters: FiltersData,

    @SerializedName("isAdmin")
    val isAdmin: Boolean
)

data class MetricsData(
    @SerializedName("total_kegiatan")
    val totalKegiatan: String,

    @SerializedName("total_durasi_menit")
    val totalDurasiMenit: String,

    @SerializedName("rata_rata_kegiatan_per_hari")
    val rataRataKegiatanPerHari: String,

    @SerializedName("total_diverifikasi")
    val totalDiverifikasi: String,

    @SerializedName("total_pending")
    val totalPending: String,

    @SerializedName("total_ditolak")
    val totalDitolak: String,

    @SerializedName("persentase_verifikasi")
    val persentaseVerifikasi: String,

    @SerializedName("rata_rata_rating")
    val rataRataRating: String,

    @SerializedName("total_revisi")
    val totalRevisi: String
)

data class TimeSeriesItem(
    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int,

    @SerializedName("bulan_nama")
    val bulanNama: String,

    @SerializedName("total_kegiatan")
    val totalKegiatan: String
)

data class FiltersData(
    @SerializedName("skpdList")
    val skpdList: List<SkpdItem>,

    @SerializedName("pegawaiList")
    val pegawaiList: List<PegawaiItem>,

    @SerializedName("bulanList")
    val bulanList: List<BulanItem>,

    @SerializedName("tahunList")
    val tahunList: List<TahunItem>
)

data class SkpdItem(
    @SerializedName("skpdid")
    val skpdid: Int,

    @SerializedName("skpd")
    val skpd: String
)

data class PegawaiItem(
    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String
)

data class BulanItem(
    @SerializedName("value")
    val value: Int,

    @SerializedName("label")
    val label: String
)

data class TahunItem(
    @SerializedName("value")
    val value: Int,

    @SerializedName("label")
    val label: String
)

data class DashboardOverviewResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: DashboardOverviewData? = null,

    @SerializedName("message")
    val message: String? = null
)

data class DashboardOverviewData(
    @SerializedName("assessmentSummary")
    val assessmentSummary: AssessmentSummaryData? = null,

    @SerializedName("targetSummary")
    val targetSummary: DashboardTargetSummaryData? = null,

    @SerializedName("actionAlerts")
    val actionAlerts: DashboardActionAlertsData? = null
)

data class AssessmentSummaryData(
    @SerializedName("activePeriodStatus")
    val activePeriodStatus: String? = null,

    @SerializedName("activePeriodLabel")
    val activePeriodLabel: String? = null,

    @SerializedName("selectedPeriodScore")
    val selectedPeriodScore: Double? = null,

    @SerializedName("selectedPeriodPredicate")
    val selectedPeriodPredicate: String? = null,

    @SerializedName("latestFinalScore")
    val latestFinalScore: Double? = null,

    @SerializedName("latestPredicate")
    val latestPredicate: String? = null,

    @SerializedName("latestFinalPeriodLabel")
    val latestFinalPeriodLabel: String? = null,

    @SerializedName("pendingOwnAssessment")
    val pendingOwnAssessment: Int? = null,

    @SerializedName("pendingSubordinateAssessments")
    val pendingSubordinateAssessments: Int? = null,

    @SerializedName("missingSubordinateAssessments")
    val missingSubordinateAssessments: Int? = null,

    @SerializedName("canReviewSubordinates")
    val canReviewSubordinates: Boolean? = null,

    @SerializedName("canProposeSubordinates")
    val canProposeSubordinates: Boolean? = null
)

data class DashboardTargetSummaryData(
    @SerializedName("activePeriodLabel")
    val activePeriodLabel: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("totalTargets")
    val totalTargets: Int? = null,

    @SerializedName("totalItems")
    val totalItems: Int? = null,

    @SerializedName("itemSudahRealisasi")
    val itemSudahRealisasi: Int? = null,

    @SerializedName("itemBelumRealisasi")
    val itemBelumRealisasi: Int? = null,

    @SerializedName("totalLaporanTertaut")
    val totalLaporanTertaut: Int? = null,

    @SerializedName("persentaseProgress")
    val persentaseProgress: Double? = null
)

data class DashboardActionAlertsData(
    @SerializedName("laporanPendingCount")
    val laporanPendingCount: Int? = null,

    @SerializedName("ownLaporanRevisionCount")
    val ownLaporanRevisionCount: Int? = null,

    @SerializedName("subordinateLaporanRevisionCount")
    val subordinateLaporanRevisionCount: Int? = null,

    @SerializedName("scopedLaporanRevisionCount")
    val scopedLaporanRevisionCount: Int? = null,

    @SerializedName("targetNeedAttentionCount")
    val targetNeedAttentionCount: Int? = null,

    @SerializedName("realisasiNeedAttentionCount")
    val realisasiNeedAttentionCount: Int? = null,

    @SerializedName("ownAssessmentPendingCount")
    val ownAssessmentPendingCount: Int? = null,

    @SerializedName("subordinateReviewCount")
    val subordinateReviewCount: Int? = null,

    @SerializedName("missingAssessmentCount")
    val missingAssessmentCount: Int? = null,

    @SerializedName("subordinateTargetSubmittedCount")
    val subordinateTargetSubmittedCount: Int? = null,

    @SerializedName("subordinateTargetRevisionCount")
    val subordinateTargetRevisionCount: Int? = null,

    @SerializedName("subordinateLaporanPendingCount")
    val subordinateLaporanPendingCount: Int? = null,

    @SerializedName("subordinateRealisasiIncompleteCount")
    val subordinateRealisasiIncompleteCount: Int? = null
)
