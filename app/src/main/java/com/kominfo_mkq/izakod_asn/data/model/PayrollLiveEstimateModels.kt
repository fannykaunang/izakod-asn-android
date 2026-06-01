package com.kominfo_mkq.izakod_asn.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class PayrollLiveEstimateResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: PayrollLiveEstimateData? = null,

    @SerializedName("meta")
    val meta: JsonObject? = null
)

data class PayrollLiveEstimateData(
    @SerializedName("jenis_estimasi")
    val jenisEstimasi: String? = null,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("available")
    val available: Boolean? = null,

    @SerializedName("is_final")
    val isFinal: Boolean? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("periode")
    val periode: PayrollLiveEstimatePeriod? = null,

    @SerializedName("pegawai")
    val pegawai: PayrollLiveEstimatePegawai? = null,

    @SerializedName("snapshot_summary")
    val snapshotSummary: PayrollLiveSnapshotSummary? = null,

    @SerializedName("nominal_tpp")
    val nominalTpp: TppNominal? = null,

    @SerializedName("nominal_gaji")
    val nominalGaji: GajiNonAsnPerhitungan? = null
)

data class PayrollLiveEstimatePeriod(
    @SerializedName("tahun")
    val tahun: Int? = null,

    @SerializedName("bulan")
    val bulan: Int? = null,

    @SerializedName("periode_label")
    val periodeLabel: String? = null,

    @SerializedName("status_snapshot")
    val statusSnapshot: String? = null
)

data class PayrollLiveEstimatePegawai(
    @SerializedName("pegawai_id")
    val pegawaiId: Int? = null,

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
    val skpd: String? = null
)

data class PayrollLiveSnapshotSummary(
    @SerializedName("total_hari_kerja")
    val totalHariKerja: Int? = null,

    @SerializedName("total_hari_kerja_periode")
    val totalHariKerjaPeriode: Int? = null,

    @SerializedName("total_hari_kerja_snapshot")
    val totalHariKerjaSnapshot: Int? = null,

    @SerializedName("hadir_lengkap")
    val hadirLengkap: Int? = null,

    @SerializedName("hadir_sah")
    val hadirSah: Int? = null,

    @SerializedName("checkin_belum_checkout")
    val checkinBelumCheckout: Int? = null,

    @SerializedName("tk")
    val tk: Int? = null,

    @SerializedName("tidak_lengkap")
    val tidakLengkap: Int? = null,

    @SerializedName("terlambat")
    val terlambat: Int? = null,

    @SerializedName("pulang_cepat")
    val pulangCepat: Int? = null,

    @SerializedName("pending_verifikasi")
    val pendingVerifikasi: Int? = null,

    @SerializedName("potongan_disiplin_persen")
    val potonganDisiplinPersen: Double? = null
)
