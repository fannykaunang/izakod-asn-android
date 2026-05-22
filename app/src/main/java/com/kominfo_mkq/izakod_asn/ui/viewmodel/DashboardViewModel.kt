package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.AssessmentSummaryData
import com.kominfo_mkq.izakod_asn.data.model.DashboardActionAlertsData
import com.kominfo_mkq.izakod_asn.data.model.DashboardTargetSummaryData
import com.kominfo_mkq.izakod_asn.data.model.MetricsData
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import com.kominfo_mkq.izakod_asn.data.model.TimeSeriesItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.remote.EabsenRetrofitClient
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
    val isAdmin: Boolean = false,
    val pegawaiProfile: PegawaiProfile? = null,
    val photoUrl: String? = null,
    val isLoadingProfile: Boolean = false,
    val unreadNotificationCount: Int = 0,
    val assessmentSummary: AssessmentSummaryData? = null,
    val targetSummary: DashboardTargetSummaryData? = null,
    val actionAlerts: DashboardActionAlertsData? = null,
    val tertundaCount: Int? = null,
    val targetItems: List<TargetKinerjaItem> = emptyList(),
    val currentPegawaiId: Int? = null,
    val targetPeriodYear: Int? = null,
    val targetPeriodMonth: Int? = null
)

/**
 * ViewModel untuk Dashboard
 * FIXED: Added refresh() function for auto-reload
 */
class DashboardViewModel : ViewModel() {

    private val repository = StatistikRepository()
    private val tertundaLoader = TertundaDataLoader()
    private val initialCalendar = Calendar.getInstance()
    private var selectedTargetYear: Int = initialCalendar.get(Calendar.YEAR)
    private var selectedTargetMonth: Int = initialCalendar.get(Calendar.MONTH) + 1

    // UI State
    private val _uiState = MutableStateFlow(
        DashboardUiState(
            targetPeriodYear = selectedTargetYear,
            targetPeriodMonth = selectedTargetMonth
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _currentTabIndex = MutableStateFlow(0)
    val currentTabIndex: StateFlow<Int> = _currentTabIndex.asStateFlow()

    fun updateTabIndex(index: Int) {
        _currentTabIndex.value = index
    }

    /**
     * Load pegawai profile
     */
    fun loadPegawaiProfile(pin: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingProfile = true) }

                val response = EabsenRetrofitClient.apiService.getPegawaiProfile(pin)

                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    val profile = response.body()!!.data!!

                    // Bersihkan path jika ada double slash
                    val cleanPath = profile.photoPath?.removePrefix("/")
                    val fullPhotoUrl = if (cleanPath != null) {
                        "https://entago.merauke.go.id/$cleanPath"
                    } else null

                    _uiState.update {
                        it.copy(
                            pegawaiProfile = profile,
                            photoUrl = fullPhotoUrl,
                            isLoadingProfile = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingProfile = false) }
            }
        }
    }

    init {
        // Load statistik saat ViewModel dibuat
        loadStatistik()
    }

    fun loadNotificationCount() {
        viewModelScope.launch {
            try {
                android.util.Log.d("DashboardViewModel", "🔔 Loading notification count...")
                val apiService = ApiClient.eabsenApiService

                val pegawaiId = StatistikRepository.getPegawaiId()
                    ?: throw Exception("Session expired")
                val response = apiService.getNotifications(pegawaiId)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        android.util.Log.d("DashboardViewModel", "✅ Unread notifications: ${body.unread}")

                        _uiState.value = _uiState.value.copy(
                            unreadNotificationCount = body.unread
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "❌ Error loading notification count: ${e.message}")
            }
        }
    }

    /**
     * ✅ PUBLIC refresh function
     * Call this when returning to Dashboard to reload data
     */
    fun refresh(context: Context? = null) {
        loadStatistik()
        loadNotificationCount()
        loadAssessmentSummary(
            tahun = selectedTargetYear,
            bulan = selectedTargetMonth
        )
        loadDashboardTargets(
            tahun = selectedTargetYear,
            bulan = selectedTargetMonth
        )
        context?.let { loadTertundaCount(it.applicationContext) }
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
                _uiState.update { it.copy(isLoading = true) }

                val finalPegawaiId = pegawaiId ?: StatistikRepository.getPegawaiId()

                if (finalPegawaiId == null) {
                    // GUNAKAN .update agar tidak meriset seluruh objek
                    _uiState.update {
                        it.copy(isLoading = false, isError = true, errorMessage = "Session expired")
                    }
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
                    // GUNAKAN .update agar data lama (seperti profile) tidak hilang
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isError = false,
                            metrics = response.data.data.metrics,
                            timeSeries = response.data.data.timeSeries,
                            isAdmin = response.data.data.isAdmin
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, isError = true, errorMessage = response.error ?: "Gagal memuat data")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isError = true, errorMessage = e.message ?: "Error")
                }
            }
        }
    }

    /**
     * Retry load statistik
     */
    fun retry(context: Context? = null) {
        loadStatistik()
        loadAssessmentSummary(
            tahun = selectedTargetYear,
            bulan = selectedTargetMonth
        )
        loadDashboardTargets(
            tahun = selectedTargetYear,
            bulan = selectedTargetMonth
        )
        context?.let { loadTertundaCount(it.applicationContext) }
    }

    fun loadTertundaCount(context: Context) {
        viewModelScope.launch {
            try {
                val snapshot = tertundaLoader.load(context.applicationContext)
                _uiState.update {
                    it.copy(tertundaCount = snapshot.total)
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "DashboardViewModel",
                    "Error loading tertunda count: ${e.message}"
                )
            }
        }
    }

    fun selectTargetPeriod(tahun: Int, bulan: Int) {
        if (tahun <= 0 || bulan !in 1..12) return

        selectedTargetYear = tahun
        selectedTargetMonth = bulan
        _uiState.update {
            it.copy(
                targetPeriodYear = tahun,
                targetPeriodMonth = bulan
            )
        }
        loadAssessmentSummary(tahun = tahun, bulan = bulan)
        loadDashboardTargets(tahun = tahun, bulan = bulan)
    }

    fun loadAssessmentSummary(
        tahun: Int? = selectedTargetYear,
        bulan: Int? = selectedTargetMonth
    ) {
        viewModelScope.launch {
            try {
                val response = ApiClient.eabsenApiService.getDashboardOverview(
                    tahun = tahun,
                    bulan = bulan
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update {
                        it.copy(
                            assessmentSummary = response.body()?.data?.assessmentSummary,
                            targetSummary = response.body()?.data?.targetSummary,
                            actionAlerts = response.body()?.data?.actionAlerts,
                            targetPeriodYear = tahun,
                            targetPeriodMonth = bulan
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "DashboardViewModel",
                    "❌ Error loading assessment summary: ${e.message}"
                )
            }
        }
    }

    fun loadDashboardTargets(
        tahun: Int? = selectedTargetYear,
        bulan: Int? = selectedTargetMonth
    ) {
        viewModelScope.launch {
            try {
                val response = ApiClient.eabsenApiService.getTargetKinerjaList(
                    tahun = tahun,
                    bulan = bulan
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()
                    _uiState.update {
                        it.copy(
                            targetItems = body?.data ?: emptyList(),
                            currentPegawaiId = body?.meta?.currentPegawaiId ?: it.currentPegawaiId,
                            targetPeriodYear = tahun,
                            targetPeriodMonth = bulan
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "DashboardViewModel",
                    "❌ Error loading dashboard targets: ${e.message}"
                )
            }
        }
    }
}
