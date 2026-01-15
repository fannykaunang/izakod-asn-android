# ==========================================
# KONFIGURASI PROGUARD - OPTIMIZED
# ==========================================

# 1. ATRIBUT PENTING (Wajib untuk Generics/Reflection)
# Ini menjaga agar Signature <T> tidak dibuang oleh R8.
# Solusi utama untuk error "Class cannot be cast to ParameterizedType"
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# 2. KOTLIN METADATA
-keep class kotlin.Metadata { *; }

# 3. RETROFIT & OKHTTP
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class retrofit2.CallAdapter$Factory { *; }
-keep class retrofit2.Converter$Factory { *; }
-keep class retrofit2.http.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**

# 4. GSON (Serialization/Deserialization)
-keep class com.google.gson.** { *; }
# Tambahan: Menjaga TypeToken agar tidak crash saat R8 agresif
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# 5. KOTLIN COROUTINES (Jika pakai suspend function)
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }

# ==========================================
# ATURAN KHUSUS PROJECT ANDA (APP SPECIFIC)
# ==========================================

# Menjaga Interface API Retrofit
-keep interface com.kominfo_mkq.izakod_asn.data.remote.EabsenApiService { *; }

# Menjaga Data Model (POJO) agar field tidak di-rename/hilang
# -keep class com.kominfo_mkq.izakod_asn.data.model.** { *; }
-keepclassmembers class com.kominfo_mkq.izakod_asn.data.model.** { *; }

# --- PENAMBAHAN KRUSIAL ---
# Jika Anda memiliki class Wrapper Generic (misal: BaseResponse<T>)
# yang lokasinya BUKAN di dalam package 'data.model',
# aturan di bawah ini akan menjaganya.
# (Saya asumsikan wrapper mungkin ada di data.remote atau root data)

-keep class com.kominfo_mkq.izakod_asn.data.remote.** { *; }
# -keep class com.kominfo_mkq.izakod_asn.data.** { *; }

# Atau jika Anda menggunakan SerializedName, pastikan field-nya aman:
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}