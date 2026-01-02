package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class LaporanCetakResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: LaporanCetakData?,

    @SerializedName("meta")
    val meta: LaporanCetakMeta?
)

data class LaporanCetakData(
    @SerializedName("pegawai")
    val pegawai: PegawaiCetak?,

    @SerializedName("laporan")
    val laporan: List<LaporanKegiatan> = emptyList(),

    @SerializedName("lampiran")
    val lampiran: List<LampiranKegiatan> = emptyList()
)

data class PegawaiCetak(
    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String,

    @SerializedName("pegawai_nip")
    val pegawaiNip: String,

    @SerializedName("jabatan")
    val jabatan: String?,

    @SerializedName("skpd")
    val skpd: String?
)

data class LampiranKegiatan(
    @SerializedName("file_id")
    val fileId: Int,

    @SerializedName("laporan_id")
    val laporanId: Int,

    @SerializedName("nama_file_asli")
    val namaFileAsli: String?,

    @SerializedName("nama_file_sistem")
    val namaFileSistem: String?,

    @SerializedName("path_file")
    val pathFile: String?,

    @SerializedName("tipe_file")
    val tipeFile: String?,

    @SerializedName("ukuran_file")
    val ukuranFile: Long?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("nama_kegiatan")
    val namaKegiatan: String?,

    @SerializedName("tanggal_kegiatan")
    val tanggalKegiatan: String?
)

data class LaporanCetakMeta(
    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int,

    @SerializedName("total")
    val total: Int
)
