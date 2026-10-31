package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.auth.attemptAutoSignIn
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.auth.AuthAndOnboardingScreen
import com.example.ui.communities.CommunitiesScreen
import com.example.ui.direct.ChatDetailsScreen
import com.example.ui.direct.ChatRoomScreen
import com.example.ui.direct.DirectMessagesScreen
import com.example.ui.feed.FeedScreen
import com.example.ui.instants.InstantsScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.SettingsScreen
import com.example.ui.profile.UserProfileDetailDialog
import com.example.ui.profile.FullUserProfileScreen
import com.example.ui.story.StoryViewer
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Search
import com.example.ui.search.SearchScreen
import com.example.ui.components.GlassNavBar
import com.example.ui.components.GlassNavBarItem
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinTheme
import com.example.ui.theme.AppThemeState
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VisualThemeMode
import com.example.ui.viewmodel.MainViewModel
import com.example.util.NotificationHelper

import com.example.ui.security.AppLockScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppThemeState.load(this)
        NotificationHelper.createNotificationChannel(this)
        handleNotificationIntent(intent)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val visualThemeMode by viewModel.visualThemeMode.collectAsState()
            val accentColor = AppThemeState.currentAccent.value
            LoopinTheme(
                darkTheme = isDarkTheme,
                visualThemeMode = visualThemeMode,
                accentColor = accentColor
            ) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val roomId = intent?.getStringExtra(NotificationHelper.EXTRA_ROOM_ID)
        if (!roomId.isNullOrBlank()) {
            viewModel.selectRoomById(roomId)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setUserOnlineStatus(true)
        viewModel.setAppInForeground(true)
        viewModel.onAppResumed()
    }

    override fun onPause() {
        super.onPause()
        viewModel.setAppInForeground(false)
        viewModel.onAppPaused()
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isProfileLoaded by viewModel.isProfileLoaded.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val currentRoomMessages by viewModel.currentRoomMessages.collectAsState()
    val musicNotes by viewModel.musicNotes.collectAsState()
    val instants by viewModel.instants.collectAsState()
    val publicUsers by viewModel.publicUsers.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val selectedUserProfile by viewModel.selectedUserProfile.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val selectedPostForComments by viewModel.selectedPostForComments.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val visualThemeMode by viewModel.visualThemeMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationSoundEnabled by viewModel.notificationSoundEnabled.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val appLockPinLength by viewModel.appLockPinLength.collectAsState()
    val biometricUnlockEnabled by viewModel.biometricUnlockEnabled.collectAsState()
    val appLockTimeoutMins by viewModel.appLockTimeoutMins.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentNavIndex by remember { mutableIntStateOf(0) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showChatDetails by remember { mutableStateOf(false) }
    var fullScreenUserProfile by remember { mutableStateOf<UserProfile?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Direct message alerts disabled", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(context, coroutineScope, onSuccess = {}, onUnauthenticated = {})
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            viewModel.setUserOnlineStatus(true)
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (currentUser != null && !isProfileLoaded) {
        LiquidGlassBackground {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = LoopinIrisGlow,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connecting to Loopin...",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    } else if (currentUser == null || userProfile == null || !userProfile!!.onboardingComplete) {
        AuthAndOnboardingScreen(
            isSignedIn = currentUser != null,
            onAuthSuccess = { /* auth listener updates automatically */ },
            onSaveProfile = { username, displayName, bio, avatarUrl, squadId ->
                viewModel.saveProfileAndSquad(
                    username = username,
                    displayName = displayName,
                    bio = bio,
                    avatarUrl = avatarUrl,
                    squadId = squadId
                )
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content screens rendered edge-to-edge behind the floating glass bottom bar
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                    when (currentNavIndex) {
                        // 0: Feed / Home
                        0 -> FeedScreen(
                            userProfile = userProfile,
                            posts = posts,
                            stories = stories,
                            comments = comments,
                            selectedPostForComments = selectedPostForComments,
                            onLikePost = { viewModel.likePost(it) },
                            onOpenComments = { viewModel.openCommentsForPost(it) },
                            onCloseComments = { viewModel.closeComments() },
                            onAddComment = { viewModel.addComment(it) },
                            onOpenStory = { viewModel.openStory(it) },
                            onCreateStory = { mediaUrl, caption, songTitle, songArtist ->
                                viewModel.createStory(mediaUrl, caption, songTitle, songArtist)
                            },
                            onCreatePost = { caption, imageUrl -> viewModel.createPost(caption, imageUrl) },
                            onNavigateToDMs = { currentNavIndex = 3 },
                            onOpenUserProfile = { username -> viewModel.openUserProfileByUsername(username) },
                            onDeletePost = { viewModel.deletePost(it.id) },
                            onEditPostCaption = { post, newCaption -> viewModel.updatePostCaption(post.id, newCaption) },
                            onToggleFollow = { viewModel.toggleFollowUser(it) },
                            onToggleBlock = { viewModel.toggleBlockUser(it) },
                            onSharePostToDM = { currentNavIndex = 3 }
                        )

                        // 1: Communities Hub (Open groups / topics, not locked to a single community)
                        1 -> CommunitiesScreen(
                            userProfile = userProfile,
                            communities = communities,
                            posts = posts,
                            comments = comments,
                            selectedPostForComments = selectedPostForComments,
                            onLikePost = { viewModel.likePost(it) },
                            onOpenComments = { viewModel.openCommentsForPost(it) },
                            onCloseComments = { viewModel.closeComments() },
                            onAddComment = { viewModel.addComment(it) },
                            onCreatePost = { caption, imageUrl, commId, commName ->
                                viewModel.createPost(caption, imageUrl, commId, commName)
                            },
                            onOpenUserProfile = { username -> viewModel.openUserProfileByUsername(username) }
                        )

                        // 2: Instants (Flash photo drops)
                        2 -> InstantsScreen(
                            userProfile = userProfile,
                            instants = instants,
                            onPostInstant = { imageUrl, caption -> viewModel.postInstant(imageUrl, caption) }
                        )

                        // 3: Messages (Private DMs & Shared Group Chats)
                        3 -> DirectMessagesScreen(
                            userProfile = userProfile,
                            rooms = rooms,
                            musicNotes = musicNotes,
                            publicUsers = publicUsers,
                            onSelectRoom = { viewModel.selectRoom(it) },
                            onStartDirectMessage = { userId, username ->
                                viewModel.startDirectMessage(userId, username)
                            },
                            onCreateGroupRoom = { groupName, memberIds, memberUsernames ->
                                viewModel.createGroupRoom(groupName, memberIds, memberUsernames)
                            },
                            onOpenUserProfile = { username -> viewModel.openUserProfileByUsername(username) },
                            onSaveNote = { song, artist, text, cover, uri, audio, colorTheme ->
                                viewModel.saveMusicNote(song, artist, text, cover, uri, audio, colorTheme)
                            },
                            onDeleteNote = { noteId ->
                                viewModel.deleteMusicNote(noteId)
                            },
                            onBack = { currentNavIndex = 0 }
                        )

                        // 4: Profile
                        4 -> ProfileScreen(
                            userProfile = userProfile,
                            userPosts = posts.filter { it.userId == userProfile?.userId },
                            onOpenSettings = { showSettingsScreen = true }
                        )

                        // 5: Separate Search & Explore Tab (matches attached images)
                        5 -> SearchScreen(
                            posts = posts,
                            publicUsers = publicUsers,
                            onOpenUserProfile = { username -> viewModel.openUserProfileByUsername(username) },
                            onOpenPostComments = { viewModel.openCommentsForPost(it) },
                            onBack = { currentNavIndex = 0 }
                        )
                    }

                    BackHandler(enabled = currentNavIndex != 0 && currentRoom == null && activeStory == null && !showSettingsScreen && fullScreenUserProfile == null) {
                        currentNavIndex = 0
                    }
                }

            // Floating Navigation Bar without any background block behind it
            if (currentRoom == null && activeStory == null && !showSettingsScreen && fullScreenUserProfile == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    InstagramBottomBar(
                        selectedIndex = currentNavIndex,
                        onSelect = { currentNavIndex = it },
                        avatarUrl = userProfile?.avatarUrl ?: ""
                    )
                }
            }

            // Dedicated Settings Screen Overlay
            if (showSettingsScreen) {
                SettingsScreen(
                    userProfile = userProfile,
                    isDarkTheme = isDarkTheme,
                    visualThemeMode = visualThemeMode,
                    notificationsEnabled = notificationsEnabled,
                    notificationSoundEnabled = notificationSoundEnabled,
                    appLockEnabled = appLockEnabled,
                    appLockPinLength = appLockPinLength,
                    biometricUnlockEnabled = biometricUnlockEnabled,
                    appLockTimeoutMins = appLockTimeoutMins,
                    onSaveProfile = { name, bio, user, avatar, isPublic, isPrivate ->
                        viewModel.updateUserProfile(name, bio, user, avatar, isPublic, isPrivate)
                    },
                    onToggleOnlineStatus = { viewModel.setUserOnlineStatus(it) },
                    onToggleBlock = { viewModel.toggleBlockUser(it) },
                    onToggleTheme = { viewModel.setDarkTheme(it) },
                    onSelectVisualThemeMode = { viewModel.setVisualThemeMode(it) },
                    onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
                    onToggleNotificationSound = { viewModel.setNotificationSoundEnabled(it) },
                    onSendTestNotification = { viewModel.sendTestNotification() },
                    onConfigureAppLock = { enabled, pin, pinLen, biometric, timeoutMins ->
                        viewModel.configureAppLock(enabled, pin, pinLen, biometric, timeoutMins)
                    },
                    onDisableAppLock = { viewModel.disableAppLock() },
                    onSignOut = { showSettingsScreen = false },
                    onBack = { showSettingsScreen = false }
                )
            }

            // Real-Time Chat Room Overlay
            if (currentRoom != null) {
                ChatRoomScreen(
                    room = currentRoom!!,
                    messages = currentRoomMessages,
                    currentUserProfile = userProfile,
                    onSendMessage = { text, type, mediaUrl, effect, songTitle, songArtist, songCoverUrl, spotifyTrackUri, audioDurationSec, replyId, replyText, replySender ->
                        viewModel.sendMessage(
                            text = text,
                            type = type,
                            mediaUrl = mediaUrl,
                            effect = effect,
                            songTitle = songTitle,
                            songArtist = songArtist,
                            songCoverUrl = songCoverUrl,
                            spotifyTrackUri = spotifyTrackUri,
                            audioDurationSec = audioDurationSec,
                            replyToMessageId = replyId,
                            replyToText = replyText,
                            replyToSenderName = replySender
                        )
                    },
                    onToggleReaction = { msgId, reactions, emoji ->
                        viewModel.toggleMessageReaction(msgId, reactions, emoji)
                    },
                    onUnsendMessage = { viewModel.unsendMessage(it) },
                    onDeleteForYou = { viewModel.deleteMessageForYou(it) },
                    onForwardMessage = { targetRoom, msg -> viewModel.forwardMessage(targetRoom, msg) },
                    availableRooms = rooms,
                    onOpenUserProfile = { username -> viewModel.openUserProfileByUsername(username) },
                    onOpenDetails = { showChatDetails = true },
                    onMarkMessageViewed = { viewModel.markMessageAsViewed(it) },
                    onUserTyping = { isTyping -> viewModel.setUserTyping(isTyping) },
                    onBack = {
                        showChatDetails = false
                        viewModel.closeRoom()
                    }
                )
            }

            // Conversation Details Overlay
            if (showChatDetails && currentRoom != null) {
                ChatDetailsScreen(
                    room = currentRoom!!,
                    messages = currentRoomMessages,
                    currentUserProfile = userProfile,
                    onBack = { showChatDetails = false },
                    onOpenUserProfile = { username ->
                        showChatDetails = false
                        viewModel.openUserProfileByUsername(username)
                    },
                    onStartSearchInChat = {
                        showChatDetails = false
                    },
                    onUpdateTheme = { themeId ->
                        viewModel.updateChatTheme(currentRoom!!.id, themeId)
                    },
                    onSetDisappearingMessages = { durationSec ->
                        viewModel.setDisappearingMessages(currentRoom!!.id, durationSec)
                    },
                    onSetNickname = { targetUserId, nickname ->
                        viewModel.setParticipantNickname(currentRoom!!.id, targetUserId, nickname)
                    },
                    onToggleMute = { isMuted ->
                        viewModel.toggleMuteRoom(currentRoom!!.id, isMuted)
                    },
                    onToggleBlock = { targetUserId, _ ->
                        viewModel.toggleBlockUser(targetUserId)
                    }
                )
            }

            // Full-screen Story Viewer Overlay
            if (activeStory != null) {
                StoryViewer(
                    story = activeStory!!,
                    onClose = { viewModel.closeStory() },
                    onReplyViaDM = { replyText ->
                        val targetStory = activeStory!!
                        viewModel.startDirectMessage(targetStory.userId, targetStory.username)
                        viewModel.sendMessage("Replying to story: $replyText")
                    }
                )
            }

            // User Profile Detail Modal (Directly triggered when clicking any username)
            // User Profile Detail Modal (Card with Expand Option)
            if (selectedUserProfile != null && fullScreenUserProfile == null) {
                UserProfileDetailDialog(
                    user = selectedUserProfile!!,
                    currentUser = userProfile,
                    userPosts = posts.filter { it.userId == selectedUserProfile?.userId },
                    isDarkTheme = isDarkTheme,
                    onToggleFollow = { viewModel.toggleFollowUser(it) },
                    onToggleBlock = { viewModel.toggleBlockUser(it) },
                    onStartDirectMessage = { uid, uname ->
                        viewModel.closeSelectedUserProfile()
                        viewModel.startDirectMessage(uid, uname)
                    },
                    onExpandToFullScreen = { user ->
                        fullScreenUserProfile = user
                    },
                    onDismiss = { viewModel.closeSelectedUserProfile() }
                )
            }

            // Full Size User Page (Opened when expanded or directly)
            if (fullScreenUserProfile != null) {
                FullUserProfileScreen(
                    user = fullScreenUserProfile!!,
                    currentUser = userProfile,
                    userPosts = posts.filter { it.userId == fullScreenUserProfile?.userId },
                    isDarkTheme = isDarkTheme,
                    onToggleFollow = { viewModel.toggleFollowUser(it) },
                    onToggleBlock = { viewModel.toggleBlockUser(it) },
                    onStartDirectMessage = { uid, uname ->
                        fullScreenUserProfile = null
                        viewModel.startDirectMessage(uid, uname)
                    },
                    onLikePost = { viewModel.likePost(it) },
                    onDismiss = { fullScreenUserProfile = null }
                )
            }

            // Security & App Lock Screen Overlay
            if (isAppLocked) {
                AppLockScreen(
                    viewModel = viewModel,
                    onUnlocked = { viewModel.unlockApp() }
                )
            }
        }
    }
}

@Composable
fun InstagramBottomBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    avatarUrl: String
) {
    val isDark = LocalIsDarkTheme.current
    val navItems = remember(avatarUrl) {
        listOf(
            GlassNavBarItem(
                id = 0,
                icon = Icons.Default.Home,
                label = "Feed",
                testTag = "nav_item_feed"
            ),
            GlassNavBarItem(
                id = 1,
                icon = Icons.Default.Groups,
                label = "Communities",
                testTag = "nav_item_communities"
            ),
            GlassNavBarItem(
                id = 2,
                icon = Icons.Default.FlashOn,
                label = "Instants",
                testTag = "nav_item_instants"
            ),
            GlassNavBarItem(
                id = 3,
                icon = Icons.AutoMirrored.Filled.Send,
                label = "Messages",
                hasBadge = true,
                testTag = "nav_item_messages"
            ),
            GlassNavBarItem(
                id = 4,
                icon = Icons.Default.Person,
                label = "Profile",
                avatarUrl = avatarUrl,
                testTag = "nav_item_profile"
            )
        )
    }

    GlassNavBar(
        items = navItems,
        selectedIndex = selectedIndex,
        onItemSelected = onSelect,
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("instagram_bottom_navigation"),
        trailingItem = {
            // Separate Circular Search Tab Button
            val isSearchSelected = selectedIndex == 5
            val searchInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val isPressed by searchInteractionSource.collectIsPressedAsState()
            val scaleAnim by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isPressed) 0.90f else 1.0f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.6f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                ),
                label = "searchButtonScale"
            )

            Box(
                modifier = Modifier
                    .scale(scaleAnim)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSearchSelected) {
                            Brush.linearGradient(listOf(LoopinIris, LoopinIrisGlow))
                        } else {
                            if (isDark) Brush.linearGradient(listOf(Color(0x38141724), Color(0x38141724)))
                            else Brush.linearGradient(listOf(Color(0xF7FFFFFF), Color(0xF7FFFFFF)))
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSearchSelected) LoopinIrisGlow else if (isDark) GlassBorder else Color(0x18000000),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = searchInteractionSource,
                        indication = null
                    ) { onSelect(5) }
                    .testTag("bottom_bar_search_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search & Explore",
                    tint = if (isSearchSelected) Color.White else (if (isDark) TextSecondary else Color(0xFF141728)),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}
