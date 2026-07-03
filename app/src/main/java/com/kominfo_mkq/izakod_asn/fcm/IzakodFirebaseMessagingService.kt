package com.kominfo_mkq.izakod_asn.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kominfo_mkq.izakod_asn.MainActivity
import com.kominfo_mkq.izakod_asn.R
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.FcmRegisterRequest
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IzakodFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        scope.launch {
            val prefs = UserPreferences(applicationContext)

            // ✅ simpan token FCM secara aman
            prefs.setMobileFcmToken(token)

            // ✅ pastikan JWT tersedia untuk interceptor
            val jwt = TokenStore.getToken() ?: prefs.getMobileJwtToken()
            if (jwt.isNullOrBlank()) {
                // Belum login / token belum ada -> tidak bisa register sekarang
                return@launch
            }

            // penting: isi TokenStore supaya interceptor menambahkan Bearer
            TokenStore.setToken(jwt)

            val appVersion = DeviceInfo.appVersion(applicationContext)

            val req = FcmRegisterRequest(
                fcm_token = token,
                device_id = DeviceInfo.androidId(applicationContext),
                device_model = DeviceInfo.model(),
                app_version = appVersion
            )

            try {
                val resp = ApiClient.eabsenApiService.registerFcmToken(req)
                if (!resp.isSuccessful) {
                    android.util.Log.w("FCM", "onNewToken register failed: ${resp.code()}")
                } else {
                    android.util.Log.d("FCM", "onNewToken register success")
                }
            } catch (e: Exception) {
                android.util.Log.w("FCM", "onNewToken register exception: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "IZAKOD-ASN"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Ada notifikasi baru"

        showNotification(title, body, message.data)
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val channelId = "izakod_default"
        val pengumumanId = data["pengumuman_id"]
            ?: data["pengumumanId"]
            ?: extractPengumumanIdFromLink(data["link_tujuan"])
        val linkTujuan = data["link_tujuan"] ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_VIEW
            if (!pengumumanId.isNullOrBlank()) {
                setData(Uri.parse("izakod-asn://pengumuman/$pengumumanId"))
            }
            putExtra("notifikasi_id", data["notifikasi_id"] ?: "")
            putExtra("pengumuman_id", pengumumanId ?: "")
            putExtra("laporan_id", data["laporan_id"] ?: "")
            putExtra("link_tujuan", linkTujuan)
            putExtra("tipe_notifikasi", data["tipe_notifikasi"] ?: "")
            putExtra("action_required", data["action_required"] ?: "")
        }

        val requestCode = data["notifikasi_id"]?.toIntOrNull()
            ?: data["pengumuman_id"]?.toIntOrNull()
            ?: data["pengumumanId"]?.toIntOrNull()
            ?: data["laporan_id"]?.toIntOrNull()
            ?: System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (PendingIntent.FLAG_IMMUTABLE)
        )

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId,
                "IZAKOD Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(ch)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            // kalau kamu belum punya ic_notification, sementara pakai launcher:
            .setSmallIcon(R.drawable.ic_notification) // atau R.mipmap.ic_launcher
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun extractPengumumanIdFromLink(link: String?): String? {
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
                ?.takeIf { it.toIntOrNull()?.let { value -> value > 0 } == true }
                ?.let { return it }
        }

        if (uri.host.equals("pengumuman", ignoreCase = true)) {
            pathSegments.firstOrNull()
                ?.takeIf { it.toIntOrNull()?.let { value -> value > 0 } == true }
                ?.let { return it }
        }

        return uri.getQueryParameter("pengumuman_id")
            ?.takeIf { it.toIntOrNull()?.let { value -> value > 0 } == true }
            ?: uri.getQueryParameter("pengumumanId")
                ?.takeIf { it.toIntOrNull()?.let { value -> value > 0 } == true }
    }
}
