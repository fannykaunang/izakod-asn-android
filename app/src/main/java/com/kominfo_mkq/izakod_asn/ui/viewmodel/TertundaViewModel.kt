package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.data.repository.LaporanRepository
import com.kominfo_mkq.izakod_asn.data.repository.PenilaianKinerjaRepository
import com.kominfo_mkq.izakod_asn.data.repository.TargetKinerjaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class TertundaUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val tahun: Int = Calendar.getInstance().get(Calendar.YEAR),
    val bulan: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val laporan: List<TertundaLaporanItem> = emptyList(),
    val target: List<TertundaTargetItem> = emptyList(),
    val penilaian: List<TertundaPenilaianItem> = emptyList()
) {
    val total: Int
        get() = laporan.size + target.size + penilaian.size
}

data class TertundaSnapshot(
    val laporan: List<TertundaLaporanItem> = emptyList(),
    val target: List<TertundaTargetItem> = emptyList(),
    val penilaian: List<TertundaPenilaianItem> = emptyList()
) {
    val total: Int
        get() = laporan.size + target.size + penilaian.size
}

data class TertundaLaporanItem(
    val id: Int,
    val title: String,
    val date: String,
    val status: String,
    val category: String?,
    val note: String?
)

data class TertundaTargetItem(
    val id: Int,
    val title: String,
    val periodLabel: String,
    val tahun: Int,
    val bulan: Int,
    val status: String,
    val totalItems: Int,
    val filledItems: Int,
    val missingItems: Int,
    val kind: TertundaTargetKind
)

enum class TertundaTargetKind {
    TARGET_STATUS,
    REALISASI
}

data class TertundaPenilaianItem(
    val id: Int,
    val title: String,
    val periodLabel: String,
    val tahun: Int,
    val bulan: Int,
    val status: String,
    val nilaiAkhir: Double?,
    val predikat: String?
)

class TertundaDataLoader {
    private val laporanRepository = LaporanRepository()
    private val targetRepository = TargetKinerjaRepository()
    private val penilaianRepository = PenilaianKinerjaRepository()

    suspend fun load(context: Context): TertundaSnapshot = coroutineScope {
        val laporanDeferred = async { loadLaporanTertunda(context) }
        val targetDeferred = async { loadTargetTertunda() }
        val penilaianDeferred = async { loadPenilaianTertunda() }

        TertundaSnapshot(
            laporan = laporanDeferred.await(),
            target = targetDeferred.await(),
            penilaian = penilaianDeferred.await()
        )
    }

    private suspend fun loadLaporanTertunda(context: Context): List<TertundaLaporanItem> {
        val response = laporanRepository.getLaporanList(context)
        if (!response.isSuccessful || response.body()?.success != true) {
            throw Exception(response.body()?.meta?.toString() ?: "Gagal memuat laporan")
        }

        return response.body()
            ?.data
            .orEmpty()
            .filter { it.isActionableReport() }
            .sortedWith(compareByDescending<LaporanKegiatan> { it.tanggalKegiatan }.thenByDescending { it.laporanId })
            .map { laporan ->
                TertundaLaporanItem(
                    id = laporan.laporanId,
                    title = laporan.namaKegiatan,
                    date = laporan.tanggalKegiatan,
                    status = laporan.statusLaporan,
                    category = laporan.kategoriNama,
                    note = laporan.catatanVerifikator
                )
            }
    }

    private suspend fun loadTargetTertunda(): List<TertundaTargetItem> {
        val response = targetRepository.getTargetKinerjaList()
        if (!response.success || response.data == null) {
            throw Exception(response.error ?: "Gagal memuat target")
        }

        val currentPegawaiId = response.data.meta?.currentPegawaiId
        val myTargets = response.data.data.orEmpty()
            .filter { target -> currentPegawaiId == null || target.pegawaiId == currentPegawaiId }

        return coroutineScope {
            myTargets.map { target ->
                async { target.toTertundaTargetOrNull() }
            }.awaitAll().filterNotNull()
                .sortedWith(
                    compareByDescending<TertundaTargetItem> {
                        it.periodSortKey
                    }.thenBy { it.kind.ordinal }
                )
        }
    }

