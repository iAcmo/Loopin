package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Community
import com.example.data.model.InstantItem
import com.example.data.model.MusicNote
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.Room
import com.example.data.model.RoomMessage
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class InstaSquadRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(db.app)
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun currentUserId(): String? = auth.currentUser?.uid

    // User Profile
    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { it.toObject(UserProfile::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, path)
                throw e
            }
    }

    fun observePublicUsers(): Flow<List<UserProfile>> {
        val path = "users"
        val currentUid = currentUserId()
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(UserProfile::class.java).filter { user ->
                    user.userId.isNotBlank() && user.userId != currentUid
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun checkUsernameAvailable(desiredUsername: String, currentUid: String): Boolean {
        val clean = desiredUsername.lowercase().trim()
        if (clean.length < 2) return false
        val snap = db.collection("users")
            .whereEqualTo("username", clean)
            .limit(2)
            .get()
            .await()
        return snap.documents.all { doc -> doc.id == currentUid }
    }

    suspend fun saveUserProfile(
        username: String,
        displayName: String,
        bio: String,
        avatarUrl: String,
        squadId: String = "",
        onboardingComplete: Boolean = true
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val data = mapOf(
            "userId" to uid,
            "username" to username.lowercase().trim(),
            "displayName" to displayName,
            "bio" to bio,
            "avatarUrl" to avatarUrl,
            "squadId" to squadId.uppercase().trim(),
            "isOnline" to true,
            "isPublicDirectory" to true,
            "isPrivate" to false,
            "followers" to emptyList<String>(),
            "following" to emptyList<String>(),
            "blockedUsers" to emptyList<String>(),
            "onboardingComplete" to onboardingComplete,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        bio: String,
        username: String,
        avatarUrl: String,
        isPublicDirectory: Boolean = true,
        isPrivate: Boolean = false
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val data = mapOf(
            "displayName" to displayName,
            "bio" to bio,
            "username" to username.lowercase().trim(),
            "avatarUrl" to avatarUrl,
            "isPublicDirectory" to isPublicDirectory,
            "isPrivate" to isPrivate,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleFollowUser(targetUserId: String): Result<Boolean> = runCatching {
        val uid = requireUserId()
        if (uid == targetUserId) return@runCatching false

        val currentDoc = db.collection("users").document(uid).get().await()
        val currentProfile = currentDoc.toObject(UserProfile::class.java) ?: UserProfile()
        val isFollowing = currentProfile.following.contains(targetUserId)

        val targetDocRef = db.collection("users").document(targetUserId)
        val currentDocRef = db.collection("users").document(uid)

        if (isFollowing) {
            targetDocRef.update("followers", FieldValue.arrayRemove(uid)).await()
            currentDocRef.update("following", FieldValue.arrayRemove(targetUserId)).await()
            false
        } else {
            targetDocRef.update("followers", FieldValue.arrayUnion(uid)).await()
            currentDocRef.update("following", FieldValue.arrayUnion(targetUserId)).await()
            true
        }
    }

    suspend fun toggleBlockUser(targetUserId: String): Result<Boolean> = runCatching {
        val uid = requireUserId()
        if (uid == targetUserId) return@runCatching false

        val currentDoc = db.collection("users").document(uid).get().await()
        val currentProfile = currentDoc.toObject(UserProfile::class.java) ?: UserProfile()
        val isBlocked = currentProfile.blockedUsers.contains(targetUserId)
        val currentDocRef = db.collection("users").document(uid)

        if (isBlocked) {
            currentDocRef.update("blockedUsers", FieldValue.arrayRemove(targetUserId)).await()
            false
        } else {
            currentDocRef.update(
                mapOf(
                    "blockedUsers" to FieldValue.arrayUnion(targetUserId),
                    "following" to FieldValue.arrayRemove(targetUserId)
                )
            ).await()
            db.collection("users").document(targetUserId).update("followers", FieldValue.arrayRemove(uid)).await()
            true
        }
    }

    suspend fun setAccountPrivacy(isPrivate: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        docRef.update("isPrivate", isPrivate, "updatedAt", FieldValue.serverTimestamp()).await()
    }

    suspend fun setUserOnlineStatus(isOnline: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val data = mapOf(
            "isOnline" to isOnline,
            "onlineStatusText" to if (isOnline) "Active now" else "Offline",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateSquad(newSquadId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val data = mapOf(
            "squadId" to newSquadId.uppercase().trim(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // Communities (Open communities replacing locked squads)
    fun observeCommunities(): Flow<List<Community>> {
        val path = "communities"
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(Community::class.java).ifEmpty {
                    DEFAULT_COMMUNITIES
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                emit(DEFAULT_COMMUNITIES)
            }
    }

    suspend fun seedDefaultCommunities() {
        runCatching {
            val coll = db.collection("communities")
            for (comm in DEFAULT_COMMUNITIES) {
                coll.document(comm.id).set(comm, SetOptions.merge()).await()
            }
        }
    }

    // Posts & Feed
    fun observePosts(): Flow<List<Post>> {
        val path = "posts"
        return db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(Post::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun createPost(
        caption: String,
        imageUrl: String,
        squadId: String = "",
        username: String,
        userAvatar: String,
        communityId: String = "",
        communityName: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val postsRef = db.collection("posts")
        val payload = mapOf(
            "userId" to uid,
            "username" to username,
            "userAvatar" to userAvatar,
            "caption" to caption,
            "imageUrl" to imageUrl,
            "squadId" to squadId.uppercase().trim(),
            "communityId" to communityId,
            "communityName" to communityName,
            "likesCount" to 0L,
            "commentsCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            val doc = postsRef.add(payload).await()
            doc.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, postsRef.path)
            throw e
        }
    }

    suspend fun likePost(postId: String, currentLikes: Long): Result<Unit> = runCatching {
        val docRef = db.collection("posts").document(postId)
        try {
            docRef.update("likesCount", currentLikes + 1).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeComments(postId: String): Flow<List<PostComment>> {
        val path = "posts/$postId/comments"
        return db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(PostComment::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun addComment(
        postId: String,
        text: String,
        username: String,
        userAvatar: String,
        currentCommentsCount: Long
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val commentsRef = db.collection("posts").document(postId).collection("comments")
        val payload = mapOf(
            "userId" to uid,
            "username" to username,
            "userAvatar" to userAvatar,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            commentsRef.add(payload).await()
            db.collection("posts").document(postId).update("commentsCount", currentCommentsCount + 1).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentsRef.path)
            throw e
        }
    }

    // Stories
    fun observeStories(): Flow<List<Story>> {
        val path = "stories"
        return db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(Story::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun createStory(
        mediaUrl: String,
        caption: String,
        squadId: String = "",
        username: String,
        userAvatar: String,
        songTitle: String = "",
        songArtist: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val storiesRef = db.collection("stories")
        val payload = mapOf(
            "userId" to uid,
            "username" to username,
            "userAvatar" to userAvatar,
            "mediaUrl" to mediaUrl,
            "caption" to caption,
            "squadId" to squadId.uppercase().trim(),
            "songTitle" to songTitle,
            "songArtist" to songArtist,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            val doc = storiesRef.add(payload).await()
            doc.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, storiesRef.path)
            throw e
        }
    }

    // Rooms (Strictly Private DMs and Group chats)
    fun observeRooms(): Flow<List<Room>> {
        val path = "rooms"
        val uid = requireUserId()
        return db.collection(path)
            .snapshots()
            .map { snap ->
                snap.toObjects(Room::class.java).filter { room ->
                    room.memberIds.contains(uid)
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun createDirectMessageRoom(otherUserId: String, otherUserName: String, myName: String): Result<String> = runCatching {
        val uid = requireUserId()
        val sortedIds = listOf(uid, otherUserId).sorted()
        val roomId = "dm_${sortedIds[0]}_${sortedIds[1]}"
        val docRef = db.collection("rooms").document(roomId)
        try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val payload = mapOf(
                    "roomId" to roomId,
                    "squadId" to "",
                    "name" to otherUserName,
                    "isSquadRoom" to false,
                    "isGroup" to false,
                    "memberIds" to listOf(uid, otherUserId),
                    "memberUsernames" to listOf(myName, otherUserName),
                    "lastMessage" to "Started a conversation",
                    "lastMessageSender" to myName,
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            roomId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun createGroupRoom(groupName: String, memberIds: List<String>, memberUsernames: List<String>, creatorName: String): Result<String> = runCatching {
        val uid = requireUserId()
        val allMemberIds = (memberIds + uid).distinct()
        val allMemberUsernames = (memberUsernames + creatorName).distinct()
        val roomId = "group_${System.currentTimeMillis()}_${uid.take(5)}"
        val docRef = db.collection("rooms").document(roomId)
        val payload = mapOf(
            "roomId" to roomId,
            "squadId" to "",
            "name" to groupName.trim(),
            "isSquadRoom" to false,
            "isGroup" to true,
            "groupAdminId" to uid,
            "memberIds" to allMemberIds,
            "memberUsernames" to allMemberUsernames,
            "lastMessage" to "Created group $groupName",
            "lastMessageSender" to creatorName,
            "lastMessageTime" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        roomId
    }

    // Messages
    fun observeMessages(roomId: String): Flow<List<RoomMessage>> {
        val path = "rooms/$roomId/messages"
        val uid = currentUserId() ?: ""
        return db.collection("rooms").document(roomId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap ->
                snap.toObjects(RoomMessage::class.java).filter { msg ->
                    !msg.deletedForUserIds.contains(uid)
                }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun sendMessage(
        roomId: String,
        text: String,
        type: String = "text",
        mediaUrl: String = "",
        effect: String = "",
        songTitle: String = "",
        songArtist: String = "",
        songCoverUrl: String = "",
        spotifyTrackUri: String = "",
        audioDurationSec: Int = 0,
        senderName: String,
        senderAvatar: String,
        replyToMessageId: String = "",
        replyToText: String = "",
        replyToSenderName: String = "",
        isForwarded: Boolean = false
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val msgRef = db.collection("rooms").document(roomId).collection("messages")
        val payload = mapOf(
            "senderId" to uid,
            "senderName" to senderName,
            "senderAvatar" to senderAvatar,
            "text" to text,
            "type" to type,
            "mediaUrl" to mediaUrl,
            "effect" to effect,
            "songTitle" to songTitle,
            "songArtist" to songArtist,
            "songCoverUrl" to songCoverUrl,
            "spotifyTrackUri" to spotifyTrackUri,
            "audioDurationSec" to audioDurationSec,
            "replyToMessageId" to replyToMessageId,
            "replyToText" to replyToText,
            "replyToSenderName" to replyToSenderName,
            "isUnsent" to false,
            "isForwarded" to isForwarded,
            "status" to "sent",
            "seenByUserIds" to listOf(uid),
            "deletedForUserIds" to emptyList<String>(),
            "reactions" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            val doc = msgRef.add(payload).await()
            val previewText = when {
                isForwarded -> "Forwarded message: $text"
                type == "instant" -> "Sent an Instant photo ⚡"
                type == "image" -> "Sent an image 📷"
                type == "voice" -> "Sent a voice message 🎙️ (${audioDurationSec}s)"
                type == "spotify" -> "Shared track: $songTitle - $songArtist 🎵"
                else -> text.ifBlank { "Shared media" }
            }
            runCatching {
                db.collection("rooms").document(roomId).set(
                    mapOf(
                        "roomId" to roomId,
                        "lastMessage" to previewText,
                        "lastMessageSender" to senderName,
                        "lastMessageTime" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
            }
            doc.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            throw e
        }
    }

    suspend fun unsendMessage(roomId: String, messageId: String): Result<Unit> = runCatching {
        val docRef = db.collection("rooms").document(roomId).collection("messages").document(messageId)
        val snapshot = docRef.get().await()
        val createdAt = snapshot.getTimestamp("createdAt")
        if (createdAt != null) {
            val ageMs = System.currentTimeMillis() - createdAt.toDate().time
            if (ageMs > 30 * 60 * 1000L) {
                throw IllegalStateException("Messages can only be unsent within 30 minutes")
            }
        }
        docRef.update(
            mapOf(
                "isUnsent" to true,
                "text" to "This message was unsent"
            )
        ).await()
    }

    suspend fun deleteMessageForUser(roomId: String, messageId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("rooms").document(roomId).collection("messages").document(messageId)
        docRef.update("deletedForUserIds", FieldValue.arrayUnion(uid)).await()
    }

    suspend fun markMessagesAsSeen(roomId: String, messages: List<RoomMessage>): Result<Unit> = runCatching {
        val uid = requireUserId()
        val unseen = messages.filter { it.senderId != uid && !it.seenByUserIds.contains(uid) }
        val messagesColl = db.collection("rooms").document(roomId).collection("messages")
        for (msg in unseen) {
            runCatching {
                messagesColl.document(msg.id).update(
                    mapOf(
                        "seenByUserIds" to FieldValue.arrayUnion(uid),
                        "status" to "seen",
                        "seenAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }
    }

    suspend fun toggleReaction(roomId: String, messageId: String, currentReactions: List<String>, reactionEmoji: String): Result<Unit> = runCatching {
        val docRef = db.collection("rooms").document(roomId).collection("messages").document(messageId)
        val newReactions = if (currentReactions.contains(reactionEmoji)) {
            currentReactions.filter { it != reactionEmoji }
        } else {
            currentReactions + reactionEmoji
        }
        try {
            docRef.update("reactions", newReactions).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // Music Notes
    fun observeNotes(): Flow<List<MusicNote>> {
        val path = "notes"
        return db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(MusicNote::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun saveMusicNote(
        songTitle: String,
        artist: String,
        noteText: String,
        albumCoverUrl: String = "",
        spotifyUri: String = "",
        previewAudioUrl: String = "",
        squadId: String = "",
        username: String,
        userAvatar: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("notes").document(uid)
        val payload = mapOf(
            "userId" to uid,
            "username" to username,
            "userAvatar" to userAvatar,
            "squadId" to squadId.uppercase().trim(),
            "songTitle" to songTitle,
            "artist" to artist,
            "noteText" to noteText,
            "albumCoverUrl" to albumCoverUrl,
            "spotifyUri" to spotifyUri,
            "previewAudioUrl" to previewAudioUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMusicNote(noteId: String = ""): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docId = if (noteId.isNotBlank()) noteId else uid
        val docRef = db.collection("notes").document(docId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // Instants
    fun observeInstants(): Flow<List<InstantItem>> {
        val path = "instants"
        return db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(InstantItem::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun postInstant(
        imageUrl: String,
        caption: String,
        squadId: String = "",
        username: String,
        userAvatar: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val instantsRef = db.collection("instants")
        val payload = mapOf(
            "userId" to uid,
            "username" to username,
            "userAvatar" to userAvatar,
            "squadId" to squadId.uppercase().trim(),
            "imageUrl" to imageUrl,
            "caption" to caption,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            val doc = instantsRef.add(payload).await()
            doc.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, instantsRef.path)
            throw e
        }
    }

    companion object {
        val DEFAULT_COMMUNITIES = listOf(
            Community(
                id = "tech_ai",
                name = "Tech & AI",
                slug = "tech",
                description = "Silicon dreams, generative AI, robotics & coding",
                iconEmoji = "🤖",
                bannerColorHex = "#7E74F1",
                memberCount = 1420L,
                category = "Technology"
            ),
            Community(
                id = "music_beats",
                name = "Music & Beats",
                slug = "music",
                description = "Track releases, Spotify deep cuts & indie drops",
                iconEmoji = "🎵",
                bannerColorHex = "#1DB954",
                memberCount = 2890L,
                category = "Music"
            ),
            Community(
                id = "art_design",
                name = "Design & Aesthetics",
                slug = "design",
                description = "Liquid glassmorphism, UI/UX, typography & 3D art",
                iconEmoji = "🎨",
                bannerColorHex = "#EC4899",
                memberCount = 980L,
                category = "Creative"
            ),
            Community(
                id = "gaming_esports",
                name = "Gaming & Esports",
                slug = "gaming",
                description = "Clips, setups, esports scrims & game recommendations",
                iconEmoji = "🎮",
                bannerColorHex = "#8B5CF6",
                memberCount = 3450L,
                category = "Gaming"
            ),
            Community(
                id = "photo_visuals",
                name = "Photography & Vibes",
                slug = "photography",
                description = "Street photography, film aesthetics & moody portraits",
                iconEmoji = "📸",
                bannerColorHex = "#06B6D4",
                memberCount = 1670L,
                category = "Creative"
            ),
            Community(
                id = "fitness_energy",
                name = "Fitness & Lifestyle",
                slug = "fitness",
                description = "Workouts, daily routines & health motivation",
                iconEmoji = "⚡",
                bannerColorHex = "#F59E0B",
                memberCount = 890L,
                category = "Lifestyle"
            ),
            Community(
                id = "anime_manga",
                name = "Anime & Pop Culture",
                slug = "anime",
                description = "Seasonal discussions, cosplay & anime art",
                iconEmoji = "🌸",
                bannerColorHex = "#F43F5E",
                memberCount = 2100L,
                category = "Entertainment"
            ),
            Community(
                id = "general_lounge",
                name = "Global Lounge",
                slug = "lounge",
                description = "Casual conversation, spontaneous thoughts & vibe check",
                iconEmoji = "☕",
                bannerColorHex = "#6366F1",
                memberCount = 5120L,
                category = "Community"
            )
        )
    }
}
