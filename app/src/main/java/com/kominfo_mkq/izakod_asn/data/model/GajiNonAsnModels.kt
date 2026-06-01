package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class GajiNonAsnMeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: GajiNonAsnMeData? = null
)

data class GajiNonAsnMeData(
    @SerializedName("periode")
    val periode: GajiNonAsnPeriodeInfo,

    @SerializedName("pegawai")
    val pegawai: GajiNonAsnPegawaiInfo,

    @SerializedName("profile")
    val profile: GajiNonAsnProfile? = null,

    @SerializedName("kontrak")
    val kontrak: GajiNonAsnKontrak? = null,

    @SerializedName("rekap")
    val rekap: GajiNonAsnRekap? = null,

    @SerializedName("perhitungan")
    val perhitungan: GajiNonAsnPerhitungan? = null,

    @SerializedName("pengajuan_opd")
    val pengajuanOpd: GajiNonAsnPengajuanOpd? = null,

    @SerializedName("status")
    val status: GajiNonAsnStatus
)

data class GajiNonAsnPeriodeInfo(
    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int
)

data class GajiNonAsnPegawaiInfo(
    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("pegawai_pin")
    val pegawaiPin: String? = null,

    @SerializedName("pegawai_nip")
    val pegawaiNip: String? = null,

    @SerializedName("pegawai_nama")
    val pegawaiNama: String? = null,

    @SerializedName("jabatan")
    val jabatan: String? = null,

    @SerializedName("jenis_non_asn")
    val jenisNonAsn: String? = null,

    @SerializedName("skpdid")
    val skpdid: Int? = null,

    @SerializedName("skpd")
    val skpd: String? = null,

    @SerializedName("photo_path")
    val photoPath: String? = null,

    @SerializedName("last_sync")
    val lastSync: String? = null
)

data class GajiNonAsnProfile(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("jenis_non_asn")
    val jenisNonAsn: String? = null,

    @SerializedName("status_payroll")
    val statusPayroll: String? = null,

    @SerializedName("pend_id")
    val pendId: Int? = null,

    @SerializedName("pendidikan_snapshot")
    val pendidikanSnapshot: String? = null,

    @SerializedName("pendidikan_nama")
    val pendidikanNama: String? = null,

    @SerializedName("kelompok_pendidikan")
    val kelompokPendidikan: String? = null,

    @SerializedName("gaji_pokok")
    val gajiPokok: Double? = null,

    @SerializedName("potongan_tk_harian")
    val potonganTkHarian: Double? = null,

    @SerializedName("verified_at")
    val verifiedAt: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class GajiNonAsnKontrak(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("nomor_kontrak")
    val nomorKontrak: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("tanggal_mulai")
    val tanggalMulai: String? = null,

    @SerializedName("tanggal_selesai")
    val tanggalSelesai: String? = null
)

data class GajiNonAsnRekap(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("total_hari_kerja")
    val totalHariKerja: Int? = null,

    @SerializedName("hadir_sah")
    val hadirSah: Int? = null,

    @SerializedName("tk")
    val tk: Int? = null,

    @SerializedName("tidak_lengkap")
    val tidakLengkap: Int? = null,

    @SerializedName("tidak_lengkap_ditoleransi")
    val tidakLengkapDitoleransi: Int? = null,

    @SerializedName("tidak_lengkap_kena_potong")
    val tidakLengkapKenaPotong: Int? = null,

    @SerializedName("terlambat")
    val terlambat: Int? = null,

    @SerializedName("pulang_cepat")
    val pulangCepat: Int? = null,

    @SerializedName("izin")
    val izin: Int? = null,

    @SerializedName("sakit_dengan_surat")
    val sakitDenganSurat: Int? = null,

    @SerializedName("sakit_tanpa_surat")
    val sakitTanpaSurat: Int? = null,

    @SerializedName("cuti_normatif")
    val cutiNormatif: Int? = null,

    @SerializedName("cuti_pribadi")
    val cutiPribadi: Int? = null,

    @SerializedName("dinas_luar")
    val dinasLuar: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("sumber_rekap")
    val sumberRekap: String? = null,

    @SerializedName("synced_at")
    val syncedAt: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class GajiNonAsnPerhitungan(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("pengajuan_opd_id")
    val pengajuanOpdId: Int? = null,

    @SerializedName("rekap_kehadiran_id")
    val rekapKehadiranId: Int? = null,

    @SerializedName("jenis_non_asn")
    val jenisNonAsn: String? = null,

    @SerializedName("kontrak_id")
    val kontrakId: Int? = null,

    @SerializedName("nomor_kontrak")
    val nomorKontrak: String? = null,

    @SerializedName("pend_id")
    val pendId: Int? = null,

    @SerializedName("pendidikan")
    val pendidikan: String? = null,

    @SerializedName("gaji_pokok")
    val gajiPokok: Double? = null,

    @SerializedName("potongan_tk_harian")
    val potonganTkHarian: Double? = null,

    @SerializedName("total_hari_kerja")
    val totalHariKerja: Int? = null,

    @SerializedName("jumlah_tk")
    val jumlahTk: Int? = null,

    @SerializedName("jumlah_tidak_lengkap")
    val jumlahTidakLengkap: Int? = null,

    @SerializedName("jumlah_tidak_lengkap_ditoleransi")
    val jumlahTidakLengkapDitoleransi: Int? = null,

    @SerializedName("jumlah_tidak_lengkap_kena_potong")
    val jumlahTidakLengkapKenaPotong: Int? = null,

    @SerializedName("terlambat")
    val terlambat: Int? = null,

    @SerializedName("pulang_cepat")
    val pulangCepat: Int? = null,

    @SerializedName("potongan_tk")
    val potonganTk: Double? = null,

    @SerializedName("potongan_tidak_lengkap")
    val potonganTidakLengkap: Double? = null,

    @SerializedName("potongan_lainnya")
    val potonganLainnya: Double? = null,

    @SerializedName("total_potongan")
    val totalPotongan: Double? = null,

    @SerializedName("total_dibayar")
    val totalDibayar: Double? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("calculated_at")
    val calculatedAt: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class GajiNonAsnPengajuanOpd(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("nomor_pengajuan")
    val nomorPengajuan: String? = null,

    @SerializedName("submitted_at")
    val submittedAt: String? = null,

    @SerializedName("verified_at")
    val verifiedAt: String? = null,

    @SerializedName("finalized_at")
    val finalizedAt: String? = null
)

data class GajiNonAsnStatus(
    @SerializedName("profile_available")
    val profileAvailable: Boolean = false,

    @SerializedName("profile_active")
    val profileActive: Boolean = false,

    @SerializedName("rekap_available")
    val rekapAvailable: Boolean = false,

    @SerializedName("calculation_available")
    val calculationAvailable: Boolean = false,

    @SerializedName("ready")
    val ready: Boolean = false,

    @SerializedName("issues")
    val issues: List<String> = emptyList(),

    @SerializedName("label")
    val label: String? = null
)
