package com.kominfo_mkq.izakod_asn.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kominfo_mkq.izakod_asn.R
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.viewmodel.ProfileViewModel
import com.kominfo_mkq.izakod_asn.BuildConfig
import androidx.core.net.toUri

enum class ProfileTab(val title: String) { PROFILE("Profil"), SETTINGS("Settings") }

/* =========================
   PROFILE SCREEN
   ========================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackToDashboard: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // prefs
    val userPrefs = remember { UserPreferences(context) }
    val pin = remember { userPrefs.getPin() }

    // dialog state (HANYA di Profile)
    var showLogoutDialog by remember { mutableStateOf(false) }

    // load profile hanya sekali (tidak reload saat balik tab)
    LaunchedEffect(pin, uiState.profile, uiState.isLoading) {
        if (pin != null && uiState.profile == null && !uiState.isLoading) {
            viewModel.loadProfile(pin)
        }
    }

    // Back selalu ke dashboard
    BackHandler { onBackToDashboard() }

    // fungsi logout dipusatkan biar tidak error scope
    val doLogout = remember {
        {
            userPrefs.clearSession()
            StatistikRepository.clearData()
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            AccountTopBar(
                title = "Akun",
                onBack = onBackToDashboard
            )
        },
        bottomBar = {
            // Sticky logout hanya di tab Profil
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AccountTabs(
                selectedTab = ProfileTab.PROFILE,
                onClickProfile = { /* tetap di sini */ },
                onClickSettings = onNavigateToSettings
            )

            when {
                uiState.isLoading -> ProfileLoadingContent()

                uiState.isError -> {
                    ProfileErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { pin?.let { viewModel.loadProfile(it) } }
                    )
                }

                uiState.profile != null -> {
                    val profile = uiState.profile!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            // supaya konten tidak ketutup sticky logout
                            .padding(bottom = 96.dp)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        ProfileHeader(
                            photoUrl = uiState.photoUrl,
                            nama = profile.pegawaiNama,
                            nip = profile.pegawaiNip,
                            jabatan = profile.jabatan ?: "-"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Field profil tetap (tidak diubah)
                        PersonalInfoSection(profile)

                        Spacer(modifier = Modifier.height(12.dp))

                        WorkInfoSection(profile)

                        Spacer(modifier = Modifier.height(12.dp))

                        ContactInfoSection(profile)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
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
                        showLogoutDialog = false
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackToDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler { onBackToDashboard() }

    Scaffold(
        topBar = {
            AccountTopBar(
                title = "Akun",
                onBack = onBackToDashboard
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AccountTabs(
                selectedTab = ProfileTab.SETTINGS,
                onClickProfile = onNavigateToProfile,
                onClickSettings = { /* tetap di sini */ }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Tampilan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        SettingSwitchRow(
                            icon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                            title = "Mode Gelap",
                            subtitle = "Aktifkan tema gelap",
                            checked = isDarkTheme,
                            onCheckedChange = onToggleTheme
                        )
                    }
                }

                ElevatedCard {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Notifikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        SettingSwitchRow(
                            icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                            title = "Notifikasi",
                            subtitle = "Aktifkan notifikasi aplikasi",
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotifications(it) }
                        )
                    }
                }

                ElevatedCard {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Aplikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        SettingActionRow(
                            icon = { Icon(Icons.Outlined.SystemUpdate, contentDescription = null) },
                            title = "Cek Pembaruan",
                            subtitle = "Buka halaman aplikasi di Google Play",
                            onClick = { openPlayStoreListing(context) }
                        )

                        SettingActionRow(
                            icon = { Icon(Icons.Outlined.StarRate, contentDescription = null) },
                            title = "Beri Rating",
                            subtitle = "Beri ulasan di Google Play",
                            onClick = { openPlayStoreListing(context) }
                        )

                        SettingActionRow(
                            icon = { Icon(Icons.Outlined.Public, contentDescription = null) },
                            title = "Website",
                            subtitle = "Website resmi IZAKOD-ASN",
                            onClick = { openOfficialWebsite(context) }
                        )
                    }
                }

                ElevatedCard {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Tentang",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Integrasi Laporan Kegiatan Online Digital-ASN \nIZAKOD-ASN Versi ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/* =========================
   SMALL REUSABLE UI
   ========================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountTopBar(
    title: String,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            }
        }
    )
}

@Composable
private fun AccountTabs(
    selectedTab: ProfileTab,
    onClickProfile: () -> Unit,
    onClickSettings: () -> Unit
) {
    val selectedIndex = if (selectedTab == ProfileTab.PROFILE) 0 else 1

    TabRow(selectedTabIndex = selectedIndex) {
        Tab(
            selected = selectedTab == ProfileTab.PROFILE,
            onClick = onClickProfile,
            text = { Text(ProfileTab.PROFILE.title) }
        )
        Tab(
            selected = selectedTab == ProfileTab.SETTINGS,
            onClick = onClickSettings,
            text = { Text(ProfileTab.SETTINGS.title) }
        )
    }
}

/* =========================
   PROFILE CONTENT (tetap)
   ========================= */

@Composable
private fun ProfileHeader(
    photoUrl: String?,
    nama: String,
    nip: String,
    jabatan: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryLight)
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
private fun PersonalInfoSection(profile: PegawaiProfile) {
    InfoCard(title = "Informasi Pribadi", icon = Icons.Default.Person) {
        InfoRow(label = "Nama Lengkap", value = profile.pegawaiNama)
        InfoRow(label = "NIP", value = profile.pegawaiNip)
        InfoRow(label = "PIN", value = profile.pegawaiPin)

        if (!profile.tempatLahir.isNullOrEmpty()) InfoRow(label = "Tempat Lahir", value = profile.tempatLahir)
        if (profile.tglLahir != null && profile.tglLahir != "0") InfoRow(label = "Tanggal Lahir", value = profile.tglLahir)

        InfoRow(label = "Jenis Kelamin", value = if (profile.gender == 1) "Laki-laki" else "Perempuan")
        InfoRow(label = "Status", value = if (profile.pegawaiStatus == 1) "Aktif" else "Tidak Aktif")
    }
}

@Composable
private fun WorkInfoSection(profile: PegawaiProfile) {
    InfoCard(title = "Informasi Kepegawaian", icon = Icons.Default.Work) {
        if (profile.jabatan != null) InfoRow(label = "Jabatan", value = profile.jabatan)
        if (profile.skpd != null) InfoRow(label = "SKPD", value = profile.skpd)
        if (profile.sotk != null) InfoRow(label = "SOTK", value = profile.sotk)
        if (profile.tglMulaiKerja != null) InfoRow(label = "Tanggal Mulai Kerja", value = profile.tglMulaiKerja)

        if (profile.pegawaiPrivilege != null) {
            InfoRow(
                label = "Level",
                value = when (profile.pegawaiPrivilege) {
                    "1" -> "Admin"
                    "2" -> "Atasan"
                    "3" -> "Pegawai"
                    else -> profile.pegawaiPrivilege
                }
            )
        }
    }
}

@Composable
private fun ContactInfoSection(profile: PegawaiProfile) {
    InfoCard(title = "Kontak", icon = Icons.Default.Phone) {
        if (!profile.pegawaiTelp.isNullOrEmpty()) {
            InfoRow(label = "No. Telepon", value = profile.pegawaiTelp)
        } else {
            Text(
                text = "Tidak ada informasi kontak",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
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

/* =========================
   SETTINGS UI HELPERS
   ========================= */

@Composable
private fun SettingSwitchRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingActionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onClick) { Text("Buka") }
    }
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
