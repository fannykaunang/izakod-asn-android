package com.kominfo_mkq.izakod_asn.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.text.style.URLSpan
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
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
    if (detail.isWithdrawn) {
        PengumumanWithdrawnContent(modifier = modifier, detail = detail)
        return
    }
    val publicUrl = detail.publicWebUrl()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PengumumanHeroCard(
                detail = detail,
                publicUrl = publicUrl
            )
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
                    PengumumanHtmlText(
                        value = detail.isiKonten?.takeIf { it.isNotBlank() }
                            ?: detail.pesan?.takeIf { it.isNotBlank() }
                            ?: "Belum ada isi informasi."
                    )
                }
            }
        }
    }
}

@Composable
private fun PengumumanWithdrawnContent(
    modifier: Modifier,
    detail: PengumumanReadDetail
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = "Sudah ditarik",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = detail.judul,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = detail.withdrawnReason?.takeIf { it.isNotBlank() }
                            ?: detail.ringkasan?.takeIf { it.isNotBlank() }
                            ?: "Pengumuman ini sudah ditarik oleh admin dan tidak lagi tersedia sebagai informasi aktif.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Waktu tarik: ${formatPengumumanDate(detail.withdrawnAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PengumumanWebActions(
    detail: PengumumanReadDetail,
    publicUrl: String
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { openPengumumanPublicUrl(context, publicUrl) },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.OpenInBrowser,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text("Buka Web", maxLines = 1)
        }
        Button(
            onClick = { sharePengumumanPublicUrl(context, detail, publicUrl) },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text("Bagikan", maxLines = 1)
        }
    }
}

@Composable
private fun PengumumanHtmlText(value: String) {
    val uriHandler = LocalUriHandler.current
    val linkColor = MaterialTheme.colorScheme.primary
    val annotatedText = remember(value, linkColor) {
        buildPengumumanAnnotatedContent(value, linkColor)
    }

    ClickableText(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = { offset ->
            annotatedText
                .getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { annotation ->
                    runCatching { uriHandler.openUri(annotation.item) }
                }
        }
    )
}

private fun buildPengumumanAnnotatedContent(
    value: String,
    linkColor: Color
): AnnotatedString {
    val spanned = parsePengumumanHtml(value)
    val rawText = spanned.toString()
    val contentStart = rawText.indexOfFirst { !it.isWhitespace() }.takeIf { it >= 0 } ?: 0
    val contentEnd = rawText.indexOfLast { !it.isWhitespace() }.takeIf { it >= 0 }
        ?.plus(1)
        ?: rawText.length
    val text = rawText.substring(contentStart, contentEnd)
    val builder = AnnotatedString.Builder(text)

    spanned.getSpans(0, spanned.length, StyleSpan::class.java).forEach { span ->
        builder.addPengumumanSpanStyle(spanned, span, contentStart, text.length) {
            when (span.style) {
                Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                Typeface.BOLD_ITALIC -> SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )

                else -> null
            }
        }
    }

    spanned.getSpans(0, spanned.length, RelativeSizeSpan::class.java).forEach { span ->
        builder.addPengumumanSpanStyle(spanned, span, contentStart, text.length) {
            SpanStyle(
                fontSize = (16f * span.sizeChange)
                    .coerceIn(12f, 28f)
                    .sp
            )
        }
    }

    spanned.getSpans(0, spanned.length, AbsoluteSizeSpan::class.java).forEach { span ->
        builder.addPengumumanSpanStyle(spanned, span, contentStart, text.length) {
            SpanStyle(
                fontSize = span.size
                    .toFloat()
                    .coerceIn(12f, 30f)
                    .sp
            )
        }
    }

    spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java).forEach { span ->
        builder.addPengumumanSpanStyle(spanned, span, contentStart, text.length) {
            SpanStyle(color = Color(span.foregroundColor))
        }
    }

    spanned.getSpans(0, spanned.length, UnderlineSpan::class.java).forEach { span ->
        builder.addPengumumanSpanStyle(spanned, span, contentStart, text.length) {
            SpanStyle(textDecoration = TextDecoration.Underline)
        }
    }

    spanned.getSpans(0, spanned.length, StrikethroughSpan::class.java).forEach { span ->
        builder.addPengumumanSpanStyle(spanned, span, contentStart, text.length) {
            SpanStyle(textDecoration = TextDecoration.LineThrough)
        }
    }

    spanned.getSpans(0, spanned.length, URLSpan::class.java).forEach { span ->
        val start = (spanned.getSpanStart(span) - contentStart).coerceIn(0, text.length)
        val end = (spanned.getSpanEnd(span) - contentStart).coerceIn(start, text.length)
        val url = span.url?.trim().orEmpty()

        if (url.isNotBlank() && start < end) {
            builder.addStyle(
                style = SpanStyle(
                    color = linkColor,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end
            )
            builder.addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = start,
                end = end
            )
        }
    }

    return builder.toAnnotatedString()
}

