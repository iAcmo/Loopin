package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class InstaSquadRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedUser_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = InstaSquadRepository(firestore, auth)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserProfile(
                username = "alice_wonder",
                displayName = "Alice W",
                bio = "Hello world",
                avatarUrl = "https://example.com/avatar.jpg",
                squadId = "ALPHA"
            )
        }
        if (result.isFailure) {
            System.err.println("SAVE_PROFILE_ERROR: " + result.exceptionOrNull())
            result.exceptionOrNull()?.printStackTrace()
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun createPost_andObserve_returnsCreatedPost() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = InstaSquadRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createPost(
                caption = "Testing Instagram Feed Post",
                imageUrl = "https://example.com/post.jpg",
                squadId = "ALPHA",
                username = "alice_wonder",
                userAvatar = "https://example.com/avatar.jpg"
            )
        }
        assertTrue(createResult.isSuccess)
        val postId = createResult.getOrThrow()
        assertTrue(postId.isNotEmpty())

        val posts = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observePosts().first { list -> list.any { it.id == postId } }
        }
        assertTrue(posts.any { it.id == postId })
    }

    @Test
    fun squadRoom_ensureAndSendMessage_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = InstaSquadRepository(firestore, auth)

        val roomResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureSquadRoom("ALPHA")
        }
        assertTrue(roomResult.isSuccess)
        val room = roomResult.getOrThrow()

        val msgResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.sendMessage(
                roomId = room.roomId,
                text = "Hey squad ALPHA!",
                type = "text",
                mediaUrl = "",
                senderName = "Alice",
                senderAvatar = "https://example.com/avatar.jpg"
            )
        }
        assertTrue(msgResult.isSuccess)
    }

    @Test
    fun unauthenticated_requireUserId_throwsIllegalStateException() {
        auth.signOut()
        val repository = InstaSquadRepository(firestore, auth)

        try {
            repository.requireUserId()
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertNotNull(e.message)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
