package com.kominfo_mkq.izakod_asn.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

/**
 * UserPreferences - Secure storage untuk session data
 * Menggunakan EncryptedSharedPreferences untuk keamanan
 */
class UserPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_EMAIL = "email"
        private const val KEY_PIN = "pin"
        private const val KEY_LEVEL = "level"
        private const val KEY_SKPDID = "skpdid"
        private const val KEY_PEGAWAI_ID = "pegawai_id"
    }

    /**
     * Save user session after successful login
     */
    fun saveSession(
        email: String,
        pin: String,
        level: Int,
        skpdid: Int,
        pegawaiId: Int?
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_EMAIL, email)
            putString(KEY_PIN, pin)
            putInt(KEY_LEVEL, level)
            putInt(KEY_SKPDID, skpdid)
            pegawaiId?.let { putInt(KEY_PEGAWAI_ID, it) }
            apply()
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Get stored email
     */
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /**
     * Get stored PIN
     */
    fun getPin(): String? = prefs.getString(KEY_PIN, null)

    /**
     * Get stored level
     */
    fun getLevel(): Int = prefs.getInt(KEY_LEVEL, 0)

    /**
     * Get stored SKPD ID
     */
    fun getSkpdid(): Int = prefs.getInt(KEY_SKPDID, 0)

    /**
     * Get stored Pegawai ID
     */
    fun getPegawaiId(): Int? {
        return if (prefs.contains(KEY_PEGAWAI_ID)) {
            prefs.getInt(KEY_PEGAWAI_ID, 0)
        } else {
            null
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

    /**
     * Clear session (logout)
     */
    fun clearSession() {
        prefs.edit { clear() }
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