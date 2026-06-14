package com.kominfo_mkq.izakod_asn.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.kominfo_mkq.izakod_asn.data.local.AppContextHolder
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeData
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeResponse
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnPegawaiInfo
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnPerhitungan
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnPeriodeInfo
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnStatus
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import com.kominfo_mkq.izakod_asn.data.model.TppMeData
import com.kominfo_mkq.izakod_asn.data.model.TppMeResponse
import com.kominfo_mkq.izakod_asn.data.model.TppMeStatus
import com.kominfo_mkq.izakod_asn.data.model.TppNominal
import com.kominfo_mkq.izakod_asn.data.model.TppPegawaiInfo
import com.kominfo_mkq.izakod_asn.data.model.TppPeriodeInfo

class SsoPayrollEstimateCacheRepository {

    private val gson = Gson()
    private val maxAgeMillis = 2 * 60 * 60 * 1000L

    fun getGajiEstimate(tahun: Int, bulan: Int): GajiNonAsnPerhitungan? {
        val cache = readCache()
        if (cache == null) {
            Log.d(TAG, "Gaji cache miss: no cached SSO payload for $tahun-$bulan")
            return null
        }

        val matches = cache.matches(
            estimateKinds = setOf("gaji_non_asn"),
            targetKinds = setOf("gaji", "gaji_non_asn"),
            tahun = tahun,
            bulan = bulan
        )
        Log.d(
            TAG,
            "Gaji cache match check: requested=$tahun-$bulan, matches=$matches, " +
                "estimateKind=${cache.estimate.stringOrNull("jenis_estimasi")}, " +
                "targetKind=${cache.target?.stringOrNull("jenis")}, " +
                "estimatePeriod=${cache.estimate.periodDebug()}, targetPeriod=${cache.target?.periodDebug()}, " +
                "available=${cache.estimate.booleanOrNull("available")}"
        )
        if (!matches) return null

        return cache.parseGajiEstimate()
    }

    fun getTppEstimate(tahun: Int, bulan: Int): TppNominal? {
        val cache = readCache()
        if (cache == null) {
            Log.d(TAG, "TPP cache miss: no cached SSO payload for $tahun-$bulan")
            return null
        }

        val matches = cache.matches(
            estimateKinds = setOf("tpp_asn"),
            targetKinds = setOf("tpp", "tpp_asn"),
            tahun = tahun,
            bulan = bulan
        )
        Log.d(
            TAG,
            "TPP cache match check: requested=$tahun-$bulan, matches=$matches, " +
                "estimateKind=${cache.estimate.stringOrNull("jenis_estimasi")}, " +
                "targetKind=${cache.target?.stringOrNull("jenis")}, " +
                "estimatePeriod=${cache.estimate.periodDebug()}, targetPeriod=${cache.target?.periodDebug()}, " +
                "available=${cache.estimate.booleanOrNull("available")}"
        )
        if (!matches) return null

        return cache.parseTppEstimate()
    }

    fun getGajiFallbackResponse(
        tahun: Int,
        bulan: Int,
        message: String = SSO_FALLBACK_MESSAGE
    ): GajiNonAsnMeResponse? {
        val cache = readCache()
        if (cache == null) {
            Log.d(TAG, "Gaji fallback miss: no cached SSO payload for $tahun-$bulan")
            return null
        }

        val matches = cache.matches(
            estimateKinds = setOf("gaji_non_asn"),
            targetKinds = setOf("gaji", "gaji_non_asn"),
            tahun = tahun,
            bulan = bulan
        )
        Log.d(
            TAG,
            "Gaji fallback cache match check: requested=$tahun-$bulan, matches=$matches, " +
                "estimateKind=${cache.estimate.stringOrNull("jenis_estimasi")}, " +
                "targetKind=${cache.target?.stringOrNull("jenis")}, " +
                "estimatePeriod=${cache.estimate.periodDebug()}, targetPeriod=${cache.target?.periodDebug()}"
        )
        if (!matches) return null

        val calculation = cache.parseGajiEstimate() ?: return null
        Log.d(
            TAG,
            "Gaji fallback response built from SSO cache: total=${calculation.totalDibayar}, " +
                "status=${calculation.status}"
        )

        return GajiNonAsnMeResponse(
            success = true,
            message = message,
            data = GajiNonAsnMeData(
                periode = GajiNonAsnPeriodeInfo(
                    tahun = tahun,
                    bulan = bulan
                ),
                pegawai = cache.toGajiPegawaiInfo(),
                perhitungan = calculation,
                status = GajiNonAsnStatus(
                    calculationAvailable = true,
                    ready = false,
                    issues = listOf(SSO_FALLBACK_ISSUE),
                    label = SSO_FALLBACK_LABEL
                )
            )
        )
    }

