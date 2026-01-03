package com.kominfo_mkq.izakod_asn.data.local

object TokenStore {
    @Volatile private var jwtToken: String? = null

    fun setToken(token: String?) {
        jwtToken = token?.trim()
    }

    fun getToken(): String? = jwtToken
}
