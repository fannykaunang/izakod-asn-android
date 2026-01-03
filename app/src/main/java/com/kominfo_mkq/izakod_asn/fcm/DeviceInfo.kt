package com.kominfo_mkq.izakod_asn.fcm

import android.content.Context
import android.os.Build
import android.provider.Settings

object DeviceInfo {
    fun androidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }

    fun model(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
}
