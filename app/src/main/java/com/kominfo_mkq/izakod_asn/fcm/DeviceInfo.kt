package com.kominfo_mkq.izakod_asn.fcm

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

object DeviceInfo {
    fun androidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }

    fun model(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    fun appVersion(context: Context): String {
        return runCatching {
            val pm = context.packageManager
            val pkg = context.packageName

            val info = if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
            }

            info.versionName ?: "unknown"
        }.getOrDefault("unknown")
    }
}
