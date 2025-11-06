package pe.pixelcollage.app.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


open class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val pdfUsageCollection = firestore.collection("pdf_usage")

    open suspend fun login(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    open suspend fun createAccount(email: String, password: String): FirebaseUser? {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user
    }

    suspend fun signInAnonymously(): FirebaseUser? {
        val result = firebaseAuth.signInAnonymously().await()
        return result.user
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    suspend fun getPdfCount(deviceId: String): Int {
        if (deviceId.isBlank()) return 0
        val document = pdfUsageCollection.document(deviceId).get().await()
        return document.getLong("count")?.toInt() ?: 0
    }

    suspend fun incrementPdfCount(deviceId: String) {
        if (deviceId.isBlank()) return
        val docRef = pdfUsageCollection.document(deviceId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val newCount = (snapshot.getLong("count") ?: 0) + 1
            transaction.set(docRef, mapOf("count" to newCount))
        }.await()
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
