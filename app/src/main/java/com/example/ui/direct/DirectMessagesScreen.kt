package com.example.ui.direct

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.statusBarsPadding
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
    onCreateGroup: (groupName: String, memberIds: List<String>, memberUsernames: List<String>) -> Unit = { _, _, _ -> },
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
    var showLeaveNoteDialog by remember { mutableStateOf(false) }
    var showMyNoteOptionsDialog by remember { mutableStateOf(false) }
    var showStartChatDialog by remember { mutableStateOf(false) }
    var selectedNoteToReply by remember { mutableStateOf<MusicNote?>(null) }

    val playingId by LoopinAudioController.playingId.collectAsState()

    val squadRooms = rooms.filter { it.isSquadRoom }
    val directRooms = rooms.filter { !it.isSquadRoom }

    val myNote = musicNotes.firstOrNull { it.userId == userProfile?.userId }
    val squadNotes = musicNotes.filter { it.userId != userProfile?.userId }

    LiquidGlassBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter)
            ) {
                // DM Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(60.dp)
                        .background(Color(0x16000000))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("dm_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) TextPrimary else Color(0xFF141728)
                        )
                    }

                    Text(
                        text = userProfile?.username ?: "Messages",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimary else Color(0xFF141728)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp)
                ) {
                    // Search Input
                    item {
                        GlassTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "Search messages...",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("dm_search_input")
                        )
                    }

                    // Music Notes Horizontal Shelf (with Spotify connection & Audio playback)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 4.dp),
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
                                text = "Tap note to play 🎵",
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
                                // User's own Note / Add Note
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

                            items(squadNotes) { note ->
                                FrostedSquadNoteBubble(
                                    note = note,
                                    isPlaying = playingId == note.id,
                                    onClick = { selectedNoteToReply = note }
                                )
                            }
                        }
                        Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)
                    }

                    // Squad Room Section (Liquid Frosted Glass Card)
                    item {
                        Text(
                            text = "Squad Communication Lounge",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextSecondary else Color(0xFF5B6178),
                            modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
                        )

                        squadRooms.forEach { room ->
                            FrostedSquadRoomItem(
                                room = room,
                                onClick = { onSelectRoom(room) }
                            )
                        }
                    }

                    // Direct Messages Section
                    item {
                        Text(
                            text = "Direct Messages",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextSecondary else Color(0xFF5B6178),
                            modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    if (directRooms.isEmpty()) {
                        item {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = LoopinIrisGlow,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No direct chats yet",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = "Tap the pencil button in the bottom right corner to start a conversation with anyone!",
                                        fontSize = 12.sp,
                                        color = if (isDark) TextSecondary else Color(0xFF5B6178),
                                        modifier = Modifier.padding(top = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(directRooms) { room ->
                            FrostedDirectChatItem(
                                room = room,
                                onClick = { onSelectRoom(room) }
                            )
                        }
                    }
                }
            }

            // Pencil Floating Action Button in bottom right corner (User requested)
            FloatingActionButton(
                onClick = { showStartChatDialog = true },
                containerColor = LoopinIris,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 85.dp)
                    .size(56.dp)
                    .testTag("start_chat_pencil_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Start Conversation",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Leave Music Note Dialog (with Spotify Picker Connection)
        if (showLeaveNoteDialog) {
            LeaveMusicNoteGlassModal(
                onDismiss = { showLeaveNoteDialog = false },
                onSave = { song, artist, text, cover, uri, audio ->
                    onSaveNote(song, artist, text, cover, uri, audio)
                    showLeaveNoteDialog = false
                }
            )
        }

        // User's own note options: Play, Edit, or Completely Remove Note
        if (showMyNoteOptionsDialog && myNote != null) {
            AlertDialog(
                onDismissRequest = { showMyNoteOptionsDialog = false },
                title = {
                    Text(
                        text = "My Music Note 🎵",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimary else Color(0xFF141728)
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "${myNote.songTitle} - ${myNote.artist}",
                            fontWeight = FontWeight.SemiBold,
                            color = SpotifyGreen,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${myNote.noteText}\"",
                            color = if (isDark) TextSecondary else Color(0xFF5B6178),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Play / Stop button
                        GlassButton(
                            onClick = {
                                LoopinAudioController.togglePlay(
                                    myNote.id,
                                    myNote.songTitle,
                                    myNote.artist,
                                    myNote.previewAudioUrl,
                                    context
                                )
                            },
                            text = if (playingId == myNote.id) "Pause Audio ⏸️" else "Play Music 🎵",
                            leadingIcon = if (playingId == myNote.id) Icons.Default.Pause else Icons.Default.PlayArrow,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (myNote.spotifyUri.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            GlassButton(
                                onClick = {
                                    SpotifyMusicService.openInSpotify(context, myNote.spotifyUri, "https://open.spotify.com")
                                },
                                text = "Open in Spotify",
                                leadingIcon = Icons.Default.MusicNote,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Option to completely remove note (User requested)
                        OutlinedButton(
                            onClick = {
                                onDeleteNote(myNote.id)
                                LoopinAudioController.stopPlayback()
                                showMyNoteOptionsDialog = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delete_music_note_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = LoopinLikeRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remove Music Note", color = LoopinLikeRed, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showMyNoteOptionsDialog = false
                        showLeaveNoteDialog = true
                    }) {
                        Text("Change Note", color = LoopinIrisGlow)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMyNoteOptionsDialog = false }) {
                        Text("Close", color = TextSecondary)
                    }
                },
                containerColor = if (isDark) Color(0xFF141724) else Color(0xFFFBFBFE),
                shape = RoundedCornerShape(22.dp)
            )
        }

        // View/Play Squad Note Dialog
        if (selectedNoteToReply != null) {
            val note = selectedNoteToReply!!
            AlertDialog(
                onDismissRequest = {
                    selectedNoteToReply = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (note.userAvatar.isNotBlank()) {
                            AsyncImage(
                                model = note.userAvatar,
                                contentDescription = note.username,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, FrostedStoryRingGradient, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LoopinSilver,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${note.username}'s Note",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) TextPrimary else Color(0xFF141728)
                            )
                            Text(
                                text = "Spotify Music Note",
                                fontSize = 11.sp,
                                color = SpotifyGreen
                            )
                        }
                    }
                },
                text = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x221DB954))
                                .border(1.dp, Color(0x441DB954), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            if (note.albumCoverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = note.albumCoverUrl,
                                    contentDescription = note.songTitle,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.songTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) TextPrimary else Color(0xFF141728)
                                )
                                Text(
                                    text = note.artist,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "\"${note.noteText}\"",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) TextPrimary else Color(0xFF141728)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Audio Playback button (melodic synth engine)
                        GlassButton(
                            onClick = {
                                LoopinAudioController.togglePlay(
                                    note.id,
                                    note.songTitle,
                                    note.artist,
                                    note.previewAudioUrl,
                                    context
                                )
                            },
                            text = if (playingId == note.id) "Pause Audio ⏸️" else "Play Music Preview 🎵",
                            leadingIcon = if (playingId == note.id) Icons.Default.Pause else Icons.Default.PlayArrow,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (note.spotifyUri.isNotBlank()) {
                            GlassButton(
                                onClick = {
                                    SpotifyMusicService.openInSpotify(
                                        context,
                                        note.spotifyUri,
                                        "https://open.spotify.com"
                                    )
                                },
                                text = "Spotify 🎧",
                                isPrimary = false
                            )
                        }
                        TextButton(onClick = { selectedNoteToReply = null }) {
                            Text("Close", color = TextSecondary)
                        }
                    }
                },
                containerColor = if (isDark) Color(0xFF141724) else Color(0xFFFBFBFE),
                shape = RoundedCornerShape(22.dp)
            )
        }

        // Start Conversation Dialog (Pencil button: connect with anyone or make a group)
        if (showStartChatDialog) {
            StartConversationGlassModal(
                publicUsers = publicUsers,
                onSelectUser = { targetUser ->
                    onStartDirectMessage(targetUser.userId, targetUser.username)
                    showStartChatDialog = false
                },
                onDirectUsername = { typedUsername ->
                    onStartDirectMessage("user_$typedUsername", typedUsername)
                    showStartChatDialog = false
                },
                onCreateGroup = { groupName, memberIds, memberUsernames ->
                    onCreateGroup(groupName, memberIds, memberUsernames)
                    showStartChatDialog = false
                },
                onDismiss = { showStartChatDialog = false }
            )
        }
    }
}

