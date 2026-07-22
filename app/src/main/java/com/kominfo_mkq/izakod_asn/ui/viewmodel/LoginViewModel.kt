package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AuthenticatedSession
import com.kominfo_mkq.izakod_asn.data.model.MobileTokenResponse
import com.kominfo_mkq.izakod_asn.data.repository.AuthRepository
import com.kominfo_mkq.izakod_asn.data.repository.LoginSessionPostSetup
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userData: AuthenticatedSession? = null
)

private data class MobileTokenIssueResult(
    val token: String? = null,
    val errorMessage: String? = null
)

private const val DEFAULT_MOBILE_TOKEN_ERROR_MESSAGE =
    "Login berhasil, tetapi token aplikasi belum dapat dibuat. Silakan coba lagi."

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val userPrefs = UserPreferences(application.applicationContext)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun checkLoginStatus(): Boolean {
        val isLoggedIn = userPrefs.isLoggedIn()

        if (!isLoggedIn) return false

        migrateLegacyTokensIfNeeded()

        val sessionData = userPrefs.getSessionData()
        sessionData?.let {
            StatistikRepository.setUserData(it.pegawaiId, it.pin)
        }

        val jwt = userPrefs.getMobileJwtToken()?.takeIf { isLikelyMobileToken(it) }
        if (jwt == null) {
            userPrefs.setMobileJwtToken(null)
        }
        if (!jwt.isNullOrBlank()) {
            TokenStore.setToken(jwt)
        }
        TokenStore.setRefreshToken(userPrefs.getRefreshToken())

        if (jwt.isNullOrBlank()) {
            sessionData?.let { session ->
                viewModelScope.launch {
                    issueAndPersistMobileToken(
                        pegawaiId = session.pegawaiId ?: return@launch,
                        pin = session.pin
                    )
                }
            }
        }

        viewModelScope.launch {
            LoginSessionPostSetup.registerFcmTokenIfPossible(
                getApplication<Application>().applicationContext
            )
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
                    userPrefs.setEntagoAccessToken(authSession.token)
                    userPrefs.setEntagoRefreshToken(authSession.refreshToken)

                    val mobileTokenResult = issueAndPersistMobileToken(
                        pegawaiId = pegawaiId,
                        pin = userData.pin
                    )

                    if (mobileTokenResult.token.isNullOrBlank()) {
                        userPrefs.clearSession()
                        TokenStore.setToken(null)
                        TokenStore.setRefreshToken(null)
                        _uiState.value = LoginUiState(
                            errorMessage = mobileTokenResult.errorMessage
                                ?: DEFAULT_MOBILE_TOKEN_ERROR_MESSAGE
                        )
                        return@launch
                    }

                    StatistikRepository.setUserData(
                        pegawaiId = pegawaiId,
                        pin = userData.pin
                    )

                    LoginSessionPostSetup.registerFcmTokenIfPossible(
                        getApplication<Application>().applicationContext
                    )

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

    private suspend fun issueAndPersistMobileToken(
        pegawaiId: Int,
        pin: String
    ): MobileTokenIssueResult {
        return try {
            val response = repository.fetchNextJsMobileToken(pegawaiId, pin)
            val body: MobileTokenResponse? = response.body()
            val data = body?.data
            val token = data?.token?.trim()
            val refreshToken = data?.refreshToken?.trim()

            if (!response.isSuccessful || body?.success != true || token.isNullOrBlank()) {
                android.util.Log.e(
                    "LoginViewModel",
                    "Failed to issue mobile token: code=${response.code()} message=${body?.message}"
                )
                MobileTokenIssueResult(
                    errorMessage = readMobileTokenErrorMessage(response, body)
                )
            } else {
                userPrefs.setMobileJwtToken(token)
                userPrefs.setRefreshToken(refreshToken)
                TokenStore.setToken(token)
                TokenStore.setRefreshToken(refreshToken)
                MobileTokenIssueResult(token = token)
            }
        } catch (e: Exception) {
            android.util.Log.e("LoginViewModel", "Failed to issue mobile token: ${e.message}", e)
            MobileTokenIssueResult(errorMessage = DEFAULT_MOBILE_TOKEN_ERROR_MESSAGE)
        }
    }

    private fun readMobileTokenErrorMessage(
        response: retrofit2.Response<MobileTokenResponse>,
        body: MobileTokenResponse?
    ): String {
        val responseMessage = body
            ?.takeIf { !it.success }
            ?.message
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (responseMessage != null) return responseMessage

        val errorMessage = try {
            val rawError = response.errorBody()?.string().orEmpty()
            JSONObject(rawError).optString("message").trim().takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }

        return errorMessage ?: DEFAULT_MOBILE_TOKEN_ERROR_MESSAGE
    }

    private fun isLikelyMobileToken(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return false

            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val payload = JSONObject(String(payloadBytes))
            val authType = payload.optString("authType")
            val pegawaiId = payload.optInt("pegawai_id", 0)

            authType == "mobile" && pegawaiId > 0
        } catch (_: Exception) {
            false
        }
    }

    private fun migrateLegacyTokensIfNeeded() {
        val entagoAccess = userPrefs.getEntagoAccessToken()
        val entagoRefresh = userPrefs.getEntagoRefreshToken()
        val legacyMobileToken = userPrefs.getMobileJwtToken()
        val legacyRefreshToken = userPrefs.getRefreshToken()
        val legacyAccessIsEntagoToken =
            entagoAccess.isNullOrBlank() &&
                !legacyMobileToken.isNullOrBlank() &&
                !isLikelyMobileToken(legacyMobileToken)

        if (legacyAccessIsEntagoToken) {
            userPrefs.setEntagoAccessToken(legacyMobileToken)
            android.util.Log.d("LoginViewModel", "✅ Migrated legacy E-NTAGO access token")
        }

        if (
            legacyAccessIsEntagoToken &&
            entagoRefresh.isNullOrBlank() &&
            !legacyRefreshToken.isNullOrBlank()
        ) {
            userPrefs.setEntagoRefreshToken(legacyRefreshToken)
            android.util.Log.d("LoginViewModel", "✅ Migrated legacy E-NTAGO refresh token")
        } else if (
            !entagoRefresh.isNullOrBlank() &&
            !legacyRefreshToken.isNullOrBlank() &&
            entagoRefresh == legacyRefreshToken &&
            !legacyMobileToken.isNullOrBlank() &&
            isLikelyMobileToken(legacyMobileToken)
        ) {
            userPrefs.setEntagoRefreshToken(null)
            android.util.Log.w("LoginViewModel", "Cleared invalid E-NTAGO refresh token copied from IZAKOD mobile token")
        }
    }
}
