# Laporan Perubahan Android IZAKOD-ASN

Periode audit: 15 Januari 2026 sampai 7 Juli 2026  
Fokus utama: project Android IZAKOD-ASN  
Lokasi repo: `E:\Android\izakod-asn\izakod-asn-android`

## Batasan Audit

Laporan ini dibuat dari riwayat Git lokal pada repo Android IZAKOD-ASN. Fokusnya adalah perubahan Android, terutama perubahan yang berpengaruh ke pengalaman pegawai di aplikasi, integrasi dengan backend IZAKOD-ASN, SSO dari E-NTAGO, notifikasi, TPP/Gaji, target kinerja, penilaian, laporan kegiatan, dan alur atasan-bawahan.

Audit ini tidak membahas seluruh detail perubahan backend/web secara lengkap. Perubahan backend/web hanya disebut jika terlihat berhubungan langsung dengan fitur Android, misalnya endpoint dashboard, TPP/Gaji, SSO, AI Panduan, atau notifikasi.

## Ringkasan Angka

| Item | Hasil |
| --- | --- |
| Jumlah commit pada periode audit | 31 commit |
| Commit terakhir yang terdeteksi | `efa7dee` - 25 Juni 2026 |
| Pembaruan setelah commit terakhir | Dicatat dari working tree lokal sampai 7 Juli 2026 |
| Total penambahan baris | 37.042 baris |
| Total penghapusan baris | 7.927 baris |
| Area paling banyak berubah | `app/src/main/java` |
| Status repo saat audit awal | Bersih sebelum file laporan ini dibuat |

Distribusi commit per tanggal:

| Tanggal | Jumlah commit |
| --- | ---: |
| 15 Januari 2026 | 1 |
| 19 Maret 2026 | 1 |
| 19 April 2026 | 1 |
| 20 April 2026 | 1 |
| 6 Mei 2026 | 1 |
| 16 Mei 2026 | 1 |
| 19 Mei 2026 | 2 |
| 20 Mei 2026 | 1 |
| 23 Mei 2026 | 2 |
| 24 Mei 2026 | 2 |
| 25 Mei 2026 | 2 |
| 28 Mei 2026 | 1 |
| 1 Juni 2026 | 2 |
| 2 Juni 2026 | 1 |
| 5 Juni 2026 | 1 |
| 7 Juni 2026 | 1 |
| 13 Juni 2026 | 1 |
| 15 Juni 2026 | 3 |
| 16 Juni 2026 | 1 |
| 17 Juni 2026 | 3 |
| 20 Juni 2026 | 1 |
| 25 Juni 2026 | 1 |

## Ringkasan Besar

Dari 15 Januari sampai 7 Juli 2026, aplikasi Android IZAKOD-ASN berubah dari aplikasi yang masih berfokus pada login, daftar laporan, profil, dan notifikasi dasar menjadi aplikasi kerja pegawai yang jauh lebih lengkap.

Perubahan terbesar adalah:

1. Penambahan modul Target Kinerja, Penilaian Kinerja, Statistik, Tertunda, dan relasi laporan-target.
2. Penambahan modul TPP ASN/PPPK dan Gaji Non-ASN, termasuk tampilan detail dan integrasi estimasi berjalan.
3. Penambahan SSO/deeplink dari E-NTAGO ke IZAKOD-ASN, termasuk cache estimasi TPP/Gaji agar transisi tidak membingungkan pegawai.
4. Penambahan AI Tanya Asisten berbasis topik panduan/SOP.
5. Penambahan alur atasan-bawahan, termasuk halaman Bawahan Saya dan Verifikasi Atasan-Bawahan.
6. Pembenahan notifikasi FCM, termasuk arah klik notifikasi ke halaman detail laporan.
7. Perapian dashboard: badge count, carousel Perlu Tindakan, guard agar data tidak tampil salah sebelum selesai dimuat, dan refresh yang lebih aman saat aplikasi dibuka kembali.
8. Perapian UI besar-besaran agar halaman lama lebih selaras dengan tampilan baru.
9. Penambahan sistem kontrol versi aplikasi Android agar admin bisa mengatur update opsional atau wajib dari server.
10. Penyetaraan SSO/deeplink E-NTAGO dengan login biasa, termasuk penyimpanan sesi E-NTAGO dan registrasi FCM setelah sesi berhasil.
11. Penambahan Informasi Pilihan/Pengumuman di dashboard Android, halaman detail pengumuman, routing notifikasi ke detail pengumuman, dan dukungan link pada isi informasi.
12. Perapian tampilan carousel dashboard agar card Informasi Pilihan dan Perlu Tindakan lebih konsisten dengan lebar hero identitas pegawai.

## Pembaruan 26 Juni 2026 sampai 7 Juli 2026

Bagian ini mencatat perubahan lanjutan setelah audit awal 25 Juni 2026. Sebagian perubahan masih berada pada working tree lokal dan belum tentu sudah menjadi commit Git.

Perubahan utama Android:

1. Dashboard Android menampilkan section Informasi Pilihan berisi carousel pengumuman/panduan dari server.
2. Card Informasi Pilihan memakai thumbnail, judul, dan ringkasan agar pegawai dapat membuka tutorial atau kabar penting langsung dari dashboard.
3. Ditambahkan halaman detail Informasi/Pengumuman di Android.
4. Thumbnail pada detail pengumuman dibuat lebih ringkas dan responsif agar tidak terlalu tinggi dibanding card dashboard.
5. Isi pengumuman dari editor web dapat menampilkan link yang bisa diketuk di Android.
6. Notifikasi FCM dari pengumuman diarahkan ke detail pengumuman, bukan hanya berhenti di dashboard.
7. Jika pengumuman dibuka dari halaman Notifikasi Android, notifikasi tersebut ditandai sebagai dibaca.
8. Routing notifikasi Android diperluas agar dapat membaca `pengumuman_id`, `target_id`, atau link tujuan yang mengarah ke pengumuman.
9. Dashboard tetap menjaga carousel Perlu Tindakan sebagai daftar prioritas pegawai/atasan.
10. Tampilan carousel Informasi Pilihan dan Perlu Tindakan diselaraskan agar lebar card mengikuti lebar hero identitas pegawai, dengan sedikit card berikutnya tetap terlihat sebagai tanda bisa digeser.
11. Detail Informasi/Pengumuman menampilkan tombol `Buka Web` dan `Bagikan` langsung di dalam card utama, di bawah informasi pembuat, sehingga pegawai tidak perlu mencari link publik di card terpisah.
12. URL publik dan URL thumbnail pengumuman dinormalisasi ke domain HTTPS IZAKOD-ASN ketika server mengirim alamat `http` atau alamat lokal, agar gambar dan link publik tetap terbuka benar di perangkat pegawai.
13. Model pengumuman Android diperluas untuk membaca `public_slug`, `public_url`, status, dan informasi pengumuman yang sudah ditarik.
14. Android menampilkan kondisi pengumuman yang sudah ditarik dengan pesan khusus, sehingga pegawai tidak membaca informasi yang sudah tidak berlaku.
15. Isi informasi dari TinyMCE kini mempertahankan format HTML dasar di Android, termasuk heading, teks tebal, miring, underline, warna, ukuran teks, dan link yang bisa diketuk.
16. Prompt izin notifikasi Android 13+ dipindahkan agar muncul setelah sesi login benar-benar aktif, baik dari login biasa maupun SSO/deeplink.
17. Jika izin notifikasi sudah pernah ditolak atau perangkat tidak lagi menampilkan prompt sistem, aplikasi menampilkan dialog arahan untuk membuka pengaturan notifikasi IZAKOD-ASN.