    fun getTppFallbackResponse(
        tahun: Int,
        bulan: Int,
        message: String = SSO_FALLBACK_MESSAGE
    ): TppMeResponse? {
        val cache = readCache()
        if (cache == null) {
            Log.d(TAG, "TPP fallback miss: no cached SSO payload for $tahun-$bulan")
            return null
        }

        val matches = cache.matches(
            estimateKinds = setOf("tpp_asn"),
            targetKinds = setOf("tpp", "tpp_asn"),
            tahun = tahun,
            bulan = bulan
        )
        Log.d(
            TAG,
            "TPP fallback cache match check: requested=$tahun-$bulan, matches=$matches, " +
                "estimateKind=${cache.estimate.stringOrNull("jenis_estimasi")}, " +
                "targetKind=${cache.target?.stringOrNull("jenis")}, " +
                "estimatePeriod=${cache.estimate.periodDebug()}, targetPeriod=${cache.target?.periodDebug()}"
        )
        if (!matches) return null

        val nominal = cache.parseTppEstimate() ?: return null
        Log.d(
            TAG,
            "TPP fallback response built from SSO cache: estimasi=${nominal.estimasiDiterima}, " +
                "totalDibayar=${nominal.totalDibayar}, status=${nominal.status}"
        )

        return TppMeResponse(
            success = true,
            message = message,
            data = TppMeData(
                periode = TppPeriodeInfo(
                    tahun = tahun,
                    bulan = bulan
                ),
                pegawai = cache.toTppPegawaiInfo(),
                nominalTpp = nominal,
                status = TppMeStatus(
                    profileReadinessIssues = listOf(SSO_FALLBACK_ISSUE),
                    perhitunganAvailable = true,
                    siapDihitung = false,
                    dataSource = "sso_payroll_estimate_cache",
                    label = SSO_FALLBACK_LABEL
                )
            )
        )
    }

    private fun readCache(): CachedEstimate? {
        val context = AppContextHolder.get() ?: return null
        val raw = UserPreferences(context).getSsoPayrollEstimateJson(maxAgeMillis) ?: return null
        Log.d(TAG, "Read SSO cache raw payload: length=${raw.length}")

        return runCatching {
            val root = JsonParser.parseString(raw).takeIf { it.isJsonObject }?.asJsonObject
                ?: return@runCatching null
            val rawEstimate = root.objectOrNull("payroll_estimate")
                ?: root.objectOrNull("estimate")
                ?: root
            val dataEstimate = rawEstimate.objectOrNull("data")
            val isUnwrappedData = dataEstimate?.looksLikePayrollEstimate() == true
            val estimate = dataEstimate
                ?.takeIf { isUnwrappedData }
                ?: rawEstimate
            Log.d(
                TAG,
                "SSO cache shape: hasPayrollEstimate=${root.objectOrNull("payroll_estimate") != null}, " +
                    "hasEstimate=${root.objectOrNull("estimate") != null}, hasRootTarget=${root.objectOrNull("target") != null}, " +
                    "rawHasData=${dataEstimate != null}, unwrappedData=$isUnwrappedData"
            )

            CachedEstimate(
                estimate = estimate,
                target = root.objectOrNull("target")
            )
        }.onFailure { error ->
            Log.w(TAG, "Read SSO cache failed: ${error.message}")
        }.getOrNull()
    }

    private fun CachedEstimate.matches(
        estimateKinds: Set<String>,
        targetKinds: Set<String>,
        tahun: Int,
        bulan: Int
    ): Boolean {
        val actualKind = estimate.stringOrNull("jenis_estimasi")?.lowercase()
        if (actualKind != null && actualKind !in estimateKinds) return false

        val actualTarget = target?.stringOrNull("jenis")?.lowercase()
        if (actualTarget != null && actualTarget !in targetKinds) return false

        if (estimate.booleanOrNull("available") == false) return false

        val detail = estimate.objectOrNull("detail")
        val periode = estimate.objectOrNull("periode") ?: detail?.objectOrNull("periode")
        val periodYear = periode?.intOrNull("tahun") ?: target?.intOrNull("tahun")
        val periodMonth = periode?.intOrNull("bulan") ?: target?.intOrNull("bulan")

        return periodYear == tahun && periodMonth == bulan
    }

