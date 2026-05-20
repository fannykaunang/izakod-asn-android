package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanTargetRelationSection(
    targetKinerjaList: List<TargetKinerjaItem>,
    selectedTargetKinerjaId: String,
    selectedTargetKinerjaDetailId: String,
    isLoading: Boolean,
    onTargetChange: (String) -> Unit,
    onDetailChange: (String) -> Unit
) {
    val selectedTarget = targetKinerjaList.firstOrNull {
        it.id.toString() == selectedTargetKinerjaId
    }
    val targetDetails = selectedTarget?.details.orEmpty().filter { it.id != null }
    val selectedDetail = targetDetails.firstOrNull {
        it.id?.toString() == selectedTargetKinerjaDetailId
    }

    var targetExpanded by remember { mutableStateOf(false) }
    var detailExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Relasi Target Kinerja",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = "Opsional, hubungkan laporan ke target dan detail kinerja periode tanggal kegiatan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            ExposedDropdownMenuBox(
                expanded = targetExpanded,
                onExpandedChange = {
                    if (!isLoading) targetExpanded = it
                }
            ) {
                OutlinedTextField(
                    value = selectedTarget?.let { formatTargetLabel(it) }.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Kinerja") },
                    placeholder = {
                        Text(
                            if (isLoading) "Memuat target..." else "Tidak dikaitkan"
                        )
                    },
                    trailingIcon = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !isLoading
                )

                ExposedDropdownMenu(
                    expanded = targetExpanded,
                    onDismissRequest = { targetExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tidak dikaitkan") },
                        onClick = {
                            onTargetChange("")
                            targetExpanded = false
                        }
                    )

                    targetKinerjaList.forEach { target ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = formatTargetLabel(target),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = {
                                onTargetChange(target.id.toString())
                                targetExpanded = false
                            }
                        )
                    }
                }
            }

            if (!isLoading && targetKinerjaList.isEmpty()) {
                Text(
                    text = "Belum ada target kinerja disetujui/final untuk periode tanggal kegiatan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = detailExpanded,
                onExpandedChange = {
                    if (selectedTarget != null && targetDetails.isNotEmpty()) {
                        detailExpanded = it
                    }
                }
            ) {
                OutlinedTextField(
                    value = selectedDetail?.uraianTarget.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Detail Target") },
                    placeholder = {
                        Text(
                            when {
                                selectedTarget == null -> "Pilih target terlebih dahulu"
                                targetDetails.isEmpty() -> "Target ini belum punya detail"
                                else -> "Pilih detail target"
                            }
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = detailExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = selectedTarget != null && targetDetails.isNotEmpty()
                )

                ExposedDropdownMenu(
                    expanded = detailExpanded,
                    onDismissRequest = { detailExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tanpa detail target") },
                        onClick = {
                            onDetailChange("")
                            detailExpanded = false
                        }
                    )

                    targetDetails.forEach { detail ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = detail.uraianTarget,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = {
                                onDetailChange(detail.id?.toString().orEmpty())
                                detailExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatTargetLabel(target: TargetKinerjaItem): String {
    return "${monthName(target.bulan)} ${target.tahun} - ${formatTargetStatus(target.status)}"
}

private fun formatTargetStatus(status: String): String {
    return when (status.lowercase(Locale.getDefault())) {
        "disetujui" -> "Disetujui"
        "final" -> "Final"
        else -> status.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}

private fun monthName(month: Int): String {
    return when (month) {
        1 -> "Januari"
        2 -> "Februari"
        3 -> "Maret"
        4 -> "April"
        5 -> "Mei"
        6 -> "Juni"
        7 -> "Juli"
        8 -> "Agustus"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        12 -> "Desember"
        else -> "Bulan $month"
    }
}
