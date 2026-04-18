package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AuthenticatedSession
import com.kominfo_mkq.izakod_asn.data.model.FcmRegisterRequest
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.repository.AuthRepository
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import com.kominfo_mkq.izakod_asn.fcm.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userData: AuthenticatedSession? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val userPrefs = UserPreferences(application.applicationContext)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun checkLoginStatus(): Boolean {
        val isLoggedIn = userPrefs.isLoggedIn()

        if (!isLoggedIn) return false

        val sessionData = userPrefs.getSessionData()
        sessionData?.let {
            StatistikRepository.setUserData(it.pegawaiId, it.pin)
        }

        val jwt = userPrefs.getMobileJwtToken()
        if (!jwt.isNullOrBlank()) {
            TokenStore.setToken(jwt)
        }
        TokenStore.setRefreshToken(userPrefs.getRefreshToken())

        viewModelScope.launch {
            try {
                val token = TokenStore.getToken() ?: userPrefs.getMobileJwtToken()
                if (token.isNullOrBlank()) return@launch

                val ctx = getApplication<Application>().applicationContext
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                userPrefs.setMobileFcmToken(fcmToken)

                val regResp = ApiClient.eabsenApiService.registerFcmToken(
                    FcmRegisterRequest(
                        fcm_token = fcmToken,
                        device_id = DeviceInfo.androidId(ctx),
                        device_model = DeviceInfo.model(),
                        app_version = DeviceInfo.appVersion(ctx)
                    )
                )

                if (!regResp.isSuccessful) {
                    android.util.Log.w("FCM", "ensure register failed: ${regResp.code()}")
                } else {
                    android.util.Log.d("FCM", "ensure register success")
                }
            } catch (e: Exception) {
                android.util.Log.w("FCM", "ensure register exception: ${e.message}")
            }
        }

        return true
    }

    fun login(email: String, password: String) {
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()

        if (normalizedEmail.isBlank() || normalizedPassword.isBlank()) {
            _uiState.value = LoginUiState(
                errorMessage = "Email dan password tidak boleh kosong"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            try {
                val response = repository.login(normalizedEmail, normalizedPassword)

                if (response.success && response.data != null) {
                    val authSession = response.data
                    val userData = authSession.user
                    val pegawaiId = userData.pegawaiId

                    if (pegawaiId <= 0) {
                        _uiState.value = LoginUiState(
                            errorMessage = "Login berhasil, tetapi data pegawai belum lengkap. Silakan hubungi admin."
                        )
                        return@launch
                    }

                    userPrefs.saveSession(
                        email = userData.email,
                        pin = userData.pin,
                        level = userData.level,
                        skpdid = userData.skpdid,
                        pegawaiId = pegawaiId
                    )
                    userPrefs.setMobileJwtToken(authSession.token)
                    userPrefs.setRefreshToken(authSession.refreshToken)

                    TokenStore.setToken(authSession.token)
                    TokenStore.setRefreshToken(authSession.refreshToken)

                    StatistikRepository.setUserData(
                        pegawaiId = pegawaiId,
                        pin = userData.pin
                    )

                    val ctx = getApplication<Application>().applicationContext
                    val fcmToken = FirebaseMessaging.getInstance().token.await()
                    userPrefs.setMobileFcmToken(fcmToken)

                    val appVersion = DeviceInfo.appVersion(ctx)

                    try {
                        val regResp = ApiClient.eabsenApiService.registerFcmToken(
                            FcmRegisterRequest(
                                fcm_token = fcmToken,
                                device_id = DeviceInfo.androidId(ctx),
                                device_model = DeviceInfo.model(),
                                app_version = appVersion
                            )
                        )

                        if (!regResp.isSuccessful) {
                            android.util.Log.w(
                                "FCM",
                                "registerFcmToken failed: ${regResp.code()} ${regResp.errorBody()?.string()}"
                            )
                        } else {
                            android.util.Log.d("FCM", "registerFcmToken success")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("FCM", "registerFcmToken exception: ${e.message}")
                    }

                    _uiState.value = LoginUiState(
                        isSuccess = true,
                        userData = authSession
                    )
                } else {
                    _uiState.value = LoginUiState(
                        errorMessage = response.error ?: "Login gagal"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
