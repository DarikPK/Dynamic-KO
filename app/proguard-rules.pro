# -------------------------------
# PDFBOX
# -------------------------------
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-keep class com.gemalto.jp2.** { *; }
-dontwarn com.gemalto.jp2.**
-keep class org.apache.fontbox.** { *; }
-dontwarn org.apache.fontbox.**

# -------------------------------
# ATRIBUTOS NECESARIOS (MUY IMPORTANTE)
# -------------------------------
-keepattributes Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault, EnclosingMethod, InnerClasses, KotlinMetadata

# -------------------------------
# RETROFIT Y GSON
# -------------------------------
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.Platform$Java8

-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# -------------------------------
# TU API SERVICE
# -------------------------------
-keep interface pe.pixelcollage.app.remote.SunatApiService { *; }

# -------------------------------
# MODELOS
# -------------------------------
-keep class pe.pixelcollage.app.remote.RucData { *; }
-keep class pe.pixelcollage.app.remote.DniData { *; }
-keep class pe.pixelcollage.app.data.model.** { *; }

# -------------------------------
# CAMPOS CON @SerializedName
# -------------------------------
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName *;
}
