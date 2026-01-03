package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatan
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateRequest
import com.kominfo_mkq.izakod_asn.data.repository.TemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TemplateKegiatanUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val templates: List<TemplateKegiatan> = emptyList(),
    val isMutating: Boolean = false, // ✅ create/edit/delete loading
    val actionMessage: String? = null
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
                android.util.Log.d("TemplateVM", "HTTP: ${response.code()}")

                android.util.Log.d("TemplateKegiatanViewModel", "📡 Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    android.util.Log.d("TemplateVM", "BODY: $body")

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
                    android.util.Log.e("TemplateVM", "ERROR_BODY: $errorBody")

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

    private fun setActionMessage(msg: String?) {
        _uiState.value = _uiState.value.copy(actionMessage = msg)
    }

    fun consumeActionMessage() {
        setActionMessage(null)
    }

    fun createTemplate(request: TemplateKegiatanCreateRequest) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isMutating = true, actionMessage = null)

                val res = repository.createTemplate(request)
                val body = res.body()

                if (res.isSuccessful && body != null && body.success) {
                    _uiState.value = _uiState.value.copy(isMutating = false, actionMessage = body.message)
                    loadTemplates()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isMutating = false,
                        actionMessage = body?.message ?: "Gagal menambah template (HTTP ${res.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isMutating = false, actionMessage = e.message ?: "Error")
            }
        }
    }

    fun updateTemplate(templateId: Int, request: TemplateKegiatanCreateRequest) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isMutating = true, actionMessage = null)

                val res = repository.updateTemplate(templateId, request)
                val body = res.body()

                if (res.isSuccessful && body != null && body.success) {
                    _uiState.value = _uiState.value.copy(isMutating = false, actionMessage = body.message)
                    loadTemplates()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isMutating = false,
                        actionMessage = body?.message ?: "Gagal mengubah template (HTTP ${res.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isMutating = false, actionMessage = e.message ?: "Error")
            }
        }
    }

    fun deleteTemplate(templateId: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isMutating = true, actionMessage = null)

                val res = repository.deleteTemplate(templateId)
                val body = res.body()

                if (res.isSuccessful && body != null && body.success) {
                    _uiState.value = _uiState.value.copy(isMutating = false, actionMessage = body.message)
                    loadTemplates()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isMutating = false,
                        actionMessage = body?.message ?: "Gagal menghapus template (HTTP ${res.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isMutating = false, actionMessage = e.message ?: "Error")
            }
        }
    }


}