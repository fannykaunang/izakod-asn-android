package com.kominfo_mkq.izakod_asn.data.remote

import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API Service for ASP.NET Core Eabsen Server
 * Base URL: https://dev.api.eabsen.merauke.go.id/
 */
interface EabsenCoreApiService {

    /**
     * Get pegawai profile by PIN
     * GET /api/pegawai/{pin}
     */
    @GET("api/pegawai/{pin}")
    suspend fun getPegawaiProfile(
        @Path("pin") pin: String
    ): Response<PegawaiProfile>
}