Perubahan terkait TPP/Gaji dan SSO:

1. Jalur tombol Buka Detail IZAKOD-ASN dari E-NTAGO dipastikan membuka detail TPP Saya untuk ASN/PPPK dan detail Gaji Saya untuk Non-ASN/Honorer/Kontrak.
2. Detail IZAKOD-ASN menyimpan dan memakai cache estimasi dari SSO bila data resmi belum berhasil dimuat, agar tampilan tidak berbeda jauh dari bottom sheet E-NTAGO.
3. Status tampilan TPP/Gaji yang perlu dipahami pegawai adalah Estimasi, Dihitung, Diajukan, Verifikasi, Final, Revisi, Ditolak, Belum, Terakhir, Memuat, dan Gagal.
4. Repository TPP/Gaji Android membaca field tampilan payroll dari backend agar login normal, SSO, dan deeplink memakai sumber status yang sama.
5. Estimasi berjalan dicocokkan memakai PIN pegawai. ID internal E-NTAGO dan ID internal IZAKOD-ASN tidak wajib sama.
6. Pegawai Non-ASN/Honorer/Kontrak boleh tidak memiliki NIP ASN. Kondisi NIP kosong tidak boleh otomatis membuat estimasi ditampilkan sebagai `Belum` jika PIN dan data pendukung lain sudah valid.
7. Pesan error detail estimasi dibuat lebih jelas agar jika kontrak snapshot bermasalah, log Android menampilkan field mana yang ditolak oleh backend.

Pembaruan khusus 6-7 Juli 2026:

1. Detail pengumuman Android menampilkan aksi publik dengan lebih ringkas:
   - `Buka Web` untuk membuka artikel publik IZAKOD-ASN di browser.
   - `Bagikan` untuk membagikan judul, ringkasan, dan link publik ke aplikasi lain seperti WhatsApp.
2. Card detail pengumuman tidak lagi memisahkan link publik sebagai section tersendiri. Tombol aksi ditempatkan di card hero agar konteksnya jelas dan tampilan lebih pendek.
3. Halaman detail pengumuman tetap menampilkan thumbnail 16:9 yang selaras dengan card Informasi Pilihan di dashboard.
4. Konten TinyMCE yang dikirim dari web sekarang dirender sebagai teks kaya di Android. Judul besar seperti `<h2>`, teks `<strong>`, warna link, dan ukuran teks tidak lagi disamakan menjadi body text biasa.
5. Link di isi pengumuman tetap dapat diklik. Contoh: teks `Android E-NTAGO` yang diarahkan ke Play Store tetap menjadi link aktif di Android.
6. URL asset pengumuman yang berasal dari domain produksi namun masih memakai `http://izakod-asn.merauke.go.id` otomatis dinaikkan menjadi `https://izakod-asn.merauke.go.id`.
7. Jalur notifikasi dan deeplink pengumuman tetap diarahkan ke detail Informasi, termasuk bila data membawa `pengumuman_id` atau target route pengumuman.
8. Aplikasi menyiapkan dialog khusus untuk pengumuman yang sudah ditarik agar pegawai memahami bahwa informasi tersebut tidak lagi berlaku.
9. Alur permintaan izin notifikasi dibuat lebih aman: aplikasi tidak meminta izin sebelum sesi pegawai siap, dan memberi arahan pengaturan jika prompt sistem tidak muncul.

File Android penting pada pembaruan ini:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/PengumumanScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/NotificationScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodel/NotificationViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/fcm/IzakodFirebaseMessagingService.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/MainActivity.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/model/PengumumanModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/navigation/Navigation.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/EabsenApiService.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/repository/PayrollLiveEstimateRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/GajiNonAsnRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TppRepository.kt`

## Timeline Perubahan

### 15 Januari 2026

Commit: `42194db` - `15-01-2026`

Perubahan utama:

- Menyiapkan fondasi build/release Android.
- Menambahkan dan merapikan icon launcher, icon notifikasi, splash, dan resource drawable.
- Menambahkan konfigurasi ProGuard.
- Memperbarui `AndroidManifest.xml`.
- Menambahkan atau memperkuat FCM service.
- Memperbarui login, daftar laporan, API client, repository auth, dan penyimpanan preferensi user.

File penting:

- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/MainActivity.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/UserPreferences.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/ApiClient.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/AuthRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/notifications/IzakodFirebaseMessagingService.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/LoginScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportListScreen.kt`

Makna perubahan:

Tahap ini lebih banyak berupa fondasi aplikasi Android agar siap dipakai, bisa login, bisa menerima notifikasi, dan punya tampilan dasar yang lebih rapi.

### 19 Maret 2026

Commit: `48e5e76` - `19-03-2026`

Perubahan utama:

- Memperbarui konfigurasi Gradle.
- Merapikan `MainActivity`.
- Menambahkan atau memperbaiki asset icon notifikasi dan string aplikasi.

File penting:

- `app/build.gradle.kts`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/MainActivity.kt`
- `app/src/main/res/drawable/ic_notification.xml`
- `app/src/main/res/mipmap-*/ic_launcher*`
- `app/src/main/res/values/strings.xml`

Makna perubahan:

Tahap ini memperkuat tampilan identitas aplikasi dan kesiapan notifikasi Android.

### 19 April 2026

Commit: `7255952` - `19-04-2026`

Perubahan utama:

- Pembaruan besar pada layer autentikasi, API, token, dashboard, dan profil.
- Penambahan atau perubahan model API.
- Pembaruan repository auth dan API client.
- Dashboard dan profile mulai lebih bergantung pada data backend yang lebih lengkap.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/ApiModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/ApiClient.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/AuthRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TokenStore.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ProfileScreen.kt`

Makna perubahan:

Ini menjadi awal pergeseran aplikasi dari sekadar layar dasar menjadi aplikasi yang mengambil data pegawai dan dashboard dari backend secara lebih serius.

### 20 April 2026

Commit: `f393e73` - `20-04-2026`

Perubahan utama:

- Pembaruan besar pada navigasi aplikasi.
- Pembaruan layar laporan kegiatan: buat, edit, detail, daftar, dan pencarian.
- Pembaruan notifikasi, pengingat, template kegiatan, verifikasi laporan, dan profil.
- Pembaruan ViewModel laporan kegiatan.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/navigation/Navigation.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/CreateReportScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/EditReportScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportDetailScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportListScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/NotificationScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReminderScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TemplateScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/VerifikasiLaporanScreen.kt`

Makna perubahan:

Laporan kegiatan mulai menjadi workflow utama di Android. Pegawai tidak hanya melihat data, tetapi juga membuat, mengedit, melihat detail, dan mengikuti status laporan.

### 6 Mei 2026

Commit: `7f9e362` - `06-05-2026`

Perubahan utama:

- Pembaruan dashboard.
- Pembaruan profile.
- Pembaruan statistik.
- Penambahan atau pembaruan dialog info update.
- Pembaruan konfigurasi network.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/config/NetworkConfig.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ProfileScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/StatisticsScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/UpdateInfoDialog.kt`

