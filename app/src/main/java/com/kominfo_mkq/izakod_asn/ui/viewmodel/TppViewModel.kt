package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.TppMeData
import com.kominfo_mkq.izakod_asn.data.repository.TppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class TppSayaUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val tahun: Int = Calendar.getInstance().get(Calendar.YEAR),
    val bulan: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val data: TppMeData? = null
)

class TppSayaViewModel : ViewModel() {

    private val repository = TppRepository()

    private val _uiState = MutableStateFlow(TppSayaUiState(isLoading = true))
    val uiState: StateFlow<TppSayaUiState> = _uiState.asStateFlow()

    fun refresh() {
        loadTppSaya(
            tahun = _uiState.value.tahun,
            bulan = _uiState.value.bulan,
            refreshOnly = _uiState.value.data != null
        )
    }

    fun setPeriod(tahun: Int, bulan: Int) {
        if (tahun == _uiState.value.tahun && bulan == _uiState.value.bulan && _uiState.value.data != null) {
            return
        }

        _uiState.value = _uiState.value.copy(
            tahun = tahun,
            bulan = bulan
        )
        loadTppSaya(tahun, bulan)
    }

    fun moveMonth(offset: Int) {
        val current = _uiState.value
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, current.tahun)
            set(Calendar.MONTH, current.bulan - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, offset)
        }

        setPeriod(
            tahun = calendar.get(Calendar.YEAR),
            bulan = calendar.get(Calendar.MONTH) + 1
        )
    }

    private fun loadTppSaya(
        tahun: Int,
        bulan: Int,
        refreshOnly: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !refreshOnly,
                isRefreshing = refreshOnly,
                isError = false,
                errorMessage = null
            )

            val response = repository.getTppSaya(
                tahun = tahun,
                bulan = bulan
            )

            if (_uiState.value.tahun != tahun || _uiState.value.bulan != bulan) {
                return@launch
            }

            if (response.success && response.data?.data != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isError = false,
                    errorMessage = null,
                    data = response.data.data
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isError = true,
                    errorMessage = response.error ?: "Gagal memuat TPP Saya"
                )
            }
        }
    }
}
