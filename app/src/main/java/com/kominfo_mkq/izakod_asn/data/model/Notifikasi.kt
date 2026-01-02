package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class NotifikasiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<Notifikasi>,

    @SerializedName("count")
    val count: Int,

    @SerializedName("unread")
    val unread: Int
)

data class Notifikasi(
    @SerializedName("notifikasi_id")
    val notifikasiId: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("judul")
    val judul: String,

    @SerializedName("pesan")
    val pesan: String,

    @SerializedName("tipe_notifikasi")
    val tipeNotifikasi: String,  // "Verifikasi", "Penolakan", "Komentar"

    @SerializedName("laporan_id")
    val laporanId: Int?,

    @SerializedName("link_tujuan")
    val linkTujuan: String?,

    @SerializedName("action_required")
    val actionRequired: Int,  // 0 or 1

    @SerializedName("is_read")
    val isRead: Int,  // 0 or 1

    @SerializedName("tanggal_dibaca")
    val tanggalDibaca: String?,

    @SerializedName("created_at")
    val createdAt: String
)