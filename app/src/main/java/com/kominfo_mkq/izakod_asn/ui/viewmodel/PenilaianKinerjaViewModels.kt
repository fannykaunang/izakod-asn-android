package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaAutoFill
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaItem
import com.kominfo_mkq.izakod_asn.data.repository.PenilaianKinerjaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PenilaianKinerjaListUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val currentPegawaiId: Int? = null,
    val canReviewSubordinates: Boolean = false,
    val assessments: List<PenilaianKinerjaItem> = emptyList()
)

class PenilaianKinerjaListViewModel : ViewModel() {
    private val repository = PenilaianKinerjaRepository()

    private val _uiState = MutableStateFlow(PenilaianKinerjaListUiState(isLoading = true))
    val uiState: StateFlow<PenilaianKinerjaListUiState> = _uiState.asStateFlow()

    fun refresh(
        tahun: Int? = null,
        bulan: Int? = null,
        statusFinalisasi: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isError = false,
                errorMessage = null
            )

            val response = repository.getPenilaianKinerjaList(
                tahun = tahun,
                bulan = bulan,
                statusFinalisasi = statusFinalisasi
            )

            if (response.success && response.data != null) {
                _uiState.value = PenilaianKinerjaListUiState(
                    isLoading = false,
                    currentPegawaiId = response.data.meta?.currentPegawaiId,
                    canReviewSubordinates = response.data.meta?.canReviewSubordinates == true,
                    assessments = response.data.data
                )
            } else {
                _uiState.value = PenilaianKinerjaListUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = response.error ?: "Gagal memuat penilaian kinerja"
                )
            }
        }
    }

    fun createDraft(
        tahun: Int,
        bulan: Int,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val response = repository.createDraftPenilaianKinerja(
                tahun = tahun,
                bulan = bulan
            )

            if (response.success && response.data?.data != null) {
                onSuccess(response.data.data.id)
            } else {
                onError(response.error ?: "Gagal membuat draft penilaian")
            }
        }
    }
}

data class PenilaianKinerjaDetailUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val assessment: PenilaianKinerjaItem? = null,
    val autoFill: PenilaianKinerjaAutoFill? = null,
    val canReview: Boolean = false,
    val isOwner: Boolean = false,
    val isSaving: Boolean = false,
    val isFinalizing: Boolean = false
)

class PenilaianKinerjaDetailViewModel : ViewModel() {
    private val repository = PenilaianKinerjaRepository()

    private val _uiState = MutableStateFlow(PenilaianKinerjaDetailUiState(isLoading = true))
    val uiState: StateFlow<PenilaianKinerjaDetailUiState> = _uiState.asStateFlow()

    fun loadDetail(assessmentId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isError = false,
                errorMessage = null
            )

            val response = repository.getPenilaianKinerjaDetail(assessmentId)
            if (response.success && response.data?.data != null) {
                _uiState.value = PenilaianKinerjaDetailUiState(
                    isLoading = false,
                    assessment = response.data.data,
                    autoFill = response.data.meta?.autoFill,
                    canReview = response.data.meta?.canReview == true,
                    isOwner = response.data.meta?.isOwner == true
                )
            } else {
                _uiState.value = PenilaianKinerjaDetailUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = response.error ?: "Gagal memuat detail penilaian"
                )
            }
        }
    }

    fun saveAssessment(
        assessmentId: Int,
        nilaiTarget: Double?,
        nilaiRealisasi: Double?,
        nilaiAkhir: Double?,
        predikat: String?,
        catatan: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val response = repository.updatePenilaianKinerja(
                assessmentId = assessmentId,
                nilaiTarget = nilaiTarget,
                nilaiRealisasi = nilaiRealisasi,
                nilaiAkhir = nilaiAkhir,
                predikat = predikat,
                catatan = catatan
            )

            _uiState.value = _uiState.value.copy(isSaving = false)

            if (response.success) {
                onSuccess(response.data?.message ?: "Penilaian berhasil disimpan")
                loadDetail(assessmentId)
            } else {
                onError(response.error ?: "Gagal menyimpan penilaian")
            }
        }
    }

    fun finalisasiAssessment(
        assessmentId: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFinalizing = true, errorMessage = null)

            val response = repository.finalisasiPenilaianKinerja(assessmentId)

            _uiState.value = _uiState.value.copy(isFinalizing = false)

            if (response.success) {
                onSuccess(response.data?.message ?: "Penilaian berhasil difinalkan")
                loadDetail(assessmentId)
            } else {
                onError(response.error ?: "Gagal memfinalkan penilaian")
            }
        }
    }
}
