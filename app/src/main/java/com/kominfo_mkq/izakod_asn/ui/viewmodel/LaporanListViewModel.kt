package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
import com.kominfo_mkq.izakod_asn.data.repository.LaporanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LaporanListUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val laporanList: List<LaporanKegiatan> = emptyList(),
    val filterBulan: Int? = null,
    val filterTahun: Int? = null,
    val totalFiltered: Int? = null
)

class LaporanListViewModel : ViewModel() {

    private val repository = LaporanRepository()

    private val _uiState = MutableStateFlow(LaporanListUiState())
    val uiState: StateFlow<LaporanListUiState> = _uiState.asStateFlow()

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
