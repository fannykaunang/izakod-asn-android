package com.kominfo_mkq.izakod_asn.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.kominfo_mkq.izakod_asn.data.model.LaporanDetailResponse
import com.kominfo_mkq.izakod_asn.data.model.LaporanListResponse
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.UpdateLaporanResponse
import com.kominfo_mkq.izakod_asn.data.model.VerifikasiLaporanRequest
import com.kominfo_mkq.izakod_asn.data.model.VerifikasiLaporanResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient.eabsenApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import retrofit2.Response

class LaporanRepository {
    private val apiService = eabsenApiService
// TODO tampilan button verifikasi diatas tidak terlalu bagus dan saat verifikasi masih error 401
    /**
     * Verifikasi laporan (Terima, Revisi, atau Tolak)
     */
    suspend fun verifikasiLaporan(
        laporanId: Int,
        status: String,
        rating: Int?,
        catatan: String?
    ): Response<VerifikasiLaporanResponse> {
        android.util.Log.d("LaporanRepository", "📝 Verifying laporan: $laporanId")
        android.util.Log.d("LaporanRepository", "📊 Status: $status, Rating: $rating")

        val pegawaiId = StatistikRepository.getPegawaiId()

        val request = VerifikasiLaporanRequest(
            status_laporan = status,
            rating_kualitas = rating,
            catatan_verifikasi = catatan
        )

        return apiService.verifikasiLaporan(laporanId, request, pegawaiId)
    }

    /**
     * Get all laporan kegiatan
     */
    suspend fun getLaporanList(): Response<LaporanListResponse> {
        val pegawaiId = StatistikRepository.getPegawaiId() ?: throw Exception("Session expired")

        return eabsenApiService.getLaporanList(pegawaiId)
    }

    /**
     * Update existing laporan
     */
    suspend fun updateLaporan(
        laporanId: Int,
        request: UpdateLaporanRequest
    ): Response<UpdateLaporanResponse> {
        val pegawaiId = StatistikRepository.getPegawaiId()

        android.util.Log.d("LaporanRepository", "📝 Updating laporan_id: $laporanId")

        if (pegawaiId == null) {
            throw Exception("Session expired")
        }

        return eabsenApiService.updateLaporan(laporanId, request, pegawaiId)
    }

    /**
     * Get laporan detail by ID
     */
    suspend fun getLaporanDetail(laporanId: Int): Response<LaporanDetailResponse> {
        val pegawaiId = StatistikRepository.getPegawaiId()

        android.util.Log.d("LaporanRepository", "📋 Getting detail for laporan_id: $laporanId")

        if (pegawaiId == null) {
            throw Exception("Session expired")
        }

        return eabsenApiService.getLaporanDetail(laporanId, pegawaiId)
    }

    suspend fun uploadImages(
        context: Context,
        laporanId: Int,
        imageUris: List<Uri>
    ): UploadResult {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("LaporanRepository", "📤 Starting upload for laporan_id: $laporanId")
                android.util.Log.d("LaporanRepository", "📤 Number of images: ${imageUris.size}")

                val client = OkHttpClient()

                // Create multipart request body
                val requestBodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("laporan_id", laporanId.toString())

                // Add each image file
                imageUris.forEachIndexed { index, uri ->
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()

                        if (bytes != null) {
                            val fileName = getFileNameFromUri(context, uri) ?: "image_$index.jpg"
                            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

                            android.util.Log.d("LaporanRepository", "📎 Adding file: $fileName (${bytes.size} bytes)")

                            requestBodyBuilder.addFormDataPart(
                                "files",
                                fileName,
                                bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("LaporanRepository", "❌ Error reading image $index: ${e.message}")
                    }
                }

                val requestBody = requestBodyBuilder.build()

                // Build request
                val request = Request.Builder()
                    .url("${ApiClient.BASE_URL}/api/file-upload")
                    //.addHeader("EabsenApiKey", ApiClient.API_KEY)
                    .post(requestBody)
                    .build()

                android.util.Log.d("LaporanRepository", "📡 Sending request to: ${ApiClient.BASE_URL}/api/file-upload")

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                android.util.Log.d("LaporanRepository", "📡 Response code: ${response.code}")
                android.util.Log.d("LaporanRepository", "📡 Response body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val success = json.optBoolean("success", false)

                    if (success) {
                        android.util.Log.d("LaporanRepository", "✅ Upload successful!")
                        UploadResult(success = true, error = null)
                    } else {
                        val message = json.optString("message", "Upload failed")
                        android.util.Log.e("LaporanRepository", "❌ Upload failed: $message")
                        UploadResult(success = false, error = message)
                    }
                } else {
                    val error = "Upload failed: HTTP ${response.code}"
                    android.util.Log.e("LaporanRepository", "❌ $error")
                    UploadResult(success = false, error = error)
                }
            } catch (e: IOException) {
                android.util.Log.e("LaporanRepository", "❌ Network error: ${e.message}", e)
                UploadResult(success = false, error = "Network error: ${e.message}")
            } catch (e: Exception) {
                android.util.Log.e("LaporanRepository", "❌ Upload error: ${e.message}", e)
                UploadResult(success = false, error = e.message)
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
}

data class UploadResult(
    val success: Boolean,
    val error: String?
)