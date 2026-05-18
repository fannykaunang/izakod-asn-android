package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaHistoryItem
import com.kominfo_mkq.izakod_asn.data.model.RealisasiLinkedLaporanItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaDetailItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaHistoryItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaDetailPayload
import com.kominfo_mkq.izakod_asn.data.repository.TargetKinerjaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TargetKinerjaListUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val currentPegawaiId: Int? = null,
    val canReviewSubordinates: Boolean = false,
    val targets: List<TargetKinerjaItem> = emptyList()
)

class TargetKinerjaListViewModel : ViewModel() {
    private val repository = TargetKinerjaRepository()

    private val _uiState = MutableStateFlow(TargetKinerjaListUiState(isLoading = true))
    val uiState: StateFlow<TargetKinerjaListUiState> = _uiState.asStateFlow()

    fun refresh(
        tahun: Int? = null,
        bulan: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isError = false,
                errorMessage = null
            )

            val response = repository.getTargetKinerjaList(
                tahun = tahun,
                bulan = bulan
            )
            if (response.success && response.data != null) {
                _uiState.value = TargetKinerjaListUiState(
                    isLoading = false,
                    currentPegawaiId = response.data.meta?.currentPegawaiId,
                    canReviewSubordinates = response.data.meta?.canReviewSubordinates == true,
                    targets = response.data.data
                )
            } else {
                _uiState.value = TargetKinerjaListUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = response.error ?: "Gagal memuat target kinerja"
                )
            }
        }
    }
}

data class TargetKinerjaDetailUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val target: TargetKinerjaItem? = null,
    val realisasiItems: List<RealisasiKinerjaItem> = emptyList(),
    val linkedLaporanByDetailId: Map<Int, List<RealisasiLinkedLaporanItem>> = emptyMap(),
    val targetHistory: List<TargetKinerjaHistoryItem> = emptyList(),
    val realisasiHistoryById: Map<Int, List<RealisasiKinerjaHistoryItem>> = emptyMap(),
    val candidateLaporan: List<LaporanKegiatan> = emptyList(),
    val isRefreshingRealisasi: Boolean = false,
    val savingRealisasiDetailId: Int? = null,
    val linkingDetailId: Int? = null,
    val unlinkingKey: String? = null
)

class TargetKinerjaDetailViewModel : ViewModel() {
    private val repository = TargetKinerjaRepository()

