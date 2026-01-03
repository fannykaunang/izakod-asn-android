package com.kominfo_mkq.izakod_asn.data.repository

import com.kominfo_mkq.izakod_asn.data.model.BasicActionResponse
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateRequest
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateResponse
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanResponse
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import retrofit2.Response

class TemplateRepository {

    private val apiService = ApiClient.eabsenApiService

    /**
     * Get all templates (public + unit kerja user)
     */
    suspend fun getAllTemplates(pegawaiId: Int? = null): Response<TemplateKegiatanResponse> {
        android.util.Log.d("TemplateRepository", "📋 Getting all templates")
        return apiService.getTemplateKegiatan(
            pegawaiId = pegawaiId,
            kategoriId = null,
            isPublic = null,
            unitKerja = null
        )
    }

    /**
     * Get templates by kategori
     */
    suspend fun getTemplatesByKategori(kategoriId: Int): Response<TemplateKegiatanResponse> {
        android.util.Log.d("TemplateRepository", "📋 Getting templates for kategori: $kategoriId")
        return apiService.getTemplateKegiatan(
            pegawaiId = null,
            kategoriId = kategoriId,
            isPublic = null,
            unitKerja = null
        )
    }

//    /**
//     * Get only public templates
//     */
//    suspend fun getPublicTemplates(): Response<TemplateKegiatanResponse> {
//        android.util.Log.d("TemplateRepository", "📋 Getting public templates")
//        return apiService.getTemplateKegiatan(isPublic = 1)
//    }

//    /**
//     * Get templates by unit kerja
//     */
//    suspend fun getTemplatesByUnitKerja(unitKerja: String): Response<TemplateKegiatanResponse> {
//        android.util.Log.d("TemplateRepository", "📋 Getting templates for unit: $unitKerja")
//        return apiService.getTemplateKegiatan(unitKerja = unitKerja)
//    }

    suspend fun createTemplate(request: TemplateKegiatanCreateRequest): Response<TemplateKegiatanCreateResponse> {
        return apiService.createTemplateKegiatan(request)
    }

    suspend fun updateTemplate(templateId: Int, request: TemplateKegiatanCreateRequest): Response<BasicActionResponse> {
        return apiService.updateTemplateKegiatan(templateId, request)
    }

    suspend fun deleteTemplate(templateId: Int): Response<BasicActionResponse> {
        return apiService.deleteTemplateKegiatan(templateId)
    }
}