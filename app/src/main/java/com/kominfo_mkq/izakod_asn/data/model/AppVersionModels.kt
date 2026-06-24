package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class AppVersionPolicyResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: AppVersionPolicy? = null
)

data class AppVersionPolicy(
    @SerializedName("policy_id")
    val policyId: Long? = null,

    @SerializedName("app_key")
    val appKey: String? = null,

    @SerializedName("platform")
    val platform: String? = null,

    @SerializedName("package_name")
    val packageName: String? = null,

    @SerializedName("current_version_code")
    val currentVersionCode: Int? = null,

    @SerializedName("current_version_name")
    val currentVersionName: String? = null,

    @SerializedName("latest_version_code")
    val latestVersionCode: Int? = null,

    @SerializedName("latest_version_name")
    val latestVersionName: String? = null,

    @SerializedName("minimum_supported_version_code")
    val minimumSupportedVersionCode: Int? = null,

    @SerializedName("update_available")
    val updateAvailable: Boolean = false,

    @SerializedName("update_required")
    val updateRequired: Boolean = false,

    @SerializedName("update_title")
    val updateTitle: String? = null,

    @SerializedName("update_message")
    val updateMessage: String? = null,

    @SerializedName("release_notes")
    val releaseNotes: String? = null,

    @SerializedName("store_url")
    val storeUrl: String? = null,

    @SerializedName("check_interval_seconds")
    val checkIntervalSeconds: Long = 86_400L,

    @SerializedName("checked_at")
    val checkedAt: String? = null
)

data class AppUpdateEventRequest(
    @SerializedName("app_key")
    val appKey: String = "izakod_asn",

    @SerializedName("platform")
    val platform: String = "android",

    @SerializedName("package_name")
    val packageName: String? = null,

    @SerializedName("policy_id")
    val policyId: Long? = null,

    @SerializedName("device_id")
    val deviceId: String? = null,

    @SerializedName("device_model")
    val deviceModel: String? = null,

    @SerializedName("event_type")
    val eventType: String,

    @SerializedName("from_version_code")
    val fromVersionCode: Int? = null,

    @SerializedName("from_version_name")
    val fromVersionName: String? = null,

    @SerializedName("target_version_code")
    val targetVersionCode: Int? = null,

    @SerializedName("target_version_name")
    val targetVersionName: String? = null,

    @SerializedName("to_version_code")
    val toVersionCode: Int? = null,

    @SerializedName("to_version_name")
    val toVersionName: String? = null,

    @SerializedName("update_required")
    val updateRequired: Boolean = false,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("metadata")
    val metadata: Map<String, Any?>? = null
)

data class AppUpdateEventResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: AppUpdateEventResult? = null
)

data class AppUpdateEventResult(
    @SerializedName("event_id")
    val eventId: Long? = null,

    @SerializedName("logged_activity")
    val loggedActivity: Boolean = false,

    @SerializedName("activity_log_id")
    val activityLogId: Long? = null
)
