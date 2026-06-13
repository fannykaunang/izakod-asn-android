package com.kominfo_mkq.izakod_asn.data.remote

import com.google.gson.JsonObject
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfileResponse
import com.kominfo_mkq.izakod_asn.data.model.RefreshTokenRequest
import com.kominfo_mkq.izakod_asn.data.model.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API Service for ASP.NET Core Eabsen Server
 * Base URL: https://entago.merauke.go.id/
 */
interface EabsenCoreApiService {

    @POST("api/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    /**
     * Get pegawai profile by PIN
     * GET /api/pegawai/{pin}
     */
    @GET("api/pegawai/{pin}")
    suspend fun getPegawaiProfile(
        @Path("pin") pin: String
    ): Response<PegawaiProfileResponse>

    @GET("api/payroll/absensi-live/me")
    suspend fun getPayrollAbsensiLiveSnapshot(
        @Query("tahun") tahun: Int,
        @Query("bulan") bulan: Int
    ): Response<JsonObject>
}
