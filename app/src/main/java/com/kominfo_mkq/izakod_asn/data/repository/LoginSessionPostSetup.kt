package com.kominfo_mkq.izakod_asn.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.FcmRegisterRequest
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.fcm.DeviceInfo
import kotlinx.coroutines.tasks.await

object LoginSessionPostSetup {
    suspend fun registerFcmTokenIfPossible(context: Context): Boolean {
        val appContext = context.applicationContext
        val prefs = UserPreferences(appContext)
        val jwt = TokenStore.getToken() ?: prefs.getMobileJwtToken()

        if (jwt.isNullOrBlank()) {
            Log.w(TAG, "FCM register skipped: mobile token is missing")
            return false
        }

        TokenStore.setToken(jwt)

        return try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            prefs.setMobileFcmToken(fcmToken)

            val response = ApiClient.eabsenApiService.registerFcmToken(
                FcmRegisterRequest(
                    fcm_token = fcmToken,
                    device_id = DeviceInfo.androidId(appContext),
                    device_model = DeviceInfo.model(),
                    app_version = DeviceInfo.appVersion(appContext)
                )
            )

            if (!response.isSuccessful) {
                Log.w(TAG, "FCM register failed: ${response.code()} ${response.errorBody()?.string()}")
                false
            } else {
                Log.d(TAG, "FCM register success")
                true
            }
        } catch (error: Exception) {
            Log.w(TAG, "FCM register exception: ${error.message}")
            false
        }
    }

    private const val TAG = "LoginPostSetup"
}
