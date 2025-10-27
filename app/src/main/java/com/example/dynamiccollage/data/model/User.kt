package com.example.dynamiccollage.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "",
    val parentId: String = "",
    val allow_auto_login: Boolean = true,
    val createdAt: Long = 0L
)
