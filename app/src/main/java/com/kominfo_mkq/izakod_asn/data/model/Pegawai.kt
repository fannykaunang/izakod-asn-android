package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

/**
 * Pegawai Profile from ASP.NET Core Eabsen API
 */
data class PegawaiProfile(
    @SerializedName(value = "pegawai_id", alternate = ["pegawai_Id", "Pegawai_Id"])
    val pegawaiId: Int,

    @SerializedName(value = "pegawai_pin", alternate = ["pegawai_Pin", "Pegawai_Pin", "pin"])
    val pegawaiPin: String?,

    @SerializedName(value = "pegawai_nip", alternate = ["pegawai_Nip", "Pegawai_Nip"])
    val pegawaiNip: String?,

    @SerializedName(value = "pegawai_nama", alternate = ["pegawai_Nama", "Pegawai_Nama"])
    val pegawaiNama: String?,

    @SerializedName(value = "tempat_lahir", alternate = ["tempat_Lahir", "Tempat_Lahir"])
    val tempatLahir: String?,

    @SerializedName(value = "pegawai_privilege", alternate = ["pegawai_Privilege", "Pegawai_Privilege"])
    val pegawaiPrivilege: String?,

    @SerializedName(value = "pegawai_telp", alternate = ["pegawai_Telp", "Pegawai_Telp"])
    val pegawaiTelp: String?,

    @SerializedName(value = "pegawai_status", alternate = ["pegawai_Status", "Pegawai_Status"])
    val pegawaiStatus: Int?,

    @SerializedName(value = "tgl_lahir", alternate = ["tgl_Lahir", "Tgl_Lahir"])
    val tglLahir: String?,

    @SerializedName(value = "jabatan", alternate = ["Jabatan"])
    val jabatan: String?,

    @SerializedName(value = "skpd", alternate = ["Skpd"])
    val skpd: String?,

    @SerializedName(value = "sotk", alternate = ["Sotk"])
    val sotk: String?,

    @SerializedName(value = "tgl_mulai_kerja", alternate = ["tgl_Mulai_Kerja", "Tgl_Mulai_Kerja"])
    val tglMulaiKerja: String?,

    @SerializedName(value = "gender", alternate = ["Gender"])
    val gender: Int,

    @SerializedName(value = "photo_path", alternate = ["photo_Path", "Photo_Path"])
    val photoPath: String?,

    @SerializedName(value = "deviceid", alternate = ["deviceId", "DeviceId"])
    val deviceId: String?
)

data class PegawaiProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: PegawaiProfile? = null
)
