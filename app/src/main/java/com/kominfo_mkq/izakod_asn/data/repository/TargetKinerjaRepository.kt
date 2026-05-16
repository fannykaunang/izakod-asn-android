package com.kominfo_mkq.izakod_asn.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.kominfo_mkq.izakod_asn.data.model.ApiResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaHistoryItem
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaListResponse
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.model.RealisasiLinkLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.RealisasiLinkedLaporanItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaHistoryItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaListResponse
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaMutationResponse
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaRequest
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaReviewRequest
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TargetKinerjaRepository {

    private val apiService = ApiClient.eabsenApiService

    suspend fun getTargetKinerjaList(
        tahun: Int? = null,
        bulan: Int? = null,
        status: String? = null,
        pegawaiId: Int? = null
    ): ApiResponse<TargetKinerjaListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTargetKinerjaList(
                tahun = tahun,
                bulan = bulan,
                status = status,
                pegawaiId = pegawaiId
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat target kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun getTargetKinerjaDetail(
        targetId: Int
    ): ApiResponse<TargetKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTargetKinerjaDetail(targetId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat detail target kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun getTargetKinerjaHistory(
        targetId: Int
    ): ApiResponse<List<TargetKinerjaHistoryItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTargetKinerjaHistory(targetId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body.data)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat riwayat target kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun createTargetKinerja(
        request: TargetKinerjaRequest
    ): ApiResponse<TargetKinerjaMutationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createTargetKinerja(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal membuat target kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun updateTargetKinerja(
        targetId: Int,
        request: TargetKinerjaRequest
    ): ApiResponse<TargetKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateTargetKinerja(targetId, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memperbarui target kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun deleteTargetKinerja(
        targetId: Int
    ): ApiResponse<TargetKinerjaMutationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTargetKinerja(targetId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal menghapus target kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun submitTargetKinerja(
        targetId: Int
    ): ApiResponse<TargetKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.submitTargetKinerja(targetId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal mengajukan target kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun reviewTargetKinerja(
        targetId: Int,
        aksi: String,
        catatanAtasan: String?
    ): ApiResponse<TargetKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.reviewTargetKinerja(
                targetId,
                TargetKinerjaReviewRequest(
                    aksi = aksi,
                    catatanAtasan = catatanAtasan
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memproses review target kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun getRealisasiKinerjaList(
        targetKinerjaId: Int,
        pegawaiId: Int? = null
    ): ApiResponse<RealisasiKinerjaListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRealisasiKinerjaList(
                targetKinerjaId = targetKinerjaId,
                pegawaiId = pegawaiId
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat realisasi kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun getRealisasiKinerjaHistory(
        realisasiId: Int
    ): ApiResponse<List<RealisasiKinerjaHistoryItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRealisasiKinerjaHistory(realisasiId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body.data)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat riwayat realisasi kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun saveRealisasiKinerja(
        detailId: Int,
        realisasiKuantitas: Double?,
        realisasiKualitas: Double?,
        realisasiWaktu: Double?,
        catatan: String?
    ): ApiResponse<RealisasiKinerjaDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.saveRealisasiKinerja(
                RealisasiKinerjaRequest(
                    targetKinerjaDetailId = detailId,
                    realisasiKuantitas = realisasiKuantitas,
                    realisasiKualitas = realisasiKualitas,
                    realisasiWaktu = realisasiWaktu,
                    catatan = catatan
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal menyimpan realisasi kinerja"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun getLinkedLaporan(
        realisasiId: Int
    ): ApiResponse<List<RealisasiLinkedLaporanItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRealisasiLinkedLaporan(realisasiId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body.data)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal memuat laporan pendukung"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun linkLaporanToRealisasi(
        realisasiId: Int,
        laporanId: Int
    ): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.linkLaporanToRealisasi(
                realisasiId = realisasiId,
                request = RealisasiLinkLaporanRequest(laporanId = laporanId)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body.message)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal menautkan laporan kegiatan"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    suspend fun unlinkLaporanFromRealisasi(
        realisasiId: Int,
        laporanId: Int
    ): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.unlinkLaporanFromRealisasi(
                realisasiId = realisasiId,
                laporanId = laporanId
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    ApiResponse(success = true, data = body.message)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.message ?: "Gagal melepas laporan kegiatan"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCandidateLaporanForTarget(
        target: TargetKinerjaItem
    ): ApiResponse<List<LaporanKegiatan>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLaporanList()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val filtered = body.data.filter { laporan ->
                        if (laporan.pegawaiId != target.pegawaiId) return@filter false
                        val datePart = laporan.tanggalKegiatan.take(10)
                        val localDate = java.time.LocalDate.parse(datePart)
                        localDate.year == target.tahun && localDate.monthValue == target.bulan
                    }
                    ApiResponse(success = true, data = filtered)
                } else {
                    ApiResponse(
                        success = false,
                        error = body?.let { "Gagal memuat daftar laporan kegiatan" }
                            ?: "Gagal memuat daftar laporan kegiatan"
                    )
                }
            } else {
                ApiResponse(
                    success = false,
                    error = "Error: ${response.code()} ${response.message()}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(success = false, error = e.message ?: "Network error")
        }
    }
}
