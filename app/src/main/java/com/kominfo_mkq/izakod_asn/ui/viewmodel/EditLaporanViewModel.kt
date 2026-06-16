package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import com.kominfo_mkq.izakod_asn.data.model.KategoriKegiatan
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.repository.LaporanRepository
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import com.kominfo_mkq.izakod_asn.data.repository.TargetKinerjaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DEFAULT_SUBMISSION_DEADLINE_DAYS = 7

private fun todayDateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private fun addDays(dateString: String, days: Int): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = formatter.parse(dateString) ?: Date()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return formatter.format(calendar.time)
}

data class EditLaporanUiState(
    // Loading states
    val isLoadingData: Boolean = false,
    val loadError: Boolean = false,
    val isUpdating: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,

    // Form fields (same as CreateLaporanUiState)
    val laporanId: Int = 0,
    val tanggalKegiatan: String = "",
    val minTanggalKegiatan: String = addDays(todayDateString(), -DEFAULT_SUBMISSION_DEADLINE_DAYS),
    val maxTanggalKegiatan: String = todayDateString(),
    val submissionDeadlineDays: Int = DEFAULT_SUBMISSION_DEADLINE_DAYS,
    val kategoriId: String = "",
    val namaKegiatan: String = "",
    val deskripsiKegiatan: String = "",
    val targetOutput: String = "",
    val hasilOutput: String = "",
    val selectedTargetKinerjaId: String = "",
    val selectedTargetKinerjaDetailId: String = "",
    val waktuMulai: String = "",
    val waktuSelesai: String = "",
    val lokasiKegiatan: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val pesertaKegiatan: String = "",
    val jumlahPeserta: String = "",
    val linkReferensi: String = "",
    val kendala: String = "",
    val solusi: String = "",
    val statusLaporan: String = "",

    // Metadata
    val kategoris: List<KategoriKegiatan> = emptyList(),
    val targetKinerjaList: List<TargetKinerjaItem> = emptyList(),
    val isLoadingTargetKinerja: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val gettingLocation: Boolean = false
)

class EditLaporanViewModel : ViewModel() {

    private val apiService = ApiClient.eabsenApiService
    private val repository = LaporanRepository()
    private val targetRepository = TargetKinerjaRepository()

    private val _uiState = MutableStateFlow(EditLaporanUiState())
    val uiState: StateFlow<EditLaporanUiState> = _uiState.asStateFlow()

