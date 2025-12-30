package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model untuk login API eAbsen
 */
data class EabsenLoginResponse(
    @SerializedName("result")
    val result: Int,

    @SerializedName("response")
    val response: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("pin")
    val pin: String,

    @SerializedName("level")
    val level: Int,

    @SerializedName("skpdid")
    val skpdid: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int? = null
)

/**
 * Request body untuk login
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("pwd")
    val pwd: String
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