package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model untuk Create Laporan Kegiatan
 */

// Request body untuk create laporan
data class CreateLaporanRequest(
    @SerializedName("tanggal_kegiatan")
    val tanggalKegiatan: String,  // Format: YYYY-MM-DD

    @SerializedName("kategori_id")
    val kategoriId: Int,

    @SerializedName("nama_kegiatan")
    val namaKegiatan: String,

    @SerializedName("deskripsi_kegiatan")
    val deskripsiKegiatan: String,

    @SerializedName("target_output")
    val targetOutput: String? = null,

    @SerializedName("hasil_output")
    val hasilOutput: String? = null,

    @SerializedName("waktu_mulai")
    val waktuMulai: String,  // Format: HH:mm

    @SerializedName("waktu_selesai")
    val waktuSelesai: String,  // Format: HH:mm

    @SerializedName("lokasi_kegiatan")
    val lokasiKegiatan: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("peserta_kegiatan")
    val pesertaKegiatan: String? = null,

    @SerializedName("jumlah_peserta")
    val jumlahPeserta: Int = 0,

    @SerializedName("link_referensi")
    val linkReferensi: String? = null,

    @SerializedName("kendala")
    val kendala: String? = null,

    @SerializedName("solusi")
    val solusi: String? = null,

    @SerializedName("status_laporan")
    val statusLaporan: String = "Draft"  // "Draft" or "Diajukan"
)

// Response dari create laporan
data class CreateLaporanResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("laporan_id")
    val laporanId: Int?
)

// Error response dengan detail
data class LaporanErrorResponse(
    @SerializedName("error")
    val error: String,

    @SerializedName("requiresAttendance")
    val requiresAttendance: Boolean? = null
)

// Model untuk Kategori Kegiatan
data class KategoriKegiatan(
    @SerializedName("kategori_id")
    val kategoriId: Int,

    @SerializedName("nama_kategori")
    val namaKategori: String,

    @SerializedName("kode_kategori")
    val kodeKategori: String? = null,

    @SerializedName("warna")
    val warna: String? = null,

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("is_active")
    val isActive: Int = 1
)

// Response untuk list kategori
data class KategoriListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<KategoriKegiatan>
)