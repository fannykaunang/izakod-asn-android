package com.kominfo_mkq.izakod_asn.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.AEADBadTagException

/**
 * UserPreferences - Secure storage untuk session data
 * Menggunakan EncryptedSharedPreferences untuk keamanan
 */
class UserPreferences(context: Context) {
    private val appContext = context.applicationContext
    private var prefs: SharedPreferences = createEncryptedPrefsWithRecovery(appContext)

    companion object {
        private const val TAG = "UserPreferences"
        private const val PREFS_NAME = "user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_EMAIL = "email"
        private const val KEY_PIN = "pin"
        private const val KEY_LEVEL = "level"
        private const val KEY_SKPDID = "skpdid"
        private const val KEY_PEGAWAI_ID = "pegawai_id"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_MOBILE_JWT_TOKEN = "mobile_jwt_token"
        private const val KEY_MOBILE_REFRESH_TOKEN = "mobile_refresh_token"
        private const val KEY_ENTAGO_ACCESS_TOKEN = "entago_access_token"
        private const val KEY_ENTAGO_REFRESH_TOKEN = "entago_refresh_token"
        private const val KEY_MOBILE_FCM_TOKEN = "mobile_fcm_token"
        private const val KEY_NOTIF_PERMISSION_ASKED = "notif_permission_asked"
        private const val KEY_DASHBOARD_COACHMARK_DISMISSED_PREFIX = "dashboard_coachmark_dismissed_"

        private fun createEncryptedPrefsWithRecovery(context: Context): SharedPreferences {
            return try {
                createEncryptedPrefs(context)
            } catch (error: Throwable) {
                if (!error.isRecoverableEncryptedPrefsError()) throw error

                Log.w(TAG, "Encrypted preferences rusak saat inisialisasi. Sesi lokal direset.", error)
                context.deleteSharedPreferences(PREFS_NAME)
                deleteMasterKeyEntry()
                createEncryptedPrefs(context)
            }
        }

        private fun createEncryptedPrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        private fun deleteMasterKeyEntry() {
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                if (keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                    keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                }
            } catch (error: Exception) {
                Log.w(TAG, "Gagal menghapus master key lama.", error)
            }
        }

        private fun Throwable.isRecoverableEncryptedPrefsError(): Boolean {
            var current: Throwable? = this
            while (current != null) {
                if (
                    current is AEADBadTagException ||
                    current is GeneralSecurityException ||
                    current is IOException
                ) {
                    return true
                }

                val message = current.message.orEmpty()
                if (
                    message.contains("decrypt", ignoreCase = true) ||
                    message.contains("keystore", ignoreCase = true) ||
                    message.contains("keyset", ignoreCase = true)
                ) {
                    return true
                }

                current = current.cause
            }

            return false
        }
    }

    private fun recoverEncryptedPrefs(error: Throwable) {
        Log.w(TAG, "Encrypted preferences tidak bisa dibaca. Sesi lokal direset.", error)
        appContext.deleteSharedPreferences(PREFS_NAME)
        prefs = createEncryptedPrefsWithRecovery(appContext)
    }

    private inline fun <T> readSafely(defaultValue: T, block: SharedPreferences.() -> T): T {
        return try {
            prefs.block()
        } catch (error: Throwable) {
            if (!error.isRecoverableEncryptedPrefsError()) throw error
            recoverEncryptedPrefs(error)
            defaultValue
        }
    }

    private inline fun editSafely(crossinline block: SharedPreferences.Editor.() -> Unit) {
        try {
            prefs.edit { block() }
        } catch (error: Throwable) {
            if (!error.isRecoverableEncryptedPrefsError()) throw error
            recoverEncryptedPrefs(error)
            prefs.edit { block() }
        }
    }

    fun isDarkTheme(): Boolean =
        readSafely(false) { getBoolean(KEY_DARK_THEME, false) }

    fun setDarkTheme(enabled: Boolean) {
        editSafely { putBoolean(KEY_DARK_THEME, enabled) }
    }

    fun setMobileFcmToken(token: String?) {
        editSafely {
            if (token.isNullOrBlank()) remove(KEY_MOBILE_FCM_TOKEN)
            else putString(KEY_MOBILE_FCM_TOKEN, token)
        }
    }
