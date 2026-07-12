package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiData
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
import com.kominfo_mkq.izakod_asn.data.model.LaporanCetakData
import com.kominfo_mkq.izakod_asn.data.model.LaporanMeta
import com.kominfo_mkq.izakod_asn.data.repository.LaporanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LaporanListUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val laporanList: List<LaporanKegiatan> = emptyList(),
    val filterBulan: Int? = null,
    val filterTahun: Int? = null,
    val totalFiltered: Int? = null,
    val cetakData: LaporanCetakData? = null,
    val laporanMeta: LaporanMeta? = null,
    val hasActiveSubordinates: Boolean = false,
    val ownActionCount: Int = 0,
    val subordinateActionCount: Int = 0,
    val atasanPegawai: AtasanPegawaiData? = null,
    val isLoadingAtasan: Boolean = false,
    val errorAtasan: String? = null
)

class LaporanListViewModel : ViewModel() {

    private val repository = LaporanRepository()

    private val _uiState = MutableStateFlow(LaporanListUiState())
    val uiState: StateFlow<LaporanListUiState> = _uiState.asStateFlow()

//    fun loadAtasanPegawai(context: Context) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(
//                isLoadingAtasan = true,
//                errorAtasan = null
//            )
//
//            try {
//                val pegawaiId = UserPreferences(context).getPegawaiId()
//                    ?: throw Exception("Session expired: pegawai_id tidak ditemukan")
//
//                val response = withContext(Dispatchers.IO) {
//                    repository.getAtasanPegawaiByBawahan(pegawaiId)
//                }
//
//                if (response.isSuccessful && response.body()?.success == true) {
//                    val data = response.body()?.data
//                    _uiState.value = _uiState.value.copy(
//                        atasanPegawai = data,
//                        isLoadingAtasan = false
//                    )
//                } else {
//                    _uiState.value = _uiState.value.copy(
//                        isLoadingAtasan = false,
//                        errorAtasan = response.body()?.message ?: "Gagal memuat data atasan"
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoadingAtasan = false,
//                    errorAtasan = e.message
//                )
//            }
//        }
//    }

    fun loadAtasanPegawai(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingAtasan = true,
                errorAtasan = null,
                atasanPegawai = null
            )

            try {
                val pegawaiId = UserPreferences(context).getPegawaiId()
                    ?: throw Exception("Session expired: pegawai_id tidak ditemukan")

                val response = withContext(Dispatchers.IO) {
                    repository.getAtasanPegawaiByBawahan(pegawaiId)
                }

                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    val err = response.errorBody()?.string()
                    Log.e(TAG, "loadAtasanPegawai failed: code=${response.code()} err=$err")
                    _uiState.value = _uiState.value.copy(
                        isLoadingAtasan = false,
                        errorAtasan = "Gagal memuat atasan (code=${response.code()})"
                    )
                    return@launch
                }

                // ✅ success=true tapi data=null
                if (body.success == true && body.data == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingAtasan = false,
                        atasanPegawai = null,
                        errorAtasan = "Data atasan belum tersedia (data kosong)."
                    )
                    return@launch
                }

                // ✅ success & data ada
                if (body.success == true && body.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingAtasan = false,
                        atasanPegawai = body.data,
                        errorAtasan = null
                    )
                    return@launch
                }

                // ✅ success=false
                _uiState.value = _uiState.value.copy(
                    isLoadingAtasan = false,
                    errorAtasan = body.message ?: "Gagal memuat data atasan"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingAtasan = false,
                    errorAtasan = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun loadLaporanList(
        context: Context,
        includeSubordinates: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, isError = false, errorMessage = null)

                val response = repository.getLaporanList(
                    context = context,
                    includeSubordinates = includeSubordinates
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (!body.success) throw Exception("Gagal memuat laporan")
                    val hasActiveSubordinates = body.meta?.isAtasan == true ||
                        body.meta?.supervisedPegawaiIds?.isNotEmpty() == true ||
                        _uiState.value.hasActiveSubordinates
                    val subordinateActionCount = if (includeSubordinates) {
                        body.data.count { it.isSubordinateActionReport() }
                    } else {
                        _uiState.value.subordinateActionCount
                    }
                    val ownActionCount = if (includeSubordinates) {
                        _uiState.value.ownActionCount
                    } else {
                        body.data.count { it.isOwnActionReport() }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        laporanList = body.data,
                        laporanMeta = body.meta,
                        hasActiveSubordinates = hasActiveSubordinates,
                        ownActionCount = ownActionCount,
                        subordinateActionCount = subordinateActionCount,
                        filterBulan = null,
                        filterTahun = null,
                        totalFiltered = null,
                        cetakData = null
                    )
                    if (!includeSubordinates && hasActiveSubordinates) {
                        refreshSubordinateActionCount(context)
                    } else if (includeSubordinates) {
                        refreshOwnActionCount(context)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isError = true, errorMessage = e.message)
            }
        }
    }

    fun loadLaporanBulanan(context: Context, bulan: Int, tahun: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, isError = false, errorMessage = null)

                val response = repository.getLaporanBulananCetak(context, bulan, tahun)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (!body.success) throw Exception("Gagal memuat laporan bulanan")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        laporanList = body.data?.laporan ?: emptyList(),
                        filterBulan = body.meta?.bulan,
                        filterTahun = body.meta?.tahun,
                        totalFiltered = body.meta?.total,
                        cetakData = body.data,
                        ownActionCount = body.data?.laporan.orEmpty().count { it.isOwnActionReport() }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isError = true, errorMessage = e.message)
            }
        }
    }

    fun clearFilter(
        context: Context,
        includeSubordinates: Boolean = false
    ) {
        loadLaporanList(context, includeSubordinates)
    }

    private suspend fun refreshSubordinateActionCount(context: Context) {
        try {
            val response = repository.getLaporanList(
                context = context,
                includeSubordinates = true
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                _uiState.value = _uiState.value.copy(
                    subordinateActionCount = body.data.count { it.isSubordinateActionReport() },
                    hasActiveSubordinates = body.meta?.isAtasan == true ||
                        body.meta?.supervisedPegawaiIds?.isNotEmpty() == true ||
                        _uiState.value.hasActiveSubordinates
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "refreshSubordinateActionCount failed: ${e.message}")
        }
    }

    private suspend fun refreshOwnActionCount(context: Context) {
        try {
            val response = repository.getLaporanList(
                context = context,
                includeSubordinates = false
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                _uiState.value = _uiState.value.copy(
                    ownActionCount = body.data.count { it.isOwnActionReport() }
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "refreshOwnActionCount failed: ${e.message}")
        }
    }
}

private fun LaporanKegiatan.isOwnActionReport(): Boolean {
    return when (statusLaporan.trim().lowercase()) {
        "draft", "ditolak", "rejected", "revisi", "perlu revisi", "revised", "revision" -> true
        else -> false
    }
}

private fun LaporanKegiatan.isSubordinateActionReport(): Boolean {
    return when (statusLaporan.trim().lowercase()) {
        "diajukan", "pending", "revisi", "perlu revisi", "revised", "revision" -> true
        else -> false
    }
}
