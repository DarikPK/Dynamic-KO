package com.example.dynamiccollage.data.model

data class User(
    val uid: String = "",
    val role: String = "",
    val parentId: String = "",
    val allow_auto_login: Boolean = true
)