private fun AnnotatedString.Builder.addPengumumanSpanStyle(
    spanned: Spanned,
    span: Any,
    contentStart: Int,
    textLength: Int,
    styleFactory: () -> SpanStyle?
) {
    val start = (spanned.getSpanStart(span) - contentStart).coerceIn(0, textLength)
    val end = (spanned.getSpanEnd(span) - contentStart).coerceIn(start, textLength)
    val style = styleFactory()

    if (style != null && start < end) {
        addStyle(style = style, start = start, end = end)
    }
}

private fun parsePengumumanHtml(value: String): Spanned {
    val tagRegex = Regex("""</?[a-z][\s\S]*>""", RegexOption.IGNORE_CASE)
    val decodedOnce = HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    val source = when {
        tagRegex.containsMatchIn(value) -> value
        tagRegex.containsMatchIn(decodedOnce) -> decodedOnce
        else -> value
    }

    return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

@Composable
private fun PengumumanHeroCard(
    detail: PengumumanReadDetail,
    publicUrl: String?
) {
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
                        .height(198.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(198.dp)
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

                publicUrl?.let { url ->
                    PengumumanWebActions(
                        detail = detail,
                        publicUrl = url
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
    if (raw.startsWith("http://izakod-asn.merauke.go.id/", ignoreCase = true)) {
        return raw.replaceFirst("http://", "https://", ignoreCase = true)
    }
    if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
    return ApiClient.BASE_URL.trimEnd('/') + "/" + raw.trimStart('/')
}

private fun PengumumanReadDetail.publicWebUrl(): String? {
    publicUrl?.trim()?.takeIf { it.isNotBlank() }?.let {
        return it.toIzakodAssetUrl() ?: it
    }
    val slug = publicSlug?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return "${ApiClient.BASE_URL.trimEnd('/')}/informasi/${slug}"
}

private fun openPengumumanPublicUrl(context: Context, publicUrl: String) {
    val intent = Intent(Intent.ACTION_VIEW, publicUrl.toUri())
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(
                context,
                "Tidak ada aplikasi untuk membuka link.",
                Toast.LENGTH_SHORT
            ).show()
        }
}

private fun copyPengumumanPublicUrl(context: Context, publicUrl: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Link informasi IZAKOD-ASN", publicUrl))
    Toast.makeText(context, "Link informasi disalin.", Toast.LENGTH_SHORT).show()
}

private fun sharePengumumanPublicUrl(
    context: Context,
    detail: PengumumanReadDetail,
    publicUrl: String
) {
    val shareText = buildString {
        append(detail.judul)
        detail.ringkasan?.trim()?.takeIf { it.isNotBlank() }?.let {
            append("\n\n")
            append(it)
        }
        append("\n\n")
        append(publicUrl)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, detail.judul)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Bagikan informasi"))
    }.onFailure {
        Toast.makeText(
            context,
            "Tidak ada aplikasi untuk membagikan link.",
            Toast.LENGTH_SHORT
        ).show()
    }
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
