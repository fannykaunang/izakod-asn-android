package com.kominfo_mkq.izakod_asn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kominfo_mkq.izakod_asn.data.model.AppVersionPolicy

@Composable
fun AppUpdateDialog(
    policy: AppVersionPolicy,
    isRequired: Boolean,
    onDismissRequest: () -> Unit,
    onSkipClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    val latestVersion = policy.latestVersionName?.takeIf { it.isNotBlank() }
    val title = policy.updateTitle?.takeIf { it.isNotBlank() }
        ?: if (isRequired) "Update aplikasi wajib" else "Update aplikasi tersedia"
    val message = policy.updateMessage?.takeIf { it.isNotBlank() }
        ?: if (isRequired) {
            "Versi aplikasi yang Anda gunakan sudah tidak didukung. Silakan update agar IZAKOD-ASN tetap bisa digunakan dengan aman."
        } else {
            "Versi terbaru IZAKOD-ASN sudah tersedia. Anda bisa update sekarang atau melanjutkan nanti."
        }

    AlertDialog(
        onDismissRequest = {
            if (!isRequired) onDismissRequest()
        },
        icon = {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.NewReleases,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (latestVersion != null) {
                    VersionInfoRow(label = "Versi terbaru", value = latestVersion)
                }

                val notes = policy.releaseNotes?.trim().orEmpty()
                if (notes.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Yang berubah",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpdateClick,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            if (!isRequired) {
                TextButton(onClick = onSkipClick) {
                    Text("Nanti saja")
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun VersionInfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
