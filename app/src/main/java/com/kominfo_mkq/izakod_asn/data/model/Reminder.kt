package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response from GET /api/reminder
 */
data class ReminderListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<Reminder>,

    @SerializedName("pagination")
    val pagination: Pagination,

    @SerializedName("stats")
    val stats: ReminderStats,

    @SerializedName("meta")
    val meta: ReminderMeta,

    @SerializedName("pegawaiOptions")
    val pegawaiOptions: List<PegawaiOption>
)

/**
 * Individual Reminder
 */
data class Reminder(
    @SerializedName("reminder_id")
    val reminderId: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("judul_reminder")
    val judulReminder: String,

    @SerializedName("pesan_reminder")
    val pesanReminder: String?,

    @SerializedName("tipe_reminder")
    val tipeReminder: String,  // "Harian", "Mingguan", "Bulanan", "Sekali"

    @SerializedName("waktu_reminder")
    val waktuReminder: String,  // "HH:MM:SS"

    @SerializedName("hari_dalam_minggu")
    val hariDalamMinggu: List<String>?,  // ["Senin", "Rabu", ...]

    @SerializedName("tanggal_spesifik")
    val tanggalSpesifik: String?,  // "YYYY-MM-DD"

    @SerializedName("is_active")
    val isActive: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String?
)

data class Pagination(
    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("total")
    val total: Int,

    @SerializedName("totalPages")
    val totalPages: Int
)

data class ReminderStats(
    @SerializedName("total")
    val total: Int,

    @SerializedName("active")
    val active: String,

    @SerializedName("harian")
    val harian: String,

    @SerializedName("mingguan")
    val mingguan: String,

    @SerializedName("bulanan")
    val bulanan: String
)

data class ReminderMeta(
    @SerializedName("isAdmin")
    val isAdmin: Boolean,

    @SerializedName("currentPegawaiId")
    val currentPegawaiId: Int,

    @SerializedName("currentPegawaiName")
    val currentPegawaiName: String?
)

data class PegawaiOption(
    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String?
)

/**
 * Request for POST /api/reminder
 */
data class CreateReminderRequest(
    @SerializedName("pegawai_id")
    val pegawaiId: Int? = null,

    @SerializedName("judul_reminder")
    val judulReminder: String,

    @SerializedName("pesan_reminder")
    val pesanReminder: String? = null,

    @SerializedName("tipe_reminder")
    val tipeReminder: String,  // "Harian", "Mingguan", "Bulanan", "Sekali"

    @SerializedName("waktu_reminder")
    val waktuReminder: String,  // "HH:MM"

    @SerializedName("hari_dalam_minggu")
    val hariDalamMinggu: List<String>? = null,

    @SerializedName("tanggal_spesifik")
    val tanggalSpesifik: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean = true
)

/**
 * Response from POST /api/reminder
 */
data class CreateReminderResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: Reminder?
)

data class DeleteReminderResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null
)