    private fun JsonObject.findNestedObject(name: String): JsonObject? {
        return objectOrNull(name)
            ?: objectOrNull("detail")?.objectOrNull(name)
            ?: objectOrNull("data")?.objectOrNull(name)
            ?: objectOrNull("detail")?.objectOrNull("data")?.objectOrNull(name)
    }

    private fun CachedEstimate.parseGajiEstimate(): GajiNonAsnPerhitungan? {
        val nominal = estimate.findNestedObject("nominal_gaji")
        Log.d(
            TAG,
            "Gaji cache payload: hasNominalGaji=${nominal != null}, " +
                "hasDetail=${estimate.objectOrNull("detail") != null}, " +
                "totalEstimasi=${estimate.doubleOrNull("total_estimasi")}"
        )
        if (nominal == null) {
            val minimal = estimate.toMinimalGajiEstimate()
            Log.d(
                TAG,
                "Gaji cache minimal parsed: success=${minimal != null}, " +
                    "totalDibayar=${minimal?.totalDibayar}, status=${minimal?.status}"
            )
            return minimal
        }

        return runCatching {
            gson.fromJson(nominal, GajiNonAsnPerhitungan::class.java)
                ?.withEstimateFallback(estimate)
        }.onSuccess { parsed ->
            Log.d(
                TAG,
                "Gaji cache nominal parsed: success=${parsed != null}, " +
                    "totalDibayar=${parsed?.totalDibayar}, status=${parsed?.status}"
            )
        }.onFailure { error ->
            Log.w(TAG, "Gaji cache nominal parse failed: ${error.message}")
        }.getOrNull()
    }

    private fun CachedEstimate.parseTppEstimate(): TppNominal? {
        val nominal = estimate.findNestedObject("nominal_tpp")
        Log.d(
            TAG,
            "TPP cache payload: hasNominalTpp=${nominal != null}, " +
                "hasDetail=${estimate.objectOrNull("detail") != null}, " +
                "totalEstimasi=${estimate.doubleOrNull("total_estimasi")}"
        )
        if (nominal == null) {
            val minimal = estimate.toMinimalTppEstimate()
            Log.d(
                TAG,
                "TPP cache minimal parsed: success=${minimal != null}, " +
                    "estimasiDiterima=${minimal?.estimasiDiterima}, totalDibayar=${minimal?.totalDibayar}, " +
                    "status=${minimal?.status}"
            )
            return minimal
        }

        return runCatching {
            gson.fromJson(nominal, TppNominal::class.java)
                ?.withEstimateFallback(estimate)
        }.onSuccess { parsed ->
            Log.d(
                TAG,
                "TPP cache nominal parsed: success=${parsed != null}, " +
                    "estimasiDiterima=${parsed?.estimasiDiterima}, totalDibayar=${parsed?.totalDibayar}, " +
                    "status=${parsed?.status}"
            )
        }.onFailure { error ->
            Log.w(TAG, "TPP cache nominal parse failed: ${error.message}")
        }.getOrNull()
    }

    private fun CachedEstimate.toGajiPegawaiInfo(): GajiNonAsnPegawaiInfo {
        val pegawai = estimate.findPayrollPegawai()
        val profile = cachedProfile()
        return GajiNonAsnPegawaiInfo(
            pegawaiId = pegawai?.intOrNull("pegawai_id")
                ?: pegawai?.intOrNull("id")
                ?: profile?.pegawaiId
                ?: 0,
            pegawaiPin = pegawai?.stringOrNull("pegawai_pin")
                ?: pegawai?.stringOrNull("pin")
                ?: profile?.pegawaiPin,
            pegawaiNip = pegawai?.stringOrNull("pegawai_nip")
                ?: pegawai?.stringOrNull("nip")
                ?: pegawai?.stringOrNull("nip_nik")
                ?: profile?.pegawaiNip,
            pegawaiNama = pegawai?.stringOrNull("pegawai_nama")
                ?: pegawai?.stringOrNull("nama")
                ?: profile?.pegawaiNama,
            jabatan = pegawai?.stringOrNull("jabatan") ?: profile?.jabatan,
            jenisNonAsn = estimate.stringOrNull("jenis_non_asn")
                ?: pegawai?.stringOrNull("jenis_non_asn"),
            skpdid = pegawai?.intOrNull("skpdid") ?: pegawai?.intOrNull("skpd_id"),
            skpd = pegawai?.stringOrNull("skpd") ?: profile?.skpd,
            photoPath = pegawai?.stringOrNull("photo_path")
                ?: pegawai?.stringOrNull("foto")
                ?: profile?.photoPath,
            lastSync = estimate.stringOrNull("calculated_at")
                ?: estimate.objectOrNull("detail")?.stringOrNull("calculated_at")
        )
    }

