package com.kominfo_mkq.izakod_asn.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kominfo_mkq.izakod_asn.data.model.AiPanduanChatMessage
import com.kominfo_mkq.izakod_asn.data.model.AiPanduanRequest
import com.kominfo_mkq.izakod_asn.data.model.AiPanduanResponse
import com.kominfo_mkq.izakod_asn.data.model.AiPanduanSource
import com.kominfo_mkq.izakod_asn.data.model.AiPanduanTopic
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import kotlinx.coroutines.launch

class AiPanduanChatViewModel : ViewModel() {

    private val gson = Gson()

    val topics: List<AiPanduanTopic> = assistantTopics
    val messages = mutableStateListOf<AiPanduanChatMessage>()

    var inputError by mutableStateOf<String?>(null)
        private set

    var isSending by mutableStateOf(false)
        private set

    init {
        messages.add(
            AiPanduanChatMessage(
                text = "Halo, saya Asisten IZAKOD-ASN. Pilih topik yang tersedia atau tulis pertanyaan singkat tentang SOP dan panduan aplikasi.",
                isFromUser = false
            )
        )
    }

    fun chooseTopic(topic: AiPanduanTopic) {
        inputError = null
        messages.add(
            AiPanduanChatMessage(
                text = topic.question,
                isFromUser = true
            )
        )
        messages.add(
            AiPanduanChatMessage(
                text = topic.answer,
                isFromUser = false,
                sources = listOf(AiPanduanSource(title = topic.sourceTitle)),
                disclaimer = DEFAULT_DISCLAIMER
            )
        )
    }

