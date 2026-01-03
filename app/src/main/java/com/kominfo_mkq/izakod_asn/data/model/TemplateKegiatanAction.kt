package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class TemplateKegiatanCreateRequest(
    @SerializedName("nama_template") val namaTemplate: String,
    @SerializedName("kategori_id") val kategoriId: Int,
    @SerializedName("deskripsi_template") val deskripsiTemplate: String?,
    @SerializedName("target_output_default") val targetOutputDefault: String?,
    @SerializedName("lokasi_default") val lokasiDefault: String?,
    @SerializedName("durasi_estimasi_menit") val durasiEstimasiMenit: Int?,
    @SerializedName("is_public") val isPublic: Int,
    @SerializedName("unit_kerja_akses") val unitKerjaAkses: String?,
    @SerializedName("is_active") val isActive: Int = 1
)

data class TemplateKegiatanCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TemplateKegiatanCreateData?
)

data class TemplateKegiatanCreateData(
    @SerializedName("template_id") val templateId: Int
)

data class BasicActionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
