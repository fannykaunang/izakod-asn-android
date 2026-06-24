package com.kominfo_mkq.izakod_asn.data.repository

import android.content.Context
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.AppUpdateEventRequest
import com.kominfo_mkq.izakod_asn.data.model.AppUpdateEventResult
import com.kominfo_mkq.izakod_asn.data.model.AppVersionPolicy
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.fcm.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class AppVersionRepository(context: Context) {

    private val appContext = context.applicationContext
    private val preferences = UserPreferences(appContext)
    private val apiService = ApiClient.eabsenApiService

    suspend fun getAppVersionPolicy(
        forceRefresh: Boolean = false
    ): ApiResponse<AppVersionPolicy> = withContext(Dispatchers.IO) {
        val cached = preferences.getCachedAppVersionPolicy()
        val cacheMaxAgeMillis = cacheMaxAgeMillis(cached)

        if (!forceRefresh && cached != null && preferences.isAppVersionPolicyCacheFresh(cacheMaxAgeMillis)) {
            return@withContext ApiResponse(success = true, data = cached)
        }

        try {
            val response = apiService.getMobileAppVersion(
                appKey = APP_KEY,
                platform = PLATFORM,
                versionCode = DeviceInfo.appVersionCode(appContext),
                versionName = DeviceInfo.appVersion(appContext)
            )

            if (response.isSuccessful) {
                val body = response.body()
                val policy = body?.data
                if (body?.success == true && policy != null) {
                    preferences.saveAppVersionPolicyCache(policy)
                    ApiResponse(success = true, data = policy)
                } else {
                    cachedFallback(
                        cached = cached,
                        error = body?.message ?: "Policy versi aplikasi belum tersedia"
                    )
                }
            } else {
                cachedFallback(
                    cached = cached,
                    error = response.errorMessage("Gagal memuat policy versi aplikasi")
                )
            }
        } catch (error: Exception) {
            cachedFallback(
                cached = cached,
                error = error.message ?: "Koneksi bermasalah saat memuat policy versi aplikasi"
            )
        }
    }

    fun getCachedAppVersionPolicy(): AppVersionPolicy? =
        preferences.getCachedAppVersionPolicy()

    fun shouldCheckAppVersion(): Boolean {
        val cached = preferences.getCachedAppVersionPolicy() ?: return true
        return !preferences.isAppVersionPolicyCacheFresh(cacheMaxAgeMillis(cached))
    }

    fun clearCache() {
        preferences.clearAppVersionPolicyCache()
    }

    suspend fun recordUpdateEvent(
        eventType: String,
        policy: AppVersionPolicy? = preferences.getCachedAppVersionPolicy(),
        source: String? = null,
        fromVersionCode: Int? = DeviceInfo.appVersionCode(appContext),
        fromVersionName: String? = DeviceInfo.appVersion(appContext),
        toVersionCode: Int? = null,
        toVersionName: String? = null,
        metadata: Map<String, Any?>? = null
    ): ApiResponse<AppUpdateEventResult> = withContext(Dispatchers.IO) {
        try {
            val request = AppUpdateEventRequest(
                appKey = policy?.appKey?.takeIf { it.isNotBlank() } ?: APP_KEY,
                platform = policy?.platform?.takeIf { it.isNotBlank() } ?: PLATFORM,
                packageName = policy?.packageName ?: appContext.packageName,
                policyId = policy?.policyId,
                deviceId = DeviceInfo.androidId(appContext).takeIf { it.isNotBlank() },
                deviceModel = DeviceInfo.model().takeIf { it.isNotBlank() },
                eventType = eventType,
                fromVersionCode = fromVersionCode,
                fromVersionName = fromVersionName,
                targetVersionCode = policy?.latestVersionCode,
                targetVersionName = policy?.latestVersionName,
                toVersionCode = toVersionCode,
                toVersionName = toVersionName,
                updateRequired = policy?.updateRequired == true,
                source = source,
                metadata = metadata
            )

            val response = apiService.createMobileAppUpdateEvent(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    ApiResponse(success = true, data = body.data)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal mencatat event pembaruan aplikasi"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = response.errorMessage("Gagal mencatat event pembaruan aplikasi")
                )
            }
        } catch (error: Exception) {
            ApiResponse(
                success = false,
                error = error.message ?: "Koneksi bermasalah saat mencatat event pembaruan aplikasi"
            )
        }
    }

    private fun cacheMaxAgeMillis(policy: AppVersionPolicy?): Long {
        val seconds = policy?.checkIntervalSeconds
            ?.coerceAtLeast(MIN_CHECK_INTERVAL_SECONDS)
            ?: DEFAULT_CHECK_INTERVAL_SECONDS

        return seconds * 1000L
    }

    private fun cachedFallback(
        cached: AppVersionPolicy?,
        error: String
    ): ApiResponse<AppVersionPolicy> {
        return if (cached != null) {
            ApiResponse(success = true, data = cached)
        } else {
            ApiResponse(success = false, error = error)
        }
    }

    private fun Response<*>.errorMessage(fallback: String): String {
        val raw = runCatching { errorBody()?.string() }.getOrNull().orEmpty()
        if (raw.isBlank()) return "$fallback: ${code()} ${message()}".trim()

        return runCatching {
            JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
        }.getOrNull() ?: "$fallback: ${code()} ${message()}".trim()
    }

    companion object {
        const val APP_KEY = "izakod_asn"
        const val PLATFORM = "android"

        const val EVENT_UPDATE_SHOWN = "update_shown"
        const val EVENT_UPDATE_CLICKED = "update_clicked"
        const val EVENT_UPDATE_COMPLETED = "update_completed"
        const val EVENT_UPDATE_SKIPPED = "update_skipped"
        const val EVENT_UPDATE_DISMISSED = "update_dismissed"

        private const val DEFAULT_CHECK_INTERVAL_SECONDS = 86_400L
        private const val MIN_CHECK_INTERVAL_SECONDS = 60L
    }
}
