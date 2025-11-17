# Reglas de ProGuard para PDFBox
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

# Reglas para las dependencias de PDFBox que R8 no puede encontrar
-keep class com.gemalto.jp2.** { *; }
-dontwarn com.gemalto.jp2.**

# Mantener las fuentes de PDFBox
-keep class org.apache.fontbox.** { *; }
-dontwarn org.apache.fontbox.**

# Otras reglas generales de Android
-keep class androidx.core.os.TraceCompat
-keep class androidx.versionedparcelable.CustomVersionedParcelable
-keep class androidx.versionedparcelable.VersionedParcelable
-keep class androidx.versionedparcelable.VersionedParcelize

#######################################################
# 🔐 CORRECCIÓN DE RETROFIT + GSON (SUNAT API)
#######################################################

# Mantener información de tipos genéricos
-keepattributes Signature
-keepattributes *Annotation*

# Mantener clases e interfaces de Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.Platform$Java8
-dontwarn retrofit2.adapter.rxjava2.**

# Mantener el convertidor Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-keep class com.google.gson.reflect.TypeToken
-keep class com.google.gson.TypeAdapter
-keep class com.google.gson.TypeAdapterFactory
-keep class com.google.gson.internal.bind.** { *; }

# Mantener todos los modelos y servicios SUNAT
-keep class pe.pixelcollage.app.remote.** { *; }

# Mantener los modelos de datos para la serialización del estado del proyecto
-keep class pe.pixelcollage.app.data.model.** { *; }

# Mantener campos con @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
