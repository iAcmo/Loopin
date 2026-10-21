package com.example.ui.direct

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MusicNote
import com.example.data.model.Room
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.SpotifyGreen
import com.example.ui.components.SpotifyTrackPickerDialog
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinLikeRed
import com.example.ui.theme.LoopinOnlineGreen
import com.example.ui.theme.LoopinSilver
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LoopinAudioController
import com.example.util.SpotifyMusicService

@Composable
fun DirectMessagesScreen(
    userProfile: UserProfile?,
    rooms: List<Room>,
    musicNotes: List<MusicNote>,
    publicUsers: List<UserProfile> = emptyList(),
    onSelectRoom: (Room) -> Unit,
    onStartDirectMessage: (userId: String, username: String) -> Unit = { _, _ -> },
    onCreateGroupRoom: (groupName: String, memberIds: List<String>, memberUsernames: List<String>) -> Unit = { _, _, _ -> },
    onOpenUserProfile: (username: String) -> Unit = {},
    onSaveNote: (
        songTitle: String,
        artist: String,
        noteText: String,
        albumCoverUrl: String,
        spotifyUri: String,
        previewAudioUrl: String
    ) -> Unit,
    onDeleteNote: (noteId: String) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Direct (Private), 1: Groups
    var showLeaveNoteDialog by remember { mutableStateOf(false) }
    var showMyNoteOptionsDialog by remember { mutableStateOf(false) }
    var showStartChatDialog by remember { mutableStateOf(false) }
    var showMakeGroupDialog by remember { mutableStateOf(false) }
    var selectedNoteToReply by remember { mutableStateOf<MusicNote?>(null) }

    val playingId by LoopinAudioController.playingId.collectAsState()

    // Strictly separate DMs and Groups.
    // DMs are private only to the 2 participants (current user + the other party).
    val currentUid = userProfile?.userId ?: ""
    val myRooms = rooms.filter { it.memberIds.contains(currentUid) }
    val directRooms = myRooms.filter { !it.isGroup && !it.isSquadRoom }
    val groupRooms = myRooms.filter { it.isGroup }

    val myNote = musicNotes.firstOrNull { it.userId == currentUid }
    val otherNotes = musicNotes.filter { it.userId != currentUid }

    LiquidGlassBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(if (isDark) Color(0x3308090E) else Color(0xEEFFFFFF))
                        .border(
                            width = 0.5.dp,
                            color = if (isDark) Color(0x18FFFFFF) else Color(0x12000000),
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("dm_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Text(
                                text = "Messages",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            userProfile?.username?.let {
                                Text(
                                    text = "@$it",
                                    fontSize = 11.sp,
                                    color = LoopinIrisGlow
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Make Group button
                        IconButton(
                            onClick = { showMakeGroupDialog = true },
                            modifier = Modifier.testTag("make_group_header_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "Make Group",
                                tint = LoopinIrisGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // New Chat button
                        IconButton(
                            onClick = { showStartChatDialog = true },
                            modifier = Modifier.testTag("new_chat_header_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "New Direct Message",
                                tint = if (isDark) TextPrimary else Color(0xFF141728),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Search by Username Input
                GlassTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search by username or name (@username)...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("dm_search_input")
                )

                // Search results dropdown/section if user is actively searching by username
                if (searchQuery.isNotBlank()) {
                    val cleanQuery = searchQuery.removePrefix("@").trim()
                    val matchingUsers = publicUsers.filter {
                        it.username.contains(cleanQuery, ignoreCase = true) ||
                                it.displayName.contains(cleanQuery, ignoreCase = true)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Users matching \"@$cleanQuery\" (${matchingUsers.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoopinIrisGlow,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        if (matchingUsers.isEmpty()) {
                            Text(
                                text = "No user found with username @$cleanQuery",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((matchingUsers.size * 64).coerceAtMost(220).dp)
                            ) {
                                items(matchingUsers, key = { it.userId }) { user ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x18FFFFFF))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onOpenUserProfile(user.username) },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            com.example.ui.components.LoopinAvatar(
                                                avatarUrl = user.avatarUrl,
                                                size = 36.dp
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Text(
                                                    text = user.displayName.ifBlank { user.username },
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) TextPrimary else Color(0xFF141728)
                                                )
                                                Text(
                                                    text = "@${user.username}",
                                                    fontSize = 11.sp,
                                                    color = LoopinIrisGlow
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            GlassButton(
                                                onClick = {
                                                    searchQuery = ""
                                                    onStartDirectMessage(user.userId, user.username)
                                                },
                                                text = "Chat",
                                                isPrimary = true,
                                                modifier = Modifier.height(32.dp)
                                            )
                                            GlassButton(
                                                onClick = { onOpenUserProfile(user.username) },
                                                text = "Profile",
                                                isPrimary = false,
                                                modifier = Modifier.height(32.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                        Divider(
                            color = Color(0x1AFFFFFF),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // Tabs: Direct (Private) vs Groups
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = LoopinIrisGlow,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = LoopinIrisGlow,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Direct (${directRooms.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) LoopinIrisGlow else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Groups (${groupRooms.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) LoopinIrisGlow else TextSecondary
                            )
                        }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp)
                ) {
                    // Music Notes Section (at top of Direct tab)
                    if (selectedTab == 0) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Spotify Music Notes",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                }

                                Text(
                                    text = "Tap to listen 🎵",
                                    fontSize = 11.sp,
                                    color = SpotifyGreen
                                )
                            }

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    FrostedUserNoteBubble(
                                        userProfile = userProfile,
                                        myNote = myNote,
                                        isPlaying = playingId == myNote?.id,
                                        onClick = {
                                            if (myNote != null) {
                                                showMyNoteOptionsDialog = true
                                            } else {
                                                showLeaveNoteDialog = true
                                            }
                                        }
                                    )
                                }

                                items(otherNotes) { note ->
                                    FrostedSquadNoteBubble(
                                        note = note,
                                        isPlaying = playingId == note.id,
                                        onClick = { selectedNoteToReply = note }
                                    )
                                }
                            }
                            Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)
                        }
                    }

                    // TAB 0: Direct Messages (Private 1-on-1 only)
                    if (selectedTab == 0) {
                        val filteredDirect = directRooms.filter {
                            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.lastMessage.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredDirect.isEmpty()) {
                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = LoopinIrisGlow,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No private direct messages yet",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextPrimary else Color(0xFF141728)
                                        )
                                        Text(
                                            text = "Direct messages are 100% private to you and the recipient. Use the search bar or '+' to start a chat!",
                                            fontSize = 12.sp,
                                            color = if (isDark) TextSecondary else Color(0xFF5B6178),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        GlassButton(
                                            onClick = { showStartChatDialog = true },
                                            text = "Start a Private Chat",
                                            isPrimary = true
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredDirect, key = { it.id.ifBlank { it.roomId } }) { room ->
                                FrostedDirectChatItem(
                                    room = room,
                                    currentUserId = userProfile?.userId ?: "",
                                    currentUsername = userProfile?.displayName?.ifBlank { userProfile.username } ?: userProfile?.username ?: "",
                                    onClick = { onSelectRoom(room) },
                                    onAvatarClick = { onOpenUserProfile(room.name) }
                                )
                            }
                        }
                    }

                    // TAB 1: Groups (Multi-person chats)
                    if (selectedTab == 1) {
                        item {
                            // "Create Group" banner at top of Groups
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .clickable { showMakeGroupDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x337E74F1))
                                            .border(1.dp, LoopinIrisGlow, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GroupAdd,
                                            contentDescription = "Create Group",
                                            tint = LoopinIrisGlow,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Make a Group Chat",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Chat with multiple friends in a shared group space",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = LoopinIrisGlow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        val filteredGroups = groupRooms.filter {
                            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.lastMessage.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredGroups.isEmpty()) {
                            item {
                                Text(
                                    text = "No group chats yet. Tap 'Make a Group Chat' above to bring friends together!",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp)
                                )
                            }
                        } else {
                            items(filteredGroups, key = { it.id.ifBlank { it.roomId } }) { group ->
                                FrostedGroupChatItem(
                                    room = group,
                                    currentUserId = userProfile?.userId ?: "",
                                    currentUsername = userProfile?.displayName?.ifBlank { userProfile.username } ?: userProfile?.username ?: "",
                                    onClick = { onSelectRoom(group) }
                                )
                            }
                        }
                    }
                }
            }

            // Floating Action Button
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 1) {
                        showMakeGroupDialog = true
                    } else {
                        showStartChatDialog = true
                    }
                },
                containerColor = LoopinIris,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 85.dp)
                    .size(54.dp)
                    .testTag("dm_fab_button")
            ) {
                Icon(
                    imageVector = if (selectedTab == 1) Icons.Default.GroupAdd else Icons.Default.Edit,
                    contentDescription = if (selectedTab == 1) "Make Group" else "New Chat",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Leave Music Note Dialog
        if (showLeaveNoteDialog) {
            LeaveMusicNoteDialog(
                onDismiss = { showLeaveNoteDialog = false },
                onSave = { song, artist, text, cover, uri, audio ->
                    onSaveNote(song, artist, text, cover, uri, audio)
                    showLeaveNoteDialog = false
                }
            )
        }

        // My Note Options Dialog
        if (showMyNoteOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showMyNoteOptionsDialog = false },
                containerColor = Color(0xFF141728),
                title = { Text("Your Spotify Note", color = TextPrimary) },
                text = {
                    Column {
                        myNote?.let {
                            Text(
                                text = "\"${it.noteText}\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LoopinIrisGlow
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Track: ${it.songTitle} • ${it.artist}",
                                fontSize = 12.sp,
                                color = SpotifyGreen
                            )
                        }
                    }
                },
                confirmButton = {
                    GlassButton(
                        onClick = {
                            showMyNoteOptionsDialog = false
                            showLeaveNoteDialog = true
                        },
                        text = "Change Note",
                        isPrimary = true
                    )
                },
                dismissButton = {
                    GlassButton(
                        onClick = {
                            myNote?.let { onDeleteNote(it.id) }
                            showMyNoteOptionsDialog = false
                        },
                        text = "Delete",
                        isPrimary = false
                    )
                }
            )
        }

        // Reply to Note Dialog
        if (selectedNoteToReply != null) {
            val targetNote = selectedNoteToReply!!
            AlertDialog(
                onDismissRequest = { selectedNoteToReply = null },
                containerColor = Color(0xFF141728),
                title = { Text("Reply to @${targetNote.username}'s Note", color = TextPrimary, fontSize = 16.sp) },
                text = {
                    Column {
                        Text(
                            text = "\"${targetNote.noteText}\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoopinIrisGlow
                        )
                        Text(
                            text = "🎵 ${targetNote.songTitle} - ${targetNote.artist}",
                            fontSize = 12.sp,
                            color = SpotifyGreen,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                confirmButton = {
                    GlassButton(
                        onClick = {
                            onStartDirectMessage(targetNote.userId, targetNote.username)
                            selectedNoteToReply = null
                        },
                        text = "Send Message",
                        isPrimary = true
                    )
                },
                dismissButton = {
                    GlassButton(
                        onClick = { selectedNoteToReply = null },
                        text = "Cancel",
                        isPrimary = false
                    )
                }
            )
        }

        // Start Chat Dialog (search by username)
        if (showStartChatDialog) {
            StartChatDialog(
                publicUsers = publicUsers,
                onDismiss = { showStartChatDialog = false },
                onSelectUser = { targetUser ->
                    onStartDirectMessage(targetUser.userId, targetUser.username)
                    showStartChatDialog = false
                },
                onViewProfile = { username ->
                    showStartChatDialog = false
                    onOpenUserProfile(username)
                }
            )
        }

        // Make Group Dialog
        if (showMakeGroupDialog) {
            MakeGroupDialog(
                publicUsers = publicUsers,
                currentUserId = currentUid,
                onDismiss = { showMakeGroupDialog = false },
                onCreateGroup = { groupName, memberIds, memberUsernames ->
                    onCreateGroupRoom(groupName, memberIds, memberUsernames)
                    showMakeGroupDialog = false
                }
            )
        }
    }
}

@Composable
fun FrostedDirectChatItem(
    room: Room,
    currentUserId: String = "",
    currentUsername: String = "",
    onClick: () -> Unit,
    onAvatarClick: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    val isOtherTyping = room.typingUserIds.any { it != currentUserId }
    val isUnread = room.unreadByUserIds.contains(currentUserId)
    val isSentByMe = currentUsername.isNotBlank() && (room.lastMessageSender == currentUsername)

    val otherUserId = room.memberIds.firstOrNull { it != currentUserId } ?: ""
    val otherIndex = room.memberIds.indexOf(otherUserId)
    val otherUsername = if (otherIndex in room.memberUsernames.indices) {
        room.memberUsernames[otherIndex]
    } else {
        room.name.ifBlank { "User" }
    }
    val otherAvatarUrl = room.memberAvatars[otherUserId] ?: ""
    val assignedNickname = room.nicknames[otherUserId]
    val effectiveDisplayName = if (!room.isGroup) {
        if (!assignedNickname.isNullOrBlank()) assignedNickname else otherUsername
    } else {
        room.name.ifBlank { "Group Squad" }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!room.isGroup && otherAvatarUrl.isNotBlank()) {
            com.example.ui.components.LoopinAvatar(
                avatarUrl = otherAvatarUrl,
                size = 50.dp,
                onClick = onAvatarClick
            )
        } else {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isOtherTyping) Color(0x3D7E74F1) else Color(0x287E74F1))
                    .border(1.5.dp, if (isOtherTyping) LoopinIrisGlow else GlassBorder, CircleShape)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                if (room.isGroup) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Group",
                        tint = LoopinIrisGlow,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = effectiveDisplayName.take(2).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoopinIrisGlow
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = effectiveDisplayName,
                fontSize = 14.sp,
                fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isDark) TextPrimary else Color(0xFF141728)
            )
            Spacer(modifier = Modifier.height(2.dp))

            if (isOtherTyping) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(LoopinIrisGlow)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "typing...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LoopinIrisGlow
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSentByMe && room.lastMessage.isNotBlank()) {
                        val isSeen = room.unreadByUserIds.none { it != currentUserId }
                        if (isSeen) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Seen",
                                tint = LoopinIrisGlow,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Seen • ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = LoopinIrisGlow
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Sent • ",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Text(
                        text = room.lastMessage.ifBlank { "Tap to send a private message" },
                        fontSize = 12.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUnread) {
                            if (isDark) TextPrimary else Color(0xFF141728)
                        } else {
                            if (isDark) TextSecondary else Color(0xFF5B6178)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            formatMessageTime(room.lastMessageTime).let { timeStr ->
                if (timeStr.isNotBlank()) {
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUnread) LoopinIrisGlow else TextMuted
                    )
                }
            }
            if (isUnread) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LoopinIrisGlow)
                )
            }
        }
    }
}

