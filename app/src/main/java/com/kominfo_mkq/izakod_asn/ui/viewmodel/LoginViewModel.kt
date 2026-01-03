package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.EabsenLoginResponse
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

        if (!isLoggedIn) return false

        // 1) restore session ke StatistikRepository
        val sessionData = userPrefs.getSessionData()
        sessionData?.let {
            StatistikRepository.setUserData(it.pegawaiId, it.pin)
        }

        // 2) restore JWT ke TokenStore (biar interceptor aktif)
        val jwt = userPrefs.getMobileJwtToken()
        if (!jwt.isNullOrBlank()) {
            TokenStore.setToken(jwt)
        }

        // 3) ✅ ensure register FCM (untuk auto-login / token berubah)
        viewModelScope.launch {
            try {
                val token = TokenStore.getToken() ?: userPrefs.getMobileJwtToken()
                if (token.isNullOrBlank()) return@launch

                val ctx = getApplication<Application>().applicationContext
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                userPrefs.setMobileFcmToken(fcmToken)

                val deviceId = DeviceInfo.androidId(ctx)
                val deviceModel = DeviceInfo.model()

                val regResp = ApiClient.eabsenApiService.registerFcmToken(
                    FcmRegisterRequest(
                        fcm_token = fcmToken,
                        device_id = deviceId,
                        device_model = deviceModel,
                        app_version = BuildConfig.VERSION_NAME
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

                    val pegawaiId = userData.pegawaiId
                    if (pegawaiId == null || pegawaiId <= 0) {
                        // tampilkan error atau paksa fetch pegawaiId dulu
                        _uiState.value = LoginUiState(errorMessage = "pegawai_id tidak ditemukan, tidak bisa buat token mobile")
                        return@launch
                    }

                    val tokenResp = repository.fetchNextJsMobileToken(
                        pegawaiId = pegawaiId,
                        pin = userData.pin
                    )

                    if (tokenResp.isSuccessful) {
                        val token = tokenResp.body()?.data?.token?.trim()

                        if (token.isNullOrBlank()) {
                            // optional: tetap login, tapi fitur push tidak aktif
                        } else {
                            userPrefs.setMobileJwtToken(token)
                            TokenStore.setToken(token)

                            val ctx = getApplication<Application>().applicationContext

                            val fcmToken = FirebaseMessaging.getInstance().token.await()
                            userPrefs.setMobileFcmToken(fcmToken)

// ✅ device_id yang benar = ANDROID_ID
                            val deviceId = DeviceInfo.androidId(ctx)
                            val deviceModel = DeviceInfo.model()

// ✅ register ke Next.js (Authorization otomatis dari authInterceptor)
                            try {
                                val regResp = ApiClient.eabsenApiService.registerFcmToken(
                                    FcmRegisterRequest(
                                        fcm_token = fcmToken,
                                        device_id = deviceId,
                                        device_model = deviceModel,
                                        app_version = BuildConfig.VERSION_NAME
                                    )
                                )

                                if (!regResp.isSuccessful) {
                                    // jangan gagalkan login
                                    android.util.Log.w(
                                        "FCM",
                                        "registerFcmToken failed: ${regResp.code()} ${regResp.errorBody()?.string()}"
                                    )
                                } else {
                                    android.util.Log.d("FCM", "registerFcmToken success")
                                }
                            } catch (e: Exception) {
                                // jangan gagalkan login
                                android.util.Log.w("FCM", "registerFcmToken exception: ${e.message}")
                            }

                        }
                    } else {
                        // optional: tampilkan error / tetap login tapi fitur Next.js gagal
                    }

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
     * Reset error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}