    private fun CachedEstimate.toTppPegawaiInfo(): TppPegawaiInfo {
        val pegawai = estimate.findPayrollPegawai()
        val profile = cachedProfile()
        return TppPegawaiInfo(
            pegawaiId = pegawai?.intOrNull("pegawai_id")
                ?: pegawai?.intOrNull("id")
                ?: profile?.pegawaiId
                ?: 0,
            pegawaiPin = pegawai?.stringOrNull("pegawai_pin")
                ?: pegawai?.stringOrNull("pin")
                ?: profile?.pegawaiPin,
            pegawaiNip = pegawai?.stringOrNull("pegawai_nip")
                ?: pegawai?.stringOrNull("nip")
                ?: pegawai?.stringOrNull("nip_nik")
                ?: profile?.pegawaiNip,
            pegawaiNama = pegawai?.stringOrNull("pegawai_nama")
                ?: pegawai?.stringOrNull("nama")
                ?: profile?.pegawaiNama,
            jabatan = pegawai?.stringOrNull("jabatan") ?: profile?.jabatan,
            skpdid = pegawai?.intOrNull("skpdid") ?: pegawai?.intOrNull("skpd_id"),
            skpd = pegawai?.stringOrNull("skpd") ?: profile?.skpd,
            photoPath = pegawai?.stringOrNull("photo_path")
                ?: pegawai?.stringOrNull("foto")
                ?: profile?.photoPath,
            lastSync = estimate.stringOrNull("calculated_at")
                ?: estimate.objectOrNull("detail")?.stringOrNull("calculated_at")
        )
    }

    private fun cachedProfile(): PegawaiProfile? {
        val context = AppContextHolder.get() ?: return null
        return runCatching {
            UserPreferences(context).getCachedPegawaiProfile()
        }.onFailure { error ->
            Log.w(TAG, "Read cached profile for SSO fallback failed: ${error.message}")
        }.getOrNull()
    }

    private fun JsonObject.findPayrollPegawai(): JsonObject? {
        return objectOrNull("pegawai")
            ?: objectOrNull("detail")?.objectOrNull("pegawai")
            ?: objectOrNull("data")?.objectOrNull("pegawai")
            ?: objectOrNull("detail")?.objectOrNull("data")?.objectOrNull("pegawai")
    }

    private fun JsonObject.toMinimalGajiEstimate(): GajiNonAsnPerhitungan? {
        val detail = objectOrNull("detail")
        val nominal = doubleOrNull("total_estimasi")
            ?: detail?.doubleOrNull("total_estimasi")
            ?: detail?.doubleOrNull("total_dibayar")
            ?: return null

        return GajiNonAsnPerhitungan(
            totalDibayar = nominal,
            totalPotongan = detail?.doubleOrNull("total_potongan"),
            status = stringOrNull("status") ?: detail?.stringOrNull("status") ?: "estimasi_berjalan",
            calculatedAt = detail?.stringOrNull("calculated_at")
        )
    }

    private fun JsonObject.toMinimalTppEstimate(): TppNominal? {
        val detail = objectOrNull("detail")
        val nominal = doubleOrNull("total_estimasi")
            ?: detail?.doubleOrNull("estimasi_diterima")
            ?: detail?.doubleOrNull("total_dibayar")
            ?: detail?.doubleOrNull("total_netto")
            ?: return null

        return TppNominal(
            totalNetto = nominal,
            totalDibayar = nominal,
            estimasiDiterima = nominal,
            status = stringOrNull("status") ?: detail?.stringOrNull("status") ?: "estimasi_berjalan",
            isFinal = booleanOrNull("is_final") ?: detail?.booleanOrNull("is_final") ?: false,
            label = stringOrNull("label")
        )
    }