@Composable
private fun StartConversationGlassModal(
    publicUsers: List<UserProfile>,
    onSelectUser: (UserProfile) -> Unit,
    onDirectUsername: (String) -> Unit,
    onCreateGroup: (groupName: String, memberIds: List<String>, memberUsernames: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Direct Chat, 1: Create Group
    var memberSearch by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    val selectedUsersForGroup = remember { mutableStateListOf<UserProfile>() }

    val filtered = publicUsers.filter {
        memberSearch.isBlank() ||
                it.username.contains(memberSearch, ignoreCase = true) ||
                it.displayName.contains(memberSearch, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (activeTab == 0) Icons.Default.Edit else Icons.Default.Groups,
                        contentDescription = null,
                        tint = LoopinIrisGlow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeTab == 0) "New Conversation" else "Create Private Group",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimary else Color(0xFF141728)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher: Direct Chat vs Create Group
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x16FFFFFF))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTab == 0) LoopinIris else Color.Transparent)
                            .clickable { activeTab = 0 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Direct Chat",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == 0) Color.White else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTab == 1) LoopinIris else Color.Transparent)
                            .clickable { activeTab = 1 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "New Group 👥",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == 1) Color.White else TextSecondary
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (activeTab == 0) {
                    // Direct Message tab
                    Text(
                        text = "DMs are strictly private between partakers only.",
                        fontSize = 12.sp,
                        color = if (isDark) TextSecondary else Color(0xFF5B6178)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    GlassTextField(
                        value = memberSearch,
                        onValueChange = { memberSearch = it },
                        label = "Search member or enter @username",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("new_chat_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filtered.isNotEmpty()) {
                        Text(
                            text = "Directory (${filtered.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoopinIrisGlow
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            items(filtered, key = { it.userId }) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x12FFFFFF))
                                        .clickable { onSelectUser(user) }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (user.avatarUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = user.avatarUrl,
                                                contentDescription = user.username,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = LoopinSilver,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(if (user.isOnline) LoopinOnlineGreen else TextMuted)
                                                .border(1.dp, Color(0xFF141724), CircleShape)
                                                .align(Alignment.BottomEnd)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.displayName.ifBlank { user.username },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextPrimary else Color(0xFF141728)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "@${user.username} • ",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = if (user.isOnline) "Online" else "Offline",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (user.isOnline) LoopinOnlineGreen else TextMuted
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Chat 💬",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LoopinIrisGlow
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    } else if (memberSearch.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        GlassButton(
                            onClick = { onDirectUsername(memberSearch.trim().removePrefix("@")) },
                            text = "Message @${memberSearch.trim().removePrefix("@")}",
                            leadingIcon = Icons.Default.Edit,
                            isPrimary = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No public members found matching filter",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    // Create Group Tab
                    Text(
                        text = "Create a group conversation with multiple friends.",
                        fontSize = 12.sp,
                        color = if (isDark) TextSecondary else Color(0xFF5B6178)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = "Group Name (e.g. Squad Jam)",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Select Participants (${selectedUsersForGroup.size} selected)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoopinIrisGlow
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(publicUsers, key = { it.userId }) { user ->
                            val isSelected = selectedUsersForGroup.any { it.userId == user.userId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0x227E74F1) else Color(0x0EFFFFFF))
                                    .clickable {
                                        if (isSelected) {
                                            selectedUsersForGroup.removeAll { it.userId == user.userId }
                                        } else {
                                            selectedUsersForGroup.add(user)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedUsersForGroup.add(user)
                                        } else {
                                            selectedUsersForGroup.removeAll { it.userId == user.userId }
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = LoopinIris,
                                        uncheckedColor = TextMuted
                                    )
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName.ifBlank { user.username },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = "@${user.username}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassButton(
                        onClick = {
                            val ids = selectedUsersForGroup.map { it.userId }
                            val names = selectedUsersForGroup.map { it.displayName.ifBlank { it.username } }
                            onCreateGroup(groupName.trim(), ids, names)
                        },
                        text = "Create Group (${selectedUsersForGroup.size})",
                        leadingIcon = Icons.Default.Groups,
                        isPrimary = true,
                        enabled = groupName.isNotBlank() && selectedUsersForGroup.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_create_group_button")
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = if (isDark) Color(0xFF141724) else Color(0xFFFBFBFE),
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
private fun FrostedUserNoteBubble(
    userProfile: UserProfile?,
    myNote: MusicNote?,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(86.dp)
            .clickable { onClick() }
            .testTag("user_music_note_bubble")
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isPlaying) Color(0x331DB954) else Color(0x22FFFFFF))
                .border(
                    0.75.dp,
                    if (isPlaying) SpotifyGreen else GlassBorder,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (myNote != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = myNote.songTitle.take(8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyGreen,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = myNote.noteText.take(12),
                        fontSize = 10.sp,
                        color = if (isDark) TextPrimary else Color(0xFF141728),
                        maxLines = 1
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = SpotifyGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "+ Note",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            val ringBrush = if (isPlaying) SolidColor(SpotifyGreen) else FrostedStoryRingGradient
            if (!userProfile?.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = userProfile!!.avatarUrl,
                    contentDescription = "Your Note",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, ringBrush, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .border(1.5.dp, ringBrush, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = LoopinSilver,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Your Note",
            fontSize = 11.sp,
            color = if (isDark) TextSecondary else Color(0xFF5B6178),
            maxLines = 1
        )
    }
}

@Composable
private fun FrostedSquadNoteBubble(
    note: MusicNote,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(86.dp)
            .clickable { onClick() }
            .testTag("squad_note_bubble_${note.userId}")
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isPlaying) Color(0x331DB954) else Color(0x22FFFFFF))
                .border(
                    0.75.dp,
                    if (isPlaying) SpotifyGreen else GlassBorder,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = note.songTitle.take(8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        maxLines = 1
                    )
                }
                Text(
                    text = note.noteText.take(12),
                    fontSize = 10.sp,
                    color = if (isDark) TextPrimary else Color(0xFF141728),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            val ringBrush = if (isPlaying) SolidColor(SpotifyGreen) else FrostedStoryRingGradient
            if (note.userAvatar.isNotBlank()) {
                AsyncImage(
                    model = note.userAvatar,
                    contentDescription = note.username,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, ringBrush, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .border(1.5.dp, ringBrush, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = LoopinSilver,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = note.username,
            fontSize = 11.sp,
            color = if (isDark) TextSecondary else Color(0xFF5B6178),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FrostedSquadRoomItem(
    room: Room,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("squad_lounge_item_${room.roomId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x337E74F1))
                    .border(1.dp, LoopinIrisGlow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = LoopinIrisGlow,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name.ifBlank { "Squad #${room.squadId} Lounge" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )
                Text(
                    text = room.lastMessage.ifBlank { "Live group messaging for Squad #${room.squadId}" },
                    fontSize = 12.sp,
                    color = if (isDark) TextSecondary else Color(0xFF5B6178),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x2222C55E))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoopinOnlineGreen
                )
            }
        }
    }
}

@Composable
private fun FrostedDirectChatItem(
    room: Room,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("direct_chat_item_${room.roomId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(if (room.isGroup) Color(0x337E74F1) else Color(0x22FFFFFF))
                        .border(1.dp, if (room.isGroup) LoopinIrisGlow else GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (room.isGroup) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Group",
                            tint = LoopinIrisGlow,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = room.name.take(2).uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoopinIrisGlow
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(LoopinOnlineGreen)
                        .border(1.5.dp, Color(0xFF141724), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimary else Color(0xFF141728)
                    )
                    if (room.isGroup) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x227E74F1))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "GROUP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LoopinIrisGlow
                            )
                        }
                    }
                }
                Text(
                    text = if (room.isGroup) {
                        val memberCount = if (room.memberIds.isNotEmpty()) "${room.memberIds.size} members • " else ""
                        "$memberCount${room.lastMessage.ifBlank { "Active group" }}"
                    } else {
                        room.lastMessage.ifBlank { "Start conversation..." }
                    },
                    fontSize = 12.sp,
                    color = if (isDark) TextSecondary else Color(0xFF5B6178),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LeaveMusicNoteGlassModal(
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
    val isDark = LocalIsDarkTheme.current
    var songTitle by remember { mutableStateOf("Espresso") }
    var artist by remember { mutableStateOf("Sabrina Carpenter") }
    var albumCoverUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80") }
    var spotifyUri by remember { mutableStateOf("spotify:track:2qxmye6gAegTMjLKEBoR7d") }
    var noteText by remember { mutableStateOf("") }
    var showSpotifyPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Spotify Music Note",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Pick a soundtrack from Spotify & attach your thoughts to your squad shelf",
                    fontSize = 12.sp,
                    color = if (isDark) TextSecondary else Color(0xFF5B6178)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Track Preview Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x221DB954))
                        .border(1.dp, Color(0x441DB954), RoundedCornerShape(14.dp))
                        .clickable { showSpotifyPicker = true }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = albumCoverUrl,
                        contentDescription = songTitle,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = songTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextPrimary else Color(0xFF141728)
                        )
                        Text(
                            text = artist,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x331DB954))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Change",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlassButton(
                    onClick = { showSpotifyPicker = true },
                    text = "Search Spotify Library 🔍",
                    leadingIcon = Icons.Default.MusicNote,
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = "Note thought (e.g. Current mood)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_text_input")
                )
            }
        },
        confirmButton = {
            GlassButton(
                onClick = {
                    onSave(songTitle, artist, noteText, albumCoverUrl, spotifyUri, "")
                },
                text = "Share to Squad",
                isPrimary = true,
                modifier = Modifier.testTag("submit_music_note_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = if (isDark) Color(0xFF141724) else Color(0xFFFBFBFE),
        shape = RoundedCornerShape(22.dp)
    )

    if (showSpotifyPicker) {
        SpotifyTrackPickerDialog(
            onTrackSelected = { track ->
                songTitle = track.title
                artist = track.artist
                albumCoverUrl = track.albumArt
                spotifyUri = track.spotifyUri
                showSpotifyPicker = false
            },
            onDismiss = { showSpotifyPicker = false }
        )
    }
}