Makna perubahan:

Dashboard mulai diperkuat sebagai halaman utama pegawai, dengan statistik dan profil yang lebih matang.

### 16 Mei 2026

Commit: `ed2507d` - `16-05-2026`

Perubahan utama:

- Penambahan modul Target Kinerja.
- Penambahan modul Penilaian Kinerja.
- Penambahan model, repository, screen, dan ViewModel untuk target dan penilaian.
- Penambahan komponen bottom navigation dan top bar.
- Penambahan statistik sebagai bagian dari navigasi utama.
- Pembaruan splash icon adaptive.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/local/AppContextHolder.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TargetKinerjaModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TargetKinerjaRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PenilaianKinerjaModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PenilaianKinerjaRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TargetKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/PenilaianKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/StatisticsScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TargetKinerjaViewModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/PenilaianKinerjaViewModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/StatisticsViewModel.kt`

Makna perubahan:

Ini adalah perubahan besar. Android IZAKOD-ASN mulai mencakup alur kerja kinerja ASN/PPPK: target, realisasi, penilaian, dan statistik.

### 19 Mei 2026

Commit: `955a1a6` - `19-05-2026`

Perubahan utama:

- Pembaruan besar pada dashboard, laporan kegiatan, dan target kinerja.
- Penambahan relasi antara laporan kegiatan dan target.
- Pembaruan form target, detail target, dan review target.
- Pembaruan create/edit laporan agar bisa terhubung dengan target.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/CreateReportScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/EditReportScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/LaporanTargetRelationSection.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TargetKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/DashboardViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/LaporanListViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TargetKinerjaViewModels.kt`

Makna perubahan:

Laporan kegiatan mulai dipakai sebagai bukti realisasi target, bukan sekadar catatan kegiatan harian.

Commit: `307ab48` - `19-05-2026 2`

Perubahan utama:

- Penambahan modul TPP untuk Android.
- Penambahan model, repository, screen, dan ViewModel TPP.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TppModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TppRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TppSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TppViewModel.kt`

Makna perubahan:

Android mulai menampilkan informasi TPP ASN/PPPK.

### 20 Mei 2026

Commit: `97ed92e` - `20-05-2026 2`

Perubahan utama:

- Pembaruan lanjutan untuk penilaian, target, TPP, template, dan relasi laporan-target.
- Pembaruan daftar laporan dan detail laporan.
- Pembaruan ViewModel target, penilaian, dan TPP.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/LaporanTargetRelationSection.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/PenilaianKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TargetKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TemplateScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TppSayaScreen.kt`

Makna perubahan:

Fitur target, penilaian, template, laporan, dan TPP mulai saling terhubung di UI.

### 23 Mei 2026

Commit: `70813ef` - `23-05-2026`

Perubahan utama:

- Penambahan halaman Tertunda.
- Penambahan ViewModel Tertunda.
- Dashboard dan navigasi mulai menyediakan akses ke pekerjaan yang tertunda.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TertundaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TertundaViewModel.kt`

Makna perubahan:

Pegawai mulai dibantu untuk melihat pekerjaan yang belum selesai atau masih tertunda.

Commit: `3ca5c68` - `23-05-2026 2`

Perubahan utama:

- Pembaruan token, login, API client, repository auth, dan FCM.
- Pembaruan create laporan, daftar laporan, pengingat, dashboard, dan tema.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/ApiClient.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/AuthRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/notifications/IzakodFirebaseMessagingService.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/CreateReportScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportListScreen.kt`

Makna perubahan:

Autentikasi, token, FCM, dan beberapa layar utama diperkuat agar alur aplikasi lebih stabil.

### 24 Mei 2026

Commit: `0b040cc` - `24-05-2026`

Perubahan utama:

- Pembaruan dashboard dan notifikasi.
- Pembaruan detail laporan dan daftar laporan.
- Pembaruan target, penilaian, TPP, dan tertunda.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/NotificationScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportDetailScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportListScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TargetKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TppSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TertundaViewModel.kt`

Makna perubahan:

Perbaikan lintas modul agar dashboard, laporan, target, penilaian, TPP, dan notifikasi makin konsisten.

Commit: `ae020bc` - `Ignore local Android artifacts and track splash logo`

Perubahan utama:

- Merapikan `.gitignore`.
- Menambahkan asset `logo_splash.png`.

Makna perubahan:

Repo mulai lebih bersih dari artifact lokal, dan splash branding mulai distandarkan.

### 25 Mei 2026

Commit: `5afda2d` - `25-05-2026`

Perubahan utama:

- Menghapus `MobileApi.kt`.
- Merapikan navigasi, dashboard, target, dan penilaian.

Makna perubahan:

Ada penyederhanaan layer API lama dan perapian fitur utama.

Commit: `0619460` - `before add gaji non asn/kontrak`

Perubahan utama:

- Persiapan dashboard dan preferensi user sebelum modul Gaji Non-ASN/Kontrak ditambahkan.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/UserPreferences.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`

Makna perubahan:

Dashboard dan data user disiapkan agar bisa membedakan ASN/PPPK dan Non-ASN untuk kebutuhan TPP/Gaji.

### 28 Mei 2026

Commit: `15ef531` - `28-05-2026`

Perubahan utama:

- Pembaruan konfigurasi `ApiClient`.

Makna perubahan:

Ada penyesuaian koneksi Android ke backend.

### 1 Juni 2026

Commit: `034e3f2` - `01-06-2026`

Perubahan utama:

- Pembaruan UserPreferences, navigasi, dashboard, dan TPP.
- TPP makin terintegrasi dengan dashboard dan profil user.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/UserPreferences.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/navigation/Navigation.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TppSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TppViewModel.kt`

Commit: `cbd8d3d` - `01-06-2026 2`

Perubahan utama:

- Penambahan modul Gaji Non-ASN.
- Penambahan model, repository, screen, ViewModel, dan klasifikasi payroll.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/GajiNonAsnModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/GajiNonAsnRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PayrollClassification.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/GajiSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/GajiNonAsnViewModel.kt`

Makna perubahan:

Android mulai mendukung pegawai Non-ASN/Honorer/Kontrak untuk melihat Gaji Saya, terpisah dari TPP ASN/PPPK.

### 2 Juni 2026

Commit: `a430095` - `02-06-2026`

Perubahan utama:

- Penambahan model dan repository estimasi berjalan payroll.
- Dashboard, Gaji Saya, TPP Saya, profil, daftar laporan, dan detail laporan mulai membaca estimasi berjalan.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PayrollLiveEstimateModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PayrollLiveEstimateRepository.kt`

File penting yang diperbarui:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/GajiSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TppSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/DashboardViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/GajiNonAsnViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TppViewModel.kt`

Makna perubahan:

Ini adalah awal dari fitur estimasi berjalan. Pegawai bisa melihat gambaran sementara TPP/Gaji sebelum nilai resmi final tersedia.

### 5 Juni 2026

Commit: `93c59e7` - `05-06-2026 android`

