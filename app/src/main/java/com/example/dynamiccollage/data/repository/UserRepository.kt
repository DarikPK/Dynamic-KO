package com.example.dynamiccollage.data.repository

import com.example.dynamiccollage.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getUser(uid: String): User? {
        val document = usersCollection.document(uid).get().await()
        return document.toObject(User::class.java)
    }

    suspend fun getChildren(parentId: String): Flow<List<User>> = callbackFlow {
        val listener = usersCollection.whereEqualTo("parentId", parentId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUser(user: User) {
        usersCollection.document(user.uid).set(user).await()
    }
}
