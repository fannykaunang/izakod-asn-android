package com.kominfo_mkq.izakod_asn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import com.kominfo_mkq.izakod_asn.navigation.IZAKODNavigation
import com.kominfo_mkq.izakod_asn.navigation.Screen
import com.kominfo_mkq.izakod_asn.ui.theme.IZAKODASNTheme

class MainActivity : ComponentActivity() {

    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                android.util.Log.d(
                    "MainActivity",
                    "✅ Session restored: pegawai_id=${it.pegawaiId}, pin=${it.pin}"
                )
            }
            Screen.Dashboard.route
        } else {
            android.util.Log.d("MainActivity", "❌ No session found, showing Login")
            Screen.Login.route
        }
    }

    override fun onResume() {
        super.onResume()
        if (userPrefs.isLoggedIn()) {
            val sessionData = userPrefs.getSessionData()
            sessionData?.let {
                StatistikRepository.setUserData(it.pegawaiId, it.pin)
                android.util.Log.d("MainActivity", "✅ Session restored on resume")
            }
        }
    }
}