Perubahan utama:

- Pembaruan `AndroidManifest.xml`, `MainActivity`, dan navigasi.
- Ada indikasi penambahan atau perapian deeplink.

Makna perubahan:

Aplikasi mulai disiapkan untuk dibuka dari luar aplikasi, termasuk kemungkinan dari E-NTAGO.

### 7 Juni 2026

Commit: `b1fa721` - `07062026`

Perubahan utama:

- Pembaruan Gradle dan theme.
- Perubahan pada `Theme.kt`.

File penting:

- `app/build.gradle.kts`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/theme/Theme.kt`

Makna perubahan:

Tahap ini berkaitan dengan perapian dependency/theme dan kesiapan Android modern.

### 13 Juni 2026

Commit: `096ee13` - `13-06-2026`

Perubahan utama:

- Penambahan session manager.
- Penambahan model SSO mobile.
- Penambahan screen bridge SSO mobile.
- Penambahan cache estimasi TPP/Gaji dari SSO.
- Pembaruan besar pada dashboard, login, Gaji Saya, TPP Saya, profil, dan ViewModel terkait.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/AuthSessionManager.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/MobileSsoModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/SsoPayrollEstimateCacheRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/MobileSsoBridgeScreen.kt`

Makna perubahan:

Ini adalah salah satu perubahan paling penting untuk integrasi IZAKOD-ASN dan E-NTAGO. Android IZAKOD-ASN mulai bisa menerima deeplink/SSO dari E-NTAGO dan membawa konteks TPP/Gaji pegawai.

Catatan penting:

Fitur ini kemudian berhubungan dengan perbaikan agar dashboard Gaji/TPP tidak membaca pegawai Non-ASN sebagai TPP sebelum profil pegawai selesai dimuat.

### 15 Juni 2026

Commit: `3e928ea` - `15-06-2026`

Perubahan utama:

- Penambahan fitur AI Tanya Asisten.
- Penambahan model AI Panduan, repository token E-NTAGO, screen chat, dan ViewModel chat.
- Pembaruan template kegiatan dan create laporan.
- Pembaruan SSO cache dan dashboard.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/AiPanduanModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/EntagoTokenRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/AiPanduanChatScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/AiPanduanChatViewModel.kt`

Makna perubahan:

Android mendapat halaman Tanya Asisten untuk membantu pegawai bertanya tentang SOP dan panduan IZAKOD-ASN.

Commit: `692c186` - `15-06-2026 2`

Perubahan utama:

- Pembaruan `MainActivity`.
- Pembaruan FCM service.
- Pembaruan layar notifikasi.
- Pembaruan detail laporan.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/MainActivity.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/notifications/IzakodFirebaseMessagingService.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/NotificationScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportDetailScreen.kt`

Makna perubahan:

Notifikasi mulai diarahkan lebih baik ke halaman terkait. Ini penting untuk kasus notifikasi laporan kegiatan yang harus membuka detail laporan.

Commit: `5ff7f6c` - `15-06-2026 3`

Perubahan utama:

- Pembaruan dashboard dan DashboardViewModel.

Makna perubahan:

Dashboard mulai diperbaiki agar lebih aman saat data belum selesai dimuat, terutama untuk data pegawai, TPP/Gaji, dan tindakan prioritas.

### 16 Juni 2026

Commit: `8c596ac` - `add atasan choose bawahan`

Perubahan utama:

- Penambahan fitur atasan memilih/mengelola bawahan.
- Penambahan repository, screen, dan ViewModel atasan-bawahan.
- Pembaruan dashboard dan navigasi.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/AtasanPegawaiRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/BawahanSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/AtasanPegawaiViewModel.kt`

Makna perubahan:

Atasan mulai punya halaman Android untuk melihat atau mengusulkan bawahan, bukan hanya bergantung pada admin web.

### 17 Juni 2026

Commit: `a9c7da0` - `17-06-2026`

Perubahan utama:

- Pembaruan daftar laporan, terutama dukungan laporan bawahan.
- Pembaruan dashboard dan badge.
- Pembaruan laporan, target, penilaian, notifikasi, dan TPP/Gaji.

Makna perubahan:

Android mulai membedakan laporan milik sendiri dan laporan bawahan, terutama untuk atasan yang punya kewajiban review.

Commit: `cc8cf7f` - `17-06-2026 2`

Perubahan utama:

- Penambahan halaman Verifikasi Atasan-Bawahan.
- Penambahan `DashboardRefreshNotifier`.
- Banyak ViewModel mulai memakai sinyal refresh agar dashboard dan badge tidak stale setelah aksi di halaman lain.

File penting yang ditambahkan:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/VerifikasiAtasanBawahanScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/DashboardRefreshNotifier.kt`

Makna perubahan:

Kasubbag/role terkait mulai punya halaman verifikasi usulan atasan-bawahan. Dashboard juga mulai lebih responsif terhadap perubahan data setelah user menyelesaikan aksi.

Commit: `b149eda` - `17-06-2026 3`

Perubahan utama:

- Refactor besar dan perapian UI.
- Banyak layar lama dirapikan agar lebih selaras dengan tampilan baru.
- Perubahan besar pada dashboard, target, detail laporan, daftar laporan, penilaian, profil, TPP, Gaji, tertunda, dan template.

Makna perubahan:

Ini adalah gelombang besar penyelarasan UI/UX Android. Banyak tampilan lama dibuat lebih konsisten dengan desain baru.

### 20 Juni 2026

Commit: `40b9559` - `20-06-2026`

Perubahan utama:

- Pembaruan `ApiClient`.
- Pembaruan dashboard.
- Pembaruan AI Panduan Chat ViewModel.
- Pembaruan DashboardViewModel.

File penting:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/ApiClient.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/AiPanduanChatViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/DashboardViewModel.kt`

Makna perubahan:

Commit ini memperkuat koneksi API, dashboard, dan chat AI. Ini sejalan dengan perbaikan terbaru terkait dashboard, badge, dan jawaban AI agar sumber panduan tidak membingungkan.

### 25 Juni 2026

Commit: `efa7dee` - `25-06-2026`

Perubahan utama:

- Menaikkan versi aplikasi Android dari `versionCode 7` / `versionName 1.0.6` menjadi `versionCode 8` / `versionName 2.0.0`.
- Mengarahkan `ApiClient` ke server production `https://izakod-asn.merauke.go.id/`.
- Menutup cleartext traffic dan backup aplikasi lewat `AndroidManifest.xml`.
- Menambahkan sistem pengecekan versi aplikasi dari endpoint server, cache policy versi, dialog update opsional/wajib, dan pencatatan event update.
- Menambahkan event update `update_shown`, `update_clicked`, `update_skipped`, `update_dismissed`, dan `update_completed`.
- Menambahkan penyimpanan cache policy versi dan versi aplikasi terakhir yang dilihat di `UserPreferences`.
- Menyetarakan alur SSO/deeplink E-NTAGO dengan login biasa: sesi IZAKOD tetap disimpan, token E-NTAGO ikut disimpan, dan setup pasca-login dijalankan ulang.
- Memindahkan registrasi FCM pasca-login ke helper bersama `LoginSessionPostSetup`, sehingga login biasa dan SSO memakai pola yang sama.
- Memperketat halaman Bawahan Saya agar tombol membuat usulan personal hanya muncul untuk pegawai yang memang boleh mengelola bawahan personal.
- Menambahkan parameter `scope=personal` pada pengambilan usulan atasan-bawahan di Android.

