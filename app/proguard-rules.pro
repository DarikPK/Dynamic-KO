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

# 1. Preservar atributos críticos para la reflexión.
# Signature: Necesario para tipos genéricos.
# InnerClasses: Necesario si usas clases internas.
# *Annotation*: Preserva todas las anotaciones.
# RuntimeVisibleParameterAnnotations: CRÍTICO para que Retrofit lea las anotaciones de los parámetros de los métodos (@Query, @Path, etc.).
# KotlinMetaData: CRÍTICO para preservar la información de las funciones suspend (corrutinas).
-keepattributes Signature, InnerClasses, *Annotation*, RuntimeVisibleParameterAnnotations, KotlinMetaData

# 2. Preservar completamente las librerías Retrofit y Gson.
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.Platform$Java8

-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# 3. Preservar la interfaz del servicio de API de forma explícita.
# Esto asegura que R8 no elimine la interfaz, sus métodos, ni sus anotaciones.
-keep interface pe.pixelcollage.app.remote.SunatApiService { *; }

# 4. Preservar los modelos de datos (Data Classes) que usa la API.
-keep class pe.pixelcollage.app.remote.RucData { *; }
-keep class pe.pixelcollage.app.remote.DniData { *; }

# 5. Preservar los modelos de datos para la serialización del estado del proyecto.
-keep class pe.pixelcollage.app.data.model.** { *; }

# 6. Preservar campos que usan la anotación @SerializedName de Gson.
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
