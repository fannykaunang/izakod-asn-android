package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeData
import com.kominfo_mkq.izakod_asn.data.repository.GajiNonAsnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class GajiSayaUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val tahun: Int = Calendar.getInstance().get(Calendar.YEAR),
    val bulan: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val data: GajiNonAsnMeData? = null
)

class GajiSayaViewModel : ViewModel() {

    private val repository = GajiNonAsnRepository()

    private val _uiState = MutableStateFlow(GajiSayaUiState(isLoading = true))
    val uiState: StateFlow<GajiSayaUiState> = _uiState.asStateFlow()

    fun refresh() {
        Log.d(
            TAG,
            "refresh requested: tahun=${_uiState.value.tahun}, bulan=${_uiState.value.bulan}, " +
                "hasData=${_uiState.value.data != null}, " +
                "hasCalculation=${_uiState.value.data?.perhitungan != null}, " +
                "total=${_uiState.value.data?.perhitungan?.totalDibayar}"
        )
        loadGajiSaya(
            tahun = _uiState.value.tahun,
            bulan = _uiState.value.bulan,
            refreshOnly = _uiState.value.data != null
        )
    }

    fun setPeriod(tahun: Int, bulan: Int) {
        val shouldSkip = tahun == _uiState.value.tahun &&
            bulan == _uiState.value.bulan &&
            _uiState.value.data != null
        Log.d(
            TAG,
            "setPeriod requested: tahun=$tahun, bulan=$bulan, current=${_uiState.value.tahun}-${_uiState.value.bulan}, " +
                "hasData=${_uiState.value.data != null}, hasCalculation=${_uiState.value.data?.perhitungan != null}, " +
                "willSkip=$shouldSkip"
        )
        if (tahun == _uiState.value.tahun && bulan == _uiState.value.bulan && _uiState.value.data != null) {
            return
        }

        _uiState.value = _uiState.value.copy(
            tahun = tahun,
            bulan = bulan
        )
        loadGajiSaya(tahun, bulan)
    }

    fun moveMonth(offset: Int) {
        val current = _uiState.value
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, current.tahun)
            set(Calendar.MONTH, current.bulan - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, offset)
        }

        setPeriod(
            tahun = calendar.get(Calendar.YEAR),
            bulan = calendar.get(Calendar.MONTH) + 1
        )
    }

    private fun loadGajiSaya(
        tahun: Int,
        bulan: Int,
        refreshOnly: Boolean = false
    ) {
        viewModelScope.launch {
            Log.d(TAG, "loadGajiSaya start: tahun=$tahun, bulan=$bulan, refreshOnly=$refreshOnly")
            _uiState.value = _uiState.value.copy(
                isLoading = !refreshOnly,
                isRefreshing = refreshOnly,
                isError = false,
                errorMessage = null
            )

            val response = repository.getGajiSaya(
                tahun = tahun,
                bulan = bulan
            )

            if (_uiState.value.tahun != tahun || _uiState.value.bulan != bulan) {
                Log.d(
                    TAG,
                    "loadGajiSaya ignored stale response: requested=$tahun-$bulan, current=${_uiState.value.tahun}-${_uiState.value.bulan}"
                )
                return@launch
            }

            Log.d(
                TAG,
                "loadGajiSaya response: success=${response.success}, hasData=${response.data?.data != null}, " +
                    "hasCalculation=${response.data?.data?.perhitungan != null}, " +
                    "total=${response.data?.data?.perhitungan?.totalDibayar}, " +
                    "status=${response.data?.data?.perhitungan?.status}, error=${response.error}"
            )

            if (response.success && response.data?.data != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isError = false,
                    errorMessage = null,
                    data = response.data.data
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isError = true,
                    errorMessage = response.error ?: "Gagal memuat Gaji Saya"
                )
            }
        }
    }

    private companion object {
        private const val TAG = "IZAKOD_GAJI_VM"
    }
}