File penting:

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/MainActivity.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/local/UserPreferences.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/model/AppVersionModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/model/MobileSsoModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/ApiClient.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/EabsenApiService.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/repository/AppVersionRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/repository/AtasanPegawaiRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/repository/LoginSessionPostSetup.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/fcm/DeviceInfo.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/components/AppUpdateDialog.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/BawahanSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/MobileSsoBridgeScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodel/AtasanPegawaiViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodel/LoginViewModel.kt`

Makna perubahan:

Commit terakhir dalam periode audit membuat aplikasi lebih siap rilis production. Perubahan ini bukan hanya menaikkan versi, tetapi juga menambahkan kendali update dari server, mengurangi perbedaan perilaku antara login biasa dan SSO dari E-NTAGO, serta menutup celah agar operator/non-atasan tidak dapat membuat usulan bawahan personal dari Android.

## Perubahan Berdasarkan Area Fitur

### 1. Login, Auth, Token, dan Session

Perubahan penting:

- Login screen beberapa kali diperbarui.
- `UserPreferences` diperluas untuk menyimpan data user yang makin kompleks.
- `AuthRepository`, `TokenStore`, dan `ApiClient` beberapa kali diperbaiki.
- Ditambahkan `AuthSessionManager`.
- Ditambahkan integrasi session untuk SSO mobile.
- Ditambahkan `LoginSessionPostSetup` agar registrasi FCM pasca-login dipakai bersama oleh login biasa dan SSO.

Dampak untuk pegawai:

- Login dan session menjadi lebih kuat.
- Aplikasi dapat menyimpan konteks pegawai lebih baik.
- Aplikasi lebih siap menangani user dari E-NTAGO melalui SSO/deeplink.
- Login biasa dan SSO tidak lagi memiliki setup notifikasi yang berbeda.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/UserPreferences.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/AuthSessionManager.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/AuthRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TokenStore.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/repository/LoginSessionPostSetup.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/LoginScreen.kt`

### 2. Navigasi Aplikasi

Navigasi Android berubah besar. Route yang terdeteksi saat audit mencakup:

- Login
- Dashboard
- Daftar laporan
- Daftar laporan bawahan
- Pencarian laporan
- Statistik
- Template kegiatan
- Target kinerja
- Target bawahan
- Buat target
- Penilaian kinerja
- Penilaian belum dibuat
- Bawahan saya
- Verifikasi atasan-bawahan
- Tertunda
- TPP Saya
- Gaji Saya
- Pengingat
- Profil
- Pengaturan
- Developer
- Tanya Asisten
- Buat laporan
- Notifikasi
- Detail notifikasi
- Bridge SSO mobile

Dampak untuk pegawai:

Aplikasi tidak lagi hanya menjadi aplikasi laporan. Android sudah menjadi pusat kerja pegawai untuk laporan, target, penilaian, gaji/TPP, atasan-bawahan, notifikasi, dan panduan.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/navigation/Navigation.kt`

### 3. Dashboard

Dashboard adalah file yang paling sering berubah.

Perubahan penting:

- Penambahan ringkasan bulan ini.
- Penambahan akses cepat.
- Penambahan badge count.
- Penambahan carousel Perlu Tindakan.
- Penambahan kondisi pegawai baru.
- Penambahan perbedaan alur ASN/PPPK dan Non-ASN.
- Penambahan integrasi TPP/Gaji.
- Penambahan integrasi target dan penilaian.
- Penambahan tombol Tanya Asisten.
- Penambahan tombol Bawahan dan Verifikasi sesuai role.
- Perbaikan agar data tidak langsung dianggap kosong sebelum load selesai.
- Perbaikan refresh agar dashboard tidak selalu reload penuh saat kembali dari halaman lain.

Dampak untuk pegawai:

- Dashboard menjadi halaman utama yang menunjukkan pekerjaan prioritas.
- Pegawai lebih mudah tahu apa yang harus dilakukan.
- Atasan bisa melihat pekerjaan bawahan yang perlu diproses.
- Non-ASN melihat alur Gaji, bukan TPP.
- ASN/PPPK melihat alur TPP.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/DashboardViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/DashboardRefreshNotifier.kt`

### 4. Laporan Kegiatan

Perubahan penting:

- Create, edit, detail, daftar, pencarian, dan verifikasi laporan diperbarui.
- Laporan bisa ditautkan ke target.
- Ada section relasi laporan-target.
- Ada dukungan laporan milik bawahan untuk atasan.
- Ada pemisahan konteks Saya dan Bawahan.
- Detail laporan didesain ulang agar lebih selaras.
- Beberapa field input diberi placeholder yang lebih jelas.
- Perbaikan alur submit laporan dan notifikasi.

Dampak untuk pegawai:

- Pegawai lebih mudah membuat laporan.
- Laporan dapat menjadi bukti realisasi target.
- Atasan dapat memproses laporan bawahan.
- Notifikasi laporan bisa mengarah ke detail laporan.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/CreateReportScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/EditReportScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportDetailScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportListScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/LaporanTargetRelationSection.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/LaporanListViewModel.kt`

### 5. Template Kegiatan

Perubahan penting:

- Template screen diperbarui.
- Input template di Android diberi placeholder agar pegawai awam paham cara mengisi.
- Template mulai lebih terkait dengan pengalaman pembuatan laporan.

Dampak untuk pegawai:

Pegawai dapat membuat laporan lebih cepat dengan template kegiatan.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TemplateScreen.kt`

### 6. Target Kinerja

Perubahan penting:

- Ditambahkan model, repository, screen, dan ViewModel target kinerja.
- Ditambahkan alur daftar target, detail target, form target, review target, dan target bawahan.
- Target dihubungkan dengan laporan kegiatan.
- Dashboard mulai menampilkan status target.

Dampak untuk pegawai:

ASN/PPPK bisa membuat dan mengikuti target kinerja periode berjalan. Atasan bisa melihat atau meninjau target bawahan sesuai kewenangan.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TargetKinerjaModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TargetKinerjaRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TargetKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TargetKinerjaViewModels.kt`

### 7. Penilaian Kinerja

Perubahan penting:

- Ditambahkan model, repository, screen, dan ViewModel penilaian.
- Ditambahkan halaman Penilaian Kinerja.
- Ditambahkan alur penilaian belum dibuat.
- Dashboard menampilkan prioritas seperti penilaian bawahan belum dibuat atau belum final.

Dampak untuk pegawai dan atasan:

- Atasan bisa mengetahui target bawahan yang siap dinilai.
- Pegawai bisa melihat hasil penilaian.
- Dashboard membantu menunjukkan penilaian apa yang belum selesai.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PenilaianKinerjaModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PenilaianKinerjaRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/PenilaianKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/PenilaianKinerjaViewModels.kt`

### 8. Statistik

Perubahan penting:

- Statistik ditambahkan sebagai screen dan ViewModel.
- Statistik masuk navigasi utama.

Dampak untuk pegawai:

