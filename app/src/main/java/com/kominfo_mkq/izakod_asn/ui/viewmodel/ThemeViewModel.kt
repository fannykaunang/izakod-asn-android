package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = UserPreferences(app.applicationContext)

    var isDarkTheme by mutableStateOf(prefs.isDarkTheme())
        private set

    // ✅ ganti nama supaya tidak bentrok dengan setter property isDarkTheme
    fun updateDarkTheme(enabled: Boolean) {
        isDarkTheme = enabled
        prefs.setDarkTheme(enabled)
    }
}
