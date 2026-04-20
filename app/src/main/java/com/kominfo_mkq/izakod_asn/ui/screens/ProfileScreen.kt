package com.kominfo_mkq.izakod_asn.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kominfo_mkq.izakod_asn.BuildConfig
import com.kominfo_mkq.izakod_asn.R
import com.kominfo_mkq.izakod_asn.data.local.TokenStore
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private fun String?.displayOrDash(): String = this?.takeIf { it.isNotBlank() } ?: "-"

private fun String?.formatDateIndonesianOrDash(): String {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return "-"

    val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    val inputPatterns = listOf(
        "yyyy-MM-dd",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    )

    for (pattern in inputPatterns) {
        runCatching {
            val parser = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
            parser.parse(raw)
        }.getOrNull()?.let { parsedDate ->
            return outputFormat.format(parsedDate)
        }
    }

    return raw
}

/* =========================
   PROFILE SCREEN
   ========================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackToDashboard: () -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    isRootTab: Boolean = false,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // prefs
    val userPrefs = remember { UserPreferences(context) }
    val pin = userPrefs.getPin()

    // dialog state (HANYA di Profile)
    var showLogoutDialog by remember { mutableStateOf(false) }

    // load profile hanya sekali (tidak reload saat balik tab)
    LaunchedEffect(pin) {
        if (!pin.isNullOrBlank()) {
            viewModel.loadProfile(pin) // jangan reset dulu
        } else {
            viewModel.clearProfile()   // jika benar2 logout / pin kosong
        }
    }

    // Back selalu ke dashboard
    if (!isRootTab) {
        BackHandler { onBackToDashboard() }
    }

    // fungsi logout dipusatkan biar tidak error scope
    val doLogout = remember {
        {
            viewModel.resetProfile()
            userPrefs.clearSession()
            TokenStore.setToken(null)
            TokenStore.setRefreshToken(null)
            StatistikRepository.clearData()
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            AccountTopBar(
                title = "Akun",
                onBack = if (isRootTab) null else onBackToDashboard
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                pin.isNullOrBlank() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Silakan login.")
                    }
                }

                uiState.profile != null -> {
                    val profile = uiState.profile!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = if (isRootTab) 120.dp else 32.dp)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AccountProfileHero(
                            photoUrl = uiState.photoUrl,
                            nama = profile.pegawaiNama.displayOrDash(),
                            nip = profile.pegawaiNip.displayOrDash(),
                            jabatan = profile.jabatan.displayOrDash()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AccountSectionLabel("Akun Saya")
                        ProfileOverviewSection(profile)

                        Spacer(modifier = Modifier.height(14.dp))

                        AccountSectionLabel("Informasi Pegawai")
                        ProfileDetailSection(profile)

                        Spacer(modifier = Modifier.height(14.dp))

                        AccountSectionLabel("Pengaturan")
                        SettingsSwitchSection(
                            items = listOf(
                                SettingSwitchItem(
                                    icon = Icons.Outlined.DarkMode,
                                    title = "Mode Gelap",
                                    subtitle = "Sesuaikan tampilan aplikasi",
                                    checked = isDarkTheme,
                                    onCheckedChange = onToggleTheme
                                ),
                                SettingSwitchItem(
                                    icon = Icons.Outlined.Notifications,
                                    title = "Pengaturan Notifikasi",
                                    subtitle = "Kelola notifikasi aplikasi",
                                    checked = uiState.notificationsEnabled,
                                    onCheckedChange = { viewModel.setNotifications(it) }
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AccountSectionLabel("Aplikasi")
                        SettingsActionSection(
                            items = listOf(
                                SettingActionItem(
                                    icon = Icons.Outlined.SystemUpdate,
                                    title = "Cek Pembaruan",
                                    subtitle = "Buka halaman aplikasi di Google Play"
                                ) { openPlayStoreListing(context) },
                                SettingActionItem(
                                    icon = Icons.Outlined.StarRate,
                                    title = "Beri Rating",
                                    subtitle = "Nilai aplikasi IZAKOD-ASN"
                                ) { openPlayStoreListing(context) },
                                SettingActionItem(
                                    icon = Icons.Outlined.Public,
                                    title = "Website",
                                    subtitle = "Website resmi IZAKOD-ASN"
                                ) { openOfficialWebsite(context) },
                                SettingActionItem(
                                    icon = Icons.Default.Settings,
                                    title = "Versi Aplikasi",
                                    subtitle = "IZAKOD-ASN ${BuildConfig.VERSION_NAME}",
                                    showChevron = false
                                ) {}
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AccountSectionLabel("Sesi")
                        SimpleActionSection(
                            items = listOf(
                                SettingSimpleAction(
                                    title = "Logout",
                                    subtitle = "Keluar dari aplikasi IZAKOD-ASN",
                                    tint = MaterialTheme.colorScheme.error
                                ) { showLogoutDialog = true }
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                uiState.isLoading -> ProfileLoadingContent()
                uiState.isError -> {
                    ProfileErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { pin.let { viewModel.loadProfile(it) } }
                    )
                }

                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data profil.")
                    }
                }
            }
        }
    }

    // ✅ Dialog logout HANYA di ProfileScreen
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        doLogout()
                    }
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

/* =========================
   SETTINGS SCREEN
   (TIDAK ADA logout dialog di sini)
   ========================= */

