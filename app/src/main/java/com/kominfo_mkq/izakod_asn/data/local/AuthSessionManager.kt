package com.kominfo_mkq.izakod_asn.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthSessionManager {
    const val SESSION_EXPIRED_MESSAGE = "Sesi Anda berakhir, silakan login kembali."

    private val _sessionExpiredEvents = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpiredEvents: SharedFlow<String> = _sessionExpiredEvents.asSharedFlow()

    fun expireSession(context: Context?, message: String = SESSION_EXPIRED_MESSAGE) {
        context?.applicationContext?.let { appContext ->
            UserPreferences(appContext).clearSession()
        }
        TokenStore.setToken(null)
        TokenStore.setRefreshToken(null)
        _sessionExpiredEvents.tryEmit(message)
    }
}
