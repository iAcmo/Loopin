package com.example.ui.direct

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Room
import com.example.data.model.RoomMessage
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LoopinAvatar
import com.example.ui.components.SpotifyGreen
import com.example.ui.theme.ChatThemeRegistry
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderStrong
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinLikeRed
import com.example.ui.theme.LoopinOnlineGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChatDetailsScreen(
    room: Room,
    messages: List<RoomMessage>,
    currentUserProfile: UserProfile?,
    onBack: () -> Unit,
    onOpenUserProfile: (username: String) -> Unit,
    onStartSearchInChat: () -> Unit,
    onUpdateTheme: (themeId: String) -> Unit,
    onSetDisappearingMessages: (durationSec: Long) -> Unit,
    onSetNickname: (targetUserId: String, nickname: String) -> Unit,
    onToggleMute: (isMuted: Boolean) -> Unit,
    onToggleBlock: (targetUserId: String, isBlocked: Boolean) -> Unit
) {
    BackHandler { onBack() }

    val isDark = LocalIsDarkTheme.current
    val currentUid = currentUserProfile?.userId ?: ""

    // Identify other participant for 1-to-1 DMs
    val otherUserId = room.memberIds.firstOrNull { it != currentUid } ?: ""
    val otherIndex = room.memberIds.indexOf(otherUserId)
    val otherUsername = if (otherIndex in room.memberUsernames.indices) {
        room.memberUsernames[otherIndex]
    } else {
        room.name.ifBlank { "User" }
    }
    val otherAvatarUrl = room.memberAvatars[otherUserId] ?: ""
    val assignedNickname = room.nicknames[otherUserId]
    val effectiveDisplayName = if (!assignedNickname.isNullOrBlank()) assignedNickname else otherUsername

    val isMuted = room.mutedByUserIds.contains(currentUid)
    val isBlocked = room.blockedUserIds.contains(otherUserId)

    var showNicknameDialog by remember { mutableStateOf(false) }
    var targetNicknameUserId by remember { mutableStateOf(otherUserId) }
    var targetNicknameUsername by remember { mutableStateOf(otherUsername) }
    var currentNicknameInput by remember { mutableStateOf(assignedNickname ?: "") }

    var showReportDialog by remember { mutableStateOf(false) }
    var reportSubmitted by remember { mutableStateOf(false) }
    var groupNameDisplay by remember(room.name) { mutableStateOf(room.name.ifBlank { "Group Squad" }) }
    var showEditGroupNameDialog by remember { mutableStateOf(false) }
    var showMembersInChat by remember { mutableStateOf(room.showMembers) }

    var mediaTab by remember { mutableIntStateOf(0) } // 0: Photos, 1: Music, 2: Voice

    val sharedImages = remember(messages) {
        messages.filter { (it.type == "image" || it.type == "instant") && it.mediaUrl.isNotBlank() && !it.isUnsent }
    }
    val sharedTracks = remember(messages) {
        messages.filter { it.type == "spotify" && it.songTitle.isNotBlank() && !it.isUnsent }
    }
    val sharedVoice = remember(messages) {
        messages.filter { it.type == "voice" && it.mediaUrl.isNotBlank() && !it.isUnsent }
    }

    LiquidGlassBackground {
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
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("details_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Participant Card (Identity Header)
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        isElevated = true
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (room.isGroup) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x337E74F1))
                                        .border(2.dp, LoopinIrisGlow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Group",
                                        tint = LoopinIrisGlow,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            } else {
                                LoopinAvatar(
                                    avatarUrl = otherAvatarUrl,
                                    size = 80.dp,
                                    borderWidth = 2.5.dp,
                                    borderBrush = GlassStrokeBrush,
                                    onClick = { onOpenUserProfile(otherUsername) }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (room.isGroup) groupNameDisplay else effectiveDisplayName,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (room.isGroup) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { showEditGroupNameDialog = true },
                                        modifier = Modifier.size(24.dp).testTag("edit_group_name_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Group Name",
                                            tint = LoopinIrisGlow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (!room.isGroup) {
                                Text(
                                    text = "@$otherUsername",
                                    fontSize = 13.sp,
                                    color = LoopinIrisGlow,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .clickable { onOpenUserProfile(otherUsername) }
                                )
                            } else {
                                Text(
                                    text = "${room.memberIds.size} participants",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Quick Action Buttons Row: Profile, Search, Mute
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                if (!room.isGroup) {
                                    QuickActionPill(
                                        icon = Icons.Default.Person,
                                        label = "Profile",
                                        onClick = { onOpenUserProfile(otherUsername) }
                                    )
                                }
                                QuickActionPill(
                                    icon = Icons.Default.Search,
                                    label = "Search",
                                    onClick = onStartSearchInChat
                                )
                                QuickActionPill(
                                    icon = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                    label = if (isMuted) "Unmute" else "Mute",
                                    isActive = isMuted,
                                    onClick = { onToggleMute(!isMuted) }
                                )
                            }
                        }
                    }
                }

                // Theme Selection Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = LoopinIrisGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Chat Theme",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Changes color gradient for message bubbles across all participants",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(ChatThemeRegistry.allThemes) { theme ->
                                    val isSelected = room.themeId == theme.id
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) Color(0x337E74F1) else Color.Transparent)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                                color = if (isSelected) LoopinIrisGlow else GlassBorder,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { onUpdateTheme(theme.id) }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(theme.bubbleBrush)
                                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = theme.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) LoopinIrisGlow else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Disappearing Messages Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = LoopinIrisGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Disappearing Messages",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "When turned on, newly sent messages will automatically vanish after the selected duration.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val options = listOf(
                                    Pair(0L, "Off"),
                                    Pair(86400L, "24h"),
                                    Pair(604800L, "7d"),
                                    Pair(-1L, "View Once")
                                )
                                options.forEach { (sec, label) ->
                                    val isSelected = room.disappearingDurationSec == sec
                                    GlassButton(
                                        onClick = { onSetDisappearingMessages(sec) },
                                        text = label,
                                        isPrimary = isSelected,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Nicknames Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = LoopinIrisGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nicknames",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Assign personalized names visible inside this conversation.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                            )

                            if (!room.isGroup) {
                                NicknameItemRow(
                                    username = otherUsername,
                                    avatarUrl = otherAvatarUrl,
                                    nickname = assignedNickname,
                                    onEdit = {
                                        targetNicknameUserId = otherUserId
                                        targetNicknameUsername = otherUsername
                                        currentNicknameInput = assignedNickname ?: ""
                                        showNicknameDialog = true
                                    }
                                )
                            } else {
                                room.memberIds.forEachIndexed { idx, mId ->
                                    val mUser = if (idx in room.memberUsernames.indices) room.memberUsernames[idx] else "User"
                                    val nick = room.nicknames[mId]
                                    NicknameItemRow(
                                        username = mUser,
                                        avatarUrl = room.memberAvatars[mId] ?: "",
                                        nickname = nick,
                                        onEdit = {
                                            targetNicknameUserId = mId
                                            targetNicknameUsername = mUser
                                            currentNicknameInput = nick ?: ""
                                            showNicknameDialog = true
                                        }
                                    )
                                    if (idx < room.memberIds.size - 1) {
                                        Divider(
                                            color = Color(0x14FFFFFF),
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Shared Content Tabs (Photos, Music, Voice)
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Shared in Chat",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            TabRow(
                                selectedTabIndex = mediaTab,
                                containerColor = Color.Transparent,
                                contentColor = LoopinIrisGlow,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[mediaTab]),
                                        color = LoopinIrisGlow,
                                        height = 2.dp
                                    )
                                }
                            ) {
                                Tab(
                                    selected = mediaTab == 0,
                                    onClick = { mediaTab = 0 },
                                    text = { Text("Photos (${sharedImages.size})", fontSize = 13.sp) }
                                )
                                Tab(
                                    selected = mediaTab == 1,
                                    onClick = { mediaTab = 1 },
                                    text = { Text("Music (${sharedTracks.size})", fontSize = 13.sp) }
                                )
                                Tab(
                                    selected = mediaTab == 2,
                                    onClick = { mediaTab = 2 },
                                    text = { Text("Voice (${sharedVoice.size})", fontSize = 13.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            when (mediaTab) {
                                0 -> {
                                    if (sharedImages.isEmpty()) {
                                        EmptySharedState(icon = Icons.Default.Photo, text = "No photos shared yet")
                                    } else {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(3),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(sharedImages) { msg ->
                                                AsyncImage(
                                                    model = msg.mediaUrl,
                                                    contentDescription = "Shared photo",
                                                    modifier = Modifier
                                                        .aspectRatio(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .border(0.5.dp, GlassBorder, RoundedCornerShape(10.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    if (sharedTracks.isEmpty()) {
                                        EmptySharedState(icon = Icons.Default.MusicNote, text = "No Spotify tracks shared yet")
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            sharedTracks.take(5).forEach { msg ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0x18FFFFFF))
                                                        .padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (msg.songCoverUrl.isNotBlank()) {
                                                        AsyncImage(
                                                            model = msg.songCoverUrl,
                                                            contentDescription = null,
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(6.dp)),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(SpotifyGreen),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.MusicNote,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = msg.songTitle,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = TextPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = msg.songArtist,
                                                            fontSize = 11.sp,
                                                            color = TextSecondary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    if (sharedVoice.isEmpty()) {
                                        EmptySharedState(icon = Icons.Default.Mic, text = "No voice messages shared yet")
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            sharedVoice.take(5).forEach { msg ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0x18FFFFFF))
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Mic,
                                                        contentDescription = null,
                                                        tint = LoopinIrisGlow,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "Voice message (${msg.audioDurationSec}s)",
                                                        fontSize = 13.sp,
                                                        color = TextPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Privacy, Safety & Moderation Controls
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Privacy & Support",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (!room.isGroup) {
                                PrivacyActionRow(
                                    icon = Icons.Default.Block,
                                    label = if (isBlocked) "Unblock @$otherUsername" else "Block @$otherUsername",
                                    color = LoopinLikeRed,
                                    onClick = { onToggleBlock(otherUserId, !isBlocked) }
                                )
                                Divider(
                                    color = Color(0x14FFFFFF),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            PrivacyActionRow(
                                icon = Icons.Default.Flag,
                                label = "Report Conversation",
                                color = LoopinLikeRed,
                                onClick = { showReportDialog = true }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }

        // Nickname Edit Dialog
        if (showNicknameDialog) {
            AlertDialog(
                onDismissRequest = { showNicknameDialog = false },
                containerColor = Color(0xFF141728),
                title = { Text("Set Nickname for @$targetNicknameUsername", color = TextPrimary) },
                text = {
                    Column {
                        Text(
                            text = "Nicknames are only visible within this conversation.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        GlassTextField(
                            value = currentNicknameInput,
                            onValueChange = { currentNicknameInput = it },
                            label = "Nickname"
                        )
                    }
                },
                confirmButton = {
                    GlassButton(
                        onClick = {
                            onSetNickname(targetNicknameUserId, currentNicknameInput.trim())
                            showNicknameDialog = false
                        },
                        text = "Save",
                        isPrimary = true
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showNicknameDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Report Dialog
        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                containerColor = Color(0xFF141728),
                title = { Text("Report Conversation", color = TextPrimary) },
                text = {
                    Text(
                        text = "Are you sure you want to report this conversation for spam, harassment, or policy violation? Our safety team will review recent messages.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    GlassButton(
                        onClick = {
                            showReportDialog = false
                            reportSubmitted = true
                        },
                        text = "Submit Report",
                        isPrimary = true
                    )
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Report Confirmation Alert
        if (reportSubmitted) {
            AlertDialog(
                onDismissRequest = { reportSubmitted = false },
                containerColor = Color(0xFF141728),
                title = { Text("Report Received", color = TextPrimary) },
                text = {
                    Text(
                        text = "Thank you. Your report has been submitted to the moderation team.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    GlassButton(
                        onClick = { reportSubmitted = false },
                        text = "Done",
                        isPrimary = true
                    )
                }
            )
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0x337E74F1) else Color(0x18FFFFFF))
                .border(
                    1.dp,
                    if (isActive) LoopinIrisGlow else Color(0x28FFFFFF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) LoopinIrisGlow else TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive) LoopinIrisGlow else TextSecondary
        )
    }
}

@Composable
private fun NicknameItemRow(
    username: String,
    avatarUrl: String,
    nickname: String?,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoopinAvatar(avatarUrl = avatarUrl, size = 38.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (!nickname.isNullOrBlank()) nickname else username,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = if (!nickname.isNullOrBlank()) "Official: @$username" else "Tap to set nickname",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Nickname",
                tint = LoopinIrisGlow,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PrivacyActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun EmptySharedState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = text, fontSize = 12.sp, color = TextMuted)
        }
    }
}
