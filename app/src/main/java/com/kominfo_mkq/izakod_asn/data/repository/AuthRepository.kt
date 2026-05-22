package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.AuthenticatedSession
import com.kominfo_mkq.izakod_asn.data.model.EntagoLoginRequest
import com.kominfo_mkq.izakod_asn.data.model.MobileTokenRequest
import com.kominfo_mkq.izakod_asn.data.model.MobileTokenResponse
import com.kominfo_mkq.izakod_asn.data.model.RefreshTokenRequest
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class AuthRepository {

    private val apiService = ApiClient.eabsenApiService

    suspend fun login(
        email: String,
        password: String
    ): ApiResponse<AuthenticatedSession> = withContext(Dispatchers.IO) {
        try {
            val request = EntagoLoginRequest(email = email, password = password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val body = response.body()
                val loginData = body?.data
                val user = loginData?.user
                val token = loginData?.token?.trim().orEmpty()
                val refreshToken = loginData?.refreshToken?.trim()

                if (body?.success == true && user != null && token.isNotEmpty()) {
                    ApiResponse(
                        success = true,
                        data = AuthenticatedSession(
                            token = token,
                            refreshToken = refreshToken?.takeIf { it.isNotEmpty() },
                            user = user
                        )
                    )
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Login gagal"
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val json = JSONObject(errorBody ?: "{}")
                    json.optString("message").ifBlank {
                        json.optString("response")
                    }
                } catch (_: Exception) {
                    ""
                }

                ApiResponse(
                    success = false,
                    error = errorMessage.ifBlank {
                        "Login gagal: ${response.code()} ${response.message()}"
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(
                success = false,
                error = e.message ?: "Network error"
            )
        }
    }

    suspend fun fetchNextJsMobileToken(
        pegawaiId: Int,
        pin: String
    ): Response<MobileTokenResponse> {
        return ApiClient.eabsenApiService.getMobileToken(
            MobileTokenRequest(pegawai_id = pegawaiId, pin = pin)
        )
    }

    suspend fun refreshNextJsMobileToken(
        refreshToken: String
    ): Response<MobileTokenResponse> {
        return ApiClient.eabsenApiService.refreshMobileToken(
            RefreshTokenRequest(refreshToken = refreshToken)
        )
    }
}
