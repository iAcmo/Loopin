package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.example.ui.theme.VisualThemeMode
import com.example.util.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
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

    private val _isProfileLoaded = MutableStateFlow(false)
    val isProfileLoaded: StateFlow<Boolean> = _isProfileLoaded.asStateFlow()

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

    private val prefs = application.getSharedPreferences("loopin_app_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("key_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _visualThemeMode = MutableStateFlow(
        if (prefs.getString("key_visual_mode", "liquid_glass") == "static_colors") {
            VisualThemeMode.STATIC_COLORS
        } else {
            VisualThemeMode.LIQUID_GLASS
        }
    )
    val visualThemeMode: StateFlow<VisualThemeMode> = _visualThemeMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("key_notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notificationSoundEnabled = MutableStateFlow(prefs.getBoolean("key_notification_sound", true))
    val notificationSoundEnabled: StateFlow<Boolean> = _notificationSoundEnabled.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var isAppInForeground: Boolean = true
    private val lastNotifiedMessageTimes = mutableMapOf<String, Long>()
    private var initialRoomsLoaded = false

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("key_dark_theme", enabled).apply()
    }

    fun setVisualThemeMode(mode: VisualThemeMode) {
        _visualThemeMode.value = mode
        prefs.edit().putString("key_visual_mode", mode.id).apply()
        _toastMessage.value = "Switched to ${mode.title} theme"
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("key_notifications_enabled", enabled).apply()
        _toastMessage.value = if (enabled) "Message notifications enabled" else "Message notifications silenced"
    }

    fun setNotificationSoundEnabled(enabled: Boolean) {
        _notificationSoundEnabled.value = enabled
        prefs.edit().putBoolean("key_notification_sound", enabled).apply()
    }

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
    }

    fun sendTestNotification() {
        NotificationHelper.sendTestNotification(getApplication<Application>().applicationContext)
        _toastMessage.value = "Test notification sent! Check your notification bar."
    }

    private fun checkAndNotifyNewMessages(roomList: List<Room>) {
        if (!initialRoomsLoaded) {
            roomList.forEach { room ->
                val rId = room.roomId.ifBlank { room.id }
                val t = room.lastMessageTime?.toDate()?.time ?: 0L
                lastNotifiedMessageTimes[rId] = t
            }
            initialRoomsLoaded = true
            return
        }

        val myUid = auth.currentUser?.uid ?: ""
        val myProfile = _userProfile.value
        val currentOpenedRoomId = _currentRoom.value?.let { it.roomId.ifBlank { it.id } }

        roomList.forEach { room ->
            val rId = room.roomId.ifBlank { room.id }
            val msgTime = room.lastMessageTime?.toDate()?.time ?: 0L
            val prevTime = lastNotifiedMessageTimes[rId] ?: 0L

            if (msgTime > prevTime && room.lastMessage.isNotBlank()) {
                lastNotifiedMessageTimes[rId] = msgTime
                val sender = room.lastMessageSender
                val isMyMessage = sender == myProfile?.displayName ||
                                  sender == myProfile?.username ||
                                  (sender.isBlank() && room.memberIds.firstOrNull() == myUid)
                val isMuted = room.mutedByUserIds.contains(myUid)
                val isCurrentlyViewingThisRoom = currentOpenedRoomId == rId && isAppInForeground

                if (!isMyMessage && _notificationsEnabled.value && !isMuted && !isCurrentlyViewingThisRoom) {
                    NotificationHelper.showMessageNotification(
                        context = getApplication<Application>().applicationContext,
                        roomId = rId,
                        title = room.name.ifBlank { "Direct Message" },
                        messageText = room.lastMessage,
                        senderName = sender,
                        isGroup = room.isGroup
                    )
                }
            }
        }
    }

    private var dataObservationJob: Job? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                startObservingData(user.uid)
            } else {
                stopObservingData()
            }
        }
        val user = auth.currentUser
        if (user != null) {
            startObservingData(user.uid)
        } else {
            stopObservingData()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun startObservingData(uid: String) {
        dataObservationJob?.cancel()
        dataObservationJob = viewModelScope.launch {
            launch {
                repository.seedDefaultCommunities()
                repository.observeCommunities().collectLatest { list ->
                    _communities.value = list
                }
            }
            launch {
                repository.observeUserProfile(uid).collectLatest { profile ->
                    _userProfile.value = profile
                    _isProfileLoaded.value = true
                    if (profile != null) {
                        if (!profile.isOnline) {
                            repository.setUserOnlineStatus(true)
                        }
                        repository.ensureStarterContentForUser(
                            username = profile.username,
                            displayName = profile.displayName,
                            avatarUrl = profile.avatarUrl,
                            squadId = profile.squadId
                        )
                    }
                }
            }
            launch {
                repository.observePublicUsers().collectLatest { userList ->
                    _publicUsers.value = userList
                }
            }
            launch {
                repository.observePosts().collectLatest { postList ->
                    _posts.value = postList
                }
            }
            launch {
                repository.observeStories().collectLatest { storyList ->
                    _stories.value = storyList
                }
            }
            launch {
                repository.observeRooms().collectLatest { roomList ->
                    _rooms.value = roomList
                    checkAndNotifyNewMessages(roomList)
                    val current = _currentRoom.value
                    if (current != null) {
                        val currentId = current.roomId.ifBlank { current.id }
                        val updated = roomList.find { (it.roomId.ifBlank { it.id }) == currentId }
                        if (updated != null) {
                            _currentRoom.value = updated
                        }
                    }
                }
            }
            launch {
                repository.observeNotes().collectLatest { notesList ->
                    _musicNotes.value = notesList
                }
            }
            launch {
                repository.observeInstants().collectLatest { instantList ->
                    _instants.value = instantList
                }
            }
        }
    }

    private fun stopObservingData() {
        dataObservationJob?.cancel()
        dataObservationJob = null
        _userProfile.value = null
        _isProfileLoaded.value = false
        _posts.value = emptyList()
        _stories.value = emptyList()
        _rooms.value = emptyList()
        _musicNotes.value = emptyList()
        _instants.value = emptyList()
        _publicUsers.value = emptyList()
        _communities.value = InstaSquadRepository.DEFAULT_COMMUNITIES
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
                repository.ensureStarterContentForUser(
                    username = cleanUser,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                    squadId = squadId
                )
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
    private var roomJob: kotlinx.coroutines.Job? = null
    private var typingJob: kotlinx.coroutines.Job? = null

    fun selectRoom(room: Room) {
        val targetRoomId = room.roomId.ifBlank { room.id }
        _currentRoom.value = room
        messagesJob?.cancel()
        roomJob?.cancel()
        typingJob?.cancel()

        roomJob = viewModelScope.launch {
            repository.observeRoom(targetRoomId).collectLatest { updatedRoom ->
                if (updatedRoom != null) {
                    _currentRoom.value = updatedRoom
                }
            }
        }

        messagesJob = viewModelScope.launch {
            repository.observeMessages(targetRoomId).collectLatest { msgList ->
                _currentRoomMessages.value = msgList
                val currentUid = auth.currentUser?.uid ?: ""
                repository.markMessagesAsSeen(targetRoomId, msgList)
            }
        }
    }

    fun selectRoomById(roomId: String) {
        val existing = _rooms.value.find { (it.roomId.ifBlank { it.id }) == roomId }
        if (existing != null) {
            selectRoom(existing)
        } else {
            viewModelScope.launch {
                repository.observeRoom(roomId).collectLatest { room ->
                    if (room != null) {
                        selectRoom(room)
                    }
                }
            }
        }
    }

    fun closeRoom() {
        setUserTyping(false)
        typingJob?.cancel()
        typingJob = null
        roomJob?.cancel()
        roomJob = null
        messagesJob?.cancel()
        messagesJob = null
        _currentRoom.value = null
        _currentRoomMessages.value = emptyList()
    }

    fun setUserTyping(isTyping: Boolean) {
        val room = _currentRoom.value ?: return
        val targetRoomId = room.roomId.ifBlank { room.id }
        val username = _userProfile.value?.let { it.displayName.ifBlank { it.username } } ?: return

        typingJob?.cancel()
        viewModelScope.launch {
            repository.setTyping(targetRoomId, username, isTyping)
        }

        if (isTyping) {
            typingJob = viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                repository.setTyping(targetRoomId, username, false)
            }
        }
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
        isForwarded: Boolean = false,
        isViewOnce: Boolean = false
    ) {
        setUserTyping(false)
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
                isForwarded = isForwarded,
                disappearingDurationSec = if (room.disappearingDurationSec > 0L) room.disappearingDurationSec else 0L,
                isViewOnce = isViewOnce || (room.disappearingDurationSec == -1L)
            )
            if (result.isFailure) {
                _toastMessage.value = "Failed to send: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun unsendMessage(messageId: String) {
        val room = _currentRoom.value ?: return
        val targetRoomId = room.roomId.ifBlank { room.id }
        // Eagerly remove from UI for instantaneous feedback
        _currentRoomMessages.value = _currentRoomMessages.value.filter { it.id != messageId }
        viewModelScope.launch {
            val result = repository.unsendMessage(targetRoomId, messageId)
            if (result.isSuccess) {
                _toastMessage.value = "Message unsent"
            } else {
                _toastMessage.value = "Cannot unsend message"
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _posts.value = _posts.value.filter { it.id != postId }
            val result = repository.deletePost(postId)
            if (result.isSuccess) {
                _toastMessage.value = "Post deleted"
            } else {
                _toastMessage.value = "Could not delete post"
            }
        }
    }

    fun updatePostCaption(postId: String, newCaption: String) {
        viewModelScope.launch {
            _posts.value = _posts.value.map {
                if (it.id == postId) it.copy(caption = newCaption) else it
            }
            val result = repository.updatePostCaption(postId, newCaption)
            if (result.isSuccess) {
                _toastMessage.value = "Post updated"
            } else {
                _toastMessage.value = "Could not update post"
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
        songTitle: String = "",
        artist: String = "",
        noteText: String,
        albumCoverUrl: String = "",
        spotifyUri: String = "",
        previewAudioUrl: String = "",
        colorTheme: String = "neutral"
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
                userAvatar = profile.avatarUrl,
                colorTheme = colorTheme
            )
            if (result.isSuccess) {
                _toastMessage.value = "Note shared! Active for 24 hours."
            } else {
                _toastMessage.value = "Failed to save note"
            }
        }
    }

    fun saveNote(
        noteText: String,
        colorTheme: String = "neutral",
        songTitle: String = "",
        artist: String = "",
        albumCoverUrl: String = "",
        spotifyUri: String = "",
        previewAudioUrl: String = ""
    ) = saveMusicNote(
        songTitle = songTitle,
        artist = artist,
        noteText = noteText,
        albumCoverUrl = albumCoverUrl,
        spotifyUri = spotifyUri,
        previewAudioUrl = previewAudioUrl,
        colorTheme = colorTheme
    )

    fun deleteMusicNote(noteId: String = "") {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val targetId = if (noteId.isNotBlank()) noteId else profile.userId
            val result = repository.deleteMusicNote(targetId)
            if (result.isSuccess) {
                _toastMessage.value = "Note removed"
            } else {
                _toastMessage.value = "Failed to remove note"
            }
        }
    }

    // App Passcode & Biometric Security Lock
    private val _isAppLocked = MutableStateFlow(
        prefs.getBoolean("key_app_lock_enabled", false) && prefs.getString("key_app_lock_pin_hash", "").isNullOrBlank().not()
    )
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(prefs.getBoolean("key_app_lock_enabled", false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _appLockPinLength = MutableStateFlow(prefs.getInt("key_app_lock_pin_length", 4))
    val appLockPinLength: StateFlow<Int> = _appLockPinLength.asStateFlow()

    private val _biometricUnlockEnabled = MutableStateFlow(prefs.getBoolean("key_app_lock_biometric_enabled", false))
    val biometricUnlockEnabled: StateFlow<Boolean> = _biometricUnlockEnabled.asStateFlow()

    private val _appLockTimeoutMins = MutableStateFlow(prefs.getInt("key_app_lock_timeout_mins", 0))
    val appLockTimeoutMins: StateFlow<Int> = _appLockTimeoutMins.asStateFlow()

    private var lastBackgroundTimestamp = 0L

    private fun hashPin(pin: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun configureAppLock(
        enabled: Boolean,
        pin: String,
        pinLength: Int,
        biometricEnabled: Boolean,
        timeoutMins: Int
    ) {
        val pinHash = if (pin.isNotBlank()) hashPin(pin) else prefs.getString("key_app_lock_pin_hash", "") ?: ""
        prefs.edit()
            .putBoolean("key_app_lock_enabled", enabled)
            .putString("key_app_lock_pin_hash", pinHash)
            .putInt("key_app_lock_pin_length", pinLength)
            .putBoolean("key_app_lock_biometric_enabled", biometricEnabled)
            .putInt("key_app_lock_timeout_mins", timeoutMins)
            .apply()

        _appLockEnabled.value = enabled
        _appLockPinLength.value = pinLength
        _biometricUnlockEnabled.value = biometricEnabled
        _appLockTimeoutMins.value = timeoutMins

        _toastMessage.value = if (enabled) "App Passcode Lock enabled ($pinLength-digit)" else "App Lock disabled"
    }

    fun disableAppLock() {
        prefs.edit()
            .putBoolean("key_app_lock_enabled", false)
            .putString("key_app_lock_pin_hash", "")
            .apply()
        _appLockEnabled.value = false
        _isAppLocked.value = false
        _toastMessage.value = "App Lock turned off"
    }

    fun verifyPasscode(inputPin: String): Boolean {
        val storedHash = prefs.getString("key_app_lock_pin_hash", "") ?: ""
        if (storedHash.isBlank()) return true
        val inputHash = hashPin(inputPin)
        return storedHash == inputHash
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (_appLockEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun onAppPaused() {
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    fun onAppResumed() {
        if (_appLockEnabled.value) {
            val timeoutMillis = _appLockTimeoutMins.value * 60 * 1000L
            val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
            if (lastBackgroundTimestamp == 0L || elapsed >= timeoutMillis) {
                _isAppLocked.value = true
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

    fun updateChatTheme(roomId: String, themeId: String) {
        viewModelScope.launch {
            val res = repository.updateRoomTheme(roomId, themeId)
            if (res.isSuccess) {
                _currentRoom.value = _currentRoom.value?.copy(themeId = themeId)
                _toastMessage.value = "Chat theme updated 🎨"
            }
        }
    }

    fun setDisappearingMessages(roomId: String, durationSec: Long) {
        val profile = _userProfile.value ?: return
        val actorName = profile.displayName.ifBlank { profile.username }
        viewModelScope.launch {
            val res = repository.setRoomDisappearingMessages(roomId, durationSec, actorName)
            if (res.isSuccess) {
                _currentRoom.value = _currentRoom.value?.copy(disappearingDurationSec = durationSec)
                val label = when (durationSec) {
                    0L -> "Disappearing messages turned off"
                    -1L -> "View Once active: messages vanish after viewing ⚡"
                    86400L -> "Messages disappear after 24 hours ⚡"
                    604800L -> "Messages disappear after 7 days ⚡"
                    else -> "Messages disappear after ${durationSec / 3600} hours"
                }
                _toastMessage.value = label
            }
        }
    }

    fun setParticipantNickname(roomId: String, targetUserId: String, nickname: String) {
        viewModelScope.launch {
            val res = repository.setParticipantNickname(roomId, targetUserId, nickname)
            if (res.isSuccess) {
                val updatedMap = _currentRoom.value?.nicknames?.toMutableMap() ?: mutableMapOf()
                if (nickname.isBlank()) updatedMap.remove(targetUserId) else updatedMap[targetUserId] = nickname.trim()
                _currentRoom.value = _currentRoom.value?.copy(nicknames = updatedMap)
                _toastMessage.value = if (nickname.isBlank()) "Nickname removed" else "Nickname set to '$nickname'"
            }
        }
    }

    fun toggleMuteRoom(roomId: String, isMuted: Boolean) {
        viewModelScope.launch {
            val res = repository.toggleMuteRoom(roomId, isMuted)
            if (res.isSuccess) {
                val currentUid = auth.currentUser?.uid ?: ""
                val currentMuted = _currentRoom.value?.mutedByUserIds ?: emptyList()
                val updatedMuted = if (isMuted) (currentMuted + currentUid).distinct() else currentMuted.filter { it != currentUid }
                _currentRoom.value = _currentRoom.value?.copy(mutedByUserIds = updatedMuted)
                _toastMessage.value = if (isMuted) "Notifications muted 🔕" else "Notifications unmuted 🔔"
            }
        }
    }

    fun toggleBlockParticipant(roomId: String, targetUserId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            val res = repository.toggleBlockRoomParticipant(roomId, targetUserId, isBlocked)
            if (res.isSuccess) {
                val currentBlocked = _currentRoom.value?.blockedUserIds ?: emptyList()
                val updated = if (isBlocked) (currentBlocked + targetUserId).distinct() else currentBlocked.filter { it != targetUserId }
                _currentRoom.value = _currentRoom.value?.copy(blockedUserIds = updated)
                _toastMessage.value = if (isBlocked) "User blocked 🚫" else "User unblocked"
            }
        }
    }

    fun markMessageAsViewed(messageId: String) {
        val room = _currentRoom.value ?: return
        val targetRoomId = room.roomId.ifBlank { room.id }
        viewModelScope.launch {
            repository.markMessageAsViewed(targetRoomId, messageId)
        }
    }

    fun startDirectMessage(otherUserId: String, otherUserName: String, otherAvatarUrl: String = "") {
        val profile = _userProfile.value ?: return
        viewModelScope.launch {
            val result = repository.createDirectMessageRoom(
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                myName = profile.username,
                otherAvatarUrl = otherAvatarUrl,
                myAvatarUrl = profile.avatarUrl
            )
            if (result.isSuccess) {
                val roomId = result.getOrThrow()
                val targetRoom = Room(
                    roomId = roomId,
                    name = otherUserName,
                    isSquadRoom = false,
                    isGroup = false,
                    memberIds = listOf(profile.userId, otherUserId),
                    memberUsernames = listOf(profile.username, otherUserName),
                    memberAvatars = mapOf(profile.userId to profile.avatarUrl, otherUserId to otherAvatarUrl),
                    themeId = "iris_glow"
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
                    memberUsernames = (memberUsernames + profile.username).distinct(),
                    themeId = "iris_glow"
                )
                selectRoom(targetRoom)
                _toastMessage.value = "Group $groupName created!"
            } else {
                _toastMessage.value = "Failed to create group"
            }
        }
    }
}
