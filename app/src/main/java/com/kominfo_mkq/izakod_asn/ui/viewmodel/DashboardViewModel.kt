package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.local.AppContextHolder
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AssessmentSummaryData
import com.kominfo_mkq.izakod_asn.data.model.DashboardActionAlertsData
import com.kominfo_mkq.izakod_asn.data.model.DashboardTargetSummaryData
import com.kominfo_mkq.izakod_asn.data.model.MetricsData
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.TimeSeriesItem
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
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
    val isRefreshing: Boolean = false,
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
    val isDashboardOverviewLoading: Boolean = false,
    val hasDashboardOverviewLoaded: Boolean = false,
    val tertundaCount: Int? = null,
    val isDashboardTargetsLoading: Boolean = false,
    val hasDashboardTargetsLoaded: Boolean = false,
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
    private var hasStartedInitialLoad = false
    private var lastStatistikLoadedAt = 0L
    private var lastNotificationLoadedAt = 0L
    private var lastOverviewLoadedAt = 0L
    private var lastDashboardTargetsLoadedAt = 0L
    private var lastTertundaLoadedAt = 0L
    private var lastProfileLoadedAt = 0L
    private var handledRefreshVersion = 0

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

    fun consumeExternalRefreshVersion(version: Int): Boolean {
        if (version <= 0 || version <= handledRefreshVersion) {
            return false
        }
        handledRefreshVersion = version
        return true
    }

    /**
     * Load pegawai profile
     */
    fun loadPegawaiProfileIfNeeded(
        pin: String,
        force: Boolean = false
    ) {
        if (pin.isBlank()) return

        val shouldLoad = force ||
            _uiState.value.pegawaiProfile == null ||
            isStale(lastProfileLoadedAt, PROFILE_REFRESH_TTL_MS)
        if (!shouldLoad) return

        loadPegawaiProfile(pin)
    }

    fun loadPegawaiProfile(pin: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingProfile = true) }

                val response = ApiClient.eabsenApiService.getMobileProfile()

                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    val profile = response.body()!!.data!!

                    AppContextHolder.get()?.let { context ->
                        UserPreferences(context).saveProfileSnapshot(profile)
                    }

                    _uiState.update {
                        it.copy(
                            pegawaiProfile = profile,
                            photoUrl = profile.toEntagoPhotoUrl(),
                            isLoadingProfile = false
                        )
                    }
                    lastProfileLoadedAt = now()
                } else {
                    applyCachedProfileFallback()
                }
            } catch (_: Exception) {
                applyCachedProfileFallback()
            }
        }
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
                        lastNotificationLoadedAt = now()
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
    fun refreshIfNeeded(
        context: Context? = null,
        force: Boolean = false
    ) {
        val state = _uiState.value
        if (!force && (state.isLoading || state.isRefreshing || state.isDashboardOverviewLoading)) {
            return
        }

        val needsInitialLoad = !hasStartedInitialLoad ||
            state.metrics == null ||
            !state.hasDashboardOverviewLoaded ||
            state.tertundaCount == null

        if (needsInitialLoad) {
            refresh(
                context = context,
                showFullLoading = state.metrics == null && !state.hasDashboardOverviewLoaded
            )
            return
        }

        val needsRefresh = force ||
            isStale(lastStatistikLoadedAt) ||
            isStale(lastNotificationLoadedAt) ||
            isStale(lastOverviewLoadedAt) ||
            isStale(lastTertundaLoadedAt)

        if (needsRefresh) {
            refresh(context = context, showFullLoading = false)
        }
    }

    fun refresh(context: Context? = null, showFullLoading: Boolean = true) {
        hasStartedInitialLoad = true
        loadStatistik(showFullLoading = showFullLoading)
        loadNotificationCount()
        loadAssessmentSummary(
            tahun = selectedTargetYear,
            bulan = selectedTargetMonth
        )
        context?.let { loadTertundaCount(it.applicationContext) }
    }

    private fun applyCachedProfileFallback() {
        val cachedProfile = AppContextHolder.get()
            ?.let { context -> UserPreferences(context).getCachedPegawaiProfile() }

        if (cachedProfile != null) {
            _uiState.update {
                it.copy(
                    pegawaiProfile = cachedProfile,
                    photoUrl = cachedProfile.toEntagoPhotoUrl(),
                    isLoadingProfile = false
                )
            }
            lastProfileLoadedAt = now()
        } else {
            _uiState.update { it.copy(isLoadingProfile = false) }
        }
    }

    private fun PegawaiProfile.toEntagoPhotoUrl(): String? {
        val cleanPath = photoPath?.removePrefix("/") ?: return null
        return "https://entago.merauke.go.id/$cleanPath"
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
        tahun: Int? = null,
        showFullLoading: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = showFullLoading,
                        isRefreshing = !showFullLoading,
                        isError = false,
                        errorMessage = null
                    )
                }

                val finalPegawaiId = pegawaiId ?: StatistikRepository.getPegawaiId()

                if (finalPegawaiId == null) {
                    // GUNAKAN .update agar tidak meriset seluruh objek
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = true,
                            errorMessage = "Session expired"
                        )
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
                            isRefreshing = false,
                            isError = false,
                            metrics = response.data.data.metrics,
                            timeSeries = response.data.data.timeSeries,
                            isAdmin = response.data.data.isAdmin
                        )
                    }
                    lastStatistikLoadedAt = now()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isError = true,
                            errorMessage = response.error ?: "Gagal memuat data"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isError = true,
                        errorMessage = e.message ?: "Error"
                    )
                }
            }
        }
    }

    /**
     * Retry load statistik
     */
    fun retry(context: Context? = null) {
        refresh(context = context, showFullLoading = true)
    }

    fun loadTertundaCount(context: Context) {
        viewModelScope.launch {
            try {
                val snapshot = tertundaLoader.load(context.applicationContext)
                _uiState.update {
                    it.copy(tertundaCount = snapshot.total)
                }
                lastTertundaLoadedAt = now()
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

        val shouldReloadTargetDetails = _currentTabIndex.value == TARGET_TAB_INDEX ||
            _uiState.value.hasDashboardTargetsLoaded

        selectedTargetYear = tahun
        selectedTargetMonth = bulan
        _uiState.update {
            it.copy(
                targetPeriodYear = tahun,
                targetPeriodMonth = bulan,
                hasDashboardOverviewLoaded = false,
                isDashboardTargetsLoading = if (shouldReloadTargetDetails) true else it.isDashboardTargetsLoading,
                hasDashboardTargetsLoaded = if (shouldReloadTargetDetails) false else it.hasDashboardTargetsLoaded,
                targetItems = if (shouldReloadTargetDetails) emptyList() else it.targetItems
            )
        }
        loadAssessmentSummary(tahun = tahun, bulan = bulan)
        if (shouldReloadTargetDetails) {
            loadDashboardTargets(tahun = tahun, bulan = bulan)
        }
    }

    fun loadAssessmentSummary(
        tahun: Int? = selectedTargetYear,
        bulan: Int? = selectedTargetMonth
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isDashboardOverviewLoading = true) }

                val response = ApiClient.eabsenApiService.getDashboardOverview(
                    tahun = tahun,
                    bulan = bulan
                )
                val body = response.body()
                val data = body?.data

                if (response.isSuccessful && body?.success == true && data != null) {
                    _uiState.update {
                        it.copy(
                            assessmentSummary = data.assessmentSummary,
                            targetSummary = data.targetSummary,
                            actionAlerts = data.actionAlerts,
                            isDashboardOverviewLoading = false,
                            hasDashboardOverviewLoaded = true,
                            targetPeriodYear = tahun,
                            targetPeriodMonth = bulan
                        )
                    }
                    lastOverviewLoadedAt = now()
                } else {
                    _uiState.update {
                        it.copy(isDashboardOverviewLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isDashboardOverviewLoading = false)
                }
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
                _uiState.update { it.copy(isDashboardTargetsLoading = true) }

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
                            targetPeriodMonth = bulan,
                            isDashboardTargetsLoading = false,
                            hasDashboardTargetsLoaded = true
                        )
                    }
                    lastDashboardTargetsLoadedAt = now()
                } else {
                    _uiState.update {
                        it.copy(
                            isDashboardTargetsLoading = false,
                            hasDashboardTargetsLoaded = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDashboardTargetsLoading = false,
                        hasDashboardTargetsLoaded = true
                    )
                }
                android.util.Log.e(
                    "DashboardViewModel",
                    "❌ Error loading dashboard targets: ${e.message}"
                )
            }
        }
    }

    fun loadDashboardTargetsIfNeeded(force: Boolean = false) {
        val state = _uiState.value
        if (!force && state.isDashboardTargetsLoading) return

        val shouldLoad = force ||
            !state.hasDashboardTargetsLoaded ||
            isStale(lastDashboardTargetsLoadedAt)

        if (!shouldLoad) return

        loadDashboardTargets(
            tahun = selectedTargetYear,
            bulan = selectedTargetMonth
        )
    }

    private fun now(): Long = System.currentTimeMillis()

    private fun isStale(
        loadedAt: Long,
        ttlMillis: Long = DASHBOARD_REFRESH_TTL_MS
    ): Boolean {
        return loadedAt <= 0L || now() - loadedAt >= ttlMillis
    }

    private companion object {
        private const val DASHBOARD_REFRESH_TTL_MS = 60_000L
        private const val PROFILE_REFRESH_TTL_MS = 60_000L
        private const val TARGET_TAB_INDEX = 1
    }
}
