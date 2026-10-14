package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val squadId: String = "",
    val isOnline: Boolean = true,
    val isPublicDirectory: Boolean = true,
    val isPrivate: Boolean = false,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val blockedUsers: List<String> = emptyList(),
    val onlineStatusText: String = "Active now",
    val onboardingComplete: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Community(
    @DocumentId val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    val iconEmoji: String = "✨",
    val bannerColorHex: String = "#7E74F1",
    val memberCount: Long = 0L,
    val category: String = "General"
)

@IgnoreExtraProperties
data class Post(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val squadId: String = "",
    val communityId: String = "",
    val communityName: String = "",
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PostComment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Story(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val mediaUrl: String = "",
    val caption: String = "",
    val squadId: String = "",
    val songTitle: String = "",
    val songArtist: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Room(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val squadId: String = "",
    val name: String = "",
    val isSquadRoom: Boolean = false,
    val isGroup: Boolean = false,
    val groupAdminId: String = "",
    val groupAvatarUrl: String = "",
    val memberIds: List<String> = emptyList(),
    val memberUsernames: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageSender: String = "",
    @ServerTimestamp val lastMessageTime: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RoomMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val text: String = "",
    val type: String = "text", // "text", "instant", "image", "voice", "spotify"
    val mediaUrl: String = "",
    val effect: String = "", // "sparkle", "fire", "iris_glow", "whisper", "loud", "invisible"
    val songTitle: String = "",
    val songArtist: String = "",
    val songCoverUrl: String = "",
    val spotifyTrackUri: String = "",
    val audioDurationSec: Int = 0,
    val replyToMessageId: String = "",
    val replyToText: String = "",
    val replyToSenderName: String = "",
    val isUnsent: Boolean = false,
    val isForwarded: Boolean = false,
    val status: String = "sent", // "sent", "seen"
    @ServerTimestamp val seenAt: Timestamp? = null,
    val seenByUserIds: List<String> = emptyList(),
    val deletedForUserIds: List<String> = emptyList(),
    val reactions: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MusicNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val squadId: String = "",
    val songTitle: String = "",
    val artist: String = "",
    val noteText: String = "",
    val albumCoverUrl: String = "",
    val spotifyUri: String = "",
    val previewAudioUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class InstantItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val squadId: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
