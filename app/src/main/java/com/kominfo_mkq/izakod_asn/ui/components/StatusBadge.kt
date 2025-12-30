package com.kominfo_mkq.izakod_asn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kominfo_mkq.izakod_asn.ui.theme.*

enum class StatusType {
    PENDING,
    APPROVED,
    REJECTED,
    REVISED
}

@Composable
fun StatusBadge(
    status: StatusType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, statusText) = when (status) {
        StatusType.PENDING -> Triple(
            StatusPending.copy(alpha = 0.15f),
            StatusPending,
            "Diajukan"
        )
        StatusType.APPROVED -> Triple(
            StatusApproved.copy(alpha = 0.15f),
            StatusApproved,
            "Disetujui"
        )
        StatusType.REJECTED -> Triple(
            StatusRejected.copy(alpha = 0.15f),
            StatusRejected,
            "Ditolak"
        )
        StatusType.REVISED -> Triple(
            StatusRevised.copy(alpha = 0.15f),
            StatusRevised,
            "Direvisi"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun StatusBadgeLarge(
    status: StatusType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, statusText, icon) = when (status) {
        StatusType.PENDING -> Tuple4(
            StatusPending.copy(alpha = 0.1f),
            StatusPending,
            "Menunggu Persetujuan",
            "⏱️"
        )
        StatusType.APPROVED -> Tuple4(
            StatusApproved.copy(alpha = 0.1f),
            StatusApproved,
            "Telah Disetujui",
            "✓"
        )
        StatusType.REJECTED -> Tuple4(
            StatusRejected.copy(alpha = 0.1f),
            StatusRejected,
            "Ditolak",
            "✕"
        )
        StatusType.REVISED -> Tuple4(
            StatusRevised.copy(alpha = 0.1f),
            StatusRevised,
            "Perlu Direvisi",
            "↻"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}

// Helper data class untuk 4 values
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)