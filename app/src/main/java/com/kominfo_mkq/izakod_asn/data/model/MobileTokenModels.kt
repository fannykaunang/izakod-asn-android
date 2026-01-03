package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class MobileTokenRequest(
    @SerializedName("pegawai_id")
    val pegawai_id: Int,

    @SerializedName("pin")
    val pin: String
)

data class MobileTokenResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    // kalau API kamu return { data: { token: "..." } }
    @SerializedName("data")
    val data: MobileTokenData? = null
)

data class MobileTokenData(
    @SerializedName("token")
    val token: String,

    @SerializedName("token_type")
    val tokenType: String? = "Bearer",

    @SerializedName("expires_in")
    val expiresIn: Long? = null
)
