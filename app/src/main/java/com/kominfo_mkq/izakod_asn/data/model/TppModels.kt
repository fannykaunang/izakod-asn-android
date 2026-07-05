package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.annotations.SerializedName

data class TppMeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: TppMeData? = null
)

data class TppMeData(
    @SerializedName("periode")
    val periode: TppPeriodeInfo,

    @SerializedName("pegawai")
    val pegawai: TppPegawaiInfo,

    @SerializedName("profile")
    val profile: TppProfile? = null,

    @SerializedName("rekap")
    val rekap: TppRekapKehadiran? = null,

    @SerializedName("perhitungan")
    val perhitungan: TppPerhitungan? = null,

    @SerializedName("nominal_tpp")
    val nominalTpp: TppNominal? = null,

    @SerializedName("display_payroll")
    val displayPayroll: PayrollDisplay? = null,

    @SerializedName("apel_harian")
    val apelHarian: List<TppApelHarian> = emptyList(),

    @SerializedName("status")
    val status: TppMeStatus
)

data class TppPeriodeInfo(
    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int
)

data class TppPegawaiInfo(
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

    @SerializedName("skpdid")
    val skpdid: Int? = null,

    @SerializedName("skpd")
    val skpd: String? = null,

    @SerializedName("photo_path")
    val photoPath: String? = null,

    @SerializedName("last_sync")
    val lastSync: String? = null
)

