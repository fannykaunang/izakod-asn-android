package com.kominfo_mkq.izakod_asn.data.model

data class FcmRegisterRequest(
    val fcm_token: String,
    val device_id: String,
    val device_model: String,
    val app_version: String
)
