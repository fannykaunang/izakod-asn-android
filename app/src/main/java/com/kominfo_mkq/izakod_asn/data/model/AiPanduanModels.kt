package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class AiPanduanRequest(
    @SerializedName("question")
    val question: String,
    @SerializedName("force_ai")
    val forceAi: Boolean = false,
    @SerializedName("surface")
    val surface: String = "android_dashboard"
)

data class AiPanduanResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("mode")
    val mode: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("answer")
    val answer: String? = null,
    @SerializedName("sources")
    val sources: List<AiPanduanSource> = emptyList(),
    @SerializedName("actions")
    val actions: List<AiPanduanAction> = emptyList(),
    @SerializedName("disclaimer")
    val disclaimer: String? = null,
    @SerializedName("data")
    val data: AiPanduanData? = null
)

data class AiPanduanData(
    @SerializedName("mode")
    val mode: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("answer")
    val answer: String? = null,
    @SerializedName("sources")
    val sources: List<AiPanduanSource> = emptyList(),
    @SerializedName("actions")
    val actions: List<AiPanduanAction> = emptyList(),
    @SerializedName("disclaimer")
    val disclaimer: String? = null
)

data class AiPanduanSource(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("path")
    val path: String? = null,
    @SerializedName("section")
    val section: String? = null
)

data class AiPanduanAction(
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("href")
    val href: String? = null,
    @SerializedName("action")
    val action: String? = null
)

data class AiPanduanTopic(
    val id: String,
    val title: String,
    val question: String,
    val description: String,
    val answer: String,
    val sourceTitle: String
)

data class AiPanduanChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val isTyping: Boolean = false,
    val sources: List<AiPanduanSource> = emptyList(),
    val disclaimer: String? = null
)
