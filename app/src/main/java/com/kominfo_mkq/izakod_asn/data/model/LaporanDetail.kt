package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class LaporanDetail(
    @SerializedName("laporan_id")
    val laporanId: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String?,

    @SerializedName("pegawai_nip")
    val pegawaiNip: String?,

    @SerializedName("skpd")
    val skpd: String?,

    @SerializedName("tanggal_kegiatan")
    val tanggalKegiatan: String,

    @SerializedName("kategori_id")
    val kategoriId: Int,

    @SerializedName("nama_kategori")
    val kategoriNama: String?,

    @SerializedName("nama_kegiatan")
    val namaKegiatan: String,

    @SerializedName("deskripsi_kegiatan")
    val deskripsiKegiatan: String,

    @SerializedName("target_output")
    val targetOutput: String?,

    @SerializedName("hasil_output")
    val hasilOutput: String?,

    @SerializedName("target_kinerja_id")
    val targetKinerjaId: Int? = null,

    @SerializedName("target_kinerja_detail_id")
    val targetKinerjaDetailId: Int? = null,

    @SerializedName("target_detail_uraian")
    val targetDetailUraian: String? = null,

    @SerializedName("waktu_mulai")
    val waktuMulai: String,

    @SerializedName("waktu_selesai")
    val waktuSelesai: String,

    @SerializedName("durasi_menit")
    val durasiMenit: Int?,

    @SerializedName("lokasi_kegiatan")
    val lokasiKegiatan: String?,

    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?,

    @SerializedName("peserta_kegiatan")
    val pesertaKegiatan: String?,

    @SerializedName("jumlah_peserta")
    val jumlahPeserta: Int?,

    @SerializedName("link_referensi")
    val linkReferensi: String?,

    @SerializedName("kendala")
    val kendala: String?,

    @SerializedName("solusi")
    val solusi: String?,

    @SerializedName("status_laporan")
    val statusLaporan: String,

    @SerializedName("verifikasi_oleh")
    val verifikasiOleh: Int?,

    @SerializedName("tanggal_verifikasi")
    val tanggalVerifikasi: String?,

    @SerializedName("verifikator_nama")
    val verifikatorNama: String?,

    @SerializedName("catatan_verifikasi")
    val catatanVerifikator: String?,

    @SerializedName("rating_kualitas")
    val rating: Double?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("lampiran")
    val lampiran: List<LampiranKegiatan> = emptyList()
)

data class LaporanDetailResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: LaporanDetail,

    @SerializedName("canEdit")
    val canEdit: Boolean = false,

    @SerializedName("canVerify")
    val canVerify: Boolean = false
)

// Request for updating laporan
data class UpdateLaporanRequest(
    val tanggal_kegiatan: String? = null,
    val kategori_id: Int? = null,
    val nama_kegiatan: String? = null,
    val deskripsi_kegiatan: String? = null,
    val target_output: String? = null,
    val hasil_output: String? = null,
    val target_kinerja_id: JsonElement? = null,
    val target_kinerja_detail_id: JsonElement? = null,
    val waktu_mulai: String? = null,
    val waktu_selesai: String? = null,
    val lokasi_kegiatan: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val peserta_kegiatan: String? = null,
    val jumlah_peserta: Int? = null,
    val link_referensi: String? = null,
    val kendala: String? = null,
    val solusi: String? = null,
    val status_laporan: String? = null
)

data class UpdateLaporanResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String
)
