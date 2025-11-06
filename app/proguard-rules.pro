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
