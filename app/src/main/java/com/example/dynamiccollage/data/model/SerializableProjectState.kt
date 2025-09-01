package com.example.dynamiccollage.data.model

// This file contains the data classes designed for JSON serialization.
// They only use primitive types to ensure compatibility with Gson.

data class SerializableProjectState(
    val coverConfig: SerializableCoverPageConfig,
    val pageGroups: List<SerializablePageGroup>,
    val sunatData: SelectedSunatData?, // This one is already serializable
    val themeName: String? = null,
    val imageEffectSettings: Map<String, ImageEffectSettings>? = null
)

data class SerializableCoverPageConfig(
    val clientNameStyle: SerializableTextStyleConfig,
    val showClientPrefix: Boolean,
    val documentType: Int, // ordinal of DocumentType enum
    val rucStyle: SerializableTextStyleConfig,
    val subtitleStyle: SerializableTextStyleConfig,
    val showAddressPrefix: Boolean,
    val allCaps: Boolean,
    val mainImageUri: String?,
    val marginTop: Float,
    val marginBottom: Float,
    val marginLeft: Float,
    val marginRight: Float,
    val pageOrientation: Int, // ordinal of PageOrientation enum
    val clientWeight: Float,
    val rucWeight: Float,
    val addressWeight: Float,
    val separationWeight: Float,
    val photoWeight: Float,
    val photoStyle: SerializableRowStyle,
    val quality: Int?,
    val pageBackgroundColor: Int?,
    val imageBorderSettingsMap: Map<String, SerializableImageBorderSettings>?,
    val templateName: String?
)

data class SerializableImageBorderSettings(
    val style: Int, // ordinal of ImageBorderStyle
    val size: Float
)

data class SerializablePageGroup(
    val id: String,
    val groupName: String,
    val orientation: Int, // ordinal of PageOrientation enum
    val photosPerSheet: Int,
    val sheetCount: Int,
    val optionalTextStyle: SerializableTextStyleConfig?,
    val imageUris: List<String>,
    val imageSpacing: Float,
    val smartLayoutEnabled: Boolean?
)

data class SerializableTextStyleConfig(
    val id: String,
    val fontFamilyName: String, // "Default", "Calibri", etc.
    val fontSize: Int,
    val fontWeight: Int?, // weight value
    val fontStyle: Int?, // ordinal of FontStyle enum
    val textAlign: Int, // ordinal of TextAlign enum
    val fontColor: Int, // ARGB Int from Color
    val content: String,
    val allCaps: Boolean,
    val rowStyle: SerializableRowStyle
)

data class SerializableRowStyle(
    val backgroundColor: Int, // ARGB Int from Color
    val padding: SerializablePaddingValues,
    val border: SerializableBorderProperties
)

data class SerializablePaddingValues(
    val top: Float,
    val bottom: Float,
    val left: Float,
    val right: Float
)

data class SerializableBorderProperties(
    val color: Int, // ARGB Int from Color
    val thickness: Float,
    val top: Boolean,
    val bottom: Boolean,
    val left: Boolean,
    val right: Boolean
)
