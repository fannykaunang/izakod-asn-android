package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.LaporanDetail
import com.kominfo_mkq.izakod_asn.data.repository.LaporanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LaporanDetailUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val laporan: LaporanDetail? = null,
    val canEdit: Boolean = false,
    val canVerify: Boolean = false
)

class LaporanDetailViewModel : ViewModel() {

    private val repository = LaporanRepository()

    private val _uiState = MutableStateFlow(LaporanDetailUiState())
    val uiState: StateFlow<LaporanDetailUiState> = _uiState.asStateFlow()

    /**
     * Load laporan detail from API
     */
    fun loadLaporan(laporanId: Int) {
        viewModelScope.launch {
            try {
                android.util.Log.d("LaporanDetailViewModel", "📋 Loading detail for ID: $laporanId")

                _uiState.value = LaporanDetailUiState(isLoading = true)

                val response = repository.getLaporanDetail(laporanId)

                android.util.Log.d("LaporanDetailViewModel", "📡 Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        android.util.Log.d("LaporanDetailViewModel", "✅ Loaded laporan: ${body.data.namaKegiatan}")

                        _uiState.value = LaporanDetailUiState(
                            isLoading = false,
                            laporan = body.data,
                            canEdit = body.canEdit,
                            canVerify = body.canVerify
                        )
                    } else {
                        android.util.Log.e("LaporanDetailViewModel", "❌ API returned success=false")

                        _uiState.value = LaporanDetailUiState(
                            isLoading = false,
                            isError = true,
                            errorMessage = "Gagal memuat detail laporan"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("LaporanDetailViewModel", "❌ Error: $errorBody")

                    val errorMessage = when (response.code()) {
                        404 -> "Laporan tidak ditemukan"
                        403 -> "Anda tidak memiliki akses ke laporan ini"
                        else -> "Error: ${response.code()}"
                    }

                    _uiState.value = LaporanDetailUiState(
                        isLoading = false,
                        isError = true,
                        errorMessage = errorMessage
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("LaporanDetailViewModel", "❌ Exception: ${e.message}", e)
                e.printStackTrace()

                _uiState.value = LaporanDetailUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }
}