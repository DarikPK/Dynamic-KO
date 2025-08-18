package com.example.dynamiccollage.data.model

data class GeneratedPage(
    val imageUris: List<String>,
    val orientation: PageOrientation,
    val groupId: String // To look up border settings
)
