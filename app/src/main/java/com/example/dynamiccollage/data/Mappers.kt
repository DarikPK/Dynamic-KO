package com.example.dynamiccollage.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.dynamiccollage.data.model.*
import com.example.dynamiccollage.ui.theme.calibriFontFamily

// Mappers for primitive/leaf types

private fun FontFamily.toSerializableName(): String = if (this == calibriFontFamily) "Calibri" else "Default"
private fun String.toFontFamilyFromName(): FontFamily = if (this == "Calibri") calibriFontFamily else FontFamily.Default

private fun TextAlign.toSerializableInt(): Int = when (this) {
    TextAlign.Left -> 0
    TextAlign.Right -> 1
    TextAlign.Center -> 2
    TextAlign.Justify -> 3
    TextAlign.Start -> 4
    TextAlign.End -> 5
    else -> 2 // Default to Center
}
private fun Int.toTextAlign(): TextAlign = when (this) {
    0 -> TextAlign.Left
    1 -> TextAlign.Right
    2 -> TextAlign.Center
    3 -> TextAlign.Justify
    4 -> TextAlign.Start
    5 -> TextAlign.End
    else -> TextAlign.Center
}

private fun FontStyle.toSerializableInt(): Int = when (this) {
    FontStyle.Normal -> 0
    FontStyle.Italic -> 1
    else -> 0
}
private fun Int.toFontStyle(): FontStyle = when (this) {
    0 -> FontStyle.Normal
    1 -> FontStyle.Italic
    else -> FontStyle.Normal
}


// Mappers for BorderProperties
fun BorderProperties.toSerializable() = SerializableBorderProperties(
    color = this.color.toArgb(),
    thickness = this.thickness,
    top = this.top,
    bottom = this.bottom,
    left = this.left,
    right = this.right
)

fun SerializableBorderProperties.toDomain() = BorderProperties(
    color = Color(this.color),
    thickness = this.thickness,
    top = this.top,
    bottom = this.bottom,
    left = this.left,
    right = this.right
)

// Mappers for PaddingValues
fun PaddingValues.toSerializable() = SerializablePaddingValues(
    top = this.top,
    bottom = this.bottom,
    left = this.left,
    right = this.right
)

fun SerializablePaddingValues.toDomain() = PaddingValues(
    top = this.top,
    bottom = this.bottom,
    left = this.left,
    right = this.right
)

// Mappers for RowStyle
fun RowStyle.toSerializable() = SerializableRowStyle(
    backgroundColor = this.backgroundColor.toArgb(),
    padding = this.padding.toSerializable(),
    border = this.border.toSerializable()
)

fun SerializableRowStyle.toDomain() = RowStyle(
    backgroundColor = Color(this.backgroundColor),
    padding = this.padding.toDomain(),
    border = this.border.toDomain()
)

// Mappers for TextStyleConfig
fun TextStyleConfig.toSerializable() = SerializableTextStyleConfig(
    id = this.id,
    fontFamilyName = this.fontFamily.toSerializableName(),
    fontSize = this.fontSize,
    fontWeight = this.fontWeight?.weight,
    fontStyle = this.fontStyle?.toSerializableInt(),
    textAlign = this.textAlign.toSerializableInt(),
    fontColor = this.fontColor.toArgb(),
    content = this.content,
    allCaps = this.allCaps,
    rowStyle = this.rowStyle.toSerializable()
)

fun SerializableTextStyleConfig.toDomain() = TextStyleConfig(
    id = this.id,
    fontFamily = this.fontFamilyName.toFontFamilyFromName(),
    fontSize = this.fontSize,
    fontWeight = this.fontWeight?.let { FontWeight(it) },
    fontStyle = this.fontStyle?.toFontStyle(),
    textAlign = this.textAlign.toTextAlign(),
    fontColor = Color(this.fontColor),
    content = this.content,
    allCaps = this.allCaps,
    rowStyle = this.rowStyle.toDomain()
)

// Mappers for PageGroup
fun PageGroup.toSerializable() = SerializablePageGroup(
    id = this.id,
    groupName = this.groupName,
    orientation = this.orientation.ordinal,
    photosPerSheet = this.photosPerSheet,
    sheetCount = this.sheetCount,
    optionalTextStyle = this.optionalTextStyle.toSerializable(),
    imageUris = this.imageUris,
    imageSpacing = this.imageSpacing
)

fun SerializablePageGroup.toDomain() = PageGroup(
    id = this.id,
    groupName = this.groupName,
    orientation = PageOrientation.values()[this.orientation],
    photosPerSheet = this.photosPerSheet,
    sheetCount = this.sheetCount,
    optionalTextStyle = this.optionalTextStyle.toDomain(),
    imageUris = this.imageUris,
    imageSpacing = this.imageSpacing
)

// Mappers for CoverPageConfig
fun CoverPageConfig.toSerializable() = SerializableCoverPageConfig(
    clientNameStyle = this.clientNameStyle.toSerializable(),
    showClientPrefix = this.showClientPrefix,
    documentType = this.documentType.ordinal,
    rucStyle = this.rucStyle.toSerializable(),
    subtitleStyle = this.subtitleStyle.toSerializable(),
    showAddressPrefix = this.showAddressPrefix,
    allCaps = this.allCaps,
    mainImageUri = this.mainImageUri,
    marginTop = this.marginTop,
    marginBottom = this.marginBottom,
    marginLeft = this.marginLeft,
    marginRight = this.marginRight,
    pageOrientation = this.pageOrientation.ordinal,
    clientWeight = this.clientWeight,
    rucWeight = this.rucWeight,
    addressWeight = this.addressWeight,
    separationWeight = this.separationWeight,
    photoWeight = this.photoWeight,
    photoStyle = this.photoStyle.toSerializable(),
    templateName = this.templateName
)

fun SerializableCoverPageConfig.toDomain() = CoverPageConfig(
    clientNameStyle = this.clientNameStyle.toDomain(),
    showClientPrefix = this.showClientPrefix,
    documentType = DocumentType.values()[this.documentType],
    rucStyle = this.rucStyle.toDomain(),
    subtitleStyle = this.subtitleStyle.toDomain(),
    showAddressPrefix = this.showAddressPrefix,
    allCaps = this.allCaps,
    mainImageUri = this.mainImageUri,
    marginTop = this.marginTop,
    marginBottom = this.marginBottom,
    marginLeft = this.marginLeft,
    marginRight = this.marginRight,
    pageOrientation = PageOrientation.values()[this.pageOrientation],
    clientWeight = this.clientWeight,
    rucWeight = this.rucWeight,
    addressWeight = this.addressWeight,
    separationWeight = this.separationWeight,
    photoWeight = this.photoWeight,
    photoStyle = this.photoStyle.toDomain(),
    templateName = this.templateName
)

// Mapper for the top-level project state
fun SerializableProjectState.toDomain() = ProjectState(
    coverConfig = this.coverConfig.toDomain(),
    pageGroups = this.pageGroups.map { it.toDomain() },
    sunatData = this.sunatData
)
