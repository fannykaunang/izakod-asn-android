package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiData
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanItem
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanRequest
import com.kominfo_mkq.izakod_asn.data.model.KandidatBawahanItem
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.ErrorLight
import com.kominfo_mkq.izakod_asn.ui.theme.GradientEndLight
import com.kominfo_mkq.izakod_asn.ui.theme.GradientStartLight
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.SecondaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.theme.TertiaryLight
import com.kominfo_mkq.izakod_asn.ui.viewmodel.AtasanPegawaiUiState
import com.kominfo_mkq.izakod_asn.ui.viewmodel.AtasanPegawaiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BawahanSayaScreen(
    onNavigateBack: () -> Unit,
    viewModel: AtasanPegawaiViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var formState by remember { mutableStateOf<BawahanProposalFormState?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    LaunchedEffect(uiState.errorMessage, uiState.actionMessage) {
        val message = uiState.errorMessage ?: uiState.actionMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Bawahan Saya",
                subtitle = "Kelola usulan relasi atasan-bawahan",
                compact = true,
                onBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.load(context, silent = true) },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!uiState.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { formState = BawahanProposalFormState.add() },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Tambah Usulan") },
                    containerColor = PrimaryLight,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryLight
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 16.dp,
                            end = 20.dp,
                            bottom = 112.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            BawahanHeroCard(
                                uiState = uiState,
                                onAdd = { formState = BawahanProposalFormState.add() }
                            )
                        }

                        item {
                            BawahanTabs(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                activeCount = uiState.bawahan.size,
                                proposalCount = uiState.usulan.size
                            )
                        }

                        if (selectedTab == 0) {
                            if (uiState.bawahan.isEmpty()) {
                                item {
                                    EmptyBawahanState(
                                        title = "Belum ada bawahan aktif",
                                        message = "Ajukan bawahan yang berada dalam OPD yang sama. Usulan akan dicek oleh petugas kepegawaian.",
                                        buttonText = "Tambah Usulan",
                                        icon = Icons.Default.Groups,
                                        onClick = { formState = BawahanProposalFormState.add() }
                                    )
                                }
                            } else {
                                items(uiState.bawahan, key = { it.id }) { item ->
                                    BawahanActiveCard(
                                        item = item,
                                        onEdit = {
                                            formState = BawahanProposalFormState.fromRelation(
                                                relation = item,
                                                mode = "ubah"
                                            )
                                        },
                                        onDeactivate = {
                                            formState = BawahanProposalFormState.fromRelation(
                                                relation = item,
                                                mode = "nonaktif"
                                            )
                                        }
                                    )
                                }
                            }
                        } else {
                            if (uiState.usulan.isEmpty()) {
                                item {
                                    EmptyBawahanState(
                                        title = "Belum ada usulan",
                                        message = "Draft dan usulan yang sudah diajukan akan tampil di sini.",
                                        buttonText = "Buat Usulan",
                                        icon = Icons.Default.HourglassTop,
                                        onClick = { formState = BawahanProposalFormState.add() }
                                    )
                                }
                            } else {
                                items(uiState.usulan, key = { it.id }) { item ->
                                    BawahanProposalCard(
                                        item = item,
                                        isMutating = uiState.isMutating,
                                        onEdit = {
                                            formState = BawahanProposalFormState.fromProposal(item)
                                        },
                                        onSubmit = { viewModel.submitDraft(context, item) },
                                        onCancel = { viewModel.cancelProposal(context, item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val currentForm = formState
    if (currentForm != null) {
        BawahanProposalSheet(
            uiState = uiState,
            initialForm = currentForm,
            onDismiss = { formState = null },
            onSave = { request, existingId, submitNow ->
                viewModel.saveProposal(
                    context = context,
                    request = request,
                    existingUsulanId = existingId,
                    submitNow = submitNow
                )
                formState = null
            }
        )
    }
}

@Composable
private fun BawahanHeroCard(
    uiState: AtasanPegawaiUiState,
    onAdd: () -> Unit
) {
    val directCount = uiState.bawahan.count { it.jenisAtasan.equals("Langsung", ignoreCase = true) }
    val pendingCount = uiState.usulan.count { it.status.equals("diajukan", ignoreCase = true) }
    val draftCount = uiState.usulan.count { it.status.equals("draft", ignoreCase = true) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(GradientStartLight, GradientEndLight)
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Kelola Bawahan",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Ajukan perubahan relasi tanpa menunggu input manual dari admin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.86f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupervisorAccount,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(14.dp)
                                .size(30.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStatChip("${uiState.bawahan.size}", "Aktif")
                    HeroStatChip("$directCount", "Langsung")
                    HeroStatChip("${pendingCount + draftCount}", "Usulan")
                }

                FilledTonalButton(
                    onClick = onAdd,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Usulan")
                }
            }
        }
    }
}

@Composable
private fun HeroStatChip(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun BawahanTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    activeCount: Int,
    proposalCount: Int
) {
    val tabs = listOf("Aktif ($activeCount)", "Usulan ($proposalCount)")
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = PrimaryLight
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

@Composable
private fun BawahanActiveCard(
    item: AtasanPegawaiData,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                IconBadge(
                    icon = Icons.Default.Groups,
                    color = PrimaryLight,
                    background = PrimaryLight.copy(alpha = 0.12f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = item.displayPegawaiName(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.pegawaiJabatan?.takeIf { it.isNotBlank() } ?: "Jabatan belum tersedia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.pegawaiNip?.takeIf { it.isNotBlank() } ?: "NIP belum tersedia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusPill(
                    text = item.jenisAtasan,
                    color = if (item.jenisAtasan.equals("Langsung", true)) TertiaryLight else SecondaryLight
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Periode",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.periodText(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ubah")
                }
                FilledTonalButton(
                    onClick = onDeactivate,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ErrorLight.copy(alpha = 0.12f),
                        contentColor = ErrorLight
                    )
                ) {
                    Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nonaktifkan")
                }
            }
        }
    }
}

@Composable
private fun BawahanProposalCard(
    item: AtasanPegawaiUsulanItem,
    isMutating: Boolean,
    onEdit: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val statusColor = item.statusColor()
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                IconBadge(
                    icon = item.actionIcon(),
                    color = statusColor,
                    background = statusColor.copy(alpha = 0.12f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = item.actionTitle(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.pegawaiNama?.takeIf { it.isNotBlank() } ?: "Pegawai #${item.pegawaiId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.alasanPengajuan?.takeIf { it.isNotBlank() }
                            ?: "Alasan belum diisi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusPill(text = item.statusLabel(), color = statusColor)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPill("Jenis", item.jenisAtasan)
                InfoPill("Mulai", item.tanggalMulai.orDash())
            }

            if (!item.catatanVerifikasi.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StatusRejected.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = item.catatanVerifikasi,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (item.status.equals("draft", ignoreCase = true) ||
                item.status.equals("diajukan", ignoreCase = true)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (item.status.equals("draft", ignoreCase = true)) {
                        OutlinedButton(onClick = onEdit, enabled = !isMutating) {
                            Text("Edit")
                        }
                        Button(
                            onClick = onSubmit,
                            enabled = !isMutating,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                        ) {
                            Text("Ajukan")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    TextButton(onClick = onCancel, enabled = !isMutating) {
                        Text("Batalkan", color = ErrorLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBawahanState(
    title: String,
    message: String,
    buttonText: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            IconBadge(
                icon = icon,
                color = PrimaryLight,
                background = PrimaryLight.copy(alpha = 0.12f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
            ) {
                Text(buttonText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BawahanProposalSheet(
    uiState: AtasanPegawaiUiState,
    initialForm: BawahanProposalFormState,
    onDismiss: () -> Unit,
    onSave: (AtasanPegawaiUsulanRequest, Int?, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var form by remember(initialForm) { mutableStateOf(initialForm) }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    val currentPegawaiId = uiState.currentPegawaiId

    val filteredCandidates = remember(form.search, uiState.kandidat) {
        val keyword = form.search.trim().lowercase(Locale("id", "ID"))
        if (keyword.isBlank()) {
            uiState.kandidat.take(20)
        } else {
            uiState.kandidat.filter { item ->
                listOf(item.pegawaiNama, item.pegawaiNip, item.jabatan)
                    .filterNotNull()
                    .any { it.lowercase(Locale("id", "ID")).contains(keyword) }
            }.take(20)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = form.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = form.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (form.mode == "tambah") {
                item {
                    OutlinedTextField(
                        value = form.search,
                        onValueChange = { form = form.copy(search = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Cari bawahan") },
                        placeholder = { Text("Contoh: Fanny atau NIP") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true
                    )
                }

                item {
                    CandidatePicker(
                        candidates = filteredCandidates,
                        selectedPegawaiId = form.pegawaiId,
                        onSelect = { candidate ->
                            form = form.copy(
                                pegawaiId = candidate.pegawaiId,
                                pegawaiName = candidate.displayName(),
                                pegawaiSubtitle = candidate.subtitle()
                            )
                        }
                    )
                }
            } else {
                item {
                    SelectedEmployeeSummary(
                        name = form.pegawaiName,
                        subtitle = form.pegawaiSubtitle
                    )
                }
            }

            item {
                RelationTypeSelector(
                    value = form.jenisAtasan,
                    onChange = { form = form.copy(jenisAtasan = it) }
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = form.tanggalMulai,
                        onValueChange = { form = form.copy(tanggalMulai = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tanggal mulai") },
                        placeholder = { Text("Contoh: 2026-06-16") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = form.tanggalSelesai,
                        onValueChange = { form = form.copy(tanggalSelesai = it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tanggal selesai") },
                        placeholder = {
                            Text(
                                if (form.mode == "nonaktif") "Contoh: 2026-06-30"
                                else "Kosongkan jika masih aktif"
                            )
                        },
                        singleLine = true
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = form.alasan,
                    onValueChange = { form = form.copy(alasan = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Alasan usulan") },
                    placeholder = { Text("Contoh: Menyesuaikan pembagian tugas bulan ini") },
                    minLines = 3
                )
            }

            item {
                OutlinedTextField(
                    value = form.keterangan,
                    onValueChange = { form = form.copy(keterangan = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan tambahan") },
                    placeholder = { Text("Contoh: Berlaku untuk kegiatan bidang informasi") },
                    minLines = 2
                )
            }

            if (!validationMessage.isNullOrBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ErrorLight.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = validationMessage.orEmpty(),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorLight
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = {
                            val request = form.toRequest(currentPegawaiId, submitNow = false)
                            if (request == null) {
                                validationMessage = form.validationMessage(currentPegawaiId)
                            } else {
                                onSave(request, form.existingUsulanId, false)
                            }
                        },
                        enabled = !uiState.isMutating
                    ) {
                        Text("Simpan Draft")
                    }

                    Button(
                        onClick = {
                            val request = form.toRequest(currentPegawaiId, submitNow = true)
                            if (request == null) {
                                validationMessage = form.validationMessage(currentPegawaiId)
                            } else {
                                onSave(request, form.existingUsulanId, true)
                            }
                        },
                        enabled = !uiState.isMutating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (form.mode == "nonaktif") ErrorLight else PrimaryLight
                        )
                    ) {
                        Text(if (form.mode == "nonaktif") "Ajukan Nonaktif" else "Ajukan")
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidatePicker(
    candidates: List<KandidatBawahanItem>,
    selectedPegawaiId: Int?,
    onSelect: (KandidatBawahanItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Pilih pegawai dalam OPD",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (candidates.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Text(
                    text = "Tidak ada kandidat yang cocok. Pegawai yang sudah menjadi bawahan aktif tidak ditampilkan.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            candidates.forEach { candidate ->
                val selected = candidate.pegawaiId == selectedPegawaiId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelect(candidate) }
                        .border(
                            width = 1.dp,
                            color = if (selected) PrimaryLight else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(18.dp)
                        ),
                    color = if (selected) PrimaryLight.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        IconBadge(
                            icon = if (selected) Icons.Default.CheckCircle else Icons.Default.Groups,
                            color = if (selected) PrimaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                            background = if (selected) PrimaryLight.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = candidate.displayName(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = candidate.subtitle(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedEmployeeSummary(
    name: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = PrimaryLight.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconBadge(
                icon = Icons.Default.Groups,
                color = PrimaryLight,
                background = PrimaryLight.copy(alpha = 0.12f)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = name.ifBlank { "Pegawai belum dipilih" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle.ifBlank { "Detail pegawai belum tersedia" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RelationTypeSelector(
    value: String,
    onChange: (String) -> Unit
) {
    val options = listOf("Langsung", "Tidak Langsung", "PLT", "PLH")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Jenis atasan",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.take(2).forEach { option ->
                FilterChip(
                    selected = value == option,
                    onClick = { onChange(option) },
                    label = { Text(option) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.drop(2).forEach { option ->
                FilterChip(
                    selected = value == option,
                    onClick = { onChange(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    color: Color,
    background: Color
) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private data class BawahanProposalFormState(
    val mode: String,
    val title: String,
    val subtitle: String,
    val existingUsulanId: Int? = null,
    val targetRelationId: Int? = null,
    val pegawaiId: Int? = null,
    val pegawaiName: String = "",
    val pegawaiSubtitle: String = "",
    val jenisAtasan: String = "Langsung",
    val tanggalMulai: String = todayString(),
    val tanggalSelesai: String = "",
    val alasan: String = "",
    val keterangan: String = "",
    val search: String = ""
) {
    fun toRequest(currentPegawaiId: Int?, submitNow: Boolean): AtasanPegawaiUsulanRequest? {
        val atasanId = currentPegawaiId ?: return null
        val selectedPegawaiId = pegawaiId ?: return null
        if (!tanggalMulai.isValidDate()) return null
        if (tanggalSelesai.isNotBlank() && !tanggalSelesai.isValidDate()) return null

        return AtasanPegawaiUsulanRequest(
            aksi = mode,
            targetAtasanPegawaiId = targetRelationId,
            pegawaiId = selectedPegawaiId,
            atasanId = atasanId,
            jenisAtasan = jenisAtasan,
            tanggalMulai = tanggalMulai.trim(),
            tanggalSelesai = tanggalSelesai.trim().takeIf { it.isNotBlank() },
            alasanPengajuan = alasan.trim().takeIf { it.isNotBlank() },
            keterangan = keterangan.trim().takeIf { it.isNotBlank() },
            submit = submitNow
        )
    }

    fun validationMessage(currentPegawaiId: Int?): String {
        return when {
            currentPegawaiId == null -> "Sesi pegawai belum siap. Silakan muat ulang halaman."
            pegawaiId == null -> "Pilih bawahan terlebih dahulu."
            !tanggalMulai.isValidDate() -> "Tanggal mulai wajib diisi dengan format 2026-06-16."
            tanggalSelesai.isNotBlank() && !tanggalSelesai.isValidDate() ->
                "Tanggal selesai harus memakai format 2026-06-30."
            else -> "Lengkapi data usulan terlebih dahulu."
        }
    }

    companion object {
        fun add(): BawahanProposalFormState {
            return BawahanProposalFormState(
                mode = "tambah",
                title = "Tambah Bawahan",
                subtitle = "Pilih pegawai dalam OPD, lalu ajukan untuk diverifikasi."
            )
        }

        fun fromRelation(
            relation: AtasanPegawaiData,
            mode: String
        ): BawahanProposalFormState {
            val isDeactivate = mode == "nonaktif"
            return BawahanProposalFormState(
                mode = mode,
                title = if (isDeactivate) "Nonaktifkan Bawahan" else "Ubah Relasi Bawahan",
                subtitle = if (isDeactivate) {
                    "Ajukan pengakhiran relasi bawahan aktif ini."
                } else {
                    "Ajukan perubahan jenis atasan atau catatan relasi."
                },
                targetRelationId = relation.id,
                pegawaiId = relation.pegawaiId,
                pegawaiName = relation.displayPegawaiName(),
                pegawaiSubtitle = relation.pegawaiJabatan.orEmpty(),
                jenisAtasan = relation.jenisAtasan,
                tanggalMulai = relation.tanggalMulai?.take(10) ?: todayString(),
                tanggalSelesai = if (isDeactivate) todayString() else relation.tanggalSelesai?.take(10).orEmpty(),
                keterangan = relation.keterangan.orEmpty()
            )
        }

        fun fromProposal(item: AtasanPegawaiUsulanItem): BawahanProposalFormState {
            return BawahanProposalFormState(
                mode = item.aksi,
                title = when (item.aksi.lowercase(Locale("id", "ID"))) {
                    "ubah" -> "Edit Draft Perubahan"
                    "nonaktif" -> "Edit Draft Nonaktif"
                    else -> "Edit Draft Tambah"
                },
                subtitle = "Perbaiki data usulan sebelum diajukan.",
                existingUsulanId = item.id,
                targetRelationId = item.targetAtasanPegawaiId,
                pegawaiId = item.pegawaiId,
                pegawaiName = item.pegawaiNama ?: "Pegawai #${item.pegawaiId}",
                pegawaiSubtitle = item.pegawaiJabatan.orEmpty(),
                jenisAtasan = item.jenisAtasan,
                tanggalMulai = item.tanggalMulai?.take(10) ?: todayString(),
                tanggalSelesai = item.tanggalSelesai?.take(10).orEmpty(),
                alasan = item.alasanPengajuan.orEmpty(),
                keterangan = item.keterangan.orEmpty()
            )
        }
    }
}

private fun AtasanPegawaiData.displayPegawaiName(): String {
    return pegawaiNama?.takeIf { it.isNotBlank() } ?: "Pegawai #$pegawaiId"
}

private fun AtasanPegawaiData.periodText(): String {
    val start = tanggalMulai?.take(10).orDash()
    val end = tanggalSelesai?.take(10)?.takeIf { it.isNotBlank() } ?: "Sekarang"
    return "$start - $end"
}

private fun KandidatBawahanItem.displayName(): String {
    return pegawaiNama?.takeIf { it.isNotBlank() } ?: "Pegawai #$pegawaiId"
}

private fun KandidatBawahanItem.subtitle(): String {
    return listOfNotNull(
        jabatan?.takeIf { it.isNotBlank() },
        pegawaiNip?.takeIf { it.isNotBlank() }
    ).joinToString(" • ").ifBlank { "Detail pegawai belum tersedia" }
}

private fun AtasanPegawaiUsulanItem.actionTitle(): String {
    val action = when (aksi.lowercase(Locale("id", "ID"))) {
        "ubah" -> "Ubah relasi bawahan"
        "nonaktif" -> "Nonaktifkan bawahan"
        else -> "Tambah bawahan"
    }
    return action
}

private fun AtasanPegawaiUsulanItem.actionIcon(): ImageVector {
    return when (aksi.lowercase(Locale("id", "ID"))) {
        "ubah" -> Icons.Default.Edit
        "nonaktif" -> Icons.Default.PersonOff
        else -> Icons.Default.PersonAdd
    }
}

private fun AtasanPegawaiUsulanItem.statusLabel(): String {
    return when (status.lowercase(Locale("id", "ID"))) {
        "draft" -> "Draft"
        "diajukan" -> "Diajukan"
        "disetujui" -> "Disetujui"
        "ditolak" -> "Ditolak"
        "dibatalkan" -> "Dibatalkan"
        else -> status
    }
}

private fun AtasanPegawaiUsulanItem.statusColor(): Color {
    return when (status.lowercase(Locale("id", "ID"))) {
        "draft" -> SecondaryLight
        "diajukan" -> StatusPending
        "disetujui" -> StatusApproved
        "ditolak" -> StatusRejected
        "dibatalkan" -> StatusRevised
        else -> PrimaryLight
    }
}

private fun String?.orDash(): String {
    return this?.takeIf { it.isNotBlank() } ?: "-"
}

private fun String.isValidDate(): Boolean {
    return matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))
}

private fun todayString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID")).format(Date())
}