// TODO lanjut publish ke playstore di https://play.google.com/console/u/0/developers/7901927936154564272/app/4974458399961873016/app-content/testing-credentials?source=dashboard
    // tambahkan fungsi
    fun hasAskedNotificationPermission(): Boolean =
        readSafely(false) { getBoolean(KEY_NOTIF_PERMISSION_ASKED, false) }

    fun setAskedNotificationPermission(asked: Boolean) {
        editSafely { putBoolean(KEY_NOTIF_PERMISSION_ASKED, asked) }
    }

    fun isDashboardCoachmarkDismissed(key: String): Boolean =
        readSafely(false) { getBoolean(KEY_DASHBOARD_COACHMARK_DISMISSED_PREFIX + key, false) }

    fun setDashboardCoachmarkDismissed(key: String, dismissed: Boolean = true) {
        editSafely {
            putBoolean(KEY_DASHBOARD_COACHMARK_DISMISSED_PREFIX + key, dismissed)
        }
    }

    /**
     * Save user session after successful login
     */
    fun saveSession(
        email: String,
        pin: String,
        level: Int,
        skpdid: Int,
        pegawaiId: Int
    ) {
        editSafely {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_EMAIL, email)
            putString(KEY_PIN, pin)
            putInt(KEY_LEVEL, level)
            putInt(KEY_SKPDID, skpdid)
            putInt(KEY_PEGAWAI_ID, pegawaiId)
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return readSafely(false) { getBoolean(KEY_IS_LOGGED_IN, false) }
    }

    /**
     * Get stored email
     */
    fun getEmail(): String? = readSafely<String?>(null) { getString(KEY_EMAIL, null) }

    /**
     * Get stored PIN
     */
    fun getPin(): String? = readSafely<String?>(null) { getString(KEY_PIN, null) }

    /**
     * Get stored level
     */
    fun getLevel(): Int = readSafely(0) { getInt(KEY_LEVEL, 0) }

    /**
     * Get stored SKPD ID
     */
    fun getSkpdid(): Int = readSafely(0) { getInt(KEY_SKPDID, 0) }

    /**
     * Get stored Pegawai ID
     */
    fun getPegawaiId(): Int? {
        return readSafely<Int?>(null) {
            if (contains(KEY_PEGAWAI_ID)) {
                val id = getInt(KEY_PEGAWAI_ID, 0)
                if (id > 0) id else null
            } else null
        }
    }

    /**
     * Get all session data
     */
    fun getSessionData(): SessionData? {
        return if (isLoggedIn()) {
            SessionData(
                email = getEmail() ?: "",
                pin = getPin() ?: "",
                level = getLevel(),
                skpdid = getSkpdid(),
                pegawaiId = getPegawaiId()
            )
        } else {
            null
        }
    }

    fun setMobileJwtToken(token: String?) {
        editSafely {
            if (token.isNullOrBlank()) remove(KEY_MOBILE_JWT_TOKEN)
            else putString(KEY_MOBILE_JWT_TOKEN, token)
        }
    }

    fun getMobileJwtToken(): String? {
        return readSafely<String?>(null) { getString(KEY_MOBILE_JWT_TOKEN, null) }
    }

    fun setRefreshToken(token: String?) {
        editSafely {
            if (token.isNullOrBlank()) remove(KEY_MOBILE_REFRESH_TOKEN)
            else putString(KEY_MOBILE_REFRESH_TOKEN, token)
        }
    }

    fun getRefreshToken(): String? {
        return readSafely<String?>(null) { getString(KEY_MOBILE_REFRESH_TOKEN, null) }
    }

    fun setEntagoAccessToken(token: String?) {
        editSafely {
            if (token.isNullOrBlank()) remove(KEY_ENTAGO_ACCESS_TOKEN)
            else putString(KEY_ENTAGO_ACCESS_TOKEN, token)
        }
    }

    fun getEntagoAccessToken(): String? {
        return readSafely<String?>(null) { getString(KEY_ENTAGO_ACCESS_TOKEN, null) }
    }

    fun setEntagoRefreshToken(token: String?) {
        editSafely {
            if (token.isNullOrBlank()) remove(KEY_ENTAGO_REFRESH_TOKEN)
            else putString(KEY_ENTAGO_REFRESH_TOKEN, token)
        }
    }

    fun getEntagoRefreshToken(): String? {
        return readSafely<String?>(null) { getString(KEY_ENTAGO_REFRESH_TOKEN, null) }
    }


    /**
     * Clear session (logout)
     */
    fun clearSession() {
        editSafely { clear() }
    }
}

/**
 * Data class for session data
 */
data class SessionData(
    val email: String,
    val pin: String,
    val level: Int,
    val skpdid: Int,
    val pegawaiId: Int?
)
