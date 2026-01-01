package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.LaporanDetail
import com.kominfo_mkq.izakod_asn.data.repository.LaporanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VerifikasiLaporanUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isError: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    val laporan: LaporanDetail? = null,
    val canVerify: Boolean = false
)

class VerifikasiLaporanViewModel : ViewModel() {

    private val repository = LaporanRepository()

    private val _uiState = MutableStateFlow(VerifikasiLaporanUiState())
    val uiState: StateFlow<VerifikasiLaporanUiState> = _uiState.asStateFlow()

    /**
     * Load laporan detail for verification
     */
    fun loadLaporan(laporanId: Int) {
        viewModelScope.launch {
            try {
                android.util.Log.d("VerifikasiViewModel", "📋 Loading laporan: $laporanId")

                _uiState.value = VerifikasiLaporanUiState(isLoading = true)

                val response = repository.getLaporanDetail(laporanId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!

                    android.util.Log.d("VerifikasiViewModel", "✅ Loaded: ${body.data.namaKegiatan}")
                    android.util.Log.d("VerifikasiViewModel", "🔐 canVerify: ${body.canVerify}")

                    _uiState.value = VerifikasiLaporanUiState(
                        isLoading = false,
                        laporan = body.data,
                        canVerify = body.canVerify
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("VerifikasiViewModel", "❌ Error: $errorBody")

                    _uiState.value = VerifikasiLaporanUiState(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Gagal memuat laporan"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("VerifikasiViewModel", "❌ Exception: ${e.message}", e)

                _uiState.value = VerifikasiLaporanUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Verify laporan (Terima atau Tolak)
     */
    fun verifikasiLaporan(
        laporanId: Int,
        status: String,
        rating: Int?,
        catatan: String
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("VerifikasiViewModel", "📝 Verifying laporan: $laporanId")
                android.util.Log.d("VerifikasiViewModel", "📊 Status: $status, Rating: $rating")

                _uiState.value = _uiState.value.copy(isSubmitting = true)

                val response = repository.verifikasiLaporan(
                    laporanId = laporanId,
                    status = status,
                    rating = rating,
                    catatan = catatan
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    android.util.Log.d("VerifikasiViewModel", "✅ Verifikasi berhasil")

                    val successMsg = when (status) {
                        "Diverifikasi" -> "Laporan berhasil diverifikasi"
                        "Revisi" -> "Laporan dikembalikan untuk revisi"
                        "Ditolak" -> "Laporan ditolak"
                        else -> "Laporan berhasil diproses"
                    }

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        isSuccess = true,
                        successMessage = successMsg
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("VerifikasiViewModel", "❌ Error: $errorBody")

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Gagal memverifikasi laporan"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("VerifikasiViewModel", "❌ Exception: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }
}