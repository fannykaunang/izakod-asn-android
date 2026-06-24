package com.kominfo_mkq.izakod_asn.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.MobileSsoTarget
import com.kominfo_mkq.izakod_asn.data.repository.AuthRepository
import com.kominfo_mkq.izakod_asn.data.repository.LoginSessionPostSetup
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import org.json.JSONObject

private const val MOBILE_SSO_PAYROLL_LOG_TAG = "IZAKOD_SSO_PAYROLL"

@Composable
fun MobileSsoBridgeScreen(
    ticket: String,
    fallbackRoute: String?,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val context = LocalContext.current.applicationContext
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(ticket) {
        if (ticket.isBlank()) {
            onFailure("Tiket login otomatis IZAKOD-ASN tidak ditemukan.")
            return@LaunchedEffect
        }

        val result = runCatching {
            val response = AuthRepository().exchangeMobileSsoTicket(ticket)
            val body = response.body()
            val data = body?.data
            val token = data?.token?.trim()
            val user = data?.user
            val pegawaiId = user?.pegawaiId ?: 0
            val pin = user?.pin?.trim().orEmpty()
            val entagoAccessToken = data?.entagoAccessToken?.trim().orEmpty()
            val entagoRefreshToken = data?.entagoRefreshToken?.trim().orEmpty()

            Log.d(
                MOBILE_SSO_PAYROLL_LOG_TAG,
                "SSO exchange response: http=${response.code()}, success=${body?.success}, " +
                    "hasData=${data != null}, hasUser=${user != null}, " +
                    "hasPayrollEstimate=${data?.payrollEstimate != null}, " +
                    "targetJenis=${data?.target?.jenis}, targetTahun=${data?.target?.tahun}, " +
                    "targetBulan=${data?.target?.bulan}, targetRoute=${data?.target?.route}"
            )

            if (!response.isSuccessful || body?.success != true || token.isNullOrBlank() || pegawaiId <= 0 || pin.isBlank()) {
                throw IllegalStateException(
                    body?.message?.takeIf { it.isNotBlank() }
                        ?: parseSsoExchangeError(response.errorBody()?.string())
                        ?: "Login otomatis IZAKOD-ASN gagal."
                )
            }

            if (entagoAccessToken.isBlank() || entagoRefreshToken.isBlank()) {
                throw IllegalStateException(
                    "Sesi E-NTAGO dari login otomatis belum lengkap. Silakan login ulang di E-NTAGO lalu coba lagi."
                )
            }

            val userPrefs = UserPreferences(context)
            userPrefs.clearEntagoTokens()
            userPrefs.saveSession(
                email = user?.email.orEmpty(),
                pin = pin,
                level = user?.level ?: 0,
                skpdid = user?.skpdid ?: 0,
                pegawaiId = pegawaiId
            )
            userPrefs.setEntagoAccessToken(entagoAccessToken)
            userPrefs.setEntagoRefreshToken(entagoRefreshToken)
            userPrefs.setMobileJwtToken(token)
            userPrefs.setRefreshToken(data.refreshToken?.trim())
            userPrefs.saveProfileSnapshot(user)
            val payrollCacheJson = buildSsoPayrollEstimateCacheJson(
                payrollEstimate = data.payrollEstimate,
                target = data.target
            )
            Log.d(
                MOBILE_SSO_PAYROLL_LOG_TAG,
                "Saving SSO payroll estimate cache: hasCache=${payrollCacheJson != null}, " +
                    "cacheLength=${payrollCacheJson?.length ?: 0}, targetJenis=${data.target?.jenis}, " +
                    "targetTahun=${data.target?.tahun}, targetBulan=${data.target?.bulan}"
            )
            userPrefs.saveSsoPayrollEstimate(payrollCacheJson)

            TokenStore.setToken(token)
            TokenStore.setRefreshToken(data.refreshToken?.trim())
            StatistikRepository.setUserData(pegawaiId = pegawaiId, pin = pin)
            LoginSessionPostSetup.registerFcmTokenIfPossible(context)

            data.target?.route?.takeIf { it.isNotBlank() }
                ?: fallbackRoute?.takeIf { it.isNotBlank() }
                ?: "dashboard"
        }

        result
            .onSuccess { route -> onSuccess(route) }
            .onFailure { error ->
                onFailure(
                    error.message
                        ?: "Sesi otomatis tidak dapat dibuat. Silakan login kembali."
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = cs.surface),
            border = BorderStroke(1.dp, cs.outlineVariant.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Membuka IZAKOD-ASN",
                    color = cs.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sedang menyiapkan sesi aman dari E-NTAGO.",
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun parseSsoExchangeError(raw: String?): String? {
    return try {
        val json = JSONObject(raw ?: "{}")
        val message = json.optString("message").ifBlank {
            json.optString("response")
        }
        message.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}

private fun buildSsoPayrollEstimateCacheJson(
    payrollEstimate: JsonObject?,
    target: MobileSsoTarget?
): String? {
    val estimate = payrollEstimate ?: return null

    return runCatching {
        JSONObject().apply {
            put("payroll_estimate", JSONObject(estimate.toString()))
            if (target != null) {
                put(
                    "target",
                    JSONObject().apply {
                        put("jenis", target.jenis)
                        put("tahun", target.tahun)
                        put("bulan", target.bulan)
                        put("route", target.route)
                    }
                )
            }
        }.toString()
    }.getOrElse { error ->
        Log.w(
            MOBILE_SSO_PAYROLL_LOG_TAG,
            "Failed wrapping payroll estimate cache, saving raw estimate: ${error.message}"
        )
        estimate.toString()
    }
}