    /**
     * Load existing laporan data
     */
    fun loadLaporan(laporanId: Int) {
        loadLaporanSettings()
        viewModelScope.launch {
            try {
                android.util.Log.d("EditLaporanViewModel", "📋 Loading laporan ID: $laporanId")

                _uiState.value = _uiState.value.copy(isLoadingData = true)

                // Get laporan detail
                val detailResponse = repository.getLaporanDetail(laporanId)

                if (detailResponse.isSuccessful && detailResponse.body() != null) {
                    val laporan = detailResponse.body()!!.data

                    // Get kategori list
                    val kategoriResponse = apiService.getKategoriList(isActive = 1)
                    val kategoris = if (kategoriResponse.isSuccessful && kategoriResponse.body() != null) {
                        kategoriResponse.body()!!.data
                    } else {
                        emptyList()
                    }

                    fun formatTime(time: String): String {
                        return if (time.length > 5) {
                            time.substring(0, 5) // "08:30:00" -> "08:30"
                        } else {
                            time
                        }
                    }

                    // Populate form with existing data
                    _uiState.value = _uiState.value.copy(
                        isLoadingData = false,
                        laporanId = laporan.laporanId,
                        tanggalKegiatan = laporan.tanggalKegiatan,
                        kategoriId = laporan.kategoriId.toString(),
                        namaKegiatan = laporan.namaKegiatan,
                        deskripsiKegiatan = laporan.deskripsiKegiatan,
                        targetOutput = laporan.targetOutput ?: "",
                        hasilOutput = laporan.hasilOutput ?: "",
                        selectedTargetKinerjaId = laporan.targetKinerjaId?.toString().orEmpty(),
                        selectedTargetKinerjaDetailId = laporan.targetKinerjaDetailId?.toString().orEmpty(),
                        waktuMulai = formatTime(laporan.waktuMulai),    // ✅ Format to HH:mm
                        waktuSelesai = formatTime(laporan.waktuSelesai),
                        lokasiKegiatan = laporan.lokasiKegiatan ?: "",
                        latitude = laporan.latitude,
                        longitude = laporan.longitude,
                        pesertaKegiatan = laporan.pesertaKegiatan ?: "",
                        jumlahPeserta = laporan.jumlahPeserta?.toString() ?: "",
                        linkReferensi = laporan.linkReferensi ?: "",
                        kendala = laporan.kendala ?: "",
                        solusi = laporan.solusi ?: "",
                        statusLaporan = laporan.statusLaporan,
                        kategoris = kategoris
                    )

                    loadTargetKinerjaOptions(clearSelection = false)

                    android.util.Log.d("EditLaporanViewModel", "✅ Data loaded successfully")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingData = false,
                        loadError = true,
                        errorMessage = "Gagal memuat data laporan"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("EditLaporanViewModel", "❌ Error: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingData = false,
                    loadError = true,
                    errorMessage = e.message
                )
            }
        }
    }

    private fun loadLaporanSettings() {
        viewModelScope.launch {
            try {
                val pegawaiId = StatistikRepository.getPegawaiId()
                val response = apiService.getLaporanKegiatanSettings(pegawaiId)

                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { settings ->
                        val maxDate = settings.maxDate.ifBlank { todayDateString() }
                        val minDate = settings.minDate.ifBlank {
                            addDays(maxDate, -settings.submissionDeadlineDays)
                        }

                        _uiState.value = _uiState.value.copy(
                            minTanggalKegiatan = minDate,
                            maxTanggalKegiatan = maxDate,
                            submissionDeadlineDays = settings.submissionDeadlineDays
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w(
                    "EditLaporanViewModel",
                    "Gagal memuat pengaturan laporan: ${e.message}"
                )
            }
        }
    }

    /**
     * Update laporan
     */
    fun updateLaporan(context: Context) {
        if (!validateForm()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Mohon lengkapi semua field yang wajib diisi"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUpdating = true)

                val state = _uiState.value

                // ✅ DEBUG: Log date before sending
                android.util.Log.d("EditLaporanViewModel", "📅 tanggal_kegiatan in state: ${state.tanggalKegiatan}")
                android.util.Log.d("EditLaporanViewModel", "📅 After take(10): ${state.tanggalKegiatan.take(10)}")

                val request = UpdateLaporanRequest(
                    tanggal_kegiatan = state.tanggalKegiatan.take(10),
                    kategori_id = state.kategoriId.toInt(),
                    nama_kegiatan = state.namaKegiatan.trim(),
                    deskripsi_kegiatan = state.deskripsiKegiatan.trim(),
                    target_output = state.targetOutput.takeIf { it.isNotBlank() },
                    hasil_output = state.hasilOutput.takeIf { it.isNotBlank() },
                    target_kinerja_id = state.selectedTargetKinerjaId.toNullableJsonInt(),
                    target_kinerja_detail_id = state.selectedTargetKinerjaDetailId.toNullableJsonInt(),
                    waktu_mulai = state.waktuMulai,
                    waktu_selesai = state.waktuSelesai,
                    lokasi_kegiatan = state.lokasiKegiatan.takeIf { it.isNotBlank() },
                    latitude = state.latitude,
                    longitude = state.longitude,
                    peserta_kegiatan = state.pesertaKegiatan.takeIf { it.isNotBlank() },
                    jumlah_peserta = state.jumlahPeserta.toIntOrNull() ?: 0,
                    link_referensi = state.linkReferensi.takeIf { it.isNotBlank() },
                    kendala = state.kendala.takeIf { it.isNotBlank() },
                    solusi = state.solusi.takeIf { it.isNotBlank() },
                    status_laporan = state.statusLaporan
                )

                android.util.Log.d("EditLaporanViewModel", "📤 Request date: ${request.tanggal_kegiatan}")

                val response = repository.updateLaporan(context, state.laporanId, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        isSuccess = true
                    )
                    DashboardRefreshNotifier.markDirty()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        errorMessage = errorBody ?: "Gagal memperbarui laporan"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Validation
     */
    private fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        val state = _uiState.value
        val shouldValidateTanggalRange =
            !state.statusLaporan.equals("Diajukan", ignoreCase = true)

        if (state.tanggalKegiatan.isBlank()) {
            errors["tanggal_kegiatan"] = "Tanggal kegiatan wajib diisi"
        } else if (shouldValidateTanggalRange && state.tanggalKegiatan < state.minTanggalKegiatan) {
            errors["tanggal_kegiatan"] =
                "Tanggal kegiatan maksimal ${state.submissionDeadlineDays} hari ke belakang"
        } else if (shouldValidateTanggalRange && state.tanggalKegiatan > state.maxTanggalKegiatan) {
            errors["tanggal_kegiatan"] = "Tanggal kegiatan tidak boleh lebih besar dari hari ini"
        }
        if (state.kategoriId.isBlank()) {
            errors["kategori_id"] = "Kategori wajib dipilih"
        }
        if (state.namaKegiatan.isBlank()) {
            errors["nama_kegiatan"] = "Nama kegiatan wajib diisi"
        }
        if (state.deskripsiKegiatan.isBlank()) {
            errors["deskripsi_kegiatan"] = "Deskripsi wajib diisi"
        }
        if (state.waktuMulai.isBlank()) {
            errors["waktu_mulai"] = "Waktu mulai wajib diisi"
        }
        if (state.waktuSelesai.isBlank()) {
            errors["waktu_selesai"] = "Waktu selesai wajib diisi"
        }

        _uiState.value = _uiState.value.copy(errors = errors)
        return errors.isEmpty()
    }

    // Update functions (same as CreateLaporanViewModel)
    fun updateTanggalKegiatan(value: String) {
        _uiState.value = _uiState.value.copy(
            tanggalKegiatan = value,
            selectedTargetKinerjaId = "",
            selectedTargetKinerjaDetailId = ""
        )
        loadTargetKinerjaOptions(clearSelection = true)
    }

    fun updateTargetKinerja(value: String) {
        _uiState.value = _uiState.value.copy(
            selectedTargetKinerjaId = value,
            selectedTargetKinerjaDetailId = ""
        )
    }

    fun updateTargetKinerjaDetail(value: String) {
        _uiState.value = _uiState.value.copy(selectedTargetKinerjaDetailId = value)
    }

    fun updateKategori(value: String) {
        _uiState.value = _uiState.value.copy(kategoriId = value)
    }

    fun updateNamaKegiatan(value: String) {
        _uiState.value = _uiState.value.copy(namaKegiatan = value)
    }

    fun updateDeskripsi(value: String) {
        _uiState.value = _uiState.value.copy(deskripsiKegiatan = value)
    }

    fun updateTargetOutput(value: String) {
        _uiState.value = _uiState.value.copy(targetOutput = value)
    }

    fun updateHasilOutput(value: String) {
        _uiState.value = _uiState.value.copy(hasilOutput = value)
    }

    fun updateWaktuMulai(value: String) {
        _uiState.value = _uiState.value.copy(waktuMulai = value)
    }

    fun updateWaktuSelesai(value: String) {
        _uiState.value = _uiState.value.copy(waktuSelesai = value)
    }

    fun updateLokasiKegiatan(value: String) {
        _uiState.value = _uiState.value.copy(lokasiKegiatan = value)
    }

    fun updatePesertaKegiatan(value: String) {
        _uiState.value = _uiState.value.copy(pesertaKegiatan = value)
    }

    fun updateJumlahPeserta(value: String) {
        _uiState.value = _uiState.value.copy(jumlahPeserta = value)
    }

    fun updateLinkReferensi(value: String) {
        _uiState.value = _uiState.value.copy(linkReferensi = value)
    }

    fun updateKendala(value: String) {
        _uiState.value = _uiState.value.copy(kendala = value)
    }

    fun updateSolusi(value: String) {
        _uiState.value = _uiState.value.copy(solusi = value)
    }

    /**
     * Get current location
     */
    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(gettingLocation = true)

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                try {
                    val location: Location? = fusedLocationClient.lastLocation.await()

                    if (location != null) {
                        _uiState.value = _uiState.value.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            gettingLocation = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            gettingLocation = false,
                            errorMessage = "Gagal mengambil lokasi. Pastikan GPS aktif."
                        )
                    }
                } catch (e: SecurityException) {
                    _uiState.value = _uiState.value.copy(
                        gettingLocation = false,
                        errorMessage = "Izin lokasi ditolak"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    gettingLocation = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    private fun loadTargetKinerjaOptions(clearSelection: Boolean = false) {
        viewModelScope.launch {
            val (tahun, bulan) = parseYearMonth(_uiState.value.tanggalKegiatan)
                ?: run {
                    _uiState.value = _uiState.value.copy(
                        targetKinerjaList = emptyList(),
                        isLoadingTargetKinerja = false,
                        selectedTargetKinerjaId = "",
                        selectedTargetKinerjaDetailId = ""
                    )
                    return@launch
                }

            _uiState.value = _uiState.value.copy(
                isLoadingTargetKinerja = true,
                selectedTargetKinerjaId = if (clearSelection) "" else _uiState.value.selectedTargetKinerjaId,
                selectedTargetKinerjaDetailId = if (clearSelection) "" else _uiState.value.selectedTargetKinerjaDetailId
            )

            try {
                val response = targetRepository.getTargetKinerjaList(
                    tahun = tahun,
                    bulan = bulan
                )

                if (response.success) {
                    val targetOptions = response.data?.data
                        .orEmpty()
                        .filter { it.status.lowercase(Locale.getDefault()) in READY_TARGET_STATUSES }
                        .map { loadTargetWithDetails(it) }

                    val currentTargetId = _uiState.value.selectedTargetKinerjaId
                    val currentDetailId = _uiState.value.selectedTargetKinerjaDetailId
                    val selectedTarget = targetOptions.firstOrNull { it.id.toString() == currentTargetId }
                    val detailStillValid = currentDetailId.isBlank() ||
                        selectedTarget?.details?.any { it.id?.toString() == currentDetailId } == true
                    val nextTargetId = when {
                        clearSelection || currentTargetId.isBlank() -> selectedTarget?.id?.toString().orEmpty()
                        selectedTarget != null -> selectedTarget.id.toString()
                        else -> currentTargetId
                    }
                    val nextDetailId = when {
                        currentDetailId.isBlank() -> ""
                        selectedTarget != null && detailStillValid -> currentDetailId
                        selectedTarget == null && !clearSelection -> currentDetailId
                        else -> ""
                    }

                    _uiState.value = _uiState.value.copy(
                        targetKinerjaList = targetOptions,
                        isLoadingTargetKinerja = false,
                        selectedTargetKinerjaId = nextTargetId,
                        selectedTargetKinerjaDetailId = nextDetailId
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        targetKinerjaList = emptyList(),
                        isLoadingTargetKinerja = false
                    )
                    android.util.Log.e(
                        "EditLaporanViewModel",
                        "Failed to load target kinerja: ${response.error}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    targetKinerjaList = emptyList(),
                    isLoadingTargetKinerja = false
                )
                android.util.Log.e("EditLaporanViewModel", "Error loading target kinerja: ${e.message}", e)
            }
        }
    }

    private suspend fun loadTargetWithDetails(target: TargetKinerjaItem): TargetKinerjaItem {
        if (target.details.isNotEmpty()) return target

        val response = targetRepository.getTargetKinerjaDetail(target.id)
        return if (response.success) {
            response.data?.data ?: target
        } else {
            target
        }
    }

    private fun parseYearMonth(date: String): Pair<Int, Int>? {
        val parts = date.take(10).split("-")
        if (parts.size < 2) return null

        val tahun = parts[0].toIntOrNull() ?: return null
        val bulan = parts[1].toIntOrNull() ?: return null

        return tahun to bulan
    }

    private fun String.toNullableJsonInt(): JsonElement {
        return toIntOrNull()?.let { JsonPrimitive(it) } ?: JsonNull.INSTANCE
    }

    companion object {
        private val READY_TARGET_STATUSES = setOf("disetujui", "final")
    }
}
