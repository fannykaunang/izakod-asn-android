package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class PengumumanHighlightsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<PengumumanHighlightItem> = emptyList(),

    @SerializedName("meta")
    val meta: PengumumanMeta? = null
)

data class PengumumanReadResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: PengumumanReadDetail? = null
)

data class PengumumanMeta(
    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("limit")
    val limit: Int? = null
)

data class PengumumanHighlightItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("tipe")
    val tipe: String? = null,

    @SerializedName("judul")
    val judul: String,

    @SerializedName("ringkasan")
    val ringkasan: String? = null,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerializedName("kategori_konten")
    val kategoriKonten: String? = null,

    @SerializedName("public_slug")
    val publicSlug: String? = null,

    @SerializedName("public_url")
    val publicUrl: String? = null,

    @SerializedName("link_tujuan")
    val linkTujuan: String? = null,

    @SerializedName("action_required")
    val actionRequired: Boolean = false,

    @SerializedName("dashboard_priority")
    val dashboardPriority: Int = 0,

    @SerializedName("published_at")
    val publishedAt: String? = null,

    @SerializedName("expires_at")
    val expiresAt: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class PengumumanReadDetail(
    @SerializedName("id")
    val id: Int,

    @SerializedName("tipe")
    val tipe: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("judul")
    val judul: String,

    @SerializedName("ringkasan")
    val ringkasan: String? = null,

    @SerializedName("pesan")
    val pesan: String? = null,

    @SerializedName("isi_konten")
    val isiKonten: String? = null,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @SerializedName("kategori_konten")
    val kategoriKonten: String? = null,

    @SerializedName("public_slug")
    val publicSlug: String? = null,

    @SerializedName("public_url")
    val publicUrl: String? = null,

    @SerializedName("link_tujuan")
    val linkTujuan: String? = null,

    @SerializedName("action_required")
    val actionRequired: Boolean = false,

    @SerializedName("is_withdrawn")
    val isWithdrawn: Boolean = false,

    @SerializedName("withdrawn_at")
    val withdrawnAt: String? = null,

    @SerializedName("withdrawn_reason")
    val withdrawnReason: String? = null,

    @SerializedName("published_at")
    val publishedAt: String? = null,

    @SerializedName("expires_at")
    val expiresAt: String? = null,

    @SerializedName("created_by_nama")
    val createdByNama: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)
