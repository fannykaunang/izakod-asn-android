package com.kominfo_mkq.izakod_asn.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiData
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanItem
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanRequest
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanVerifyRequest
import com.kominfo_mkq.izakod_asn.data.model.KandidatBawahanItem
import com.kominfo_mkq.izakod_asn.data.repository.AtasanPegawaiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AtasanPegawaiUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isMutating: Boolean = false,
    val currentPegawaiId: Int? = null,
    val canManagePersonalSubordinates: Boolean = false,
    val bawahan: List<AtasanPegawaiData> = emptyList(),
    val kandidat: List<KandidatBawahanItem> = emptyList(),
    val usulan: List<AtasanPegawaiUsulanItem> = emptyList(),
    val errorMessage: String? = null,
    val actionMessage: String? = null
)

class AtasanPegawaiViewModel : ViewModel() {

    private val repository = AtasanPegawaiRepository()
    private val _uiState = MutableStateFlow(AtasanPegawaiUiState())
    val uiState: StateFlow<AtasanPegawaiUiState> = _uiState.asStateFlow()

    fun load(context: Context, silent: Boolean = false) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            val currentPegawaiId = UserPreferences(appContext).getPegawaiId()
            _uiState.update {
                it.copy(
                    isLoading = !silent,
                    isRefreshing = silent,
                    currentPegawaiId = currentPegawaiId,
                    errorMessage = null
                )
            }

            val bawahanResult = repository.getBawahanSaya()
            val kandidatResult = repository.getKandidatBawahan(limit = 100)
            val usulanResult = repository.getUsulan(limit = 100, scope = "personal")

            val firstError = listOf(
                bawahanResult.error,
                kandidatResult.error,
                usulanResult.error
            ).firstOrNull { !it.isNullOrBlank() }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    canManagePersonalSubordinates = kandidatResult.success,
                    bawahan = bawahanResult.data.orEmpty(),
                    kandidat = kandidatResult.data.orEmpty(),
                    usulan = usulanResult.data.orEmpty(),
                    errorMessage = firstError
                )
            }
        }
    }

    fun loadVerificationQueue(context: Context, silent: Boolean = false) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            val currentPegawaiId = UserPreferences(appContext).getPegawaiId()
            _uiState.update {
                it.copy(
                    isLoading = !silent,
                    isRefreshing = silent,
                    currentPegawaiId = currentPegawaiId,
                    errorMessage = null
                )
            }

            val usulanResult = repository.getUsulan(limit = 200)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    usulan = usulanResult.data.orEmpty(),
                    errorMessage = usulanResult.error
                )
            }
        }
    }

    fun saveProposal(
        context: Context,
        request: AtasanPegawaiUsulanRequest,
        existingUsulanId: Int? = null,
        submitNow: Boolean
    ) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            _uiState.update {
                it.copy(isMutating = true, errorMessage = null, actionMessage = null)
            }

            val result = when {
                existingUsulanId == null -> repository.createUsulan(request.copy(submit = submitNow))
                submitNow -> repository.submitUsulan(existingUsulanId, request.copy(submit = true))
                else -> repository.updateUsulan(existingUsulanId, request.copy(submit = false))
            }

            if (result.success) {
                DashboardRefreshNotifier.markDirty()
                _uiState.update {
                    it.copy(
                        isMutating = false,
                        actionMessage = result.data?.message
                            ?: if (submitNow) "Usulan berhasil diajukan" else "Draft usulan berhasil disimpan"
                    )
                }
                load(appContext, silent = true)
            } else {
                _uiState.update {
                    it.copy(
                        isMutating = false,
                        errorMessage = result.error ?: "Gagal menyimpan usulan"
                    )
                }
            }
        }
    }

    fun submitDraft(context: Context, item: AtasanPegawaiUsulanItem) {
        val currentPegawaiId = _uiState.value.currentPegawaiId ?: return
        val request = item.toRequest(currentPegawaiId).copy(submit = true)
        saveProposal(
            context = context,
            request = request,
            existingUsulanId = item.id,
            submitNow = true
        )
    }

    fun cancelProposal(context: Context, item: AtasanPegawaiUsulanItem) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            _uiState.update {
                it.copy(isMutating = true, errorMessage = null, actionMessage = null)
            }

            val result = repository.cancelUsulan(item.id)
            if (result.success) {
                DashboardRefreshNotifier.markDirty()
                _uiState.update {
                    it.copy(
                        isMutating = false,
                        actionMessage = result.data?.message ?: "Usulan berhasil dibatalkan"
                    )
                }
                load(appContext, silent = true)
            } else {
                _uiState.update {
                    it.copy(
                        isMutating = false,
                        errorMessage = result.error ?: "Gagal membatalkan usulan"
                    )
                }
            }
        }
    }

    fun verifyProposal(
        context: Context,
        item: AtasanPegawaiUsulanItem,
        keputusan: String,
        catatan: String? = null,
        verificationOnly: Boolean = false
    ) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            _uiState.update {
                it.copy(isMutating = true, errorMessage = null, actionMessage = null)
            }

            val request = AtasanPegawaiUsulanVerifyRequest(
                keputusan = keputusan,
                catatanVerifikasi = catatan?.trim()?.takeIf { it.isNotBlank() }
            )
            val result = repository.verifyUsulan(item.id, request)
            if (result.success) {
                DashboardRefreshNotifier.markDirty()
                _uiState.update {
                    it.copy(
                        isMutating = false,
                        actionMessage = result.data?.message
                            ?: if (keputusan == "setuju") {
                                "Usulan berhasil disetujui"
                            } else {
                                "Usulan berhasil ditolak"
                            }
                    )
                }
                if (verificationOnly) {
                    loadVerificationQueue(appContext, silent = true)
                } else {
                    load(appContext, silent = true)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isMutating = false,
                        errorMessage = result.error ?: "Gagal memverifikasi usulan"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(errorMessage = null, actionMessage = null)
        }
    }
}

fun AtasanPegawaiUsulanItem.toRequest(currentPegawaiId: Int): AtasanPegawaiUsulanRequest {
    return AtasanPegawaiUsulanRequest(
        aksi = aksi,
        targetAtasanPegawaiId = targetAtasanPegawaiId,
        pegawaiId = pegawaiId,
        atasanId = atasanId.takeIf { it > 0 } ?: currentPegawaiId,
        jenisAtasan = jenisAtasan,
        tanggalMulai = tanggalMulai.orEmpty(),
        tanggalSelesai = tanggalSelesai,
        alasanPengajuan = alasanPengajuan,
        keterangan = keterangan,
        submit = false
    )
}
