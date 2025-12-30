package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.kominfo_mkq.izakod_asn.data.model.CreateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.KategoriKegiatan
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * UI State untuk Create Laporan Screen
 */
data class CreateLaporanUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val requiresAttendance: Boolean = false,
    val hasCheckedAttendance: Boolean = false,

    // Form fields
    val tanggalKegiatan: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
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
    val jumlahPeserta: String = "0",
    val linkReferensi: String = "",
    val kendala: String = "",
    val solusi: String = "",

    // Data
    val kategoris: List<KategoriKegiatan> = emptyList(),

    // UI states
    val gettingLocation: Boolean = false,

    // Validation
    val errors: Map<String, String> = emptyMap()
)

/**
 * ViewModel untuk Create Laporan Kegiatan
 */
class CreateLaporanViewModel : ViewModel() {

    private val apiService = ApiClient.eabsenApiService

    private val _uiState = MutableStateFlow(CreateLaporanUiState())
    val uiState: StateFlow<CreateLaporanUiState> = _uiState.asStateFlow()

    init {
        loadKategoris()
    }

    /**
     * Load kategori kegiatan
     */
    private fun loadKategoris() {
        viewModelScope.launch {
            try {
                val response = apiService.getKategoriList(isActive = 1)

                if (response.isSuccessful && response.body()?.success == true) {
                    val kategoris = response.body()!!.data
                    _uiState.value = _uiState.value.copy(
                        kategoris = kategoris
                    )
                }
            } catch (e: Exception) {
                // Silent fail for kategori loading
                e.printStackTrace()
            }
        }
    }

    /**
     * Update form fields
     */
    fun updateTanggalKegiatan(value: String) {
        _uiState.value = _uiState.value.copy(
            tanggalKegiatan = value,
            errors = _uiState.value.errors - "tanggal_kegiatan"
        )
    }

    fun updateKategori(value: String) {
        _uiState.value = _uiState.value.copy(
            kategoriId = value,
            errors = _uiState.value.errors - "kategori_id"
        )
    }

    fun updateNamaKegiatan(value: String) {
        _uiState.value = _uiState.value.copy(
            namaKegiatan = value,
            errors = _uiState.value.errors - "nama_kegiatan"
        )
    }

    fun updateDeskripsi(value: String) {
        _uiState.value = _uiState.value.copy(
            deskripsiKegiatan = value,
            errors = _uiState.value.errors - "deskripsi_kegiatan"
        )
    }

    fun updateTargetOutput(value: String) {
        _uiState.value = _uiState.value.copy(targetOutput = value)
    }

    fun updateHasilOutput(value: String) {
        _uiState.value = _uiState.value.copy(hasilOutput = value)
    }

    fun updateWaktuMulai(value: String) {
        _uiState.value = _uiState.value.copy(
            waktuMulai = value,
            errors = _uiState.value.errors - "waktu_mulai"
        )
    }

    fun updateWaktuSelesai(value: String) {
        _uiState.value = _uiState.value.copy(
            waktuSelesai = value,
            errors = _uiState.value.errors - "waktu_selesai"
        )
    }

    fun updateLokasiKegiatan(value: String) {
        _uiState.value = _uiState.value.copy(lokasiKegiatan = value)
    }

    fun updatePesertaKegiatan(value: String) {
        _uiState.value = _uiState.value.copy(pesertaKegiatan = value)
    }

    fun updateJumlahPeserta(value: String) {
        // Only allow numbers
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(jumlahPeserta = value)
        }
    }

    fun updateKendala(value: String) {
        _uiState.value = _uiState.value.copy(kendala = value)
    }

    fun updateSolusi(value: String) {
        _uiState.value = _uiState.value.copy(solusi = value)
    }

    fun updateLinkReferensi(value: String) {
        _uiState.value = _uiState.value.copy(linkReferensi = value)
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

    /**
     * Validate form
     */
    private fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        val state = _uiState.value

        if (state.tanggalKegiatan.isEmpty()) {
            errors["tanggal_kegiatan"] = "Tanggal kegiatan wajib diisi"
        }

        if (state.kategoriId.isEmpty()) {
            errors["kategori_id"] = "Kategori kegiatan wajib dipilih"
        }

        if (state.namaKegiatan.isBlank()) {
            errors["nama_kegiatan"] = "Nama kegiatan wajib diisi"
        }

        if (state.deskripsiKegiatan.isBlank()) {
            errors["deskripsi_kegiatan"] = "Deskripsi kegiatan wajib diisi"
        }

        if (state.waktuMulai.isEmpty()) {
            errors["waktu_mulai"] = "Waktu mulai wajib diisi"
        }

        if (state.waktuSelesai.isEmpty()) {
            errors["waktu_selesai"] = "Waktu selesai wajib diisi"
        }

        if (state.waktuMulai.isNotEmpty() && state.waktuSelesai.isNotEmpty()) {
            if (state.waktuMulai >= state.waktuSelesai) {
                errors["waktu_selesai"] = "Waktu selesai harus lebih besar dari waktu mulai"
            }
        }

        _uiState.value = _uiState.value.copy(errors = errors)

        return errors.isEmpty()
    }

    /**
     * Submit laporan
     */
    fun submitLaporan(status: String) {
        if (!validateForm()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Mohon lengkapi semua field yang wajib diisi",
                requiresAttendance = false
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val state = _uiState.value

                // ✅ Get pegawai_id from StatistikRepository
                val pegawaiId = StatistikRepository.getPegawaiId()
                val pin = StatistikRepository.getPin()

                if (pegawaiId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Session expired. Please login again."
                    )
                    return@launch
                }

                if (pin == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "PIN tidak ditemukan. Please login again."
                    )
                    return@launch
                }

                val request = CreateLaporanRequest(
                    tanggalKegiatan = state.tanggalKegiatan,
                    kategoriId = state.kategoriId.toInt(),
                    namaKegiatan = state.namaKegiatan.trim(),
                    deskripsiKegiatan = state.deskripsiKegiatan.trim(),
                    targetOutput = state.targetOutput.takeIf { it.isNotBlank() },
                    hasilOutput = state.hasilOutput.takeIf { it.isNotBlank() },
                    waktuMulai = state.waktuMulai,
                    waktuSelesai = state.waktuSelesai,
                    lokasiKegiatan = state.lokasiKegiatan.takeIf { it.isNotBlank() },
                    latitude = state.latitude,
                    longitude = state.longitude,
                    pesertaKegiatan = state.pesertaKegiatan.takeIf { it.isNotBlank() },
                    jumlahPeserta = state.jumlahPeserta.toIntOrNull() ?: 0,
                    linkReferensi = state.linkReferensi.takeIf { it.isNotBlank() },
                    kendala = state.kendala.takeIf { it.isNotBlank() },
                    solusi = state.solusi.takeIf { it.isNotBlank() },
                    statusLaporan = status
                )

                val response = apiService.createLaporan(
                    request = request,
                    pegawaiId = pegawaiId,
                    pin = pin
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                } else {
                    // Parse error
                    val errorBody = response.errorBody()?.string()

                    if (errorBody != null) {
                        try {
                            val jsonObject = org.json.JSONObject(errorBody)

                            // ✅ Extract error message
                            val errorMessage = when {
                                jsonObject.has("error") -> jsonObject.getString("error")
                                jsonObject.has("message") -> jsonObject.getString("message")
                                else -> "Gagal menyimpan laporan"
                            }

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = errorMessage
                            )
                        } catch (e: Exception) {
                            // Fallback
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = errorBody
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            requiresAttendance = false
        )
    }
}