Pegawai dapat melihat ringkasan kinerja/laporan dalam format statistik.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/StatisticsScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/StatisticsViewModel.kt`

### 9. TPP ASN/PPPK

Perubahan penting:

- Ditambahkan modul TPP Saya.
- Ditambahkan model, repository, screen, dan ViewModel TPP.
- Dashboard dan top app bar Gaji/TPP mulai menampilkan nilai sesuai jenis pegawai.
- TPP dihubungkan dengan estimasi berjalan dan detail resmi.

Dampak untuk pegawai ASN/PPPK:

ASN/PPPK dapat melihat informasi TPP dari Android. Jika data resmi belum tersedia, aplikasi dapat menampilkan estimasi berjalan sesuai kondisi backend.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TppModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/TppRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TppSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TppViewModel.kt`

### 10. Gaji Non-ASN

Perubahan penting:

- Ditambahkan modul Gaji Saya.
- Ditambahkan model, repository, screen, dan ViewModel Gaji Non-ASN.
- Ditambahkan klasifikasi payroll.
- Dashboard mulai membedakan pegawai Non-ASN/Honorer/Kontrak dari ASN/PPPK.
- Perbaikan dilakukan agar dashboard tidak salah membaca pegawai Non-ASN sebagai TPP ketika profil pegawai belum selesai dimuat.

Dampak untuk pegawai Non-ASN:

Pegawai Non-ASN/Honorer/Kontrak dapat melihat Gaji Saya, bukan TPP. Ini penting karena alur TPP dan Gaji Non-ASN berbeda.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/GajiNonAsnModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/GajiNonAsnRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PayrollClassification.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/GajiSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/GajiNonAsnViewModel.kt`

### 11. Estimasi Berjalan TPP/Gaji

Perubahan penting:

- Ditambahkan model dan repository estimasi berjalan.
- Dashboard, TPP Saya, Gaji Saya, profil, laporan, dan ViewModel terkait mulai membaca data estimasi.
- Estimasi digunakan sebagai preview sementara, bukan pengganti nilai resmi final.

Dampak untuk pegawai:

Pegawai dapat melihat gambaran nilai TPP/Gaji berjalan sebelum perhitungan resmi selesai. Namun nilai resmi tetap bergantung pada proses finalisasi backend.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PayrollLiveEstimateModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/PayrollLiveEstimateRepository.kt`

### 12. SSO dan Deeplink dari E-NTAGO

Perubahan penting:

- Ditambahkan model SSO mobile.
- Ditambahkan screen bridge SSO.
- Ditambahkan cache estimasi payroll dari SSO.
- Navigasi mendukung route bridge dengan `ssoTicket` dan `fallbackRoute`.
- Route detail TPP/Gaji dapat dibuka dari deeplink.
- SSO/deeplink sekarang wajib membawa sesi E-NTAGO yang lengkap.
- Token E-NTAGO dari SSO disimpan di Android agar halaman yang membutuhkan akses E-NTAGO tidak berbeda perilaku dari login biasa.
- Setelah SSO berhasil, aplikasi menjalankan setup pasca-login yang sama, termasuk registrasi FCM.

Dampak untuk pegawai:

Pegawai dapat membuka detail IZAKOD-ASN dari E-NTAGO, terutama untuk melihat TPP/Gaji. Android IZAKOD-ASN dapat menerima konteks dari E-NTAGO, menyimpan sesi E-NTAGO, dan memperlakukan SSO sebagai sesi yang setara dengan login menggunakan akun E-NTAGO.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/MobileSsoModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/SsoPayrollEstimateCacheRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/repository/LoginSessionPostSetup.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/MobileSsoBridgeScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/navigation/Navigation.kt`

### 13. AI Tanya Asisten

Perubahan penting:

- Ditambahkan model AI Panduan.
- Ditambahkan repository token E-NTAGO.
- Ditambahkan screen Tanya Asisten.
- Ditambahkan ViewModel chat AI.
- Chat berbasis topik dan input manual.
- Pembaruan dilakukan agar sumber panduan tidak tampil berulang jika sama.

Dampak untuk pegawai:

Pegawai dapat bertanya tentang SOP dan panduan IZAKOD-ASN langsung dari Android.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/AiPanduanModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/EntagoTokenRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/AiPanduanChatScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/AiPanduanChatViewModel.kt`

### 14. Notifikasi FCM

Perubahan penting:

- FCM service diperbarui beberapa kali.
- MainActivity diperbarui agar bisa menangani intent dari notifikasi.
- Notification screen diperbarui.
- Detail laporan diperbarui agar cocok dengan notifikasi laporan.
- Notifikasi laporan dapat diarahkan ke halaman detail laporan.
- Registrasi FCM pasca-login dipusatkan di helper bersama agar login biasa dan SSO sama-sama mendaftarkan perangkat.

Dampak untuk pegawai dan atasan:

Notifikasi tidak hanya menjadi pemberitahuan pasif. Ketika diklik, notifikasi dapat membawa user ke halaman yang relevan.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/notifications/IzakodFirebaseMessagingService.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/MainActivity.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/repository/LoginSessionPostSetup.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/NotificationScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportDetailScreen.kt`

### 15. Atasan-Bawahan

Perubahan penting:

- Ditambahkan repository atasan-bawahan.
- Ditambahkan halaman Bawahan Saya.
- Ditambahkan ViewModel atasan-bawahan.
- Dashboard menampilkan menu Bawahan jika user punya bawahan atau kewenangan terkait.
- Format tanggal dan tampilan tab pada halaman Bawahan Saya diperbaiki.
- Tombol membuat usulan bawahan personal disembunyikan jika user tidak berhak mengelola bawahan personal.
- Daftar usulan personal Android memakai scope khusus agar operator/non-atasan tidak tercampur dengan workflow verifikasi umum.

Dampak untuk atasan:

Atasan dapat melihat dan mengusulkan bawahan dari Android. Ini mengurangi ketergantungan pada input admin satu per satu, tetapi tetap menjaga agar akun operator atau pegawai yang bukan atasan tidak bisa membuat usulan personal yang tidak sesuai kewenangan.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/AtasanPegawaiRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/BawahanSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/AtasanPegawaiViewModel.kt`

### 16. Verifikasi Atasan-Bawahan

Perubahan penting:

- Ditambahkan halaman Verifikasi Atasan-Bawahan.
- Dashboard dapat menampilkan menu Verifikasi untuk role yang relevan.
- Badge/count mulai digunakan agar user tahu ada usulan yang perlu diproses.

Dampak untuk Kasubbag/Verifikator terkait:

User yang punya kewenangan verifikasi dapat memproses usulan atasan-bawahan dari Android.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/VerifikasiAtasanBawahanScreen.kt`

### 17. Tertunda dan Pengingat

Perubahan penting:

- Ditambahkan halaman Tertunda.
- Pengingat dan reminder screen beberapa kali diperbarui.
- Dashboard menampilkan badge untuk item tertunda.

Dampak untuk pegawai:

Pegawai lebih mudah melihat pekerjaan yang belum selesai, termasuk pengingat dan item yang harus ditindaklanjuti.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TertundaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/TertundaViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReminderScreen.kt`

### 18. Refresh Dashboard dan Cache Sementara

Perubahan penting:

