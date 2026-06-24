package com.kominfo_mkq.izakod_asn.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.kominfo_mkq.izakod_asn.data.model.AppVersionPolicy
import com.kominfo_mkq.izakod_asn.data.model.EntagoLoginUser
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
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
    private val gson = Gson()

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
        private const val KEY_PROFILE_NAMA = "profile_nama"
        private const val KEY_PROFILE_NIP = "profile_nip"
        private const val KEY_PROFILE_TEMPAT_LAHIR = "profile_tempat_lahir"
        private const val KEY_PROFILE_TGL_LAHIR = "profile_tgl_lahir"
        private const val KEY_PROFILE_GENDER = "profile_gender"
        private const val KEY_PROFILE_TELP = "profile_telp"
        private const val KEY_PROFILE_STATUS = "profile_status"
        private const val KEY_PROFILE_JABATAN = "profile_jabatan"
        private const val KEY_PROFILE_SKPD = "profile_skpd"
        private const val KEY_PROFILE_SOTK = "profile_sotk"
        private const val KEY_PROFILE_TGL_MULAI_KERJA = "profile_tgl_mulai_kerja"
        private const val KEY_PROFILE_PHOTO_PATH = "profile_photo_path"
        private const val KEY_PROFILE_DEVICE_ID = "profile_device_id"
        private const val KEY_MOBILE_FCM_TOKEN = "mobile_fcm_token"
        private const val KEY_NOTIF_PERMISSION_ASKED = "notif_permission_asked"
        private const val KEY_DASHBOARD_COACHMARK_DISMISSED_PREFIX = "dashboard_coachmark_dismissed_"
        private const val KEY_SSO_PAYROLL_ESTIMATE_JSON = "sso_payroll_estimate_json"
        private const val KEY_SSO_PAYROLL_ESTIMATE_SAVED_AT = "sso_payroll_estimate_saved_at"
        private const val KEY_APP_VERSION_POLICY_JSON = "app_version_policy_json"
        private const val KEY_APP_VERSION_POLICY_SAVED_AT = "app_version_policy_saved_at"
        private const val KEY_APP_VERSION_LAST_SEEN_CODE = "app_version_last_seen_code"
        private const val KEY_APP_VERSION_LAST_SEEN_NAME = "app_version_last_seen_name"

        @Volatile
        private var inMemorySsoPayrollEstimateJson: String? = null

        @Volatile
        private var inMemorySsoPayrollEstimateSavedAt: Long = 0L

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
        val previousPegawaiId = getPegawaiId()
        val previousPin = getPin()
        val isDifferentPegawai = (previousPegawaiId != null && previousPegawaiId != pegawaiId) ||
                (!previousPin.isNullOrBlank() && previousPin != pin)

        editSafely {
            if (isDifferentPegawai) clearProfileSnapshotFields()
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

    fun saveSsoPayrollEstimate(json: String?) {
        val cleanedJson = json?.trim()?.takeIf { it.isNotBlank() }
        val savedAt = if (cleanedJson == null) 0L else System.currentTimeMillis()

        inMemorySsoPayrollEstimateJson = cleanedJson
        inMemorySsoPayrollEstimateSavedAt = savedAt

        editSafely {
            if (cleanedJson == null) {
                remove(KEY_SSO_PAYROLL_ESTIMATE_JSON)
                remove(KEY_SSO_PAYROLL_ESTIMATE_SAVED_AT)
            } else {
                putString(KEY_SSO_PAYROLL_ESTIMATE_JSON, cleanedJson)
                putLong(KEY_SSO_PAYROLL_ESTIMATE_SAVED_AT, savedAt)
            }
        }
    }

    fun getSsoPayrollEstimateJson(maxAgeMillis: Long = 10 * 60 * 1000L): String? {
        val now = System.currentTimeMillis()
        val savedAt = readSafely(0L) { getLong(KEY_SSO_PAYROLL_ESTIMATE_SAVED_AT, 0L) }
        val persistedJson = readSafely<String?>(null) { getString(KEY_SSO_PAYROLL_ESTIMATE_JSON, null) }
        if (savedAt > 0L && now - savedAt <= maxAgeMillis && !persistedJson.isNullOrBlank()) {
            return persistedJson
        }

        val memoryJson = inMemorySsoPayrollEstimateJson
        val memorySavedAt = inMemorySsoPayrollEstimateSavedAt
        if (memorySavedAt > 0L && now - memorySavedAt <= maxAgeMillis && !memoryJson.isNullOrBlank()) {
            return memoryJson
        }

        clearSsoPayrollEstimate()
        return null
    }

    fun clearSsoPayrollEstimate() {
        inMemorySsoPayrollEstimateJson = null
        inMemorySsoPayrollEstimateSavedAt = 0L

        editSafely {
            remove(KEY_SSO_PAYROLL_ESTIMATE_JSON)
            remove(KEY_SSO_PAYROLL_ESTIMATE_SAVED_AT)
        }
    }

    fun saveAppVersionPolicyCache(policy: AppVersionPolicy) {
        val json = gson.toJson(policy)
        editSafely {
            putString(KEY_APP_VERSION_POLICY_JSON, json)
            putLong(KEY_APP_VERSION_POLICY_SAVED_AT, System.currentTimeMillis())
        }
    }

    fun getCachedAppVersionPolicy(maxAgeMillis: Long = Long.MAX_VALUE): AppVersionPolicy? {
        val savedAt = getAppVersionPolicyCachedAt()
        val json = readSafely<String?>(null) { getString(KEY_APP_VERSION_POLICY_JSON, null) }
        if (savedAt <= 0L || json.isNullOrBlank()) return null

        val now = System.currentTimeMillis()
        if (maxAgeMillis != Long.MAX_VALUE && now - savedAt > maxAgeMillis) {
            return null
        }

        return try {
            gson.fromJson(json, AppVersionPolicy::class.java)
        } catch (error: JsonSyntaxException) {
            Log.w(TAG, "Cache policy versi aplikasi tidak valid.", error)
            clearAppVersionPolicyCache()
            null
        }
    }

    fun getAppVersionPolicyCachedAt(): Long =
        readSafely(0L) { getLong(KEY_APP_VERSION_POLICY_SAVED_AT, 0L) }

    fun isAppVersionPolicyCacheFresh(maxAgeMillis: Long): Boolean {
        val savedAt = getAppVersionPolicyCachedAt()
        return savedAt > 0L && System.currentTimeMillis() - savedAt <= maxAgeMillis
    }

    fun clearAppVersionPolicyCache() {
        editSafely {
            remove(KEY_APP_VERSION_POLICY_JSON)
            remove(KEY_APP_VERSION_POLICY_SAVED_AT)
        }
    }

    fun saveLastSeenAppVersion(versionCode: Int, versionName: String) {
        if (versionCode <= 0) return

        editSafely {
            putInt(KEY_APP_VERSION_LAST_SEEN_CODE, versionCode)
            putString(KEY_APP_VERSION_LAST_SEEN_NAME, versionName)
        }
    }

    fun getLastSeenAppVersionCode(): Int? {
        return readSafely<Int?>(null) {
            if (contains(KEY_APP_VERSION_LAST_SEEN_CODE)) {
                getInt(KEY_APP_VERSION_LAST_SEEN_CODE, 0).takeIf { it > 0 }
            } else {
                null
            }
        }
    }

    fun getLastSeenAppVersionName(): String? =
        readSafely<String?>(null) { getString(KEY_APP_VERSION_LAST_SEEN_NAME, null) }

    fun saveProfileSnapshot(profile: PegawaiProfile) {
        editSafely {
            putString(KEY_PROFILE_NAMA, profile.pegawaiNama)
            putString(KEY_PROFILE_NIP, profile.pegawaiNip)
            putString(KEY_PROFILE_TEMPAT_LAHIR, profile.tempatLahir)
            putString(KEY_PROFILE_TGL_LAHIR, profile.tglLahir)
            putInt(KEY_PROFILE_GENDER, profile.gender)
            putProfileTextIfAvailable(KEY_PROFILE_TELP, profile.pegawaiTelp)
            if (profile.pegawaiStatus == null) remove(KEY_PROFILE_STATUS)
            else putInt(KEY_PROFILE_STATUS, profile.pegawaiStatus)
            putString(KEY_PROFILE_JABATAN, profile.jabatan)
            putString(KEY_PROFILE_SKPD, profile.skpd)
            putString(KEY_PROFILE_SOTK, profile.sotk)
            putProfileTextIfAvailable(KEY_PROFILE_TGL_MULAI_KERJA, profile.tglMulaiKerja)
            putProfilePhotoPathIfAvailable(profile.photoPath)
            putString(KEY_PROFILE_DEVICE_ID, profile.deviceId)
        }
    }

    fun saveProfileSnapshot(user: EntagoLoginUser?) {
        if (user == null) return

        editSafely {
            putString(KEY_PROFILE_NAMA, user.pegawaiNama)
            putString(KEY_PROFILE_NIP, user.pegawaiNip)
            putString(KEY_PROFILE_TEMPAT_LAHIR, user.tempatLahir)
            putString(KEY_PROFILE_TGL_LAHIR, user.tglLahir)
            putInt(KEY_PROFILE_GENDER, user.gender ?: 1)
            putProfileTextIfAvailable(KEY_PROFILE_TELP, user.pegawaiTelp)
            if (user.pegawaiStatus == null) remove(KEY_PROFILE_STATUS)
            else putInt(KEY_PROFILE_STATUS, user.pegawaiStatus)
            putString(KEY_PROFILE_JABATAN, user.jabatan)
            putString(KEY_PROFILE_SKPD, user.skpd)
            putString(KEY_PROFILE_SOTK, user.sotk)
            putProfileTextIfAvailable(KEY_PROFILE_TGL_MULAI_KERJA, user.tglMulaiKerja)
            putProfilePhotoPathIfAvailable(user.photoPath)
            putString(KEY_PROFILE_DEVICE_ID, user.deviceId)
        }
    }

    private fun SharedPreferences.Editor.putProfilePhotoPathIfAvailable(photoPath: String?) {
        val cleanPath = photoPath?.trim().orEmpty()
        if (cleanPath.isNotBlank()) putString(KEY_PROFILE_PHOTO_PATH, cleanPath)
    }

    private fun SharedPreferences.Editor.putProfileTextIfAvailable(key: String, value: String?) {
        val cleanValue = value?.trim().orEmpty()
        if (cleanValue.isNotBlank()) putString(key, cleanValue)
    }

    private fun SharedPreferences.Editor.clearProfileSnapshotFields() {
        remove(KEY_PROFILE_NAMA)
        remove(KEY_PROFILE_NIP)
        remove(KEY_PROFILE_TEMPAT_LAHIR)
        remove(KEY_PROFILE_TGL_LAHIR)
        remove(KEY_PROFILE_GENDER)
        remove(KEY_PROFILE_TELP)
        remove(KEY_PROFILE_STATUS)
        remove(KEY_PROFILE_JABATAN)
        remove(KEY_PROFILE_SKPD)
        remove(KEY_PROFILE_SOTK)
        remove(KEY_PROFILE_TGL_MULAI_KERJA)
        remove(KEY_PROFILE_PHOTO_PATH)
        remove(KEY_PROFILE_DEVICE_ID)
    }

    fun getCachedPegawaiProfile(): PegawaiProfile? {
        val session = getSessionData() ?: return null
        val pegawaiId = session.pegawaiId ?: return null
        val nama = readSafely<String?>(null) { getString(KEY_PROFILE_NAMA, null) }
            ?: session.email.takeIf { it.isNotBlank() }

        return PegawaiProfile(
            pegawaiId = pegawaiId,
            pegawaiPin = session.pin,
            pegawaiNip = readSafely<String?>(null) { getString(KEY_PROFILE_NIP, null) },
            pegawaiNama = nama,
            tempatLahir = readSafely<String?>(null) { getString(KEY_PROFILE_TEMPAT_LAHIR, null) },
            pegawaiPrivilege = session.level.toString(),
            pegawaiTelp = readSafely<String?>(null) { getString(KEY_PROFILE_TELP, null) },
            pegawaiStatus = readSafely<Int?>(null) {
                if (contains(KEY_PROFILE_STATUS)) getInt(KEY_PROFILE_STATUS, 1) else null
            },
            tglLahir = readSafely<String?>(null) { getString(KEY_PROFILE_TGL_LAHIR, null) },
            jabatan = readSafely<String?>(null) { getString(KEY_PROFILE_JABATAN, null) },
            skpd = readSafely<String?>(null) { getString(KEY_PROFILE_SKPD, null) },
            sotk = readSafely<String?>(null) { getString(KEY_PROFILE_SOTK, null) },
            tglMulaiKerja = readSafely<String?>(null) { getString(KEY_PROFILE_TGL_MULAI_KERJA, null) },
            gender = readSafely(1) { getInt(KEY_PROFILE_GENDER, 1) },
            photoPath = readSafely<String?>(null) { getString(KEY_PROFILE_PHOTO_PATH, null) },
            deviceId = readSafely<String?>(null) { getString(KEY_PROFILE_DEVICE_ID, null) }
        )
    }

    fun clearEntagoTokens() {
        editSafely {
            remove(KEY_ENTAGO_ACCESS_TOKEN)
            remove(KEY_ENTAGO_REFRESH_TOKEN)
        }
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
