package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.*
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReminderUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val reminders: List<Reminder> = emptyList(),
    val stats: ReminderStats? = null,
    val meta: ReminderMeta? = null,
    val pagination: Pagination? = null,
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false
)

class ReminderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    fun loadReminders(page: Int = 1, tipe: String? = null) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ReminderViewModel", "📋 Loading reminders...")

                _uiState.value = _uiState.value.copy(isLoading = true, isError = false)

                val apiService = ApiClient.eabsenApiService
                val pegawaiId = StatistikRepository.getPegawaiId()
                    ?: throw Exception("Session expired")

                val response = apiService.getReminders(
                    pegawaiId,
                    page = page,
                    limit = 10,
                    tipe = tipe
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        android.util.Log.d("ReminderViewModel", "✅ Loaded ${body.data.size} reminders")

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            reminders = body.data,
                            stats = body.stats,
                            meta = body.meta,
                            pagination = body.pagination
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = "Gagal memuat reminder"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Gagal memuat reminder (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderViewModel", "❌ Error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun createReminder(request: CreateReminderRequest) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ReminderViewModel", "➕ Creating reminder: ${request.judulReminder}")

                _uiState.value = _uiState.value.copy(isCreating = true, createSuccess = false)

                val apiService = ApiClient.eabsenApiService
                val pegawaiId = StatistikRepository.getPegawaiId()
                    ?: throw Exception("Session expired")

                val response = apiService.createReminder(pegawaiId,request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        android.util.Log.d("ReminderViewModel", "✅ Reminder created successfully")

                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            createSuccess = true
                        )

                        // Reload list
                        loadReminders()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            isError = true,
                            errorMessage = "Gagal membuat reminder"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        isError = true,
                        errorMessage = "Gagal membuat reminder (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderViewModel", "❌ Error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun resetCreateSuccess() {
        _uiState.value = _uiState.value.copy(createSuccess = false)
    }

    fun deleteReminder(reminderId: Int) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ReminderViewModel", "🗑️ Deleting reminder: $reminderId")

                _uiState.value = _uiState.value.copy(isDeleting = true, deleteSuccess = false)

                val pegawaiId = StatistikRepository.getPegawaiId()
                    ?: throw Exception("Session expired")

                val apiService = ApiClient.eabsenApiService
                val response = apiService.deleteReminder(reminderId, pegawaiId)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        android.util.Log.d("ReminderViewModel", "✅ Reminder deleted successfully")

                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            deleteSuccess = true
                        )

                        // Reload list
                        loadReminders()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            isError = true,
                            errorMessage = body.message ?: "Gagal menghapus reminder"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        isError = true,
                        errorMessage = "Gagal menghapus reminder (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderViewModel", "❌ Error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun resetDeleteSuccess() {
        _uiState.value = _uiState.value.copy(deleteSuccess = false)
    }
}