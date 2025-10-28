package com.example.dynamiccollage.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

open class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val functions = Firebase.functions("us-central1")

    open suspend fun login(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        // Forzar la actualización del token para obtener las custom claims (roles)
        result.user?.getIdToken(true)
        return result.user
    }

    suspend fun createChildUser(nick: String, email: String, password: String, allowAutoLogin: Boolean) {
        val data = hashMapOf(
            "nick" to nick,
            "email" to email,
            "password" to password,
            "allow_auto_login" to allowAutoLogin
        )
        functions.getHttpsCallable("createChildUser").call(data).await()
    }

    suspend fun deleteUser(uid: String) {
        val data = hashMapOf("uid" to uid)
        functions.getHttpsCallable("deleteUser").call(data).await()
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
