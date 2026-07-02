package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kominfo_mkq.izakod_asn.data.model.PengumumanReadDetail
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.viewmodel.PengumumanViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun PengumumanDetailScreen(
    pengumumanId: Int,
    onNavigateBack: () -> Unit,
    viewModel: PengumumanViewModel = viewModel()
) {
    val uiState by viewModel.detailState.collectAsState()

    LaunchedEffect(pengumumanId) {
        viewModel.loadDetail(pengumumanId)
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Informasi",
                subtitle = uiState.detail?.kategoriKonten ?: "Pengumuman",
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.loadDetail(pengumumanId, force = true) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.isError -> {
                PengumumanErrorContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    message = uiState.errorMessage ?: "Pengumuman tidak bisa dimuat.",
                    onRetry = { viewModel.loadDetail(pengumumanId, force = true) }
                )
            }

            uiState.detail != null -> {
                PengumumanDetailContent(
                    modifier = Modifier.padding(paddingValues),
                    detail = uiState.detail!!
                )
            }
        }
    }
}

@Composable
private fun PengumumanDetailContent(
    modifier: Modifier,
    detail: PengumumanReadDetail
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PengumumanHeroCard(detail = detail)
        }

        detail.ringkasan?.takeIf { it.isNotBlank() }?.let { ringkasan ->
            item {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ringkasan",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = ringkasan,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Isi Informasi",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = detail.isiKonten?.takeIf { it.isNotBlank() }
                            ?: detail.pesan?.takeIf { it.isNotBlank() }
                            ?: "Belum ada isi informasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PengumumanHeroCard(detail: PengumumanReadDetail) {
    val thumbnailUrl = detail.thumbnailUrl.toIzakodAssetUrl()

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (thumbnailUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = detail.judul,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 190.dp, max = 260.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PengumumanChip(detail.kategoriKonten?.takeIf { it.isNotBlank() } ?: "Info")
                    Text(
                        text = formatPengumumanDate(detail.publishedAt ?: detail.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = detail.judul,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                detail.createdByNama?.takeIf { it.isNotBlank() }?.let { author ->
                    Text(
                        text = "Oleh $author",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PengumumanChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PengumumanErrorContent(
    modifier: Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text(text = "Coba lagi")
            }
        }
    }
}

private fun String?.toIzakodAssetUrl(): String? {
    val raw = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
    return ApiClient.BASE_URL.trimEnd('/') + "/" + raw.trimStart('/')
}

private fun formatPengumumanDate(value: String?): String {
    val raw = value?.trim()?.takeIf { it.isNotBlank() } ?: return "-"
    val outputFormatter = SimpleDateFormat("dd MMM yyyy, HH.mm", Locale("id", "ID"))
    val inputFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    inputFormats.forEach { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                if (pattern.contains("'Z'")) {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            }.parse(raw)
        }.getOrNull()?.let { return outputFormatter.format(it) }
    }

    return raw
}
