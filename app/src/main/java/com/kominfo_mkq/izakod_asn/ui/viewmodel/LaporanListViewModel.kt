package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
import com.kominfo_mkq.izakod_asn.data.repository.LaporanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LaporanListUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val laporanList: List<LaporanKegiatan> = emptyList()
)

class LaporanListViewModel : ViewModel() {

    private val repository = LaporanRepository()

    private val _uiState = MutableStateFlow(LaporanListUiState())
    val uiState: StateFlow<LaporanListUiState> = _uiState.asStateFlow()

    /**
     * Load laporan list from API
     */
    fun loadLaporan() {
        viewModelScope.launch {
            try {
                android.util.Log.d("LaporanListViewModel", "📋 Loading laporan list...")

                _uiState.value = LaporanListUiState(isLoading = true)

                val response = repository.getLaporanList()

                android.util.Log.d("LaporanListViewModel", "📡 Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        android.util.Log.d("LaporanListViewModel", "✅ Loaded ${body.data.size} laporan")

                        _uiState.value = LaporanListUiState(
                            isLoading = false,
                            laporanList = body.data
                        )
                    } else {
                        android.util.Log.e("LaporanListViewModel", "❌ API returned success=false")

                        _uiState.value = LaporanListUiState(
                            isLoading = false,
                            isError = true,
                            errorMessage = "Gagal memuat data laporan"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("LaporanListViewModel", "❌ Error: $errorBody")

                    _uiState.value = LaporanListUiState(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("LaporanListViewModel", "❌ Exception: ${e.message}", e)
                e.printStackTrace()

                _uiState.value = LaporanListUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }
}