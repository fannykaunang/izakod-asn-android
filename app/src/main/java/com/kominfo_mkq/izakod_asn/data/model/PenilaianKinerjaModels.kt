package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class PenilaianKinerjaListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<PenilaianKinerjaItem> = emptyList(),

    @SerializedName("meta")
    val meta: PenilaianKinerjaListMeta? = null,

    @SerializedName("error")
    val error: PenilaianKinerjaError? = null
)

data class PenilaianKinerjaListMeta(
    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("currentPegawaiId")
    val currentPegawaiId: Int? = null,

    @SerializedName("currentRole")
    val currentRole: String? = null,

    @SerializedName("canReviewSubordinates")
    val canReviewSubordinates: Boolean? = null
)

data class PenilaianKinerjaDetailResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: PenilaianKinerjaItem? = null,

    @SerializedName("meta")
    val meta: PenilaianKinerjaDetailMeta? = null,

    @SerializedName("error")
    val error: PenilaianKinerjaError? = null
)

data class PenilaianKinerjaDetailMeta(
    @SerializedName("canReview")
    val canReview: Boolean? = null,

    @SerializedName("isOwner")
    val isOwner: Boolean? = null,

    @SerializedName("autoFill")
    val autoFill: PenilaianKinerjaAutoFill? = null
)

data class PenilaianKinerjaItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String? = null,

    @SerializedName("pegawai_skpd")
    val pegawaiSkpd: String? = null,

    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int,

    @SerializedName("nilai_target")
    val nilaiTarget: Double? = null,

    @SerializedName("nilai_realisasi")
    val nilaiRealisasi: Double? = null,

    @SerializedName("nilai_akhir")
    val nilaiAkhir: Double? = null,

    @SerializedName("predikat")
    val predikat: String? = null,

    @SerializedName("status_finalisasi")
    val statusFinalisasi: String,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class PenilaianKinerjaAutoFill(
    @SerializedName("total_target")
    val totalTarget: Int = 0,

    @SerializedName("total_item")
    val totalItem: Int = 0,

    @SerializedName("item_sudah_realisasi")
    val itemSudahRealisasi: Int = 0,

    @SerializedName("item_belum_realisasi")
    val itemBelumRealisasi: Int = 0,

    @SerializedName("total_laporan_tertaut")
    val totalLaporanTertaut: Int = 0,

    @SerializedName("persentase_progress")
    val persentaseProgress: Double = 0.0,

    @SerializedName("avg_persentase_capaian")
    val avgPersentaseCapaian: Double? = null,

    @SerializedName("avg_nilai_capaian")
    val avgNilaiCapaian: Double? = null,

    @SerializedName("suggested_nilai_target")
    val suggestedNilaiTarget: Double? = null,

    @SerializedName("suggested_nilai_realisasi")
    val suggestedNilaiRealisasi: Double? = null,

    @SerializedName("suggested_nilai_akhir")
    val suggestedNilaiAkhir: Double? = null,

    @SerializedName("suggested_predikat")
    val suggestedPredikat: String? = null
)

data class CreatePenilaianKinerjaRequest(
    @SerializedName("pegawai_id")
    val pegawaiId: Int? = null,

    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class UpdatePenilaianKinerjaRequest(
    @SerializedName("nilai_target")
    val nilaiTarget: Double? = null,

    @SerializedName("nilai_realisasi")
    val nilaiRealisasi: Double? = null,

    @SerializedName("nilai_akhir")
    val nilaiAkhir: Double? = null,

    @SerializedName("predikat")
    val predikat: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null,

    @SerializedName("status_finalisasi")
    val statusFinalisasi: String? = null
)

data class PenilaianKinerjaError(
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("details")
    val details: Any? = null
)