- Ditambahkan `DashboardRefreshNotifier`.
- Banyak ViewModel mulai memberi sinyal dirty/refresh setelah aksi berhasil.
- Dashboard diarahkan agar refresh saat resume hanya pada kondisi yang diperlukan, bukan selalu reload penuh.
- Tab Target, Penilaian, dan TPP/Gaji diarahkan ke pola lazy load saat tab dibuka pertama kali.

Dampak untuk pegawai:

Pengalaman berpindah halaman menjadi lebih nyaman. Dashboard tidak harus selalu loading ulang ketika kembali dari halaman lain, tetapi tetap bisa memperbarui data jika ada perubahan penting.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/DashboardRefreshNotifier.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/viewmodels/DashboardViewModel.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`

### 19. Penyelarasan UI/UX

Perubahan penting:

- Banyak layar lama diperbarui agar mengikuti gaya visual yang sama.
- Hero, card, tab, badge, chip, tombol, dan daftar mulai dibuat lebih konsisten.
- Dashboard menggunakan carousel untuk Perlu Tindakan.
- Tombol pada card Perlu Tindakan dipindahkan ke bawah agar teks tidak terpotong.
- Halaman detail laporan didesain ulang.
- Halaman tertunda, bawahan, dan verifikasi dibuat lebih seragam.

Dampak untuk pegawai:

Aplikasi terasa lebih konsisten dan lebih mudah dipahami, terutama untuk pegawai awam.

File utama yang sering berubah:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportDetailScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/ReportListScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/TargetKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/PenilaianKinerjaScreens.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/BawahanSayaScreen.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/screens/VerifikasiAtasanBawahanScreen.kt`

### 20. Kontrol Versi dan Update Aplikasi Android

Perubahan utama hingga 25 Juni 2026:

- Android menaikkan versi aplikasi menjadi `versionCode 8` dan `versionName 2.0.0`.
- Android menambahkan model dan repository untuk membaca policy versi aplikasi dari server IZAKOD-ASN.
- Aplikasi menyimpan cache policy versi agar pengecekan tidak membebani dashboard.
- Pengecekan versi dijalankan saat aplikasi dibuka, bukan saat dashboard melakukan reload.
- Update opsional menampilkan tombol Update dan Nanti saja.
- Update wajib tidak menampilkan tombol Nanti saja dan dialog tidak bisa ditutup dari area luar.
- Saat dialog update muncul, aplikasi mengirim catatan `update_shown`.
- Saat pegawai menekan tombol Update, aplikasi mengirim catatan `update_clicked` ke server.
- Saat pegawai menunda update opsional, aplikasi mengirim catatan `update_skipped`.
- Saat pegawai menutup dialog update opsional dari luar tombol update, aplikasi mengirim catatan `update_dismissed`.
- Saat aplikasi dibuka kembali dan `versionCode` terdeteksi naik, aplikasi mengirim catatan `update_completed`.
- Jika versi aplikasi sudah memenuhi minimum, dialog wajib tidak muncul lagi.

Dampak untuk pegawai:

Pegawai mendapat arahan yang jelas ketika versi aplikasi perlu diperbarui. Untuk update biasa, pegawai masih dapat melanjutkan aplikasi. Untuk update wajib, pegawai diarahkan memperbarui aplikasi terlebih dahulu agar layanan tetap berjalan dengan aman dan benar.

Dampak untuk admin:

Admin/superadmin dapat mengatur versi Android terbaru, versi minimum yang masih boleh digunakan, pesan update, catatan rilis, dan URL Play Store dari web IZAKOD-ASN.

File utama:

- `app/src/main/java/com/kominfo_mkq/izakod_asn/MainActivity.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/model/AppVersionModels.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/repository/AppVersionRepository.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/local/UserPreferences.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/data/remote/EabsenApiService.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/fcm/DeviceInfo.kt`
- `app/src/main/java/com/kominfo_mkq/izakod_asn/ui/components/AppUpdateDialog.kt`
- `app/build.gradle.kts`

## File Baru Penting yang Muncul Selama Periode Ini

Berikut file baru yang paling berpengaruh terhadap arah aplikasi:

| File | Fungsi |
| --- | --- |
| `data/AuthSessionManager.kt` | Pengelolaan session auth |
| `data/local/AppContextHolder.kt` | Context lokal aplikasi |
| `data/model/AppVersionModels.kt` | Model policy versi dan event update Android |
| `data/remote/AiPanduanModels.kt` | Model AI Tanya Asisten |
| `data/remote/AtasanPegawaiRepository.kt` | API atasan-bawahan |
| `data/remote/EntagoTokenRepository.kt` | Koneksi token E-NTAGO |
| `data/remote/GajiNonAsnModels.kt` | Model Gaji Non-ASN |
| `data/remote/GajiNonAsnRepository.kt` | API Gaji Non-ASN |
| `data/remote/MobileSsoModels.kt` | Model SSO mobile |
| `data/remote/PayrollClassification.kt` | Klasifikasi ASN/PPPK/Non-ASN untuk payroll |
| `data/remote/PayrollLiveEstimateModels.kt` | Model estimasi berjalan |
| `data/remote/PayrollLiveEstimateRepository.kt` | API estimasi berjalan |
| `data/remote/PenilaianKinerjaModels.kt` | Model penilaian kinerja |
| `data/remote/PenilaianKinerjaRepository.kt` | API penilaian kinerja |
| `data/remote/SsoPayrollEstimateCacheRepository.kt` | Cache estimasi dari SSO |
| `data/remote/TargetKinerjaModels.kt` | Model target kinerja |
| `data/remote/TargetKinerjaRepository.kt` | API target kinerja |
| `data/remote/TppModels.kt` | Model TPP |
| `data/remote/TppRepository.kt` | API TPP |
| `data/repository/AppVersionRepository.kt` | Repository pengecekan versi Android dan event update |
| `data/repository/LoginSessionPostSetup.kt` | Setup pasca-login bersama untuk login biasa dan SSO, termasuk registrasi FCM |
| `ui/components/AppUpdateDialog.kt` | Dialog update opsional/wajib Android |
| `ui/screens/AiPanduanChatScreen.kt` | Halaman Tanya Asisten |
| `ui/screens/BawahanSayaScreen.kt` | Halaman Bawahan Saya |
| `ui/screens/GajiSayaScreen.kt` | Halaman Gaji Saya |
| `ui/screens/LaporanTargetRelationSection.kt` | Relasi laporan dengan target |
| `ui/screens/MobileSsoBridgeScreen.kt` | Halaman bridge SSO mobile |
| `ui/screens/PenilaianKinerjaScreens.kt` | Halaman penilaian kinerja |
| `ui/screens/StatisticsScreen.kt` | Halaman statistik |
| `ui/screens/TargetKinerjaScreens.kt` | Halaman target kinerja |
| `ui/screens/TertundaScreen.kt` | Halaman tertunda |
| `ui/screens/TppSayaScreen.kt` | Halaman TPP Saya |
| `ui/screens/VerifikasiAtasanBawahanScreen.kt` | Halaman verifikasi atasan-bawahan |
| `ui/viewmodels/AiPanduanChatViewModel.kt` | State chat AI |
| `ui/viewmodels/AtasanPegawaiViewModel.kt` | State atasan-bawahan |
| `ui/viewmodels/DashboardRefreshNotifier.kt` | Sinyal refresh dashboard |
| `ui/viewmodels/GajiNonAsnViewModel.kt` | State Gaji Non-ASN |
| `ui/viewmodels/PenilaianKinerjaViewModels.kt` | State penilaian |
| `ui/viewmodels/StatisticsViewModel.kt` | State statistik |
| `ui/viewmodels/TargetKinerjaViewModels.kt` | State target |
| `ui/viewmodels/TertundaViewModel.kt` | State tertunda |
| `ui/viewmodels/TppViewModel.kt` | State TPP |

