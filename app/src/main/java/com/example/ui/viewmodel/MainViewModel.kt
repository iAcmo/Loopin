package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Community
import com.example.data.model.InstantItem
import com.example.data.model.MusicNote
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.Room
import com.example.data.model.RoomMessage
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.data.repository.InstaSquadRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InstaSquadRepository(application.applicationContext)
    private val auth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()

    private val _currentRoomMessages = MutableStateFlow<List<RoomMessage>>(emptyList())
    val currentRoomMessages: StateFlow<List<RoomMessage>> = _currentRoomMessages.asStateFlow()

    private val _musicNotes = MutableStateFlow<List<MusicNote>>(emptyList())
    val musicNotes: StateFlow<List<MusicNote>> = _musicNotes.asStateFlow()

    private val _instants = MutableStateFlow<List<InstantItem>>(emptyList())
    val instants: StateFlow<List<InstantItem>> = _instants.asStateFlow()

    private val _publicUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val publicUsers: StateFlow<List<UserProfile>> = _publicUsers.asStateFlow()

    private val _communities = MutableStateFlow<List<Community>>(InstaSquadRepository.DEFAULT_COMMUNITIES)
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    private val _selectedUserProfile = MutableStateFlow<UserProfile?>(null)
    val selectedUserProfile: StateFlow<UserProfile?> = _selectedUserProfile.asStateFlow()

    private val _activeStory = MutableStateFlow<Story?>(null)
    val activeStory: StateFlow<Story?> = _activeStory.asStateFlow()

    private val _comments = MutableStateFlow<List<PostComment>>(emptyList())
    val comments: StateFlow<List<PostComment>> = _comments.asStateFlow()

    private val _selectedPostForComments = MutableStateFlow<Post?>(null)
    val selectedPostForComments: StateFlow<Post?> = _selectedPostForComments.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                observeUserData(user.uid)
            } else {
                _userProfile.value = null
            }
        }
        val user = auth.currentUser
        if (user != null) {
            observeUserData(user.uid)
        }
        loadCommunities()
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun loadCommunities() {
        viewModelScope.launch {
            repository.seedDefaultCommunities()
            repository.observeCommunities().collectLatest { list ->
                _communities.value = list
            }
        }
    }

    private fun observeUserData(uid: String) {
        viewModelScope.launch {
            repository.observeUserProfile(uid).collectLatest { profile ->
                _userProfile.value = profile
                if (profile != null) {
                    if (!profile.isOnline) {
                        repository.setUserOnlineStatus(true)
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.observePublicUsers().collectLatest { userList ->
                _publicUsers.value = userList
            }
        }
        viewModelScope.launch {
            repository.observePosts().collectLatest { postList ->
                _posts.value = postList
            }
        }
        viewModelScope.launch {
            repository.observeStories().collectLatest { storyList ->
                _stories.value = storyList
            }
        }
        viewModelScope.launch {
            repository.observeRooms().collectLatest { roomList ->
                _rooms.value = roomList
            }
        }
        viewModelScope.launch {
            repository.observeNotes().collectLatest { notesList ->
                _musicNotes.value = notesList
            }
        }
        viewModelScope.launch {
            repository.observeInstants().collectLatest { instantList ->
                _instants.value = instantList
            }
        }
    }

    fun saveProfileAndSquad(
        username: String,
        displayName: String,
        bio: String,
        avatarUrl: String,
        squadId: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val cleanUser = username.lowercase().trim()
            val uid = auth.currentUser?.uid ?: ""
            val isAvail = repository.checkUsernameAvailable(cleanUser, uid)
            if (!isAvail) {
                _toastMessage.value = "Username @$cleanUser is already taken. Please choose another."
                return@launch
            }
            val result = repository.saveUserProfile(
                username = cleanUser,
                displayName = displayName,
                bio = bio,
                avatarUrl = avatarUrl,
                squadId = squadId
            )
            if (result.isSuccess) {
                _toastMessage.value = "Welcome to Loopin, @$cleanUser!"
                onComplete()
            } else {
                _toastMessage.value = "Failed to save profile: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun updateUserProfile(
        displayName: String,
        bio: String,
        username: String,
        avatarUrl: String,
        isPublicDirectory: Boolean = true,
        isPrivate: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val cleanUser = username.lowercase().trim()
            val uid = auth.currentUser?.uid ?: ""
            val isAvail = repository.checkUsernameAvailable(cleanUser, uid)
            if (!isAvail) {
                _toastMessage.value = "Username @$cleanUser is already taken. Please choose another."
                return@launch
            }
            val result = repository.updateUserProfile(
                displayName = displayName,
                bio = bio,
                username = cleanUser,
                avatarUrl = avatarUrl,
                isPublicDirectory = isPublicDirectory,
                isPrivate = isPrivate
            )
            if (result.isSuccess) {
                _toastMessage.value = "Profile updated successfully!"
                onComplete()
            } else {
                _toastMessage.value = "Failed to update profile: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun setAccountPrivacy(isPrivate: Boolean) {
        viewModelScope.launch {
            val result = repository.setAccountPrivacy(isPrivate)
            if (result.isSuccess) {
                _toastMessage.value = if (isPrivate) "Account set to Private" else "Account set to Public"
            }
        }
    }

    fun toggleFollowUser(targetUserId: String) {
        viewModelScope.launch {
            val result = repository.toggleFollowUser(targetUserId)
            if (result.isSuccess) {
                val isNowFollowing = result.getOrThrow()
                _toastMessage.value = if (isNowFollowing) "Following user" else "Unfollowed user"
            }
        }
    }

    fun toggleBlockUser(targetUserId: String) {
        viewModelScope.launch {
            val result = repository.toggleBlockUser(targetUserId)
            if (result.isSuccess) {
                val isNowBlocked = result.getOrThrow()
                _toastMessage.value = if (isNowBlocked) "User blocked" else "User unblocked"
            }
        }
    }

    fun openUserProfile(user: UserProfile) {
        _selectedUserProfile.value = user
    }

    fun openUserProfileByUsername(username: String) {
        val found = _publicUsers.value.firstOrNull { it.username.equals(username, ignoreCase = true) }
            ?: if (_userProfile.value?.username.equals(username, ignoreCase = true)) _userProfile.value else null
        if (found != null) {
            _selectedUserProfile.value = found
        } else {
            _toastMessage.value = "User @$username not found"
        }
    }

    fun closeSelectedUserProfile() {
        _selectedUserProfile.value = null
    }

    fun setUserOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            val result = repository.setUserOnlineStatus(isOnline)
            if (result.isSuccess) {
                _toastMessage.value = if (isOnline) "You are now online" else "You are now offline"
            }
        }
    }

    fun updateSquad(newSquadId: String) {
        viewModelScope.launch {
            val result = repository.updateSquad(newSquadId)
            if (result.isSuccess) {
                _toastMessage.value = "Updated squad to #${newSquadId.uppercase()}"
            }
        }
    }

    fun createPost(caption: String, imageUrl: String, communityId: String = "", communityName: String = "") {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val result = repository.createPost(
                caption = caption,
                imageUrl = imageUrl,
                squadId = profile.squadId,
                username = profile.username,
                userAvatar = profile.avatarUrl,
                communityId = communityId,
                communityName = communityName
            )
            if (result.isSuccess) {
                _toastMessage.value = if (communityName.isNotBlank()) "Posted in $communityName!" else "Post shared to your feed!"
            } else {
                _toastMessage.value = "Error sharing post: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            repository.likePost(post.id, post.likesCount)
        }
    }

    fun openCommentsForPost(post: Post) {
        _selectedPostForComments.value = post
        viewModelScope.launch {
            repository.observeComments(post.id).collectLatest { commentList ->
                _comments.value = commentList
            }
        }
    }

    fun closeComments() {
        _selectedPostForComments.value = null
        _comments.value = emptyList()
    }

    fun addComment(text: String) {
        val post = _selectedPostForComments.value ?: return
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val result = repository.addComment(
                postId = post.id,
                text = text,
                username = profile.username,
                userAvatar = profile.avatarUrl,
                currentCommentsCount = post.commentsCount
            )
            if (result.isFailure) {
                _toastMessage.value = "Failed to add comment"
            }
        }
    }

    fun createStory(mediaUrl: String, caption: String, songTitle: String = "", songArtist: String = "") {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val result = repository.createStory(
                mediaUrl = mediaUrl,
                caption = caption,
                squadId = profile.squadId,
                username = profile.username,
                userAvatar = profile.avatarUrl,
                songTitle = songTitle,
                songArtist = songArtist
            )
            if (result.isSuccess) {
                _toastMessage.value = "Story added!"
            } else {
                _toastMessage.value = "Failed to add story"
            }
        }
    }

    fun openStory(story: Story) {
        _activeStory.value = story
    }

    fun closeStory() {
        _activeStory.value = null
    }

    private var messagesJob: kotlinx.coroutines.Job? = null

    fun selectRoom(room: Room) {
        val targetRoomId = room.roomId.ifBlank { room.id }
        _currentRoom.value = room
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.observeMessages(targetRoomId).collectLatest { msgList ->
                _currentRoomMessages.value = msgList
                val currentUid = auth.currentUser?.uid ?: ""
                repository.markMessagesAsSeen(targetRoomId, msgList)
            }
        }
    }

    fun closeRoom() {
        messagesJob?.cancel()
        messagesJob = null
        _currentRoom.value = null
        _currentRoomMessages.value = emptyList()
    }

    fun sendMessage(
        text: String,
        type: String = "text",
        mediaUrl: String = "",
        effect: String = "",
        songTitle: String = "",
        songArtist: String = "",
        songCoverUrl: String = "",
        spotifyTrackUri: String = "",
        audioDurationSec: Int = 0,
        replyToMessageId: String = "",
        replyToText: String = "",
        replyToSenderName: String = "",
        isForwarded: Boolean = false
    ) {
        val room = _currentRoom.value ?: return
        val targetRoomId = room.roomId.ifBlank { room.id }
        val profile = _userProfile.value ?: return

        viewModelScope.launch {
            val result = repository.sendMessage(
                roomId = targetRoomId,
                text = text,
                type = type,
                mediaUrl = mediaUrl,
                effect = effect,
                songTitle = songTitle,
                songArtist = songArtist,
                songCoverUrl = songCoverUrl,
                spotifyTrackUri = spotifyTrackUri,
                audioDurationSec = audioDurationSec,
                senderName = profile.displayName.ifBlank { profile.username },
                senderAvatar = profile.avatarUrl,
                replyToMessageId = replyToMessageId,
                replyToText = replyToText,
                replyToSenderName = replyToSenderName,
                isForwarded = isForwarded
            )
            if (result.isFailure) {
                _toastMessage.value = "Failed to send: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun unsendMessage(messageId: String) {
        val room = _currentRoom.value ?: return
        val targetRoomId = room.roomId.ifBlank { room.id }
        viewModelScope.launch {
            val result = repository.unsendMessage(targetRoomId, messageId)
            if (result.isSuccess) {
                _toastMessage.value = "Message unsent"
            } else {
                _toastMessage.value = "Cannot unsend message"
            }
        }
    }

    fun deleteMessageForYou(messageId: String) {
        val room = _currentRoom.value ?: return
        val targetRoomId = room.roomId.ifBlank { room.id }
        viewModelScope.launch {
            val result = repository.deleteMessageForUser(targetRoomId, messageId)
            if (result.isSuccess) {
                _toastMessage.value = "Deleted for you"
            }
        }
    }

    fun forwardMessage(targetRoom: Room, message: RoomMessage) {
        val targetRoomId = targetRoom.roomId.ifBlank { targetRoom.id }
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                roomId = targetRoomId,
                text = message.text,
                type = message.type,
                mediaUrl = message.mediaUrl,
                effect = message.effect,
                songTitle = message.songTitle,
                songArtist = message.songArtist,
                songCoverUrl = message.songCoverUrl,
                spotifyTrackUri = message.spotifyTrackUri,
                audioDurationSec = message.audioDurationSec,
                senderName = profile.displayName.ifBlank { profile.username },
                senderAvatar = profile.avatarUrl,
                isForwarded = true
            )
            _toastMessage.value = "Forwarded to ${targetRoom.name} ↗️"
        }
    }

    fun toggleMessageReaction(messageId: String, currentReactions: List<String>, reactionEmoji: String) {
        val room = _currentRoom.value ?: return
        val targetRoomId = room.roomId.ifBlank { room.id }
        viewModelScope.launch {
            repository.toggleReaction(targetRoomId, messageId, currentReactions, reactionEmoji)
        }
    }

    fun saveMusicNote(
        songTitle: String,
        artist: String,
        noteText: String,
        albumCoverUrl: String = "",
        spotifyUri: String = "",
        previewAudioUrl: String = ""
    ) {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val result = repository.saveMusicNote(
                songTitle = songTitle,
                artist = artist,
                noteText = noteText,
                albumCoverUrl = albumCoverUrl,
                spotifyUri = spotifyUri,
                previewAudioUrl = previewAudioUrl,
                squadId = profile.squadId,
                username = profile.username,
                userAvatar = profile.avatarUrl
            )
            if (result.isSuccess) {
                _toastMessage.value = "Spotify note shared!"
            } else {
                _toastMessage.value = "Failed to save note"
            }
        }
    }

    fun deleteMusicNote(noteId: String = "") {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val targetId = if (noteId.isNotBlank()) noteId else profile.userId
            val result = repository.deleteMusicNote(targetId)
            if (result.isSuccess) {
                _toastMessage.value = "Music note removed"
            } else {
                _toastMessage.value = "Failed to remove note"
            }
        }
    }

    fun postInstant(imageUrl: String, caption: String) {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val result = repository.postInstant(
                imageUrl = imageUrl,
                caption = caption,
                squadId = profile.squadId,
                username = profile.username,
                userAvatar = profile.avatarUrl
            )
            if (result.isSuccess) {
                _toastMessage.value = "Instant photo dropped!"
            } else {
                _toastMessage.value = "Failed to drop instant"
            }
        }
    }

    fun startDirectMessage(otherUserId: String, otherUserName: String) {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val result = repository.createDirectMessageRoom(
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                myName = profile.username
            )
            if (result.isSuccess) {
                val roomId = result.getOrThrow()
                val targetRoom = Room(
                    roomId = roomId,
                    name = otherUserName,
                    isSquadRoom = false,
                    isGroup = false,
                    memberIds = listOf(profile.userId, otherUserId),
                    memberUsernames = listOf(profile.username, otherUserName)
                )
                selectRoom(targetRoom)
            }
        }
    }

    fun createGroupRoom(groupName: String, memberIds: List<String>, memberUsernames: List<String>) {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val result = repository.createGroupRoom(
                groupName = groupName,
                memberIds = memberIds,
                memberUsernames = memberUsernames,
                creatorName = profile.displayName.ifBlank { profile.username }
            )
            if (result.isSuccess) {
                val roomId = result.getOrThrow()
                val targetRoom = Room(
                    roomId = roomId,
                    name = groupName,
                    isSquadRoom = false,
                    isGroup = true,
                    memberIds = (memberIds + profile.userId).distinct(),
                    memberUsernames = (memberUsernames + profile.username).distinct()
                )
                selectRoom(targetRoom)
                _toastMessage.value = "Group $groupName created!"
            } else {
                _toastMessage.value = "Failed to create group"
            }
        }
    }
}
