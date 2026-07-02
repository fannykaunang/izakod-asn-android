package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.PengumumanReadDetail
import com.kominfo_mkq.izakod_asn.data.repository.PengumumanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PengumumanDetailUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val detail: PengumumanReadDetail? = null
)

class PengumumanViewModel : ViewModel() {

    private val repository = PengumumanRepository()

    private val _detailState = MutableStateFlow(PengumumanDetailUiState())
    val detailState: StateFlow<PengumumanDetailUiState> = _detailState.asStateFlow()

    fun loadDetail(id: Int, force: Boolean = false) {
        if (id <= 0) return

        val current = _detailState.value
        if (!force && current.detail?.id == id) return
        if (!force && current.isLoading) return

        viewModelScope.launch {
            _detailState.update {
                it.copy(
                    isLoading = true,
                    isError = false,
                    errorMessage = null
                )
            }

            val response = repository.getReadDetail(id)
            _detailState.value = if (response.success && response.data != null) {
                PengumumanDetailUiState(detail = response.data)
            } else {
                PengumumanDetailUiState(
                    isError = true,
                    errorMessage = response.error ?: "Pengumuman tidak ditemukan"
                )
            }
        }
    }
}
