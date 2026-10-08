package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserProfileDoc(
    val userId: String = "",
    val email: String = "",
    val displayName: String? = null,
    val selectedModel: String = "ScenoviX Ultra 3.5",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "email" to email,
            "selectedModel" to selectedModel,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        displayName?.let { map["displayName"] = it }
        return map
    }
}

@IgnoreExtraProperties
data class ChatMessageDoc(
    val id: String = "",
    val userId: String = "",
    val sender: String = "USER", // "USER" or "AI"
    val text: String = "",
    val timestamp: String = "",
    val attachmentCount: Int = 0,
    val tokenStats: String? = null,
    val hasReasoning: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "userId" to userId,
            "sender" to sender,
            "text" to text,
            "timestamp" to timestamp,
            "attachmentCount" to attachmentCount,
            "hasReasoning" to hasReasoning,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        tokenStats?.let { map["tokenStats"] = it }
        return map
    }
}