@Composable
fun FrostedGroupChatItem(
    room: Room,
    currentUserId: String = "",
    currentUsername: String = "",
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val typingUsers = room.typingUsernames.filter { it != currentUsername }
    val isOtherTyping = room.typingUserIds.any { it != currentUserId }
    val isUnread = room.unreadByUserIds.contains(currentUserId)
    val isSentByMe = currentUsername.isNotBlank() && (room.lastMessageSender == currentUsername)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isOtherTyping) Color(0x387E74F1) else Color(0x337E74F1))
                .border(1.5.dp, if (isOtherTyping) LoopinIrisGlow else LoopinIrisGlow.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = LoopinIrisGlow,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = room.name,
                    fontSize = 14.sp,
                    fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x227E74F1))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${room.memberIds.size} members",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = LoopinIrisGlow
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))

            if (isOtherTyping) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(LoopinIrisGlow)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (typingUsers.isNotEmpty()) "${typingUsers.first()} is typing..." else "typing...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LoopinIrisGlow
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSentByMe && room.lastMessage.isNotBlank()) {
                        val isSeen = room.unreadByUserIds.none { it != currentUserId }
                        if (isSeen) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Seen",
                                tint = LoopinIrisGlow,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Seen • ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = LoopinIrisGlow
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Sent • ",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Text(
                        text = room.lastMessage.ifBlank { "Group conversation" },
                        fontSize = 12.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUnread) {
                            if (isDark) TextPrimary else Color(0xFF141728)
                        } else {
                            if (isDark) TextSecondary else Color(0xFF5B6178)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            formatMessageTime(room.lastMessageTime).let { timeStr ->
                if (timeStr.isNotBlank()) {
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUnread) LoopinIrisGlow else TextMuted
                    )
                }
            }
            if (isUnread) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LoopinIrisGlow)
                )
            }
        }
    }
}

