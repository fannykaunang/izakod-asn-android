package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class TemplateKegiatan(
    @SerializedName("template_id")
    val templateId: Int,

    @SerializedName("kategori_id")
    val kategoriId: Int,

    @SerializedName("kategori_nama")
    val kategoriNama: String?,

    @SerializedName("nama_template")
    val namaTemplate: String,

    @SerializedName("deskripsi")
    val deskripsi: String?,

    @SerializedName("target_output_default")
    val targetOutputDefault: String?,

    @SerializedName("estimasi_durasi")
    val estimasiDurasi: Int?,

    @SerializedName("lokasi_default")
    val lokasiDefault: String?,

    @SerializedName("is_public")
    val isPublic: Int, // 0 = private, 1 = public

    @SerializedName("unit_kerja")
    val unitKerja: String?,

    @SerializedName("created_by")
    val createdBy: Int?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)

data class TemplateKegiatanResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<TemplateKegiatan>,

    @SerializedName("message")
    val message: String
)