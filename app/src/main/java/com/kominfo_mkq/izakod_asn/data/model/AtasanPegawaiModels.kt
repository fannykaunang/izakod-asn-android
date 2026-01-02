package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class AtasanPegawaiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AtasanPegawaiData?
)

data class AtasanPegawaiData(
    @SerializedName("id") val id: Int,
    @SerializedName("pegawai_id") val pegawaiId: Int,
    @SerializedName("atasan_id") val atasanId: Int,
    @SerializedName("jenis_atasan") val jenisAtasan: String,
    @SerializedName("is_active") val isActive: Int,

    @SerializedName("tanggal_mulai") val tanggalMulai: String?,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String?,

    @SerializedName("keterangan") val keterangan: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,

    @SerializedName("pegawai_nama") val pegawaiNama: String?,
    @SerializedName("pegawai_nip") val pegawaiNip: String?,
    @SerializedName("pegawai_jabatan") val pegawaiJabatan: String?,
    @SerializedName("pegawai_skpd") val pegawaiSkpd: String?,

    @SerializedName("atasan_pegawai_nama") val atasanPegawaiNama: String?,
    @SerializedName("atasan_pegawai_nip") val atasanPegawaiNip: String?,
    @SerializedName("atasan_pegawai_jabatan") val atasanPegawaiJabatan: String?
)