@Composable
fun MakeGroupDialog(
    publicUsers: List<UserProfile>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onCreateGroup: (groupName: String, memberIds: List<String>, memberUsernames: List<String>) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var groupName by remember { mutableStateOf("") }
    var memberSearch by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<UserProfile>() }

    val filteredUsers = publicUsers.filter { user ->
        user.userId != currentUserId && (
                memberSearch.isBlank() ||
                        user.username.contains(memberSearch, ignoreCase = true) ||
                        user.displayName.contains(memberSearch, ignoreCase = true)
                )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141728),
        title = {
            Text(
                text = "Create New Group Chat",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Groups are open to all added members to chat together.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = "Group Name (e.g. Design Club, Weekend Trip)",
                    modifier = Modifier.testTag("group_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = memberSearch,
                    onValueChange = { memberSearch = it },
                    label = "Search members to add...",
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                )

                if (selectedMembers.isNotEmpty()) {
                    Text(
                        text = "Selected (${selectedMembers.size}): ${selectedMembers.joinToString { "@${it.username}" }}",
                        fontSize = 11.sp,
                        color = LoopinIrisGlow,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.height(160.dp)) {
                    items(filteredUsers, key = { it.userId }) { user ->
                        val isSelected = selectedMembers.contains(user)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x337E74F1) else Color(0x10FFFFFF))
                                .clickable {
                                    if (isSelected) selectedMembers.remove(user) else selectedMembers.add(user)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x287E74F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.username.take(2).uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LoopinIrisGlow
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(user.displayName.ifBlank { user.username }, fontSize = 12.sp, color = TextPrimary)
                                    Text("@${user.username}", fontSize = 10.sp, color = TextSecondary)
                                }
                            }

                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = LoopinIrisGlow, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            GlassButton(
                onClick = {
                    if (groupName.isNotBlank() && selectedMembers.isNotEmpty()) {
                        onCreateGroup(
                            groupName.trim(),
                            selectedMembers.map { it.userId },
                            selectedMembers.map { it.username }
                        )
                    }
                },
                text = "Create Group (${selectedMembers.size})",
                isPrimary = true,
                enabled = groupName.isNotBlank() && selectedMembers.isNotEmpty()
            )
        },
        dismissButton = {
            GlassButton(onClick = onDismiss, text = "Cancel", isPrimary = false)
        }
    )
}

