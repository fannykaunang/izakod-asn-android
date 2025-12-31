package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class LaporanKegiatan(
    @SerializedName("laporan_id")
    val laporanId: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String?,

    @SerializedName("tanggal_kegiatan")
    val tanggalKegiatan: String,

    @SerializedName("kategori_id")
    val kategoriId: Int,

    @SerializedName("kategori_nama")
    val kategoriNama: String?,

    @SerializedName("nama_kegiatan")
    val namaKegiatan: String,

    @SerializedName("deskripsi_kegiatan")
    val deskripsiKegiatan: String,

    @SerializedName("target_output")
    val targetOutput: String?,

    @SerializedName("hasil_output")
    val hasilOutput: String?,

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

    @SerializedName("catatan_verifikator")
    val catatanVerifikator: String?,

    @SerializedName("rating")
    val rating: Int?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?
)

data class LaporanListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<LaporanKegiatan>,

    @SerializedName("meta")
    val meta: LaporanMeta?
)

data class LaporanMeta(
    @SerializedName("isAdmin")
    val isAdmin: Boolean,

    @SerializedName("isAtasan")
    val isAtasan: Boolean,

    @SerializedName("manageablePegawaiIds")
    val manageablePegawaiIds: List<Int>,

    @SerializedName("supervisedPegawaiIds")
    val supervisedPegawaiIds: List<Int>
)