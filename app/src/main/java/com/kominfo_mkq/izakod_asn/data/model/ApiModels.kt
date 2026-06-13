package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class EntagoLoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class EntagoLoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: EntagoLoginData? = null
)

data class EntagoLoginData(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    @SerializedName("user")
    val user: EntagoLoginUser? = null
)

data class EntagoLoginUser(
    @SerializedName("userid")
    val userId: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("pin")
    val pin: String,

    @SerializedName("skpdid")
    val skpdid: Int,

    @SerializedName("level")
    val level: Int,

    @SerializedName("deviceid")
    val deviceId: String = "",

    @SerializedName("pegawai_id")
    val pegawaiId: Int = 0,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String? = null,

    @SerializedName("pegawai_nip")
    val pegawaiNip: String? = null,

    @SerializedName("tempat_lahir")
    val tempatLahir: String? = null,

    @SerializedName("tgl_lahir")
    val tglLahir: String? = null,

    @SerializedName("gender")
    val gender: Int? = null,

    @SerializedName("pegawai_telp")
    val pegawaiTelp: String? = null,

    @SerializedName("pegawai_status")
    val pegawaiStatus: Int? = null,

    @SerializedName("jabatan")
    val jabatan: String? = null,

    @SerializedName("skpd")
    val skpd: String? = null,

    @SerializedName("sotk")
    val sotk: String? = null,

    @SerializedName("tgl_mulai_kerja")
    val tglMulaiKerja: String? = null,

    @SerializedName("photo_path")
    val photoPath: String? = null
)

data class AuthenticatedSession(
    val token: String,
    val refreshToken: String?,
    val user: EntagoLoginUser
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: RefreshTokenData? = null
)

data class RefreshTokenData(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("refresh_token")
    val refreshToken: String? = null
)

/**
 * Generic API Response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

/**
 * Data pegawai dari eAbsen API
 */
data class PegawaiData(
    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pin")
    val pin: String,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("nip")
    val nip: String? = null,

    @SerializedName("jabatan")
    val jabatan: String? = null,

    @SerializedName("skpd")
    val skpd: String? = null,

    @SerializedName("foto")
    val foto: String? = null
)
