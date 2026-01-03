package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import com.kominfo_mkq.izakod_asn.data.remote.EabsenRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val profile: PegawaiProfile? = null,
    val photoUrl: String? = null,

    // ✅ tambahan untuk Settings tab
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun setNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun clearProfile() {
        _uiState.value = ProfileUiState() // reset full
    }

    /**
     * Load pegawai profile from ASP.NET Core Eabsen API
     */
    fun loadProfile(pin: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileViewModel", "📋 Loading profile for PIN: $pin")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    isError = false,
                    errorMessage = null
                    // BIARKAN profile lama tetap ada sementara loading (no flicker)
                )

                val response = EabsenRetrofitClient.apiService.getPegawaiProfile(pin)

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!

                    android.util.Log.d("ProfileViewModel", "✅ Profile loaded: ${profile.pegawaiNama}")
                    android.util.Log.d("ProfileViewModel", "📸 Photo path: ${profile.photoPath}")

                    val photoUrl = if (profile.photoPath != null) {
                        "https://entago.merauke.go.id/${profile.photoPath}"
                    } else null

                    android.util.Log.d("ProfileViewModel", "🔗 Photo URL: $photoUrl")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        profile = profile,
                        photoUrl = photoUrl
                    )
                } else {
                    android.util.Log.e("ProfileViewModel", "❌ Failed to load profile: ${response.code()}")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Gagal memuat profil (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "❌ Error loading profile: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun resetProfile() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isError = false,
            errorMessage = null,
            profile = null,
            photoUrl = null
        )
    }
}
