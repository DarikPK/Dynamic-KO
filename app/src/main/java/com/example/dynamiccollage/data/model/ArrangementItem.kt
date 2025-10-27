package com.example.dynamiccollage.data.model

enum class SheetType {
    SINGLE, DOUBLE
}

data class PhotoArrangementItem(
    val uri: String,
    var order: Int,
    var sheetType: SheetType
)