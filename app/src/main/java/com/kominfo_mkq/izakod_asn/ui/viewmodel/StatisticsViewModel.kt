package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.DailyMetricsData
import com.kominfo_mkq.izakod_asn.data.model.DailyTimeSeriesItem
import com.kominfo_mkq.izakod_asn.data.model.MetricsData
import com.kominfo_mkq.izakod_asn.data.model.TimeSeriesItem
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val dailyMetrics: DailyMetricsData? = null,
    val dailyTimeSeries: List<DailyTimeSeriesItem> = emptyList(),
    val monthlyMetrics: MetricsData? = null,
    val monthlyTimeSeries: List<TimeSeriesItem> = emptyList(),
    val summaryMetrics: MetricsData? = null
)

class StatisticsViewModel : ViewModel() {

    private val repository = StatistikRepository()

    private val _uiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isError = false,
                    errorMessage = null
                )
            }

            try {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                val currentYear = calendar.get(Calendar.YEAR)
                val dailyDeferred = async { repository.getStatistikHarian() }
                val monthlyDeferred = async { repository.getStatistikBulanan() }
                val summaryDeferred = async {
                    repository.getStatistikBulanan(
                        bulan = currentMonth,
                        tahun = currentYear
                    )
                }

                val dailyResponse = dailyDeferred.await()
                val monthlyResponse = monthlyDeferred.await()
                val summaryResponse = summaryDeferred.await()

                val dailyData = dailyResponse.data?.data
                val monthlyData = monthlyResponse.data?.data
                val summaryData = summaryResponse.data?.data

                val hasError = !dailyResponse.success && !monthlyResponse.success && !summaryResponse.success
                val errorMessage =
                    dailyResponse.error
                        ?: monthlyResponse.error
                        ?: summaryResponse.error
                        ?: "Gagal memuat data statistik"

                _uiState.value = StatisticsUiState(
                    isLoading = false,
                    isError = hasError,
                    errorMessage = if (hasError) errorMessage else null,
                    dailyMetrics = dailyData?.metrics,
                    dailyTimeSeries = dailyData?.timeSeries.orEmpty(),
                    monthlyMetrics = monthlyData?.metrics,
                    monthlyTimeSeries = monthlyData?.timeSeries.orEmpty(),
                    summaryMetrics = summaryData?.metrics
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = StatisticsUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }
}