## Area yang Paling Banyak Berubah

### `DashboardScreen.kt`

Perubahan pada dashboard sangat besar karena dashboard menjadi pusat navigasi, status, badge, hero pegawai, carousel perlu tindakan, dan ringkasan TPP/Gaji/Target/Penilaian.

### `DashboardViewModel.kt`

ViewModel dashboard menjadi pusat penggabungan data dari banyak endpoint: profil pegawai, ringkasan dashboard, target, penilaian, laporan, TPP/Gaji, badge, dan tindakan prioritas.

### `Navigation.kt`

Navigasi berubah karena aplikasi mendapat banyak halaman baru: Target, Penilaian, TPP, Gaji, AI, SSO bridge, Bawahan, Verifikasi, Tertunda, dan route detail.

### `ReportListScreen.kt` dan `ReportDetailScreen.kt`

Laporan kegiatan makin kompleks karena mendukung laporan sendiri, laporan bawahan, relasi target, revisi, review, detail, dan notifikasi.

### `TargetKinerjaScreens.kt` dan `PenilaianKinerjaScreens.kt`

Kedua area ini membentuk workflow kinerja ASN/PPPK yang sebelumnya belum ada di Android.

## Dampak Terhadap Pengalaman Pegawai

Sebelum periode perubahan ini, Android IZAKOD-ASN lebih sederhana. Setelah perubahan sampai 25 Juni 2026, pegawai bisa:

- Login dan membuka dashboard yang lebih informatif.
- Melihat pekerjaan prioritas dari carousel Perlu Tindakan.
- Membuat, mengedit, dan melihat laporan kegiatan.
- Menautkan laporan kegiatan ke target.
- Melihat target, penilaian, statistik, dan pekerjaan tertunda.
- Melihat TPP untuk ASN/PPPK.
- Melihat Gaji untuk Non-ASN/Honorer/Kontrak.
- Melihat estimasi berjalan sebelum nilai resmi final.
- Membuka detail TPP/Gaji dari E-NTAGO melalui SSO/deeplink.
- Menerima notifikasi dan masuk ke halaman yang relevan.
- Bertanya ke AI Tanya Asisten.
- Bagi atasan, melihat/mengusulkan bawahan dan memproses laporan/penilaian bawahan.
- Bagi role terkait, memverifikasi usulan atasan-bawahan.
- Mendapat arahan update aplikasi jika versi terbaru tersedia atau jika versi lama sudah tidak didukung.

## Catatan Risiko dan Hal yang Perlu Dijaga

1. Dashboard menjadi sangat penting dan kompleks. Setiap perubahan pada dashboard perlu dicek terhadap pegawai ASN/PPPK, Non-ASN, atasan, verifikator, dan pegawai baru.
2. TPP dan Gaji Non-ASN harus tetap dipisahkan. Pegawai Non-ASN tidak boleh terbaca sebagai TPP hanya karena data profil belum selesai dimuat.
3. Estimasi berjalan harus tetap dipahami sebagai preview, bukan nilai resmi final.
4. Deeplink SSO dari E-NTAGO perlu diuji ulang setiap kali ada perubahan route TPP/Gaji.
5. Badge count harus selalu berasal dari data ringkasan yang sama agar dashboard, akses cepat, dan halaman detail tidak membingungkan.
6. Laporan bawahan dan laporan pribadi harus dibedakan jelas, terutama pada wording seperti "laporan pribadi perlu revisi" vs "laporan bawahan perlu dipantau".
7. Karena banyak perubahan UI terjadi cepat pada 15-20 Juni, regression test manual di perangkat Android tetap penting.
8. Policy versi aplikasi harus diuji dengan skenario update opsional dan wajib agar pegawai tidak terkunci oleh konfigurasi yang keliru.

## Rekomendasi Verifikasi Lanjutan

Untuk memastikan perubahan Android tetap aman, disarankan melakukan cek berikut:

1. Build Android:

   ```powershell
   cd E:\Android\izakod-asn\izakod-asn-android
   .\gradlew.bat compileDebugKotlin
   ```

2. Uji login pegawai Non-ASN:

   - Dashboard harus menampilkan Gaji, bukan TPP.
   - Gaji Saya harus sama dengan data backend.
   - SSO dari E-NTAGO harus tetap membawa user ke detail Gaji.

3. Uji login ASN/PPPK:

   - Dashboard harus menampilkan TPP.
   - Target, laporan, realisasi, dan penilaian harus sesuai.

4. Uji login atasan:

   - Menu Bawahan tampil jika punya bawahan aktif.
   - Laporan bawahan tampil di tab Bawahan.
   - Badge laporan bawahan tidak bercampur dengan laporan pribadi.

5. Uji role verifikator/kasubbag terkait:

   - Menu Verifikasi tampil sesuai kewenangan.
   - Badge verifikasi sesuai jumlah usulan.

6. Uji notifikasi:

   - Klik notifikasi laporan harus membuka detail laporan yang benar.
   - Notifikasi lama tidak memakai wording lama yang membingungkan.

7. Uji AI Tanya Asisten:

   - Topik muncul sesuai dokumen panduan.
   - Jawaban tidak menampilkan sumber panduan yang sama berulang.

8. Uji kontrol versi Android:

   - Update opsional menampilkan tombol Nanti saja.
   - Update wajib tidak menampilkan tombol Nanti saja.
   - Klik area luar dialog tidak menutup dialog update wajib.
   - Tombol Update membuka Play Store atau halaman update.
   - Setelah aplikasi dipasang dengan `versionCode` lebih baru, dialog wajib tidak muncul lagi jika versi sudah memenuhi minimum.

## Kesimpulan

Perubahan Android IZAKOD-ASN dari 15 Januari 2026 sampai 25 Juni 2026 sangat besar. Aplikasi berkembang dari aplikasi laporan dasar menjadi aplikasi kerja pegawai yang mencakup laporan kegiatan, target, penilaian, statistik, TPP, Gaji Non-ASN, estimasi berjalan, SSO E-NTAGO, notifikasi, AI Panduan, atasan-bawahan, verifikasi, dan kontrol versi aplikasi Android.

Fokus terbesar project Android saat ini adalah menjaga konsistensi data dan pengalaman pegawai: dashboard harus cepat, tidak menampilkan status yang salah saat data belum selesai dimuat, dan semua badge/tindakan harus mengarah ke halaman yang tepat.

Untuk pengembangan berikutnya, area yang paling perlu dijaga adalah Dashboard, TPP/Gaji, laporan bawahan, SSO/deeplink, dan permission role, karena area ini paling banyak bersinggungan dengan kebingungan pegawai sehari-hari.
