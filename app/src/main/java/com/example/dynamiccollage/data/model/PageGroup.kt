package com.example.dynamiccollage.data.model

data class PageGroup(
    val id: String,
    val name: String,
    val sheetCount: Int,
    val imageUris: List<String>,
    val tableLayout: Pair<Int, Int>, // Columns, Rows
    val orientation: PageOrientation,
    val optionalTextStyle: TextStyleConfig,
    val imageSpacing: Float = 2f
)