    private fun GajiNonAsnPerhitungan.withEstimateFallback(root: JsonObject): GajiNonAsnPerhitungan {
        val detail = root.objectOrNull("detail")
        return copy(
            totalDibayar = totalDibayar
                ?: root.doubleOrNull("total_estimasi")
                ?: detail?.doubleOrNull("total_dibayar")
                ?: detail?.doubleOrNull("total_estimasi"),
            totalPotongan = totalPotongan ?: detail?.doubleOrNull("total_potongan"),
            status = status
                ?: root.stringOrNull("status")
                ?: detail?.stringOrNull("status")
                ?: "estimasi_berjalan",
            calculatedAt = calculatedAt
                ?: root.stringOrNull("calculated_at")
                ?: detail?.stringOrNull("calculated_at")
        )
    }

    private fun TppNominal.withEstimateFallback(root: JsonObject): TppNominal {
        val detail = root.objectOrNull("detail")
        val fallbackNominal = root.doubleOrNull("total_estimasi")
            ?: detail?.doubleOrNull("estimasi_diterima")
            ?: detail?.doubleOrNull("total_dibayar")
            ?: detail?.doubleOrNull("total_netto")

        return copy(
            totalNetto = if (totalNetto != 0.0) totalNetto else fallbackNominal ?: totalNetto,
            totalDibayar = if (totalDibayar != 0.0) totalDibayar else fallbackNominal ?: totalDibayar,
            estimasiDiterima = if (estimasiDiterima != 0.0) estimasiDiterima else fallbackNominal ?: estimasiDiterima,
            status = status
                ?: root.stringOrNull("status")
                ?: detail?.stringOrNull("status")
                ?: "estimasi_berjalan",
            isFinal = isFinal || root.booleanOrNull("is_final") == true || detail?.booleanOrNull("is_final") == true,
            label = label ?: root.stringOrNull("label"),
            calculatedAt = calculatedAt
                ?: root.stringOrNull("calculated_at")
                ?: detail?.stringOrNull("calculated_at")
        )
    }

    private fun JsonObject.looksLikePayrollEstimate(): Boolean {
        return stringOrNull("jenis_estimasi") != null ||
            objectOrNull("detail") != null ||
            objectOrNull("periode") != null ||
            doubleOrNull("total_estimasi") != null ||
            findNestedObject("nominal_gaji") != null ||
            findNestedObject("nominal_tpp") != null
    }

    private fun JsonObject.objectOrNull(name: String): JsonObject? {
        val value = get(name) ?: return null
        return if (value.isJsonObject) value.asJsonObject else null
    }

    private fun JsonObject.stringOrNull(name: String): String? {
        val value = get(name) ?: return null
        if (!value.isJsonPrimitive) return null
        return runCatching { value.asString.trim().takeIf { it.isNotBlank() } }.getOrNull()
    }

    private fun JsonObject.intOrNull(name: String): Int? {
        val value = get(name) ?: return null
        if (!value.isJsonPrimitive) return null
        return runCatching { value.asInt }.getOrNull()
    }

    private fun JsonObject.doubleOrNull(name: String): Double? {
        val value = get(name) ?: return null
        if (!value.isJsonPrimitive) return null
        return runCatching { value.asDouble }.getOrNull()
    }

    private fun JsonObject.booleanOrNull(name: String): Boolean? {
        val value = get(name) ?: return null
        if (!value.isJsonPrimitive) return null
        return runCatching { value.asBoolean }.getOrNull()
    }

    private fun JsonObject.periodDebug(): String {
        val detail = objectOrNull("detail")
        val periode = objectOrNull("periode") ?: detail?.objectOrNull("periode")
        val year = periode?.intOrNull("tahun") ?: intOrNull("tahun")
        val month = periode?.intOrNull("bulan") ?: intOrNull("bulan")
        return "${year ?: "-"}-${month ?: "-"}"
    }

    private data class CachedEstimate(
        val estimate: JsonObject,
        val target: JsonObject?
    )

    private companion object {
        private const val TAG = "IZAKOD_PAYROLL_CACHE"
        private const val SSO_FALLBACK_LABEL = "Estimasi berjalan dari SSO"
        private const val SSO_FALLBACK_MESSAGE =
            "Menampilkan estimasi berjalan dari SSO karena data resmi belum berhasil dimuat."
        private const val SSO_FALLBACK_ISSUE =
            "Data resmi belum berhasil dimuat. Aplikasi menampilkan estimasi berjalan dari SSO E-NTAGO."
    }
}
