package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.EabsenLoginResponse
import com.kominfo_mkq.izakod_asn.data.repository.AuthRepository
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State untuk Login Screen
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userData: EabsenLoginResponse? = null
)

/**
 * ViewModel untuk Login Screen
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val userPrefs = UserPreferences(application.applicationContext)

    // UI State
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Check if user already logged in on app start
     */
    fun checkLoginStatus(): Boolean {
        val isLoggedIn = userPrefs.isLoggedIn()

        if (isLoggedIn) {
            // Restore session data
            val sessionData = userPrefs.getSessionData()
            sessionData?.let {
                // Restore to StatistikRepository
                StatistikRepository.setUserData(it.pegawaiId, it.pin)
            }
        }

        return isLoggedIn
    }

    /**
     * Perform login
     */
    fun login(email: String, password: String) {
        // Validate input
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(
                errorMessage = "Email dan password tidak boleh kosong"
            )
            return
        }

        viewModelScope.launch {
            // Set loading state
            _uiState.value = LoginUiState(isLoading = true)

            try {
                // Call repository
                val response = repository.login(email, password)

                if (response.success && response.data != null) {
                    // ✅ Login successful
                    val userData = response.data

                    userPrefs.saveSession(
                        email = userData.email,
                        pin = userData.pin,
                        level = userData.level,
                        skpdid = userData.skpdid,
                        pegawaiId = userData.pegawaiId
                    )

                    // ✅ SAVE pegawai_id dan pin ke StatistikRepository
                    // Call companion object function
                    StatistikRepository.setUserData(
                        pegawaiId = userData.pegawaiId,
                        pin = userData.pin
                    )

                    // Login successful
                    _uiState.value = LoginUiState(
                        isSuccess = true,
                        userData = response.data
                    )
                } else {
                    // Login failed
                    _uiState.value = LoginUiState(
                        errorMessage = response.error ?: "Login gagal"
                    )
                }
            } catch (e: Exception) {
                // Network or other error
                _uiState.value = LoginUiState(
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Logout - clear session
     */
    fun logout() {
        userPrefs.clearSession()
        StatistikRepository.clearData()
        _uiState.value = LoginUiState()
    }

    /**
     * Reset error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Reset state
     */
    fun resetState() {
        _uiState.value = LoginUiState()
    }
}