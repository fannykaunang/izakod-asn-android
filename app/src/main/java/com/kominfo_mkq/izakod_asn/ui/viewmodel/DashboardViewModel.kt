package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.MetricsData
import com.kominfo_mkq.izakod_asn.data.model.TimeSeriesItem
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * UI State untuk Dashboard
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val metrics: MetricsData? = null,
    val timeSeries: List<TimeSeriesItem> = emptyList(),
    val isAdmin: Boolean = false
)

/**
 * ViewModel untuk Dashboard
 * FIXED: Added refresh() function for auto-reload
 */
class DashboardViewModel : ViewModel() {

    private val repository = StatistikRepository()

    // UI State
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Load statistik saat ViewModel dibuat
        loadStatistik()
    }

    /**
     * ✅ PUBLIC refresh function
     * Call this when returning to Dashboard to reload data
     */
    fun refresh() {
        loadStatistik()
    }

    /**
     * Load statistik bulanan
     * ✅ FIXED: Auto-get pegawai_id from StatistikRepository if not provided
     * ✅ FIXED: Auto-get current month/year if not provided
     */
    fun loadStatistik(
        skpdid: Int? = null,
        pegawaiId: Int? = null,
        bulan: Int? = null,
        tahun: Int? = null
    ) {
        viewModelScope.launch {
            try {
                // ✅ Set loading FIRST
                _uiState.value = DashboardUiState(isLoading = true)

                val finalPegawaiId = pegawaiId ?: StatistikRepository.getPegawaiId()

                if (finalPegawaiId == null) {
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        isError = true,
                        errorMessage = "Session expired"
                    )
                    return@launch
                }

                val calendar = Calendar.getInstance()
                val finalBulan = bulan ?: (calendar.get(Calendar.MONTH) + 1)
                val finalTahun = tahun ?: calendar.get(Calendar.YEAR)

                val response = repository.getStatistikBulanan(
                    skpdid = skpdid,
                    pegawaiId = finalPegawaiId,
                    bulan = finalBulan,
                    tahun = finalTahun
                )

                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isError = false,
                        metrics = response.data.data.metrics,
                        timeSeries = response.data.data.timeSeries,
                        isAdmin = response.data.data.isAdmin
                    )

                    android.util.Log.d("DashboardViewModel", "✅ UI State updated with metrics")
                } else {
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        isError = true,
                        errorMessage = response.error ?: "Gagal memuat data"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Error"
                )
            }
        }
    }

    /**
     * Retry load statistik
     */
    fun retry() {
        loadStatistik()
    }
}