data class TppProfile(
    @SerializedName("id")
    val id: Int,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("skpdid")
    val skpdid: Int? = null,

    @SerializedName("skpd_snapshot")
    val skpdSnapshot: String? = null,

    @SerializedName("jabatan_snapshot")
    val jabatanSnapshot: String? = null,

    @SerializedName("jabatan_ref_id")
    val jabatanRefId: Int? = null,

    @SerializedName("jabatan_normalized")
    val jabatanNormalized: String? = null,

    @SerializedName("jenis_jabatan")
    val jenisJabatan: String? = null,

    @SerializedName("level_jabatan")
    val levelJabatan: String? = null,

    @SerializedName("kelas_jabatan")
    val kelasJabatan: Int? = null,

    @SerializedName("golongan_pajak")
    val golonganPajak: String? = null,

    @SerializedName("is_kepala_opd")
    val isKepalaOpd: Int? = null,

    @SerializedName("status_tpp")
    val statusTpp: String? = null,

    @SerializedName("verified_at")
    val verifiedAt: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class TppRekapKehadiran(
    @SerializedName("id")
    val id: Int,

    @SerializedName("periode_id")
    val periodeId: Int? = null,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("skpdid")
    val skpdid: Int? = null,

    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int,

    @SerializedName("total_hari_tercatat")
    val totalHariTercatat: Int = 0,

    @SerializedName("total_hari_kerja")
    val totalHariKerja: Int = 0,

    @SerializedName("hadir_scan_lengkap")
    val hadirScanLengkap: Int = 0,

    @SerializedName("hadir_pengecualian_resmi")
    val hadirPengecualianResmi: Int = 0,

    @SerializedName("hadir_sah")
    val hadirSah: Int? = null,

    @SerializedName("dinas_luar")
    val dinasLuar: Int = 0,

    @SerializedName("izin")
    val izin: Int = 0,

    @SerializedName("sakit_dengan_surat")
    val sakitDenganSurat: Int = 0,

    @SerializedName("sakit_dengan_surat_potong_2")
    val sakitDenganSuratPotong2: Int = 0,

    @SerializedName("sakit_dengan_surat_potong_3")
    val sakitDenganSuratPotong3: Int = 0,

    @SerializedName("sakit_lebih_3_bulan")
    val sakitLebih3Bulan: Int = 0,

    @SerializedName("sakit_tanpa_surat")
    val sakitTanpaSurat: Int = 0,

    @SerializedName("cuti_normatif")
    val cutiNormatif: Int = 0,

    @SerializedName("cuti_pribadi")
    val cutiPribadi: Int = 0,

    @SerializedName("cuti_melahirkan")
    val cutiMelahirkan: Int = 0,

    @SerializedName("cuti_alasan_penting")
    val cutiAlasanPenting: Int = 0,

    @SerializedName("cuti_tahunan_12_hari")
    val cutiTahunan12Hari: Int = 0,

    @SerializedName("tk")
    val tk: Int = 0,

    @SerializedName("tidak_lengkap")
    val tidakLengkap: Int = 0,

    @SerializedName("tl1")
    val tl1: Int = 0,

    @SerializedName("tl2")
    val tl2: Int = 0,

    @SerializedName("tl3")
    val tl3: Int = 0,

    @SerializedName("tl4")
    val tl4: Int = 0,

    @SerializedName("pc1")
    val pc1: Int = 0,

    @SerializedName("pc2")
    val pc2: Int = 0,

    @SerializedName("pc3")
    val pc3: Int = 0,

    @SerializedName("pc4")
    val pc4: Int = 0,

    @SerializedName("apel_valid")
    val apelValid: Int = 0,

    @SerializedName("apel_review")
    val apelReview: Int = 0,

    @SerializedName("apel_rejected")
    val apelRejected: Int = 0,

    @SerializedName("apel_tidak_hadir")
    val apelTidakHadir: Int = 0,

    @SerializedName("apel_total_sesi")
    val apelTotalSesi: Int = 0,

    @SerializedName("apel_tidak_ikut")
    val apelTidakIkut: Int = 0,

    @SerializedName("potongan_kehadiran_persen")
    val potonganKehadiranPersen: Double = 0.0,

    @SerializedName("potongan_apel_persen")
    val potonganApelPersen: Double = 0.0,

    @SerializedName("potongan_disiplin_persen")
    val potonganDisiplinPersen: Double = 0.0,

    @SerializedName("faktor_bayar_tpp_persen")
    val faktorBayarTppPersen: Double = 100.0,

    @SerializedName("potongan_total_tpp_persen")
    val potonganTotalTppPersen: Double = 0.0,

    @SerializedName("sumber_rekap")
    val sumberRekap: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("calculated_at")
    val calculatedAt: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class TppPerhitungan(
    @SerializedName("id")
    val id: Int,

    @SerializedName("periode_id")
    val periodeId: Int? = null,

    @SerializedName("pengajuan_opd_id")
    val pengajuanOpdId: Int? = null,

    @SerializedName("pegawai_id")
    val pegawaiId: Int,

    @SerializedName("skpdid")
    val skpdid: Int? = null,

    @SerializedName("tahun")
    val tahun: Int,

    @SerializedName("bulan")
    val bulan: Int,

    @SerializedName("jabatan_snapshot")
    val jabatanSnapshot: String? = null,

    @SerializedName("skpd_snapshot")
    val skpdSnapshot: String? = null,

    @SerializedName("total_tpp_dasar")
    val totalTppDasar: Double = 0.0,

    @SerializedName("komponen_kinerja_persen")
    val komponenKinerjaPersen: Double = 0.0,

    @SerializedName("komponen_disiplin_persen")
    val komponenDisiplinPersen: Double = 0.0,

    @SerializedName("nilai_kinerja")
    val nilaiKinerja: Double = 0.0,

    @SerializedName("nilai_disiplin")
    val nilaiDisiplin: Double = 0.0,

    @SerializedName("potongan_kinerja")
    val potonganKinerja: Double = 0.0,

    @SerializedName("potongan_disiplin")
    val potonganDisiplin: Double = 0.0,

    @SerializedName("potongan_lainnya")
    val potonganLainnya: Double = 0.0,

    @SerializedName("total_potongan")
    val totalPotongan: Double = 0.0,

    @SerializedName("total_bruto")
    val totalBruto: Double = 0.0,

    @SerializedName("total_netto")
    val totalNetto: Double = 0.0,

    @SerializedName("total_sebelum_pajak")
    val totalSebelumPajak: Double = 0.0,

    @SerializedName("golongan_pajak_snapshot")
    val golonganPajakSnapshot: String? = null,

    @SerializedName("tarif_pajak_persen")
    val tarifPajakPersen: Double = 0.0,

    @SerializedName("potongan_pajak")
    val potonganPajak: Double = 0.0,

    @SerializedName("total_dibayar")
    val totalDibayar: Double = 0.0,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("sumber_rekap_kehadiran_id")
    val sumberRekapKehadiranId: Int? = null,

    @SerializedName("calculated_at")
    val calculatedAt: String? = null,

    @SerializedName("submitted_at")
    val submittedAt: String? = null,

    @SerializedName("approved_at")
    val approvedAt: String? = null,

    @SerializedName("paid_at")
    val paidAt: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null
)

data class TppNominal(
    @SerializedName("total_tpp_dasar")
    val totalTppDasar: Double = 0.0,

    @SerializedName("nilai_disiplin")
    val nilaiDisiplin: Double = 0.0,

    @SerializedName("nilai_kinerja")
    val nilaiKinerja: Double = 0.0,

    @SerializedName("potongan_disiplin")
    val potonganDisiplin: Double = 0.0,

    @SerializedName("potongan_kinerja")
    val potonganKinerja: Double = 0.0,

    @SerializedName("potongan_lainnya")
    val potonganLainnya: Double = 0.0,

    @SerializedName("total_potongan")
    val totalPotongan: Double = 0.0,

    @SerializedName("total_bruto")
    val totalBruto: Double = 0.0,

    @SerializedName("total_netto")
    val totalNetto: Double = 0.0,

    @SerializedName("potongan_pajak")
    val potonganPajak: Double = 0.0,

    @SerializedName("total_dibayar")
    val totalDibayar: Double = 0.0,

    @SerializedName("estimasi_diterima")
    val estimasiDiterima: Double = 0.0,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("is_final")
    val isFinal: Boolean = false,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("calculated_at")
    val calculatedAt: String? = null
)

data class TppApelHarian(
    @SerializedName("id")
    val id: Int,

    @SerializedName("tanggal")
    val tanggal: String,

    @SerializedName("jenis_apel_tercatat")
    val jenisApelTercatat: String? = null,

    @SerializedName("total_sesi")
    val totalSesi: Int = 0,

    @SerializedName("sesi_final")
    val sesiFinal: Int = 0,

    @SerializedName("sesi_belum_final")
    val sesiBelumFinal: Int = 0,

    @SerializedName("apel_valid")
    val apelValid: Int = 0,

    @SerializedName("apel_review")
    val apelReview: Int = 0,

    @SerializedName("apel_rejected")
    val apelRejected: Int = 0,

    @SerializedName("apel_tidak_hadir")
    val apelTidakHadir: Int = 0,

    @SerializedName("apel_pagi_valid")
    val apelPagiValid: Int = 0,

    @SerializedName("apel_sore_valid")
    val apelSoreValid: Int = 0,

    @SerializedName("apel_khusus_valid")
    val apelKhususValid: Int = 0,

    @SerializedName("apel_korpri_valid")
    val apelKorpriValid: Int = 0,

    @SerializedName("korpri_pengganti_wfo")
    val korpriPenggantiWfo: Int = 0,

    @SerializedName("hadir_pengganti_wfo")
    val hadirPenggantiWfo: Int = 0,

    @SerializedName("siap_tpp")
    val siapTpp: Int = 0,

    @SerializedName("status_classifier")
    val statusClassifier: String? = null,

    @SerializedName("catatan")
    val catatan: String? = null,

    @SerializedName("sumber_rekap")
    val sumberRekap: String? = null
)

data class TppMeStatus(
    @SerializedName("profile_auto_created")
    val profileAutoCreated: Boolean = false,

    @SerializedName("profile_verified")
    val profileVerified: Boolean = false,

    @SerializedName("profile_ready")
    val profileReady: Boolean = false,

    @SerializedName("profile_readiness_issues")
    val profileReadinessIssues: List<String> = emptyList(),

    @SerializedName("rekap_available")
    val rekapAvailable: Boolean = false,

    @SerializedName("perhitungan_available")
    val perhitunganAvailable: Boolean = false,

    @SerializedName("siap_dihitung")
    val siapDihitung: Boolean = false,

    @SerializedName("data_source")
    val dataSource: String? = null,

    @SerializedName("label")
    val label: String? = null
)