/* =========================
   SMALL REUSABLE UI
   ========================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountTopBar(
    title: String,
    onBack: (() -> Unit)? = null
) {
    IZAKODHeaderBar(
        title = title,
        onBack = onBack
    )
}

@Composable
private fun AccountProfileHero(
    photoUrl: String?,
    nama: String,
    nip: String,
    jabatan: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = PrimaryLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .build(),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = PrimaryLight.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = nama,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "NIP: $nip",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = jabatan,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AccountSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun ProfileOverviewSection(profile: PegawaiProfile) {
    SettingsActionSection(
        items = listOf(
            SettingActionItem(
                icon = Icons.Default.Person,
                title = "Nama Lengkap",
                subtitle = profile.pegawaiNama.displayOrDash(),
                showChevron = false
            ) {},
            SettingActionItem(
                icon = Icons.Default.Work,
                title = "Jabatan",
                subtitle = profile.jabatan.displayOrDash(),
                showChevron = false
            ) {},
            SettingActionItem(
                icon = Icons.Default.Phone,
                title = "No. Telepon",
                subtitle = profile.pegawaiTelp.displayOrDash(),
                showChevron = false
            ) {}
        )
    )
}

@Composable
private fun ProfileDetailSection(profile: PegawaiProfile) {
    val detailItems = buildList {
        add(
            SettingActionItem(
                icon = Icons.Default.Person,
                title = "NIP / NIK",
                subtitle = profile.pegawaiNip.displayOrDash(),
                showChevron = false
            ) {}
        )
        add(
            SettingActionItem(
                icon = Icons.Default.Work,
                title = "SKPD",
                subtitle = profile.skpd.displayOrDash(),
                showChevron = false
            ) {}
        )
        add(
            SettingActionItem(
                icon = Icons.Default.Person,
                title = "Jenis Kelamin",
                subtitle = if (profile.gender == 1) "Laki-laki" else "Perempuan",
                showChevron = false
            ) {}
        )
        add(
            SettingActionItem(
                icon = Icons.Default.Settings,
                title = "Status Kepegawaian",
                subtitle = if (profile.pegawaiStatus == 1) "Aktif" else "Tidak Aktif",
                showChevron = false
            ) {}
        )
        if (!profile.tempatLahir.isNullOrBlank() || !profile.tglLahir.isNullOrBlank()) {
                        add(
                            SettingActionItem(
                                icon = Icons.Default.Person,
                                title = "Tempat / Tanggal Lahir",
                                subtitle = listOf(
                                    profile.tempatLahir.displayOrDash(),
                                    profile.tglLahir.formatDateIndonesianOrDash()
                                ).joinToString(", "),
                                showChevron = false
                            ) {}
                        )
        }
    }

    SettingsActionSection(items = detailItems)
}

private data class SettingActionItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val tint: Color? = null,
    val showChevron: Boolean = true,
    val onClick: () -> Unit
)

private data class SettingSwitchItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

@Composable
private fun SettingsActionSection(
    items: List<SettingActionItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                SettingListRow(
                    icon = item.icon,
                    title = item.title,
                    subtitle = item.subtitle,
                    tint = item.tint,
                    showChevron = item.showChevron,
                    onClick = item.onClick
                )

                if (index != items.lastIndex) {
                    DividerLike()
                }
            }
        }
    }
}

@Composable
private fun SimpleActionSection(
    items: List<SettingSimpleAction>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                SimpleActionRow(item = item)
                if (index != items.lastIndex) {
                    DividerLike()
                }
            }
        }
    }
}

private data class SettingSimpleAction(
    val title: String,
    val subtitle: String,
    val tint: Color? = null,
    val onClick: () -> Unit
)

@Composable
private fun SimpleActionRow(item: SettingSimpleAction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = item.tint ?: MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = item.tint ?: MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text("Memuat profil...")
        }
    }
}

@Composable
private fun ProfileErrorContent(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun SettingsSwitchSection(
    items: List<SettingSwitchItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                SettingsSwitchRow(item = item)
                if (index != items.lastIndex) {
                    DividerLike()
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(item: SettingSwitchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = item.checked, onCheckedChange = item.onCheckedChange)
    }
}

@Composable
private fun SettingListRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = tint ?: MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DividerLike() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 52.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    )
}

private fun openPlayStoreListing(context: Context) {
    val pkg = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri()))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$pkg".toUri()))
    }
}

private fun openOfficialWebsite(context: Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, "https://izakod-asn.merauke.go.id".toUri()))
}
