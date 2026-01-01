package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

/**
 * Pegawai Profile from ASP.NET Core Eabsen API
 */
data class PegawaiProfile(
    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_pin")
    val pegawaiPin: String,

    @SerializedName("pegawai_nip")
    val pegawaiNip: String,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String,

    @SerializedName("tempat_lahir")
    val tempatLahir: String?,

    @SerializedName("pegawai_privilege")
    val pegawaiPrivilege: String?,

    @SerializedName("pegawai_telp")
    val pegawaiTelp: String?,

    @SerializedName("pegawai_status")
    val pegawaiStatus: Int?,

    @SerializedName("tgl_lahir")
    val tglLahir: String?,

    @SerializedName("jabatan")
    val jabatan: String?,

    @SerializedName("skpd")
    val skpd: String?,

    @SerializedName("sotk")
    val sotk: String?,

    @SerializedName("tgl_mulai_kerja")
    val tglMulaiKerja: String?,

    @SerializedName("gender")
    val gender: Int,

    @SerializedName("photo_path")
    val photoPath: String?,

    @SerializedName("deviceid")
    val deviceId: String?
)