@Composable
fun StartChatDialog(
    publicUsers: List<UserProfile>,
    onDismiss: () -> Unit,
    onSelectUser: (UserProfile) -> Unit,
    onViewProfile: (username: String) -> Unit
) {
    var memberSearch by remember { mutableStateOf("") }
    val filtered = publicUsers.filter {
        memberSearch.isBlank() ||
                it.username.contains(memberSearch.removePrefix("@"), ignoreCase = true) ||
                it.displayName.contains(memberSearch, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141728),
        title = { Text("New Direct Conversation", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DMs are 100% private to just you and the other person.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                GlassTextField(
                    value = memberSearch,
                    onValueChange = { memberSearch = it },
                    label = "Search by @username...",
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(filtered, key = { it.userId }) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x14FFFFFF))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectUser(user) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x287E74F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.username.take(2).uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LoopinIrisGlow
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(user.displayName.ifBlank { user.username }, fontSize = 13.sp, color = TextPrimary)
                                    Text("@${user.username}", fontSize = 11.sp, color = LoopinIrisGlow)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                GlassButton(
                                    onClick = { onSelectUser(user) },
                                    text = "Chat",
                                    isPrimary = true,
                                    modifier = Modifier.height(30.dp)
                                )
                                GlassButton(
                                    onClick = { onViewProfile(user.username) },
                                    text = "Profile",
                                    isPrimary = false,
                                    modifier = Modifier.height(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            GlassButton(onClick = onDismiss, text = "Close", isPrimary = false)
        }
    )
}

@Composable
fun FrostedUserNoteBubble(
    userProfile: UserProfile?,
    myNote: MusicNote?,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            // Note pill if left
            if (myNote != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x38FFFFFF))
                        .border(1.dp, if (isPlaying) SpotifyGreen else GlassBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = myNote.noteText.take(16),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPlaying) SpotifyGreen else TextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = if (myNote != null) 24.dp else 4.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, if (myNote != null) SpotifyGreen else LoopinIrisGlow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                com.example.ui.components.LoopinAvatar(
                    avatarUrl = userProfile?.avatarUrl,
                    size = 54.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (myNote != null) "Your note" else "Share a note",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun FrostedSquadNoteBubble(
    note: MusicNote,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x38FFFFFF))
                    .border(1.dp, if (isPlaying) SpotifyGreen else GlassBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = note.noteText.take(16),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPlaying) SpotifyGreen else TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, if (isPlaying) SolidColor(SpotifyGreen) else GlassStrokeBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (note.userAvatar.isNotBlank()) {
                    AsyncImage(
                        model = note.userAvatar,
                        contentDescription = note.username,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = note.username.take(2).uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoopinSilver
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "@${note.username}",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun LeaveMusicNoteDialog(
    onDismiss: () -> Unit,
    onSave: (
        songTitle: String,
        artist: String,
        noteText: String,
        albumCoverUrl: String,
        spotifyUri: String,
        previewAudioUrl: String
    ) -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    var selectedSongTitle by remember { mutableStateOf("") }
    var selectedArtist by remember { mutableStateOf("") }
    var selectedCoverUrl by remember { mutableStateOf("") }
    var selectedSpotifyUri by remember { mutableStateOf("") }
    var showSpotifyPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141728),
        title = {
            Text(
                text = "Share a Spotify Music Note",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Friends will see your note and can listen to your song.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= 60) noteText = it },
                    label = "Share a thought... (${60 - noteText.length} left)",
                    modifier = Modifier.testTag("note_text_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedSongTitle.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x221DB954))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = selectedCoverUrl,
                            contentDescription = selectedSongTitle,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedSongTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(selectedArtist, fontSize = 11.sp, color = SpotifyGreen)
                        }
                        IconButton(onClick = { showSpotifyPicker = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Change track", tint = SpotifyGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    GlassButton(
                        onClick = { showSpotifyPicker = true },
                        text = "Pick Spotify Track 🎵",
                        leadingIcon = Icons.Default.MusicNote,
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            GlassButton(
                onClick = {
                    if (noteText.isNotBlank()) {
                        onSave(
                            selectedSongTitle,
                            selectedArtist,
                            noteText.trim(),
                            selectedCoverUrl,
                            selectedSpotifyUri,
                            ""
                        )
                    }
                },
                text = "Share Note",
                isPrimary = true,
                enabled = noteText.isNotBlank()
            )
        },
        dismissButton = {
            GlassButton(onClick = onDismiss, text = "Cancel", isPrimary = false)
        }
    )

    if (showSpotifyPicker) {
        SpotifyTrackPickerDialog(
            onTrackSelected = { track ->
                selectedSongTitle = track.title
                selectedArtist = track.artist
                selectedCoverUrl = track.albumArt
                selectedSpotifyUri = track.spotifyUri
                showSpotifyPicker = false
            },
            onDismiss = { showSpotifyPicker = false }
        )
    }
}
