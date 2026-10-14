package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.auth.AuthAndOnboardingScreen
import com.example.ui.direct.ChatRoomScreen
import com.example.ui.direct.DirectMessagesScreen
import com.example.ui.feed.FeedScreen
import com.example.ui.instants.InstantsScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.SettingsScreen
import com.example.ui.story.StoryViewer
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassFillElevated
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LoopinBg
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            LoopinTheme(darkTheme = isDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setUserOnlineStatus(true)
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val currentRoomMessages by viewModel.currentRoomMessages.collectAsState()
    val musicNotes by viewModel.musicNotes.collectAsState()
    val instants by viewModel.instants.collectAsState()
    val publicUsers by viewModel.publicUsers.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val selectedPostForComments by viewModel.selectedPostForComments.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val context = LocalContext.current
    var currentNavIndex by remember { mutableIntStateOf(0) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    // Automatically update online status upon opening the app (User requested)
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

    if (currentUser == null || userProfile == null || !userProfile!!.onboardingComplete) {
        // Initial Page (One time) for Authentication, Profile Setup & Onboarding Tutorials
        AuthAndOnboardingScreen(
            isSignedIn = currentUser != null,
            onAuthSuccess = { /* auth state listener updates */ },
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
        // Authenticated user session with completed onboarding & squad assignment
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (currentRoom == null && activeStory == null && !showSettingsScreen) {
                        InstagramBottomBar(
                            selectedIndex = currentNavIndex,
                            onSelect = { currentNavIndex = it },
                            avatarUrl = userProfile?.avatarUrl ?: ""
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentNavIndex) {
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
                            onNavigateToDMs = { currentNavIndex = 2 }
                        )
                        1 -> InstantsScreen(
                            userProfile = userProfile,
                            instants = instants,
                            onPostInstant = { imageUrl, caption -> viewModel.postInstant(imageUrl, caption) }
                        )
                        2 -> DirectMessagesScreen(
                            userProfile = userProfile,
                            rooms = rooms,
                            musicNotes = musicNotes,
                            publicUsers = publicUsers,
                            onSelectRoom = { viewModel.selectRoom(it) },
                            onStartDirectMessage = { userId, username ->
                                viewModel.startDirectMessage(userId, username)
                            },
                            onSaveNote = { song, artist, text, cover, uri, audio ->
                                viewModel.saveMusicNote(song, artist, text, cover, uri, audio)
                            },
                            onDeleteNote = { noteId ->
                                viewModel.deleteMusicNote(noteId)
                            },
                            onBack = { currentNavIndex = 0 }
                        )
                        3 -> ProfileScreen(
                            userProfile = userProfile,
                            userPosts = posts.filter { it.userId == userProfile?.userId },
                            onOpenSettings = { showSettingsScreen = true }
                        )
                    }
                }
            }

            // Dedicated Settings Screen Overlay (Profile edit, theme toggle, switch squad, publicbase switch)
            if (showSettingsScreen) {
                SettingsScreen(
                    userProfile = userProfile,
                    isDarkTheme = isDarkTheme,
                    onSaveProfile = { name, bio, user, avatar, isPublic ->
                        viewModel.updateUserProfile(name, bio, user, avatar, isPublic)
                    },
                    onToggleOnlineStatus = { viewModel.setUserOnlineStatus(it) },
                    onUpdateSquad = { viewModel.updateSquad(it) },
                    onToggleTheme = { viewModel.setDarkTheme(it) },
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
                    onSendMessage = { text, type, mediaUrl, effect, songTitle, songArtist, songCoverUrl, spotifyTrackUri, audioDurationSec ->
                        viewModel.sendMessage(
                            text = text,
                            type = type,
                            mediaUrl = mediaUrl,
                            effect = effect,
                            songTitle = songTitle,
                            songArtist = songArtist,
                            songCoverUrl = songCoverUrl,
                            spotifyTrackUri = spotifyTrackUri,
                            audioDurationSec = audioDurationSec
                        )
                    },
                    onToggleReaction = { msgId, reactions, emoji ->
                        viewModel.toggleMessageReaction(msgId, reactions, emoji)
                    },
                    onBack = { viewModel.closeRoom() }
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
        }
    }
}

@Composable
fun InstagramBottomBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    avatarUrl: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .testTag("instagram_bottom_navigation")
    ) {
        // Floating Frosted Glass Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0x28FFFFFF))
                .border(1.dp, GlassStrokeBrush, RoundedCornerShape(32.dp))
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item 0: Home / Feed
                BottomBarGlassItem(
                    isSelected = selectedIndex == 0,
                    onClick = { onSelect(0) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Feed",
                        tint = if (selectedIndex == 0) LoopinIrisGlow else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Item 1: Instants (Flash)
                BottomBarGlassItem(
                    isSelected = selectedIndex == 1,
                    onClick = { onSelect(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Instants",
                        tint = if (selectedIndex == 1) LoopinIrisGlow else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Item 2: DMs (Send)
                BottomBarGlassItem(
                    isSelected = selectedIndex == 2,
                    onClick = { onSelect(2) }
                ) {
                    BadgedBox(
                        badge = {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(LoopinIrisGlow)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Messages",
                            tint = if (selectedIndex == 2) LoopinIrisGlow else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Item 3: Profile
                BottomBarGlassItem(
                    isSelected = selectedIndex == 3,
                    onClick = { onSelect(3) }
                ) {
                    if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (selectedIndex == 3) 2.dp else 1.dp,
                                    brush = if (selectedIndex == 3) FrostedStoryRingGradient else GlassStrokeBrush,
                                    shape = CircleShape
                                )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = if (selectedIndex == 3) LoopinIrisGlow else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBarGlassItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0x337E74F1) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
