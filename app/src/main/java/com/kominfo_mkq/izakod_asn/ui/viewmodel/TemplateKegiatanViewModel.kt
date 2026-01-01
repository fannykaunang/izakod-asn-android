package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatan
import com.kominfo_mkq.izakod_asn.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TemplateKegiatanUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val templates: List<TemplateKegiatan> = emptyList()
)

class TemplateKegiatanViewModel : ViewModel() {

    private val repository = TemplateRepository()

    private val _uiState = MutableStateFlow(TemplateKegiatanUiState())
    val uiState: StateFlow<TemplateKegiatanUiState> = _uiState.asStateFlow()

    /**
     * Load all templates
     */
    fun loadTemplates() {
        viewModelScope.launch {
            try {
                android.util.Log.d("TemplateKegiatanViewModel", "📋 Loading templates")

                _uiState.value = TemplateKegiatanUiState(isLoading = true)

                val response = repository.getAllTemplates()

                android.util.Log.d("TemplateKegiatanViewModel", "📡 Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        android.util.Log.d("TemplateKegiatanViewModel", "✅ Loaded ${body.data.size} templates")

                        _uiState.value = TemplateKegiatanUiState(
                            isLoading = false,
                            templates = body.data
                        )
                    } else {
                        android.util.Log.e("TemplateKegiatanViewModel", "❌ API returned success=false")

                        _uiState.value = TemplateKegiatanUiState(
                            isLoading = false,
                            isError = true,
                            errorMessage = body.message
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("TemplateKegiatanViewModel", "❌ Error: $errorBody")

                    _uiState.value = TemplateKegiatanUiState(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("TemplateKegiatanViewModel", "❌ Exception: ${e.message}", e)
                e.printStackTrace()

                _uiState.value = TemplateKegiatanUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Load templates by kategori
     */
    fun loadTemplatesByKategori(kategoriId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = TemplateKegiatanUiState(isLoading = true)

                val response = repository.getTemplatesByKategori(kategoriId)

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = TemplateKegiatanUiState(
                        isLoading = false,
                        templates = response.body()!!.data
                    )
                } else {
                    _uiState.value = TemplateKegiatanUiState(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Gagal memuat template"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = TemplateKegiatanUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message
                )
            }
        }
    }
}