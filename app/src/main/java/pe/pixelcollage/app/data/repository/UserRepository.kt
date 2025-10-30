package pe.pixelcollage.app.data.repository

import pe.pixelcollage.app.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    open suspend fun getUser(uid: String): User? {
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

    suspend fun createUser(uid: String, nick: String, email: String) {
        val user = User(uid = uid, nick = nick, email = email, role = "user")
        usersCollection.document(uid).set(user).await()
    }

    suspend fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    suspend fun deleteUser(user: User) {
        usersCollection.document(user.uid).delete().await()
    }

    suspend fun updateUserLockState(user: User, isLocked: Boolean) {
        usersCollection.document(user.uid).update("locked", isLocked).await()
    }
}
