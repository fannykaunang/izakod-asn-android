package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.Notifikasi
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val notifications: List<Notifikasi> = emptyList(),
    val unreadCount: Int = 0
)

class NotificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    /**
     * Load notifications from API
     */
    fun loadNotifications() {
        viewModelScope.launch {
            try {
                android.util.Log.d("NotificationViewModel", "📋 Loading notifications...")

                _uiState.value = NotificationUiState(isLoading = true)

                val apiService = ApiClient.eabsenApiService

                val pegawaiId = StatistikRepository.getPegawaiId()
                    ?: throw Exception("Session expired")
                val response = apiService.getNotifications(pegawaiId)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        android.util.Log.d("NotificationViewModel", "✅ Loaded ${body.count} notifications, ${body.unread} unread")

                        _uiState.value = NotificationUiState(
                            isLoading = false,
                            notifications = body.data,
                            unreadCount = body.unread
                        )
                    } else {
                        android.util.Log.e("NotificationViewModel", "❌ API returned success=false")

                        _uiState.value = NotificationUiState(
                            isLoading = false,
                            isError = true,
                            errorMessage = "Gagal memuat notifikasi"
                        )
                    }
                } else {
                    android.util.Log.e("NotificationViewModel", "❌ Response not successful: ${response.code()}")

                    _uiState.value = NotificationUiState(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Gagal memuat notifikasi (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "❌ Error: ${e.message}", e)

                _uiState.value = NotificationUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Get unread count (for badge)
     */
    fun getUnreadCount(): Int {
        return _uiState.value.unreadCount
    }
}