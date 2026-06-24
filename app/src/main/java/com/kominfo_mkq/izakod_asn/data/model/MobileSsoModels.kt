package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class MobileSsoExchangeRequest(
    @SerializedName("ticket")
    val ticket: String
)

data class MobileSsoExchangeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: MobileSsoExchangeData? = null
)

data class MobileSsoExchangeData(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    @SerializedName("token_type")
    val tokenType: String? = null,

    @SerializedName("expires_in")
    val expiresIn: Long? = null,

    @SerializedName("entago_access_token")
    val entagoAccessToken: String? = null,

    @SerializedName("entago_refresh_token")
    val entagoRefreshToken: String? = null,

    @SerializedName("entago_token_type")
    val entagoTokenType: String? = null,

    @SerializedName("user")
    val user: EntagoLoginUser? = null,

    @SerializedName("target")
    val target: MobileSsoTarget? = null,

    @SerializedName("payroll_estimate")
    val payrollEstimate: JsonObject? = null
)

data class MobileSsoTarget(
    @SerializedName("jenis")
    val jenis: String? = null,

    @SerializedName("tahun")
    val tahun: Int? = null,

    @SerializedName("bulan")
    val bulan: Int? = null,

    @SerializedName("route")
    val route: String? = null
)
