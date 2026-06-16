package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class AtasanPegawaiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AtasanPegawaiData?
)

data class AtasanPegawaiListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<AtasanPegawaiData> = emptyList()
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

    @SerializedName(value = "atasan_pegawai_nama", alternate = ["atasan_nama"])
    val atasanPegawaiNama: String?,

    @SerializedName(value = "atasan_pegawai_nip", alternate = ["atasan_nip"])
    val atasanPegawaiNip: String?,

    @SerializedName(value = "atasan_pegawai_jabatan", alternate = ["atasan_jabatan"])
    val atasanPegawaiJabatan: String?
)

data class KandidatBawahanResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<KandidatBawahanItem> = emptyList()
)

data class KandidatBawahanItem(
    @SerializedName("pegawai_id") val pegawaiId: Int,
    @SerializedName("pegawai_nama") val pegawaiNama: String? = null,
    @SerializedName("pegawai_nip") val pegawaiNip: String? = null,
    @SerializedName("jabatan") val jabatan: String? = null,
    @SerializedName("skpdid") val skpdid: Int? = null,
    @SerializedName("skpd") val skpd: String? = null,
    @SerializedName("pegawai_status") val pegawaiStatus: Int? = null
)

data class AtasanPegawaiUsulanListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<AtasanPegawaiUsulanItem> = emptyList()
)

data class AtasanPegawaiUsulanMutationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AtasanPegawaiUsulanMutationData? = null
)

data class AtasanPegawaiUsulanMutationData(
    @SerializedName("id") val id: Int? = null
)

data class AtasanPegawaiUsulanItem(
    @SerializedName("id") val id: Int,
    @SerializedName("aksi") val aksi: String,
    @SerializedName("status") val status: String,
    @SerializedName("target_atasan_pegawai_id") val targetAtasanPegawaiId: Int? = null,
    @SerializedName("pegawai_id") val pegawaiId: Int,
    @SerializedName("atasan_id") val atasanId: Int,
    @SerializedName("jenis_atasan") val jenisAtasan: String,
    @SerializedName("is_active") val isActive: Int? = null,
    @SerializedName("tanggal_mulai") val tanggalMulai: String? = null,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String? = null,
    @SerializedName("skpdid") val skpdid: Int? = null,
    @SerializedName("skpd") val skpd: String? = null,
    @SerializedName("alasan_pengajuan") val alasanPengajuan: String? = null,
    @SerializedName("keterangan") val keterangan: String? = null,
    @SerializedName("catatan_verifikasi") val catatanVerifikasi: String? = null,
    @SerializedName("diajukan_at") val diajukanAt: String? = null,
    @SerializedName("diverifikasi_at") val diverifikasiAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("pegawai_nama") val pegawaiNama: String? = null,
    @SerializedName("pegawai_nip") val pegawaiNip: String? = null,
    @SerializedName("pegawai_jabatan") val pegawaiJabatan: String? = null,
    @SerializedName("atasan_nama") val atasanNama: String? = null,
    @SerializedName("atasan_nip") val atasanNip: String? = null,
    @SerializedName("atasan_jabatan") val atasanJabatan: String? = null,
    @SerializedName("created_by_nama") val createdByNama: String? = null,
    @SerializedName("diajukan_oleh_nama") val diajukanOlehNama: String? = null,
    @SerializedName("diverifikasi_oleh_nama") val diverifikasiOlehNama: String? = null
)

data class AtasanPegawaiUsulanRequest(
    @SerializedName("aksi") val aksi: String,
    @SerializedName("target_atasan_pegawai_id") val targetAtasanPegawaiId: Int? = null,
    @SerializedName("pegawai_id") val pegawaiId: Int,
    @SerializedName("atasan_id") val atasanId: Int,
    @SerializedName("jenis_atasan") val jenisAtasan: String = "Langsung",
    @SerializedName("tanggal_mulai") val tanggalMulai: String,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String? = null,
    @SerializedName("alasan_pengajuan") val alasanPengajuan: String? = null,
    @SerializedName("keterangan") val keterangan: String? = null,
    @SerializedName("submit") val submit: Boolean = false
)

data class AtasanPegawaiUsulanVerifyRequest(
    @SerializedName("keputusan") val keputusan: String,
    @SerializedName("catatan_verifikasi") val catatanVerifikasi: String? = null
)
