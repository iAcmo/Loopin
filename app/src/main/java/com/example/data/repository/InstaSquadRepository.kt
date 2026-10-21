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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
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
        if (auth.currentUser == null) return flowOf(null)
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
        val currentUid = currentUserId() ?: return flowOf(emptyList())
        val path = "users"
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
        if (auth.currentUser == null) return flowOf(DEFAULT_COMMUNITIES)
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
        if (auth.currentUser == null) return
        runCatching {
            val coll = db.collection("communities")
            for (comm in DEFAULT_COMMUNITIES) {
                coll.document(comm.id).set(comm, SetOptions.merge()).await()
            }
        }
    }

    // Posts & Feed
    fun observePosts(): Flow<List<Post>> {
        if (auth.currentUser == null) return flowOf(emptyList())
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
        if (auth.currentUser == null) return flowOf(emptyList())
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
        if (auth.currentUser == null) return flowOf(emptyList())
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
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        val path = "rooms"
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

    fun observeRoom(roomId: String): Flow<Room?> {
        if (auth.currentUser == null) return flowOf(null)
        val path = "rooms/$roomId"
        return db.collection("rooms").document(roomId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) snap.toObject(Room::class.java) else null
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, path)
                emit(null)
            }
    }

    suspend fun createDirectMessageRoom(
        otherUserId: String,
        otherUserName: String,
        myName: String,
        otherAvatarUrl: String = "",
        myAvatarUrl: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val sortedIds = listOf(uid, otherUserId).sorted()
        val roomId = "dm_${sortedIds[0]}_${sortedIds[1]}"
        val docRef = db.collection("rooms").document(roomId)
        try {
            val existing = docRef.get().await()
            val memberAvatarsMap = mutableMapOf<String, String>()
            if (myAvatarUrl.isNotBlank()) memberAvatarsMap[uid] = myAvatarUrl
            if (otherAvatarUrl.isNotBlank()) memberAvatarsMap[otherUserId] = otherAvatarUrl

            if (!existing.exists()) {
                val payload = mapOf(
                    "roomId" to roomId,
                    "squadId" to "",
                    "name" to otherUserName,
                    "isSquadRoom" to false,
                    "isGroup" to false,
                    "memberIds" to listOf(uid, otherUserId),
                    "memberUsernames" to listOf(myName, otherUserName),
                    "memberAvatars" to memberAvatarsMap,
                    "themeId" to "iris_glow",
                    "disappearingDurationSec" to 0L,
                    "nicknames" to emptyMap<String, String>(),
                    "mutedByUserIds" to emptyList<String>(),
                    "blockedUserIds" to emptyList<String>(),
                    "lastMessage" to "Started a conversation",
                    "lastMessageSender" to myName,
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else if (memberAvatarsMap.isNotEmpty()) {
                docRef.set(mapOf("memberAvatars" to memberAvatarsMap), SetOptions.merge()).await()
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
            "themeId" to "iris_glow",
            "disappearingDurationSec" to 0L,
            "nicknames" to emptyMap<String, String>(),
            "mutedByUserIds" to emptyList<String>(),
            "blockedUserIds" to emptyList<String>(),
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
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        val path = "rooms/$roomId/messages"
        val now = Timestamp.now()
        return db.collection("rooms").document(roomId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snap ->
                snap.toObjects(RoomMessage::class.java).filter { msg ->
                    !msg.deletedForUserIds.contains(uid) &&
                    (msg.expiresAt == null || msg.expiresAt.seconds > now.seconds)
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
        isForwarded: Boolean = false,
        disappearingDurationSec: Long = 0L,
        isViewOnce: Boolean = false
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val msgRef = db.collection("rooms").document(roomId).collection("messages")
        val expiresAtTimestamp = if (disappearingDurationSec > 0L) {
            Timestamp(java.util.Date(System.currentTimeMillis() + disappearingDurationSec * 1000L))
        } else null

        val payload = mutableMapOf<String, Any?>(
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
            "isViewOnce" to isViewOnce,
            "isViewed" to false,
            "status" to "sent",
            "seenByUserIds" to listOf(uid),
            "deletedForUserIds" to emptyList<String>(),
            "reactions" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (expiresAtTimestamp != null) {
            payload["expiresAt"] = expiresAtTimestamp
        }

        try {
            val doc = msgRef.add(payload).await()
            val previewText = when {
                isViewOnce -> "Sent a View-Once photo ⚡"
                isForwarded -> "Forwarded message: $text"
                type == "instant" -> "Sent an Instant photo ⚡"
                type == "image" -> "Sent an image 📷"
                type == "voice" -> "Sent a voice message 🎙️ (${audioDurationSec}s)"
                type == "spotify" -> "Shared track: $songTitle - $songArtist 🎵"
                else -> text.ifBlank { "Shared media" }
            }
            runCatching {
                val roomDoc = db.collection("rooms").document(roomId)
                val roomSnap = roomDoc.get().await()
                val memberIds = (roomSnap.get("memberIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val otherMembers = memberIds.filter { it != uid }

                roomDoc.set(
                    mapOf(
                        "roomId" to roomId,
                        "lastMessage" to previewText,
                        "lastMessageSender" to senderName,
                        "lastMessageTime" to FieldValue.serverTimestamp(),
                        "unreadByUserIds" to otherMembers,
                        "typingUserIds" to FieldValue.arrayRemove(uid),
                        "typingUsernames" to FieldValue.arrayRemove(senderName)
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

    suspend fun updateRoomTheme(roomId: String, themeId: String): Result<Unit> = runCatching {
        requireUserId()
        db.collection("rooms").document(roomId).update("themeId", themeId).await()
    }

    suspend fun setRoomDisappearingMessages(roomId: String, durationSec: Long, actorName: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val roomDoc = db.collection("rooms").document(roomId)
        roomDoc.update("disappearingDurationSec", durationSec).await()
        val durationLabel = when (durationSec) {
            0L -> "off"
            86400L -> "24 hours"
            604800L -> "7 days"
            else -> "${durationSec / 3600} hours"
        }
        val sysMsg = if (durationSec == 0L) {
            "$actorName turned off disappearing messages."
        } else {
            "$actorName set disappearing messages to $durationLabel."
        }
        roomDoc.collection("messages").add(
            mapOf(
                "senderId" to uid,
                "senderName" to "Loopin",
                "senderAvatar" to "",
                "text" to sysMsg,
                "type" to "system",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun setParticipantNickname(roomId: String, targetUserId: String, nickname: String): Result<Unit> = runCatching {
        requireUserId()
        val roomDoc = db.collection("rooms").document(roomId)
        roomDoc.update("nicknames.$targetUserId", nickname.trim()).await()
    }

    suspend fun toggleMuteRoom(roomId: String, isMuted: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val roomDoc = db.collection("rooms").document(roomId)
        if (isMuted) {
            roomDoc.update("mutedByUserIds", FieldValue.arrayUnion(uid)).await()
        } else {
            roomDoc.update("mutedByUserIds", FieldValue.arrayRemove(uid)).await()
        }
    }

    suspend fun toggleBlockRoomParticipant(roomId: String, targetUserId: String, isBlocked: Boolean): Result<Unit> = runCatching {
        requireUserId()
        val roomDoc = db.collection("rooms").document(roomId)
        if (isBlocked) {
            roomDoc.update("blockedUserIds", FieldValue.arrayUnion(targetUserId)).await()
        } else {
            roomDoc.update("blockedUserIds", FieldValue.arrayRemove(targetUserId)).await()
        }
    }

    suspend fun markMessageAsViewed(roomId: String, messageId: String): Result<Unit> = runCatching {
        requireUserId()
        val msgRef = db.collection("rooms").document(roomId).collection("messages").document(messageId)
        msgRef.update("isViewed", true).await()
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
        runCatching {
            db.collection("rooms").document(roomId).update(
                "unreadByUserIds", FieldValue.arrayRemove(uid)
            ).await()
        }
    }

    suspend fun setTyping(roomId: String, username: String, isTyping: Boolean): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val docRef = db.collection("rooms").document(roomId)
        if (isTyping) {
            docRef.set(
                mapOf(
                    "typingUserIds" to FieldValue.arrayUnion(uid),
                    "typingUsernames" to FieldValue.arrayUnion(username)
                ),
                SetOptions.merge()
            ).await()
        } else {
            docRef.update(
                mapOf(
                    "typingUserIds" to FieldValue.arrayRemove(uid),
                    "typingUsernames" to FieldValue.arrayRemove(username)
                )
            ).await()
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
        if (auth.currentUser == null) return flowOf(emptyList())
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
        if (auth.currentUser == null) return flowOf(emptyList())
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
                memberCount = 0L,
                category = "Technology"
            ),
            Community(
                id = "music_beats",
                name = "Music & Beats",
                slug = "music",
                description = "Track releases, Spotify deep cuts & indie drops",
                iconEmoji = "🎵",
                bannerColorHex = "#1DB954",
                memberCount = 0L,
                category = "Music"
            ),
            Community(
                id = "art_design",
                name = "Design & Aesthetics",
                slug = "design",
                description = "Liquid glassmorphism, UI/UX, typography & 3D art",
                iconEmoji = "🎨",
                bannerColorHex = "#EC4899",
                memberCount = 0L,
                category = "Creative"
            ),
            Community(
                id = "gaming_esports",
                name = "Gaming & Esports",
                slug = "gaming",
                description = "Clips, setups, esports scrims & game recommendations",
                iconEmoji = "🎮",
                bannerColorHex = "#8B5CF6",
                memberCount = 0L,
                category = "Gaming"
            ),
            Community(
                id = "photo_visuals",
                name = "Photography & Vibes",
                slug = "photography",
                description = "Street photography, film aesthetics & moody portraits",
                iconEmoji = "📸",
                bannerColorHex = "#06B6D4",
                memberCount = 0L,
                category = "Creative"
            ),
            Community(
                id = "fitness_energy",
                name = "Fitness & Lifestyle",
                slug = "fitness",
                description = "Workouts, daily routines & health motivation",
                iconEmoji = "⚡",
                bannerColorHex = "#F59E0B",
                memberCount = 0L,
                category = "Lifestyle"
            ),
            Community(
                id = "anime_manga",
                name = "Anime & Pop Culture",
                slug = "anime",
                description = "Seasonal discussions, cosplay & anime art",
                iconEmoji = "🌸",
                bannerColorHex = "#F43F5E",
                memberCount = 0L,
                category = "Entertainment"
            ),
            Community(
                id = "general_lounge",
                name = "Global Lounge",
                slug = "lounge",
                description = "Casual conversation, spontaneous thoughts & vibe check",
                iconEmoji = "☕",
                bannerColorHex = "#6366F1",
                memberCount = 0L,
                category = "Community"
            )
        )
    }
}