    private val _uiState = MutableStateFlow(TargetKinerjaDetailUiState(isLoading = true))
    val uiState: StateFlow<TargetKinerjaDetailUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTarget(targetId: Int) {
        viewModelScope.launch {
            _uiState.value = TargetKinerjaDetailUiState(isLoading = true)

            val response = repository.getTargetKinerjaDetail(targetId)
            if (response.success && response.data?.data != null) {
                val target = response.data.data
                val realisasiResponse = repository.getRealisasiKinerjaList(target.id)
                val laporanResponse = repository.getCandidateLaporanForTarget(target)
                val targetHistoryResponse = repository.getTargetKinerjaHistory(target.id)
                val realisasiItems = if (realisasiResponse.success) {
                    realisasiResponse.data?.data.orEmpty()
                } else {
                    emptyList()
                }
                val linkedLaporan = linkedLaporanForItems(realisasiItems)
                val realisasiHistory = realisasiHistoryForItems(realisasiItems)

                _uiState.value = TargetKinerjaDetailUiState(
                    isLoading = false,
                    target = target,
                    realisasiItems = realisasiItems,
                    linkedLaporanByDetailId = linkedLaporan,
                    targetHistory = if (targetHistoryResponse.success) targetHistoryResponse.data.orEmpty() else emptyList(),
                    realisasiHistoryById = realisasiHistory,
                    candidateLaporan = if (laporanResponse.success) laporanResponse.data.orEmpty() else emptyList(),
                    errorMessage = if (!realisasiResponse.success) {
                        realisasiResponse.error
                    } else if (!targetHistoryResponse.success) {
                        targetHistoryResponse.error
                    } else if (!laporanResponse.success) {
                        laporanResponse.error
                    } else {
                        null
                    }
                )
            } else {
                _uiState.value = TargetKinerjaDetailUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = response.error ?: "Gagal memuat detail target"
                )
            }
        }
    }

    private suspend fun linkedLaporanForItems(
        items: List<RealisasiKinerjaItem>
    ): Map<Int, List<RealisasiLinkedLaporanItem>> {
        val result = linkedMapOf<Int, List<RealisasiLinkedLaporanItem>>()
        items.forEach { item ->
            val response = repository.getLinkedLaporan(item.id)
            result[item.targetKinerjaDetailId] = if (response.success) {
                response.data.orEmpty()
            } else {
                emptyList()
            }
        }
        return result
    }

    private suspend fun realisasiHistoryForItems(
        items: List<RealisasiKinerjaItem>
    ): Map<Int, List<RealisasiKinerjaHistoryItem>> {
        val result = linkedMapOf<Int, List<RealisasiKinerjaHistoryItem>>()
        items.forEach { item ->
            val response = repository.getRealisasiKinerjaHistory(item.id)
            result[item.id] = if (response.success) {
                response.data.orEmpty()
            } else {
                emptyList()
            }
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun refreshSupportingData(target: TargetKinerjaItem) {
        _uiState.value = _uiState.value.copy(isRefreshingRealisasi = true, errorMessage = null)

        val realisasiResponse = repository.getRealisasiKinerjaList(target.id)
        val laporanResponse = repository.getCandidateLaporanForTarget(target)
        val targetHistoryResponse = repository.getTargetKinerjaHistory(target.id)
        val realisasiItems = if (realisasiResponse.success) {
            realisasiResponse.data?.data.orEmpty()
        } else {
            emptyList()
        }
        val linkedLaporan = linkedLaporanForItems(realisasiItems)
        val realisasiHistory = realisasiHistoryForItems(realisasiItems)

        _uiState.value = _uiState.value.copy(
            realisasiItems = realisasiItems,
            linkedLaporanByDetailId = linkedLaporan,
            targetHistory = if (targetHistoryResponse.success) targetHistoryResponse.data.orEmpty() else _uiState.value.targetHistory,
            realisasiHistoryById = realisasiHistory,
            candidateLaporan = if (laporanResponse.success) laporanResponse.data.orEmpty() else emptyList(),
            isRefreshingRealisasi = false,
            errorMessage = if (!realisasiResponse.success) {
                realisasiResponse.error
            } else if (!targetHistoryResponse.success) {
                targetHistoryResponse.error
            } else if (!laporanResponse.success) {
                laporanResponse.error
            } else {
                null
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun submitTarget(targetId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val response = repository.submitTargetKinerja(targetId)
            if (response.success) {
                onSuccess()
                loadTarget(targetId)
            } else {
                onError(response.error ?: "Gagal mengajukan target")
            }
        }
    }

    fun deleteTarget(targetId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val response = repository.deleteTargetKinerja(targetId)
            if (response.success) {
                onSuccess()
            } else {
                onError(response.error ?: "Gagal menghapus target")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun reviewTarget(
        targetId: Int,
        aksi: String,
        catatanAtasan: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val response = repository.reviewTargetKinerja(targetId, aksi, catatanAtasan)
            if (response.success) {
                onSuccess()
                loadTarget(targetId)
            } else {
                onError(response.error ?: "Gagal memproses review target")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveRealisasi(
        detailId: Int,
        realisasiKuantitas: Double?,
        realisasiKualitas: Double?,
        realisasiWaktu: Double?,
        catatan: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val target = _uiState.value.target ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(savingRealisasiDetailId = detailId)
            val response = repository.saveRealisasiKinerja(
                detailId = detailId,
                realisasiKuantitas = realisasiKuantitas,
                realisasiKualitas = realisasiKualitas,
                realisasiWaktu = realisasiWaktu,
                catatan = catatan
            )
            _uiState.value = _uiState.value.copy(savingRealisasiDetailId = null)
            if (response.success) {
                refreshSupportingData(target)
                onSuccess(response.data?.message ?: "Realisasi berhasil disimpan")
            } else {
                onError(response.error ?: "Gagal menyimpan realisasi")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun linkLaporan(
        detailId: Int,
        laporanId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val target = _uiState.value.target ?: return
        val realisasi = _uiState.value.realisasiItems.firstOrNull {
            it.targetKinerjaDetailId == detailId
        }

        if (realisasi == null) {
            onError("Simpan realisasi item terlebih dahulu")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(linkingDetailId = detailId)
            val response = repository.linkLaporanToRealisasi(realisasi.id, laporanId)
            _uiState.value = _uiState.value.copy(linkingDetailId = null)
            if (response.success) {
                refreshSupportingData(target)
                onSuccess(response.data ?: "Laporan berhasil ditautkan")
            } else {
                onError(response.error ?: "Gagal menautkan laporan")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun unlinkLaporan(
        detailId: Int,
        laporanId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val target = _uiState.value.target ?: return
        val realisasi = _uiState.value.realisasiItems.firstOrNull {
            it.targetKinerjaDetailId == detailId
        }

        if (realisasi == null) {
            onError("Realisasi item belum tersedia")
            return
        }

        val actionKey = "$detailId-$laporanId"
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(unlinkingKey = actionKey)
            val response = repository.unlinkLaporanFromRealisasi(realisasi.id, laporanId)
            _uiState.value = _uiState.value.copy(unlinkingKey = null)
            if (response.success) {
                refreshSupportingData(target)
                onSuccess(response.data ?: "Tautan laporan berhasil dilepas")
            } else {
                onError(response.error ?: "Gagal melepas tautan laporan")
            }
        }
    }
}

data class TargetKinerjaFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val target: TargetKinerjaItem? = null
)

class TargetKinerjaFormViewModel : ViewModel() {
    private val repository = TargetKinerjaRepository()

    private val _uiState = MutableStateFlow(TargetKinerjaFormUiState())
    val uiState: StateFlow<TargetKinerjaFormUiState> = _uiState.asStateFlow()

    fun loadTarget(targetId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false, errorMessage = null)

            val response = repository.getTargetKinerjaDetail(targetId)
            if (response.success && response.data?.data != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    target = response.data.data
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = response.error ?: "Gagal memuat target"
                )
            }
        }
    }

    fun createTarget(
        tahun: Int,
        bulan: Int,
        catatanPegawai: String,
        details: List<TargetKinerjaDetailPayload>,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val response = repository.createTargetKinerja(
                TargetKinerjaRequest(
                    tahun = tahun,
                    bulan = bulan,
                    catatanPegawai = catatanPegawai.ifBlank { null },
                    details = details
                )
            )

            _uiState.value = _uiState.value.copy(isSaving = false)

            if (response.success) {
                onSuccess(response.data?.data?.id ?: 0)
            } else {
                onError(response.error ?: "Gagal membuat target")
            }
        }
    }

    fun updateTarget(
        targetId: Int,
        tahun: Int,
        bulan: Int,
        catatanPegawai: String,
        details: List<TargetKinerjaDetailPayload>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val response = repository.updateTargetKinerja(
                targetId,
                TargetKinerjaRequest(
                    tahun = tahun,
                    bulan = bulan,
                    catatanPegawai = catatanPegawai.ifBlank { null },
                    details = details
                )
            )

            _uiState.value = _uiState.value.copy(isSaving = false)

            if (response.success) {
                onSuccess()
            } else {
                onError(response.error ?: "Gagal memperbarui target")
            }
        }
    }
}

fun TargetKinerjaItem.isEditableBy(currentPegawaiId: Int?): Boolean {
    return currentPegawaiId != null &&
        pegawaiId == currentPegawaiId &&
        (status == "draft" || status == "revisi")
}

fun TargetKinerjaDetailItem.toPayload(): TargetKinerjaDetailPayload {
    return TargetKinerjaDetailPayload(
        id = id,
        uraianTarget = uraianTarget,
        indikator = indikator,
        satuan = satuan,
        targetKuantitas = targetKuantitas,
        targetKualitas = targetKualitas,
        targetWaktu = targetWaktu,
        bobot = bobot
    )
}
