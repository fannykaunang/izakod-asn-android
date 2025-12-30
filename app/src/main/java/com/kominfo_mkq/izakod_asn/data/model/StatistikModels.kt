package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

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