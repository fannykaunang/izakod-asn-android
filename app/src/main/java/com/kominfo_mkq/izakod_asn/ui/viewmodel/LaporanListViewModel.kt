package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiData
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
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
    val atasanPegawai: AtasanPegawaiData? = null,
    val isLoadingAtasan: Boolean = false,
    val errorAtasan: String? = null
)

class LaporanListViewModel : ViewModel() {

    private val repository = LaporanRepository()

    private val _uiState = MutableStateFlow(LaporanListUiState())
    val uiState: StateFlow<LaporanListUiState> = _uiState.asStateFlow()

    fun loadAtasanPegawai(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingAtasan = true,
                errorAtasan = null
            )

            try {
                val pegawaiId = UserPreferences(context).getPegawaiId()
                    ?: throw Exception("Session expired: pegawai_id tidak ditemukan")

                val response = withContext(Dispatchers.IO) {
                    repository.getAtasanPegawaiByBawahan(pegawaiId)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    _uiState.value = _uiState.value.copy(
                        atasanPegawai = data,
                        isLoadingAtasan = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingAtasan = false,
                        errorAtasan = response.body()?.message ?: "Gagal memuat data atasan"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingAtasan = false,
                    errorAtasan = e.message
                )
            }
        }
    }

    fun loadLaporanList(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, isError = false, errorMessage = null)

                val response = repository.getLaporanList(context)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (!body.success) throw Exception("Gagal memuat laporan")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        laporanList = body.data,
                        filterBulan = null,
                        filterTahun = null,
                        totalFiltered = null
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
                        totalFiltered = body.meta?.total
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

    fun clearFilter(context: Context) {
        loadLaporanList(context)
    }
}
