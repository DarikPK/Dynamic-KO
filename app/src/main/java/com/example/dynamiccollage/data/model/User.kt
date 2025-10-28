package com.example.dynamiccollage.data.model

package com.example.dynamiccollage.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "",
    val parentId: String = "",
    val allow_auto_login: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
)
