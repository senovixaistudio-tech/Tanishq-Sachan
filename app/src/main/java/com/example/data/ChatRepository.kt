package com.example.data

import android.content.Context
import com.example.R
import com.example.model.ChatMessageDoc
import com.example.model.UserProfileDoc
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(),
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun saveUserProfile(profile: UserProfileDoc): Result<Unit> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            db.document(path).set(profile.toCreateMap()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun getUserProfile(): Result<UserProfileDoc?> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            val snapshot = db.document(path).get().await()
            snapshot.toObject(UserProfileDoc::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            throw e
        }
    }

    suspend fun saveMessage(message: ChatMessageDoc): Result<String> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid/messages/${message.id}"
        try {
            db.document(path).set(message.toCreateMap()).await()
            message.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    fun observeMessages(): Flow<List<ChatMessageDoc>> = callbackFlow {
        val uid = requireUserId()
        val path = "users/$uid/messages"
        val query = db.collection(path).orderBy("createdAt", Query.Direction.ASCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val messages = snapshot.documents.mapNotNull { it.toObject(ChatMessageDoc::class.java) }
                trySend(messages)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid/messages/$messageId"
        try {
            db.document(path).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    suspend fun clearAllMessages(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid/messages"
        try {
            val snapshot = db.collection(path).get().await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }
}
