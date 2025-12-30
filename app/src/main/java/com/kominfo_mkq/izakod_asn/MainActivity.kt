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

/**
 * MainActivity with Auto-Login Support
 * FIXED: Properly restore session when app is reopened
 */
class MainActivity : ComponentActivity() {

    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Initialize UserPreferences
        userPrefs = UserPreferences(this)

        setContent {
            IZAKODASNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ✅ Check login status and restore session
                    val startDestination = remember {
                        checkAndRestoreSession()
                    }

                    IZAKODNavigation(
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    /**
     * Check if user is logged in and restore session data
     */
    private fun checkAndRestoreSession(): String {
        val isLoggedIn = userPrefs.isLoggedIn()

        return if (isLoggedIn) {
            // ✅ Restore session data to StatistikRepository
            val sessionData = userPrefs.getSessionData()
            sessionData?.let {
                StatistikRepository.setUserData(
                    pegawaiId = it.pegawaiId,
                    pin = it.pin
                )
                android.util.Log.d("MainActivity", "✅ Session restored: pegawai_id=${it.pegawaiId}, pin=${it.pin}")
            }

            // Start at Dashboard
            Screen.Dashboard.route
        } else {
            android.util.Log.d("MainActivity", "❌ No session found, showing Login")
            // Start at Login
            Screen.Login.route
        }
    }

    override fun onResume() {
        super.onResume()
        // ✅ Re-check and restore session when app comes to foreground
        if (userPrefs.isLoggedIn()) {
            val sessionData = userPrefs.getSessionData()
            sessionData?.let {
                StatistikRepository.setUserData(it.pegawaiId, it.pin)
                android.util.Log.d("MainActivity", "✅ Session restored on resume")
            }
        }
    }
}