package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.ChatMessageDoc
import com.example.model.UserProfileDoc
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class ChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedUser_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ChatRepository(firestore, auth)

        val profile = UserProfileDoc(
            userId = aliceUid,
            email = ALICE_EMAIL,
            displayName = "Alice",
            selectedModel = "ScenoviX Ultra 3.5"
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserProfile(profile)
        }
        assertTrue("Save user profile failed: ${saveResult.exceptionOrNull()?.message}", saveResult.isSuccess)

        val fetchedProfileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserProfile()
        }
        assertTrue(fetchedProfileResult.isSuccess)
        val fetchedProfile = fetchedProfileResult.getOrNull()
        assertNotNull(fetchedProfile)
        assertEquals(aliceUid, fetchedProfile?.userId)
        assertEquals(ALICE_EMAIL, fetchedProfile?.email)
    }

    @Test
    fun saveMessage_andObserve_authenticatedUser_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ChatRepository(firestore, auth)
        val msgId = "msg_${UUID.randomUUID()}"

        val message = ChatMessageDoc(
            id = msgId,
            userId = aliceUid,
            sender = "USER",
            text = "Initiate multi-agent sensor scan",
            timestamp = "12:00 PM",
            attachmentCount = 1,
            tokenStats = "512 tokens",
            hasReasoning = true
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveMessage(message)
        }
        assertTrue(saveResult.isSuccess)

        val messages = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMessages().first { list -> list.any { it.id == msgId } }
        }
        assertTrue(messages.any { it.id == msgId && it.text == "Initiate multi-agent sensor scan" })
    }

    @Test
    fun getMessage_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ChatRepository(firestore, auth)
        val msgId = "msg_${UUID.randomUUID()}"

        val message = ChatMessageDoc(
            id = msgId,
            userId = aliceUid,
            sender = "USER",
            text = "Classified quantum parameters",
            timestamp = "01:00 PM"
        )
        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveMessage(message)
        }
        assertTrue(saveResult.isSuccess)

        // Switch to Bob
        signInTestUser(BOB_EMAIL)
        // Bob attempts direct Firestore read of Alice's message path
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.document("users/$aliceUid/messages/$msgId").get().await()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun saveMessage_unauthenticatedUser_failsImmediately() = runBlocking {
        auth.signOut()
        val repository = ChatRepository(firestore, auth)

        val message = ChatMessageDoc(
            id = "msg_${UUID.randomUUID()}",
            userId = "unauthenticated",
            sender = "USER",
            text = "Unauthorized message"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveMessage(message)
        }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 8000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
