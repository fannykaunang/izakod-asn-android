package com.kominfo_mkq.izakod_asn.data.repository

import android.content.Context
import android.util.Log
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.RefreshTokenRequest
import com.kominfo_mkq.izakod_asn.data.remote.EabsenRetrofitClient

object EntagoTokenRepository {
    suspend fun refreshAccessTokenIfPossible(context: Context): String? {
        val prefs = UserPreferences(context.applicationContext)
        val currentToken = prefs.getEntagoAccessToken()?.trim()?.takeIf { it.isNotBlank() }
        val refreshToken = prefs.getEntagoRefreshToken()?.trim()?.takeIf { it.isNotBlank() }

        if (refreshToken == null) return currentToken

        return try {
            val response = EabsenRetrofitClient.apiService.refreshToken(
                RefreshTokenRequest(refreshToken)
            )
            val body = response.body()
            val nextToken = body?.data?.token?.trim().orEmpty()
            val nextRefreshToken = body?.data?.refreshToken?.trim().orEmpty()

            if (!response.isSuccessful || body?.success != true || nextToken.isBlank()) {
                Log.w(
                    "EntagoTokenRepository",
                    "Refresh token E-NTAGO tidak berhasil: HTTP ${response.code()}"
                )
                return currentToken
            }

            prefs.setEntagoAccessToken(nextToken)
            if (nextRefreshToken.isNotBlank()) {
                prefs.setEntagoRefreshToken(nextRefreshToken)
            }

            nextToken
        } catch (error: Exception) {
            Log.w(
                "EntagoTokenRepository",
                "Gagal refresh token E-NTAGO sebelum submit laporan: ${error.message}"
            )
            currentToken
        }
    }
}
