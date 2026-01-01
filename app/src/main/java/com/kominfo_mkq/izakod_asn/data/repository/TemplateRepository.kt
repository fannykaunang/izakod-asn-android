package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import retrofit2.Response

class TemplateRepository {

    private val apiService = ApiClient.eabsenApiService

    /**
     * Get all templates (public + unit kerja user)
     */
    suspend fun getAllTemplates(): Response<TemplateKegiatanResponse> {
        android.util.Log.d("TemplateRepository", "📋 Getting all templates")
        return apiService.getTemplateKegiatan()
    }

    /**
     * Get templates by kategori
     */
    suspend fun getTemplatesByKategori(kategoriId: Int): Response<TemplateKegiatanResponse> {
        android.util.Log.d("TemplateRepository", "📋 Getting templates for kategori: $kategoriId")
        return apiService.getTemplateKegiatan(kategoriId = kategoriId)
    }

    /**
     * Get only public templates
     */
    suspend fun getPublicTemplates(): Response<TemplateKegiatanResponse> {
        android.util.Log.d("TemplateRepository", "📋 Getting public templates")
        return apiService.getTemplateKegiatan(isPublic = 1)
    }

    /**
     * Get templates by unit kerja
     */
    suspend fun getTemplatesByUnitKerja(unitKerja: String): Response<TemplateKegiatanResponse> {
        android.util.Log.d("TemplateRepository", "📋 Getting templates for unit: $unitKerja")
        return apiService.getTemplateKegiatan(unitKerja = unitKerja)
    }
}