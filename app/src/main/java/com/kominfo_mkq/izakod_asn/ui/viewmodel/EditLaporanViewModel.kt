package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.kominfo_mkq.izakod_asn.data.model.*
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.repository.LaporanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    val kategoriId: String = "",
    val namaKegiatan: String = "",
    val deskripsiKegiatan: String = "",
    val targetOutput: String = "",
    val hasilOutput: String = "",
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
    val errors: Map<String, String> = emptyMap(),
    val gettingLocation: Boolean = false
)

class EditLaporanViewModel : ViewModel() {

    private val apiService = ApiClient.eabsenApiService
    private val repository = LaporanRepository()

    private val _uiState = MutableStateFlow(EditLaporanUiState())
    val uiState: StateFlow<EditLaporanUiState> = _uiState.asStateFlow()

    /**
     * Load existing laporan data
     */
    fun loadLaporan(laporanId: Int) {
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

                val response = repository.updateLaporan(state.laporanId, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        isSuccess = true
                    )
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

        if (state.tanggalKegiatan.isBlank()) {
            errors["tanggal_kegiatan"] = "Tanggal kegiatan wajib diisi"
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
        _uiState.value = _uiState.value.copy(tanggalKegiatan = value)
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
}