    private suspend fun TargetKinerjaItem.toTertundaTargetOrNull(): TertundaTargetItem? {
        val statusKey = status.lowercase()
        val periodLabel = formatPeriodLabel(tahun, bulan)
        val detailItems = details.orEmpty()

        if (statusKey == "draft" || statusKey == "revisi") {
            return TertundaTargetItem(
                id = id,
                title = catatanPegawai?.takeIf { it.isNotBlank() } ?: "Target $periodLabel",
                periodLabel = periodLabel,
                tahun = tahun,
                bulan = bulan,
                status = status,
                totalItems = detailItems.size,
                filledItems = 0,
                missingItems = detailItems.size,
                kind = TertundaTargetKind.TARGET_STATUS
            )
        }

        if (statusKey != "disetujui") {
            return null
        }

        val totalItems = detailItems.size
        if (totalItems <= 0) return null

        val realisasiResponse = targetRepository.getRealisasiKinerjaList(id)
        val realisasiItems = if (realisasiResponse.success) {
            realisasiResponse.data?.data.orEmpty()
        } else {
            emptyList()
        }
        val filledDetailIds = realisasiItems
            .filter { it.hasFilledRealisasi() }
            .map { it.targetKinerjaDetailId }
            .toSet()
        val filledItems = detailItems.count { detail ->
            detail.id != null && filledDetailIds.contains(detail.id)
        }
        val missingItems = (totalItems - filledItems).coerceAtLeast(0)

        return if (missingItems > 0) {
            TertundaTargetItem(
                id = id,
                title = catatanPegawai?.takeIf { it.isNotBlank() } ?: "Realisasi target $periodLabel",
                periodLabel = periodLabel,
                tahun = tahun,
                bulan = bulan,
                status = status,
                totalItems = totalItems,
                filledItems = filledItems,
                missingItems = missingItems,
                kind = TertundaTargetKind.REALISASI
            )
        } else {
            null
        }
    }

    private suspend fun loadPenilaianTertunda(): List<TertundaPenilaianItem> {
        val response = penilaianRepository.getPenilaianKinerjaList()
        if (!response.success || response.data == null) {
            throw Exception(response.error ?: "Gagal memuat penilaian")
        }

        val currentPegawaiId = response.data.meta?.currentPegawaiId
        return response.data.data
            .filter { assessment ->
                (currentPegawaiId == null || assessment.pegawaiId == currentPegawaiId) &&
                    !assessment.statusFinalisasi.equals("final", ignoreCase = true)
            }
            .map { assessment ->
                TertundaPenilaianItem(
                    id = assessment.id,
                    title = assessment.pegawaiNama?.takeIf { it.isNotBlank() }
                        ?: "Penilaian ${formatPeriodLabel(assessment.tahun, assessment.bulan)}",
                    periodLabel = formatPeriodLabel(assessment.tahun, assessment.bulan),
                    tahun = assessment.tahun,
                    bulan = assessment.bulan,
                    status = assessment.statusFinalisasi,
                    nilaiAkhir = assessment.nilaiAkhir,
                    predikat = assessment.predikat
                )
            }
            .sortedByDescending { it.periodSortKey }
    }
}

class TertundaViewModel : ViewModel() {
    private val loader = TertundaDataLoader()

    private val calendar = Calendar.getInstance()
    private val activeYear = calendar.get(Calendar.YEAR)
    private val activeMonth = calendar.get(Calendar.MONTH) + 1

    private val _uiState = MutableStateFlow(
        TertundaUiState(
            isLoading = true,
            tahun = activeYear,
            bulan = activeMonth
        )
    )
    val uiState: StateFlow<TertundaUiState> = _uiState.asStateFlow()

    fun refresh(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isError = false,
                errorMessage = null
            )

            try {
                val snapshot = loader.load(context)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    laporan = snapshot.laporan,
                    target = snapshot.target,
                    penilaian = snapshot.penilaian,
                    tahun = activeYear,
                    bulan = activeMonth
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Gagal memuat data tertunda"
                )
            }
        }
    }
}

private val TertundaTargetItem.periodSortKey: Int
    get() = (tahun * 100) + bulan

private val TertundaPenilaianItem.periodSortKey: Int
    get() = (tahun * 100) + bulan

private fun LaporanKegiatan.isActionableReport(): Boolean {
    return when (statusLaporan.lowercase().trim()) {
        "draft",
        "revisi",
        "perlu revisi",
        "revised",
        "revision",
        "ditolak",
        "rejected" -> true
        else -> false
    }
}

private fun RealisasiKinerjaItem.hasFilledRealisasi(): Boolean {
    return listOfNotNull(
        realisasiKuantitas,
        realisasiKualitas,
        realisasiWaktu,
        catatan?.takeIf { it.isNotBlank() }
    ).isNotEmpty()
}

private fun formatPeriodLabel(tahun: Int, bulan: Int): String {
    val months = listOf(
        "Januari",
        "Februari",
        "Maret",
        "April",
        "Mei",
        "Juni",
        "Juli",
        "Agustus",
        "September",
        "Oktober",
        "November",
        "Desember"
    )
    val monthLabel = months.getOrNull(bulan - 1) ?: "Bulan $bulan"
    return "$monthLabel $tahun"
}
