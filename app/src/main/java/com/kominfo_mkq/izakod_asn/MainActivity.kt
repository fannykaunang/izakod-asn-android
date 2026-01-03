package com.kominfo_mkq.izakod_asn

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import com.kominfo_mkq.izakod_asn.ui.navigation.IZAKODNavigation
import com.kominfo_mkq.izakod_asn.ui.navigation.Screen
import com.kominfo_mkq.izakod_asn.ui.theme.IZAKODASNTheme


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = UserPreferences(this).getMobileJwtToken()
        TokenStore.setToken(token) // boleh null

        enableEdgeToEdge()

        userPrefs = UserPreferences(this)

        setContent {
            // ✅ Theme state global (dibaca dari EncryptedSharedPreferences)
            var isDarkTheme by remember { mutableStateOf(userPrefs.isDarkTheme()) }

            // ✅ startDestination tetap seperti sebelumnya
            val startDestination = remember { checkAndRestoreSession() }

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

                        // ✅ callback untuk Settings toggle
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { enabled ->
                            isDarkTheme = enabled
                            userPrefs.setDarkTheme(enabled) // simpan preferensi
                        }
                    )
                }
            }
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
}
