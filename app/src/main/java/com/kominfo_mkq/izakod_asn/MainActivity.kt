package com.kominfo_mkq.izakod_asn

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.kominfo_mkq.izakod_asn.data.local.AppContextHolder
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AppVersionPolicy
import com.kominfo_mkq.izakod_asn.data.repository.AppVersionRepository
import com.kominfo_mkq.izakod_asn.data.repository.AuthRepository
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import com.kominfo_mkq.izakod_asn.fcm.DeviceInfo
import com.kominfo_mkq.izakod_asn.ui.components.AppUpdateDialog
import com.kominfo_mkq.izakod_asn.ui.navigation.IZAKODNavigation
import com.kominfo_mkq.izakod_asn.ui.navigation.Screen
import com.kominfo_mkq.izakod_asn.ui.navigation.mobileSsoBridgeRoute
import com.kominfo_mkq.izakod_asn.ui.theme.IZAKODASNTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject


@Composable
fun RequestNotificationPermissionOnce(userPrefs: UserPreferences) {
    if (Build.VERSION.SDK_INT < 33) return

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Apa pun hasilnya, sudah dianggap "pernah diminta"
        userPrefs.setAskedNotificationPermission(true)
    }

    LaunchedEffect(Unit) {
        // kalau sudah pernah diminta -> jangan tanya lagi
        if (userPrefs.hasAskedNotificationPermission()) return@LaunchedEffect

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            // sudah granted tanpa perlu prompt, tandai saja biar tidak ngecek lagi
            userPrefs.setAskedNotificationPermission(true)
            return@LaunchedEffect
        }

        // belum granted & belum pernah diminta -> minta 1x
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var userPrefs: UserPreferences
    private var externalRouteHandler: ((String) -> Unit)? = null

    private data class ExternalRoute(
        val route: String,
        val ssoTicket: String? = null
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        AppContextHolder.init(this)

        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        // Matikan setelah 2 detik
        lifecycleScope.launch {
            delay(2000)
            keepSplashScreen = false
        }

        val prefs = UserPreferences(this)
        migrateLegacyTokensIfNeeded(prefs)
        val restoredMobileToken = prefs.getMobileJwtToken()?.takeIf { isLikelyMobileToken(it) }
        if (restoredMobileToken == null) {
            prefs.setMobileJwtToken(null)
        }
        TokenStore.setToken(restoredMobileToken)
        TokenStore.setRefreshToken(prefs.getRefreshToken())

        if (prefs.isLoggedIn() && restoredMobileToken == null) {
            val session = prefs.getSessionData()
            if (session?.pegawaiId != null && session.pin.isNotBlank()) {
                lifecycleScope.launch {
                    try {
                        val response = AuthRepository().fetchNextJsMobileToken(
                            pegawaiId = session.pegawaiId,
                            pin = session.pin
                        )
                        val tokenData = response.body()?.data
                        val nextJsMobileToken = tokenData?.token?.trim()
                        val nextJsRefreshToken = tokenData?.refreshToken?.trim()
                        if (response.isSuccessful && !nextJsMobileToken.isNullOrBlank()) {
                            prefs.setMobileJwtToken(nextJsMobileToken)
                            prefs.setRefreshToken(nextJsRefreshToken)
                            TokenStore.setToken(nextJsMobileToken)
                            TokenStore.setRefreshToken(nextJsRefreshToken)
                            Log.d("MainActivity", "✅ Next.js mobile token refreshed on startup")
                        } else {
                            Log.w("MainActivity", "Failed refreshing mobile token on startup: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.w("MainActivity", "Failed refreshing mobile token on startup: ${e.message}")
                    }
                }
            }
        }

        enableEdgeToEdge()

        userPrefs = prefs
        val initialExternalRoute = resolveExternalRoute(intent)
        val initialSsoTicket = initialExternalRoute?.ssoTicket?.takeIf { it.isNotBlank() }
        val initialSsoFallbackRoute = initialExternalRoute?.route

        setContent {
            var isDarkTheme by remember { mutableStateOf(userPrefs.isDarkTheme()) }
            var appUpdatePolicy by remember { mutableStateOf<AppVersionPolicy?>(null) }
            val appUpdateScope = rememberCoroutineScope()
            var pendingExternalRoute by rememberSaveable {
                mutableStateOf(
                    if (initialSsoTicket == null) initialExternalRoute?.route else null
                )
            }
            val startDestination = remember {
                if (initialSsoTicket != null) {
                    Screen.MobileSsoBridge.route
                } else {
                    checkAndRestoreSession()
                }
            }

            LaunchedEffect(Unit) {
                appUpdatePolicy = performAppVersionStartupCheck(userPrefs)
            }

            DisposableEffect(Unit) {
                externalRouteHandler = { route ->
                    pendingExternalRoute = route
                }
                onDispose {
                    externalRouteHandler = null
                }
            }

            IZAKODASNTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ✅ Minta izin notifikasi sekali (Android 13+)
                    // Kalau mau hanya saat user sudah login:
                    if (startDestination == Screen.Dashboard.route) {
                        RequestNotificationPermissionOnce(userPrefs)
                    }

                    IZAKODNavigation(
                        startDestination = startDestination,
                        initialSsoTicket = initialSsoTicket,
                        initialSsoFallbackRoute = initialSsoFallbackRoute,
                        pendingExternalRoute = pendingExternalRoute,
                        onPendingExternalRouteConsumed = {
                            pendingExternalRoute = null
                        },

                        // ✅ callback untuk Settings toggle
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { enabled ->
                            isDarkTheme = enabled
                            userPrefs.setDarkTheme(enabled) // simpan preferensi
                        }
                    )

                    appUpdatePolicy?.let { policy ->
                        val isUpdateRequired = policy.isUpdateRequiredForCurrentVersion(
                            DeviceInfo.appVersionCode(applicationContext)
                        )

                        LaunchedEffect(policy.policyId, policy.latestVersionCode, isUpdateRequired) {
                            val eventResult = AppVersionRepository(applicationContext).recordUpdateEvent(
                                eventType = AppVersionRepository.EVENT_UPDATE_SHOWN,
                                policy = policy,
                                source = "update_dialog"
                            )
                            if (!eventResult.success) {
                                Log.w("MainActivity", "Update shown event skipped: ${eventResult.error}")
                            }
                        }

                        AppUpdateDialog(
                            policy = policy,
                            isRequired = isUpdateRequired,
                            onDismissRequest = {
                                if (!isUpdateRequired) {
                                    appUpdateScope.launch {
                                        AppVersionRepository(applicationContext).recordUpdateEvent(
                                            eventType = AppVersionRepository.EVENT_UPDATE_DISMISSED,
                                            policy = policy,
                                            source = "update_dialog"
                                        )
                                    }
                                    appUpdatePolicy = null
                                }
                            },
                            onSkipClick = {
                                if (!isUpdateRequired) {
                                    appUpdateScope.launch {
                                        AppVersionRepository(applicationContext).recordUpdateEvent(
                                            eventType = AppVersionRepository.EVENT_UPDATE_SKIPPED,
                                            policy = policy,
                                            source = "update_dialog"
                                        )
                                    }
                                    appUpdatePolicy = null
                                }
                            },
                            onUpdateClick = {
                                appUpdateScope.launch {
                                    AppVersionRepository(applicationContext).recordUpdateEvent(
                                        eventType = AppVersionRepository.EVENT_UPDATE_CLICKED,
                                        policy = policy,
                                        source = "update_dialog"
                                    )
                                }
                                openAppStore(policy)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveExternalRoute(intent)?.let { externalRoute ->
            val route = externalRoute.ssoTicket
                ?.takeIf { it.isNotBlank() }
                ?.let { ticket ->
                    mobileSsoBridgeRoute(
                        ticket = ticket,
                        fallbackRoute = externalRoute.route
                    )
                }
                ?: externalRoute.route

            externalRouteHandler?.invoke(route)
        }
    }

    private fun checkAndRestoreSession(): String {
        val isLoggedIn = userPrefs.isLoggedIn()

        return if (isLoggedIn) {
            val sessionData = userPrefs.getSessionData()
            sessionData?.let {
                StatistikRepository.setUserData(
                    pegawaiId = it.pegawaiId,
                    pin = it.pin
                )
                Log.d(
                    "MainActivity",
                    "✅ Session restored: pegawai_id=${it.pegawaiId}, pin=${it.pin}"
                )
            }
            Screen.Dashboard.route
        } else {
            Log.d("MainActivity", "❌ No session found, showing Login")
            Screen.Login.route
        }
    }

    override fun onResume() {
        super.onResume()
        if (userPrefs.isLoggedIn()) {
            val sessionData = userPrefs.getSessionData()
            sessionData?.let {
                StatistikRepository.setUserData(it.pegawaiId, it.pin)
                Log.d("MainActivity", "✅ Session restored on resume")
            }
        }
    }

    private suspend fun performAppVersionStartupCheck(prefs: UserPreferences): AppVersionPolicy? {
        val repository = AppVersionRepository(applicationContext)
        val currentVersionCode = DeviceInfo.appVersionCode(applicationContext)
        val currentVersionName = DeviceInfo.appVersion(applicationContext)
        val previousVersionCode = prefs.getLastSeenAppVersionCode()
        val previousVersionName = prefs.getLastSeenAppVersionName()
        val cachedBeforeCheck = repository.getCachedAppVersionPolicy()
        var policy = cachedBeforeCheck
        val appVersionChanged = previousVersionCode != null && currentVersionCode > previousVersionCode
        val cachedPolicyNeedsPrompt = cachedBeforeCheck
            ?.shouldPromptForCurrentVersion(currentVersionCode) == true

        if (repository.shouldCheckAppVersion() || appVersionChanged || cachedPolicyNeedsPrompt) {
            val result = repository.getAppVersionPolicy(
                forceRefresh = appVersionChanged || cachedPolicyNeedsPrompt
            )
            if (result.success) {
                policy = result.data ?: repository.getCachedAppVersionPolicy()
            } else {
                Log.w("MainActivity", "App version check skipped: ${result.error}")
            }
        }

        if (appVersionChanged) {
            val eventResult = repository.recordUpdateEvent(
                eventType = AppVersionRepository.EVENT_UPDATE_COMPLETED,
                policy = policy,
                source = "app_start",
                fromVersionCode = previousVersionCode,
                fromVersionName = previousVersionName,
                toVersionCode = currentVersionCode,
                toVersionName = currentVersionName,
                metadata = mapOf("detected_by" to "app_start_version_change")
            )

            if (!eventResult.success) {
                Log.w("MainActivity", "Update completed event skipped: ${eventResult.error}")
            }
        }

        prefs.saveLastSeenAppVersion(currentVersionCode, currentVersionName)
        return policy?.takeIf { it.shouldPromptForCurrentVersion(currentVersionCode) }
    }

    private fun openAppStore(policy: AppVersionPolicy) {
        val targetPackageName = policy.packageName?.takeIf { it.isNotBlank() } ?: packageName
        val storeUrl = policy.storeUrl?.takeIf { it.isNotBlank() }

        if (storeUrl != null && openUri(storeUrl)) return
        if (openUri("market://details?id=$targetPackageName")) return
        openUri("https://play.google.com/store/apps/details?id=$targetPackageName")
    }

    private fun AppVersionPolicy.shouldPromptForCurrentVersion(currentVersionCode: Int): Boolean {
        return isUpdateRequiredForCurrentVersion(currentVersionCode) ||
            isUpdateAvailableForCurrentVersion(currentVersionCode)
    }

    private fun AppVersionPolicy.isUpdateRequiredForCurrentVersion(currentVersionCode: Int): Boolean {
        val minimumCode = minimumSupportedVersionCode
        return if (minimumCode != null) {
            currentVersionCode < minimumCode
        } else {
            updateRequired
        }
    }

    private fun AppVersionPolicy.isUpdateAvailableForCurrentVersion(currentVersionCode: Int): Boolean {
        val latestCode = latestVersionCode
        return if (latestCode != null) {
            currentVersionCode < latestCode
        } else {
            updateAvailable
        }
    }

    private fun openUri(rawUri: String): Boolean {
        return try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rawUri)))
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
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

    private fun migrateLegacyTokensIfNeeded(prefs: UserPreferences) {
        val entagoAccess = prefs.getEntagoAccessToken()
        val entagoRefresh = prefs.getEntagoRefreshToken()
        val legacyMobileToken = prefs.getMobileJwtToken()
        val legacyRefreshToken = prefs.getRefreshToken()
        val legacyAccessIsEntagoToken =
            entagoAccess.isNullOrBlank() &&
                !legacyMobileToken.isNullOrBlank() &&
                !isLikelyMobileToken(legacyMobileToken)

        if (legacyAccessIsEntagoToken) {
            prefs.setEntagoAccessToken(legacyMobileToken)
            Log.d("MainActivity", "✅ Migrated legacy E-NTAGO access token")
        }

        if (
            legacyAccessIsEntagoToken &&
            entagoRefresh.isNullOrBlank() &&
            !legacyRefreshToken.isNullOrBlank()
        ) {
            prefs.setEntagoRefreshToken(legacyRefreshToken)
            Log.d("MainActivity", "✅ Migrated legacy E-NTAGO refresh token")
        } else if (
            !entagoRefresh.isNullOrBlank() &&
            !legacyRefreshToken.isNullOrBlank() &&
            entagoRefresh == legacyRefreshToken &&
            !legacyMobileToken.isNullOrBlank() &&
            isLikelyMobileToken(legacyMobileToken)
        ) {
            prefs.setEntagoRefreshToken(null)
            Log.w("MainActivity", "Cleared invalid E-NTAGO refresh token copied from IZAKOD mobile token")
        }
    }

    private fun resolveExternalRoute(intent: Intent?): ExternalRoute? {
        return resolvePayrollDeepLink(intent) ?: resolveNotificationRoute(intent)
    }

    private fun resolvePayrollDeepLink(intent: Intent?): ExternalRoute? {
        val uri = intent?.data ?: return null
        if (!uri.scheme.equals("izakod-asn", ignoreCase = true)) return null
        if (!uri.host.equals("payroll", ignoreCase = true)) return null
        if (uri.pathSegments.firstOrNull()?.equals("detail", ignoreCase = true) != true) return null

        val tahun = uri.queryInt("tahun") ?: return null
        val bulan = uri.queryInt("bulan")?.takeIf { it in 1..12 } ?: return null
        val jenis = uri.getQueryParameter("jenis").orEmpty().trim().lowercase()
        val ssoTicket = uri.getQueryParameter("sso_ticket")?.trim()?.takeIf { it.isNotBlank() }

        val route = when (jenis) {
            "gaji", "gaji_non_asn", "non_asn" -> "gaji_saya_detail/$tahun/$bulan"
            "tpp", "tpp_asn" -> "tpp_saya_detail/$tahun/$bulan"
            else -> null
        } ?: return null

        Log.d(
            "MainActivity",
            "Payroll deep link diterima: jenis=$jenis, tahun=$tahun, bulan=$bulan, sso=${ssoTicket != null}"
        )

        return ExternalRoute(route = route, ssoTicket = ssoTicket)
    }

    private fun resolveNotificationRoute(intent: Intent?): ExternalRoute? {
        val pengumumanId = intent?.firstPositiveIntExtra(
            "pengumuman_id",
            "pengumumanId",
            "target_id"
        ) ?: extractPengumumanIdFromLink(intent?.getStringExtra("link_tujuan"))

        if (pengumumanId != null) {
            Log.d(
                "MainActivity",
                "Notification route diterima: pengumuman_id=$pengumumanId"
            )

            return ExternalRoute(route = "pengumuman_detail/$pengumumanId")
        }

        val laporanId = intent?.getStringExtra("laporan_id")
            ?.trim()
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: extractLaporanIdFromLink(intent?.getStringExtra("link_tujuan"))
            ?: return null

        Log.d(
            "MainActivity",
            "Notification route diterima: laporan_id=$laporanId"
        )

        return ExternalRoute(route = "laporan_detail/$laporanId")
    }

    private fun Intent.firstPositiveIntExtra(vararg names: String): Int? {
        for (name in names) {
            val value = getStringExtra(name)
                ?.trim()
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
            if (value != null) return value
        }

        return null
    }

    private fun extractPengumumanIdFromLink(link: String?): Int? {
        val rawLink = link?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val uri = runCatching { Uri.parse(rawLink) }.getOrNull() ?: return null
        val pathSegments = uri.pathSegments
        val pengumumanSegmentIndex = pathSegments.indexOfFirst { segment ->
            segment.equals("pengumuman", ignoreCase = true) ||
                segment.equals("pengumuman_detail", ignoreCase = true) ||
                segment.equals("pengumuman-detail", ignoreCase = true)
        }

        if (pengumumanSegmentIndex >= 0) {
            pathSegments.getOrNull(pengumumanSegmentIndex + 1)
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
                ?.let { return it }
        }

        if (uri.host.equals("pengumuman", ignoreCase = true)) {
            pathSegments.firstOrNull()
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
                ?.let { return it }
        }

        return uri.getQueryParameter("pengumuman_id")
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: uri.getQueryParameter("pengumumanId")
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
    }

    private fun extractLaporanIdFromLink(link: String?): Int? {
        val rawLink = link?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val uri = runCatching { Uri.parse(rawLink) }.getOrNull() ?: return null
        val pathSegments = uri.pathSegments
        val laporanSegmentIndex = pathSegments.indexOfFirst { segment ->
            segment.equals("laporan-kegiatan", ignoreCase = true) ||
                segment.equals("laporan_detail", ignoreCase = true) ||
                segment.equals("laporan-detail", ignoreCase = true)
        }

        if (laporanSegmentIndex >= 0) {
            pathSegments.getOrNull(laporanSegmentIndex + 1)
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
                ?.let { return it }
        }

        return uri.getQueryParameter("laporan_id")
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: uri.getQueryParameter("id")
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
    }

    private fun Uri.queryInt(name: String): Int? {
        return getQueryParameter(name)?.toIntOrNull()
    }
}
