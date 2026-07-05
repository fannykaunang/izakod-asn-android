package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class PayrollDisplay(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("nominal")
    val nominal: Double? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("badge")
    val badge: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("is_final")
    val isFinal: Boolean = false,

    @SerializedName("detail_status")
    val detailStatus: String? = null
)
