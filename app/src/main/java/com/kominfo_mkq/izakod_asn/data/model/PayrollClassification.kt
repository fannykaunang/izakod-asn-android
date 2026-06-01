package com.kominfo_mkq.izakod_asn.data.model

fun String?.isNonAsnJabatan(): Boolean {
    val value = this?.trim()?.lowercase() ?: return false
    if (value.isBlank()) return false

    return listOf(
        "honorer",
        "kontrak",
        "non asn",
        "non-asn",
        "nonasn",
        "tenaga kontrak",
        "pegawai kontrak",
        "staff honorer",
        "staf honorer"
    ).any { keyword -> value.contains(keyword) }
}

fun PegawaiProfile?.isNonAsnPegawai(): Boolean {
    return this?.jabatan.isNonAsnJabatan()
}
