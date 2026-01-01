package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kominfo_mkq.izakod_asn.R
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.ui.theme.*
import com.kominfo_mkq.izakod_asn.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val userPreferences = remember { UserPreferences(context) }
    val pin = remember { userPreferences.getPin() }

    // Load profile when screen opens
    LaunchedEffect(pin) {
        pin?.let { userPin ->
            android.util.Log.d("ProfileScreen", "📱 Loading profile for PIN: $userPin")
            viewModel.loadProfile(userPin)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profil Saya",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                ProfileLoadingContent()
            }
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
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Header with Photo
                    ProfileHeader(
                        photoUrl = uiState.photoUrl,
                        nama = profile.pegawaiNama,
                        nip = profile.pegawaiNip,
                        jabatan = profile.jabatan ?: "-"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Personal Information
                    PersonalInfoSection(profile)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Work Information
                    WorkInfoSection(profile)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Information
                    ContactInfoSection(profile)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

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
        colors = CardDefaults.cardColors(
            containerColor = PrimaryLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo
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

            // Name
            Text(
                text = nama,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))
// TODO lihat kode update tampilan ini di chatgpt https://chatgpt.com/c/6956b23a-6b2c-8324-b9e5-520764f52e04
            // NIP
            Text(
                text = "NIP: $nip",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Jabatan Badge
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
private fun PersonalInfoSection(profile: com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile) {
    InfoCard(
        title = "Informasi Pribadi",
        icon = Icons.Default.Person
    ) {
        InfoRow(label = "Nama Lengkap", value = profile.pegawaiNama)
        InfoRow(label = "NIP", value = profile.pegawaiNip)
        InfoRow(label = "PIN", value = profile.pegawaiPin)

        if (profile.tempatLahir != null && profile.tempatLahir.isNotEmpty()) {
            InfoRow(label = "Tempat Lahir", value = profile.tempatLahir)
        }

        if (profile.tglLahir != null && profile.tglLahir != "0") {
            InfoRow(label = "Tanggal Lahir", value = profile.tglLahir)
        }

        InfoRow(
            label = "Jenis Kelamin",
            value = if (profile.gender == 1) "Laki-laki" else "Perempuan"
        )

        InfoRow(
            label = "Status",
            value = if (profile.pegawaiStatus == 1) "Aktif" else "Tidak Aktif"
        )
    }
}

@Composable
private fun WorkInfoSection(profile: com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile) {
    InfoCard(
        title = "Informasi Kepegawaian",
        icon = Icons.Default.Work
    ) {
        if (profile.jabatan != null) {
            InfoRow(label = "Jabatan", value = profile.jabatan)
        }

        if (profile.skpd != null) {
            InfoRow(label = "SKPD", value = profile.skpd)
        }

        if (profile.sotk != null) {
            InfoRow(label = "SOTK", value = profile.sotk)
        }

        if (profile.tglMulaiKerja != null) {
            InfoRow(label = "Tanggal Mulai Kerja", value = profile.tglMulaiKerja)
        }

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
private fun ContactInfoSection(profile: com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile) {
    InfoCard(
        title = "Kontak",
        icon = Icons.Default.Phone
    ) {
        if (profile.pegawaiTelp != null && profile.pegawaiTelp.isNotEmpty()) {
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
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
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProfileLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Memuat profil...")
        }
    }
}

@Composable
private fun ProfileErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}