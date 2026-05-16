package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class TargetKinerjaListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<TargetKinerjaItem> = emptyList(),

    @SerializedName("meta")
    val meta: TargetKinerjaListMeta? = null,

    @SerializedName("error")
    val error: TargetKinerjaError? = null
)

data class TargetKinerjaListMeta(
    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("currentPegawaiId")
    val currentPegawaiId: Int? = null,

    @SerializedName("currentRole")
    val currentRole: String? = null,

    @SerializedName("canReviewSubordinates")
    val canReviewSubordinates: Boolean? = null
)

data class TargetKinerjaDetailResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: TargetKinerjaItem? = null,

    @SerializedName("error")
    val error: TargetKinerjaError? = null
)

data class TargetKinerjaMutationResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: TargetKinerjaMutationData? = null,

    @SerializedName("error")
    val error: TargetKinerjaError? = null
)

data class TargetKinerjaMutationData(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("deleted")
    val deleted: Boolean? = null,

    @SerializedName("target")
    val target: TargetKinerjaItem? = null
)

data class TargetKinerjaItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String? = null,

    @SerializedName("approver_nama")
    val approverNama: String? = null,

    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("catatan_pegawai")
    val catatanPegawai: String? = null,

    @SerializedName("catatan_atasan")
    val catatanAtasan: String? = null,

    @SerializedName("detail_count")
    val detailCount: Int? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("is_assessment_finalized")
    val isAssessmentFinalized: Boolean? = null,

    @SerializedName("details")
    val details: List<TargetKinerjaDetailItem> = emptyList()
)

data class TargetKinerjaHistoryItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("aksi")
    val aksi: String,

    @SerializedName("status_dari")
    val statusDari: String? = null,

    @SerializedName("status_ke")
    val statusKe: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null,

    @SerializedName("aksi_role")
    val aksiRole: String? = null,

    @SerializedName("aksi_sumber")
    val aksiSumber: String? = null,

    @SerializedName("created_at")
    val createdAt: String
)

data class RealisasiKinerjaHistoryItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("realisasi_kinerja_id")
    val realisasiKinerjaId: Int,

    @SerializedName("aksi")
    val aksi: String,

    @SerializedName("catatan")
    val catatan: String? = null,

    @SerializedName("aksi_role")
    val aksiRole: String? = null,

    @SerializedName("aksi_sumber")
    val aksiSumber: String? = null,

    @SerializedName("created_at")
    val createdAt: String
)

data class TargetKinerjaHistoryResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<TargetKinerjaHistoryItem> = emptyList(),

    @SerializedName("error")
    val error: TargetKinerjaError? = null
)

data class RealisasiKinerjaHistoryResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<RealisasiKinerjaHistoryItem> = emptyList(),

    @SerializedName("error")
    val error: TargetKinerjaError? = null
)

data class TargetKinerjaDetailItem(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("target_kinerja_id")
    val targetKinerjaId: Int? = null,

    @SerializedName("uraian_target")
    val uraianTarget: String,

    @SerializedName("indikator")
    val indikator: String? = null,

    @SerializedName("satuan")
    val satuan: String? = null,

    @SerializedName("target_kuantitas")
    val targetKuantitas: Double? = null,

    @SerializedName("target_kualitas")
    val targetKualitas: Double? = null,

    @SerializedName("target_waktu")
    val targetWaktu: Double? = null,

    @SerializedName("bobot")
    val bobot: Double? = null,

    @SerializedName("urutan")
    val urutan: Int? = null
)

data class TargetKinerjaRequest(
    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int,

    @SerializedName("catatan_pegawai")
    val catatanPegawai: String?,

    @SerializedName("details")
    val details: List<TargetKinerjaDetailPayload>
)

data class TargetKinerjaDetailPayload(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("uraian_target")
    val uraianTarget: String,

    @SerializedName("indikator")
    val indikator: String? = null,

    @SerializedName("satuan")
    val satuan: String? = null,

    @SerializedName("target_kuantitas")
    val targetKuantitas: Double? = null,

    @SerializedName("target_kualitas")
    val targetKualitas: Double? = null,

    @SerializedName("target_waktu")
    val targetWaktu: Double? = null,

    @SerializedName("bobot")
    val bobot: Double? = null
)

data class TargetKinerjaReviewRequest(
    @SerializedName("aksi")
    val aksi: String,

    @SerializedName("catatan_atasan")
    val catatanAtasan: String?
)

data class RealisasiKinerjaItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("target_kinerja_detail_id")
    val targetKinerjaDetailId: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("realisasi_kuantitas")
    val realisasiKuantitas: Double? = null,

    @SerializedName("realisasi_kualitas")
    val realisasiKualitas: Double? = null,

    @SerializedName("realisasi_waktu")
    val realisasiWaktu: Double? = null,

    @SerializedName("persentase_capaian")
    val persentaseCapaian: Double? = null,

    @SerializedName("nilai_capaian")
    val nilaiCapaian: Double? = null,

    @SerializedName("catatan")
    val catatan: String? = null,

    @SerializedName("laporan_count")
    val laporanCount: Int? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class RealisasiKinerjaListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<RealisasiKinerjaItem> = emptyList(),

    @SerializedName("error")
    val error: TargetKinerjaError? = null
)

data class RealisasiKinerjaDetailResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: RealisasiKinerjaItem? = null,

    @SerializedName("error")
    val error: TargetKinerjaError? = null
)

data class RealisasiKinerjaRequest(
    @SerializedName("target_kinerja_detail_id")
    val targetKinerjaDetailId: Int,

    @SerializedName("realisasi_kuantitas")
    val realisasiKuantitas: Double? = null,

    @SerializedName("realisasi_kualitas")
    val realisasiKualitas: Double? = null,

    @SerializedName("realisasi_waktu")
    val realisasiWaktu: Double? = null,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class RealisasiLinkedLaporanItem(
    @SerializedName("laporan_id")
    val laporanId: Int,

    @SerializedName("nama_kegiatan")
    val namaKegiatan: String,

    @SerializedName("status_laporan")
    val statusLaporan: String,

    @SerializedName("tanggal_kegiatan")
    val tanggalKegiatan: String,

    @SerializedName("waktu_mulai")
    val waktuMulai: String? = null,

    @SerializedName("waktu_selesai")
    val waktuSelesai: String? = null,

    @SerializedName("durasi_menit")
    val durasiMenit: Int? = null,

    @SerializedName("nama_kategori")
    val namaKategori: String? = null
)

data class RealisasiLinkedLaporanResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<RealisasiLinkedLaporanItem> = emptyList(),

    @SerializedName("error")
    val error: TargetKinerjaError? = null
)

data class RealisasiLinkLaporanRequest(
    @SerializedName("laporan_id")
    val laporanId: Int
)

data class TargetKinerjaError(
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("details")
    val details: Any? = null
)