    fun sendManualMessage(text: String) {
        val question = text.trim()
        inputError = null

        if (question.isBlank()) {
            inputError = "Tulis pertanyaan terlebih dahulu."
            return
        }

        if (question.length > MAX_QUESTION_LENGTH) {
            inputError = "Pertanyaan terlalu panjang. Maksimal $MAX_QUESTION_LENGTH karakter."
            return
        }

        messages.add(
            AiPanduanChatMessage(
                text = question,
                isFromUser = true
            )
        )

        val typingMessage = AiPanduanChatMessage(
            text = "Asisten sedang membaca panduan...",
            isFromUser = false,
            isTyping = true
        )
        messages.add(typingMessage)
        isSending = true

        viewModelScope.launch {
            try {
                val response = ApiClient.eabsenApiService.askAiPanduan(
                    AiPanduanRequest(
                        question = question,
                        forceAi = false,
                        surface = "android_dashboard"
                    )
                )

                messages.removeAll { it.id == typingMessage.id }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        messages.add(body.toChatMessage())
                    } else {
                        messages.add(errorChatMessage(body?.message ?: DEFAULT_ERROR_MESSAGE))
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val message = parseErrorMessage(rawError)
                        ?: "Asisten belum bisa menjawab pertanyaan ini. Kode respons: ${response.code()}."
                    messages.add(errorChatMessage(message))
                }
            } catch (exception: Exception) {
                messages.removeAll { it.id == typingMessage.id }
                messages.add(
                    errorChatMessage(
                        exception.message?.takeIf { it.isNotBlank() }
                            ?: "Koneksi ke Asisten belum tersedia. Coba beberapa saat lagi."
                    )
                )
            } finally {
                isSending = false
            }
        }
    }

    fun clearInputError() {
        inputError = null
    }

    private fun AiPanduanResponse.toChatMessage(): AiPanduanChatMessage {
        val payload = data
        val resolvedAnswer = payload?.answer
            ?: answer
            ?: payload?.message
            ?: message
            ?: DEFAULT_ERROR_MESSAGE

        val resolvedSources = payload?.sources?.takeIf { it.isNotEmpty() }
            ?: sources
        val resolvedDisclaimer = payload?.disclaimer
            ?: disclaimer
            ?: DEFAULT_DISCLAIMER

        return AiPanduanChatMessage(
            text = resolvedAnswer,
            isFromUser = false,
            sources = resolvedSources,
            disclaimer = resolvedDisclaimer
        )
    }

    private fun parseErrorMessage(rawError: String?): String? {
        if (rawError.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(rawError, AiPanduanResponse::class.java)
                ?.message
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private fun errorChatMessage(message: String): AiPanduanChatMessage {
        return AiPanduanChatMessage(
            text = message,
            isFromUser = false,
            disclaimer = DEFAULT_DISCLAIMER
        )
    }

    companion object {
        private const val MAX_QUESTION_LENGTH = 500
        private const val DEFAULT_DISCLAIMER =
            "Jawaban ini bersifat panduan dan tidak menggantikan keputusan resmi OPD."
        private const val DEFAULT_ERROR_MESSAGE =
            "Saya belum menemukan panduan resmi untuk pertanyaan ini di dokumen IZAKOD-ASN. Silakan hubungi operator OPD atau administrator untuk memastikan aturan yang berlaku."
    }
}

private val assistantTopics = listOf(
    AiPanduanTopic(
        id = "nominal-entago-muncul",
        title = "Agar nominal muncul di E-NTAGO",
        question = "Bagaimana urutan agar nominal TPP/Gaji muncul di dashboard E-NTAGO?",
        description = "Urutan dari sinkron pegawai sampai finalisasi OPD.",
        sourceTitle = "Panduan Operator Nominal TPP/Gaji Muncul di E-NTAGO",
        answer = "Mulai dari Sinkronisasi Pegawai agar data pegawai dari E-NTAGO tersedia di IZAKOD-ASN. Setelah itu OPD perlu melengkapi profil TPP/Gaji, memastikan periode dibuka, menyinkronkan rekap absensi, menghitung dan menyimpan hasil, mengajukan, memverifikasi, lalu memfinalkan.\n\nUntuk ASN/PPPK, jalurnya adalah TPP ASN/PPPK. Untuk Non-ASN/Honorer/Kontrak, jalurnya adalah Gaji Non-ASN. Sinkronisasi pegawai hanya menyiapkan data identitas, sehingga tidak otomatis membuat nominal muncul.\n\nJika yang tampil masih estimasi, angka tersebut masih sementara. Angka resmi muncul setelah proses OPD selesai sampai tahap final sesuai periode."
    ),
    AiPanduanTopic(
        id = "alur-tpp-opd",
        title = "Urutan kerja TPP OPD",
        question = "Apa urutan kerja operator untuk Profil TPP dan Perhitungan TPP OPD?",
        description = "Dari profil TPP sampai ajukan pengajuan OPD.",
        sourceTitle = "AI Tanya Panduan SOP IZAKOD-ASN",
        answer = "Untuk ASN/PPPK, mulai dari Profil Pegawai TPP. Pilih OPD yang benar, pastikan profil dibuat, cocokkan jabatan E-NTAGO dengan referensi jabatan TPP, lengkapi kelas jabatan dan golongan pajak, lalu verifikasi profil.\n\nSetelah profil siap, lanjut ke Perhitungan TPP OPD. Cek periode dan OPD, pastikan profil TPP serta rekap kehadiran siap, jalankan Hitung TPP, cek pajak golongan dan total dibayar, lalu ajukan OPD jika data sudah lengkap.\n\nJika tombol hitung atau ajukan tidak bisa dipakai, penyebab umum adalah periode dikunci, profil belum siap, rekap belum tersedia, pajak belum lengkap, atau pengajuan sudah masuk proses verifikasi."
    ),
    AiPanduanTopic(
        id = "alur-gaji-non-asn-opd",
        title = "Urutan kerja Gaji Non-ASN",
        question = "Apa urutan kerja operator untuk Gaji Non-ASN?",
        description = "Profil gaji, kontrak, rekap, hitung, ajukan, verifikasi.",
        sourceTitle = "AI Tanya Panduan SOP IZAKOD-ASN",
        answer = "Untuk Non-ASN/Honorer/Kontrak, mulai dari profil gaji. Pastikan profil gaji tersedia dan aktif, kontrak aktif terhubung, serta tarif atau komponen gaji yang berlaku sudah tersedia.\n\nSetelah itu buka Gaji Non-ASN: sinkronkan rekap absensi periode yang diproses, jalankan preview hitung untuk melihat pegawai siap dan tertahan, simpan hasil perhitungan, lalu ajukan OPD.\n\nBagian Keuangan OPD memverifikasi jika data sudah sesuai atau meminta revisi jika masih ada masalah. Jika profil gaji, kontrak aktif, atau rekap absensi belum lengkap, Gaji Saya bisa belum muncul atau masih estimasi."
    ),
    AiPanduanTopic(
        id = "target-laporan",
        title = "Target dulu atau laporan dulu?",
        question = "Saya harus buat target dulu atau laporan dulu?",
        description = "Urutan target, laporan harian, realisasi, dan penilaian.",
        sourceTitle = "Alur Pegawai ASN/PPPK Target, Laporan, Penilaian, TPP, dan Gaji",
        answer = "Untuk ASN/PPPK, mulai dari Target Kinerja bulan berjalan terlebih dahulu. Setelah target dibuat atau diajukan, lanjutkan membuat Laporan Kegiatan Harian sebagai bukti aktivitas.\n\nJika pekerjaan sudah berjalan tetapi target belum selesai, laporan tetap boleh dibuat agar bukti aktivitas tidak hilang.\n\nUntuk Non-ASN/Honorer/Kontrak, target, realisasi, dan penilaian bukan komponen perhitungan gaji. Jalur utamanya adalah absensi E-NTAGO, profil payroll, dan menu Gaji Saya; laporan hanya mengikuti arahan OPD bila dipakai untuk monitoring internal."
    ),
    AiPanduanTopic(
        id = "tpp-belum-muncul",
        title = "Kenapa TPP belum muncul?",
        question = "Kenapa TPP saya belum muncul?",
        description = "Penyebab TPP masih kosong, estimasi, atau belum final.",
        sourceTitle = "Alur Pegawai ASN/PPPK dan Konsep Estimasi Berjalan",
        answer = "Untuk ASN/PPPK, TPP bisa belum muncul karena masih estimasi, belum dihitung OPD, profil TPP belum siap, penilaian belum final, atau periode belum dibuka/final. Nilai resmi muncul setelah OPD menjalankan proses perhitungan dan finalisasi.\n\nUntuk Non-ASN/Honorer/Kontrak, jalurnya bukan TPP ASN/PPPK tetapi Gaji Non-ASN. Jika akun Non-ASN masih melihat istilah TPP, itu perlu dilaporkan sebagai masalah tampilan atau akses."
    ),
    AiPanduanTopic(
        id = "periode-dikunci",
        title = "Apa arti periode dikunci?",
        question = "Apa arti periode dikunci?",
        description = "Dampak status periode pada sync, hitung, ajukan, dan final.",
        sourceTitle = "Rencana Kontrol Periode Perhitungan TPP/Gaji",
        answer = "Periode dikunci berarti aksi resmi yang menulis data tidak boleh dijalankan. Preview atau estimasi tetap boleh dilihat secara read-only bila tersedia.\n\nAksi seperti sync, hitung, simpan, ajukan, verifikasi, revisi, dan final hanya boleh dilakukan ketika periode dibuka sesuai kewenangan."
    ),
    AiPanduanTopic(
        id = "gaji-estimasi",
        title = "Kenapa Gaji Saya masih estimasi?",
        question = "Kenapa Gaji Saya masih estimasi?",
        description = "Perbedaan estimasi berjalan dan nominal resmi.",
        sourceTitle = "Konsep Estimasi Berjalan TPP ASN dan Gaji Non-ASN",
        answer = "Estimasi Gaji Berjalan dihitung dari data absensi dan profil payroll yang sudah tersedia. Angka ini membantu pegawai melihat perkiraan sementara, bukan nominal resmi.\n\nNominal resmi muncul setelah OPD menghitung, menyimpan, mengajukan, memverifikasi, dan memfinalkan gaji sesuai kontrol periode."
    ),
    AiPanduanTopic(
        id = "atasan-finalisasi",
        title = "Tugas atasan sebelum final TPP",
        question = "Apa yang harus dilakukan atasan sebelum OPD finalkan TPP?",
        description = "Pengecekan target, laporan, realisasi, dan penilaian bawahan.",
        sourceTitle = "Alur Pegawai ASN/PPPK Target, Laporan, Penilaian, TPP, dan Gaji",
        answer = "Atasan perlu memastikan alur bawahan sudah tertib sebelum OPD memproses finalisasi TPP. Yang perlu dicek antara lain target kinerja, laporan kegiatan, realisasi, dan penilaian sesuai periode.\n\nJika ada data yang masih perlu revisi atau belum dinilai, selesaikan dulu melalui menu terkait. Asisten tidak dapat mengambil keputusan nilai atau memfinalkan pengajuan."
    ),
    AiPanduanTopic(
        id = "batasan-ai",
        title = "Apa yang boleh dijawab AI?",
        question = "Apa saja batasan jawaban Asisten IZAKOD-ASN?",
        description = "Batasan privasi, nominal, keputusan resmi, dan sumber dokumen.",
        sourceTitle = "Kontrak PR AI Tanya Panduan SOP IZAKOD-ASN",
        answer = "Asisten boleh menjelaskan alur penggunaan, arti status, perbedaan ASN/PPPK dan Non-ASN, penyebab estimasi, periode dikunci, dan menu yang perlu dibuka.\n\nAsisten tidak boleh menghitung nominal TPP/Gaji, mengambil keputusan resmi, mengubah data, membuka data pegawai lain, atau menjawab hal yang tidak bersumber dari dokumen internal."
    )
)
