package com.example.ui.direct

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import com.example.ui.components.LoopinAvatar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.Room
import com.example.data.model.RoomMessage
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.SpotifyGreen
import com.example.ui.components.SpotifyTrackPickerDialog
import com.example.ui.theme.ChatThemeDefinition
import com.example.ui.theme.ChatThemeRegistry
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.GlassStrokeBrush
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

val QUICK_REACTIONS = listOf("❤️", "🔥", "😂", "👏", "😮", "👍")

val MESSAGE_EFFECTS = listOf(
    Pair("", "Normal"),
    Pair("sparkle", "✨ Sparkle"),
    Pair("fire", "🔥 Flame"),
    Pair("iris_glow", "💜 Liquid Glow"),
    Pair("whisper", "💬 Whisper"),
    Pair("loud", "📢 Loud"),
    Pair("invisible", "🤫 Invisible")
)

fun formatMessageTime(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    return try {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(timestamp.toDate())
    } catch (_: Exception) {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    room: Room,
    messages: List<RoomMessage>,
    currentUserProfile: UserProfile?,
    onSendMessage: (
        text: String,
        type: String,
        mediaUrl: String,
        effect: String,
        songTitle: String,
        songArtist: String,
        songCoverUrl: String,
        spotifyTrackUri: String,
        audioDurationSec: Int,
        replyToMessageId: String,
        replyToText: String,
        replyToSenderName: String
    ) -> Unit,
    onToggleReaction: (messageId: String, currentReactions: List<String>, reactionEmoji: String) -> Unit,
    onUnsendMessage: (messageId: String) -> Unit = {},
    onDeleteForYou: (messageId: String) -> Unit = {},
    onForwardMessage: (targetRoom: Room, message: RoomMessage) -> Unit = { _, _ -> },
    availableRooms: List<Room> = emptyList(),
    onOpenUserProfile: (username: String) -> Unit = {},
    onOpenDetails: () -> Unit = {},
    onMarkMessageViewed: (messageId: String) -> Unit = {},
    onUserTyping: (isTyping: Boolean) -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler {
        onUserTyping(false)
        onBack()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDark = LocalIsDarkTheme.current
    val currentUserId = currentUserProfile?.userId ?: ""
    val myUsername = currentUserProfile?.displayName?.ifBlank { currentUserProfile.username } ?: currentUserProfile?.username ?: ""

    // Other participant resolution for 1-to-1 DMs
    val otherUserId = remember(room.memberIds, currentUserId) {
        room.memberIds.firstOrNull { it != currentUserId } ?: ""
    }
    val otherIndex = remember(room.memberIds, otherUserId) {
        room.memberIds.indexOf(otherUserId)
    }
    val otherUsername = remember(room.memberUsernames, otherIndex, room.name) {
        if (otherIndex in room.memberUsernames.indices) {
            room.memberUsernames[otherIndex]
        } else {
            room.memberUsernames.firstOrNull { it != myUsername && it != (currentUserProfile?.username ?: "") }
                ?: room.name.ifBlank { "User" }
        }
    }
    val otherAvatarUrl = remember(room.memberAvatars, otherUserId) {
        room.memberAvatars[otherUserId] ?: ""
    }
    val assignedNickname = room.nicknames[otherUserId]
    val effectiveDisplayName = if (!assignedNickname.isNullOrBlank()) assignedNickname else otherUsername

    val chatTheme = remember(room.themeId) {
        ChatThemeRegistry.getThemeById(room.themeId)
    }

    val otherTypingUsernames = remember(room.typingUserIds, room.typingUsernames, currentUserId, myUsername) {
        val list = mutableListOf<String>()
        for (i in room.typingUsernames.indices) {
            val uid = room.typingUserIds.getOrNull(i) ?: ""
            val name = room.typingUsernames[i]
            if (uid != currentUserId && name != myUsername && name != (currentUserProfile?.username ?: "")) {
                list.add(name)
            }
        }
        if (list.isEmpty() && room.typingUserIds.any { it != currentUserId }) {
            val fallback = room.typingUsernames.filter { it != myUsername && it != (currentUserProfile?.username ?: "") }
            if (fallback.isNotEmpty()) fallback else listOf(if (room.isGroup) "Someone" else effectiveDisplayName)
        } else {
            list.distinct()
        }
    }

    var inputText by remember { mutableStateOf("") }
    var selectedEffect by remember { mutableStateOf("") }
    var showEffectPicker by remember { mutableStateOf(false) }
    var showSpotifyPicker by remember { mutableStateOf(false) }
    var showMoreTools by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    // Reply state
    var replyingToMessage by remember { mutableStateOf<RoomMessage?>(null) }

    // Message action sheet state
    var activeActionMessage by remember { mutableStateOf<RoomMessage?>(null) }
    var showForwardDialog by remember { mutableStateOf(false) }

    val isRecording by LoopinAudioController.isRecording.collectAsState()
    val recordingDurationSec by LoopinAudioController.recordingDurationSec.collectAsState()

    // Timer for recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            var count = 0
            while (isRecording) {
                delay(1000)
                count++
                LoopinAudioController.updateDuration(count)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            LoopinAudioController.stopPlayback()
            onUserTyping(false)
        }
    }

    val listState = rememberLazyListState()

    // Auto-scroll to latest message or typing indicator
    LaunchedEffect(messages.size, messages.lastOrNull()?.id, otherTypingUsernames.isNotEmpty()) {
        if (messages.isNotEmpty()) {
            val targetIndex = if (otherTypingUsernames.isNotEmpty()) messages.size else messages.size - 1
            listState.animateScrollToItem(targetIndex.coerceAtLeast(0))
        }
    }

    // Photo Picker contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSendMessage(
                "Shared an image",
                "image",
                uri.toString(),
                "",
                "",
                "",
                "",
                "",
                0,
                replyingToMessage?.id ?: "",
                replyingToMessage?.text ?: "",
                replyingToMessage?.senderName ?: ""
            )
            replyingToMessage = null
        }
    }

    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "chat_cam_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                onSendMessage(
                    "Captured photo",
                    "image",
                    file.toURI().toString(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    0,
                    replyingToMessage?.id ?: "",
                    replyingToMessage?.text ?: "",
                    replyingToMessage?.senderName ?: ""
                )
                replyingToMessage = null
            } catch (_: Exception) {}
        }
    }

    // Permission launcher for Mic
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            LoopinAudioController.startRecording(context)
        }
    }

    // Permission launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    LiquidGlassBackground {
        if (chatTheme.id != ChatThemeRegistry.IrisGlow.id) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                chatTheme.accentColor.copy(alpha = if (isDark) 0.16f else 0.10f),
                                chatTheme.glowColor.copy(alpha = if (isDark) 0.08f else 0.04f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            // Frosted Room Header with Online Status Indicator (pinned at top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0x20000000))
                    .border(width = 0.5.dp, color = Color(0x18FFFFFF), shape = RoundedCornerShape(0.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("room_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                if (room.isGroup) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x337E74F1))
                            .border(1.dp, LoopinIrisGlow, CircleShape)
                            .clickable { onOpenDetails() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Group Squad",
                            tint = LoopinIrisGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    LoopinAvatar(
                        avatarUrl = otherAvatarUrl,
                        size = 38.dp,
                        onClick = onOpenDetails
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenDetails() }
                        .testTag("chat_header_details_target")
                ) {
                    Text(
                        text = if (room.isGroup) room.name.ifBlank { "Group Squad" } else effectiveDisplayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (otherTypingUsernames.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val infiniteTransition = rememberInfiniteTransition(label = "header_typing")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.35f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "header_typing_alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(LoopinIrisGlow.copy(alpha = alpha))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (room.isGroup) {
                                    "${otherTypingUsernames.joinToString(", ")} is typing..."
                                } else {
                                    "typing..."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LoopinIrisGlow
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LoopinOnlineGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (room.isGroup) "${room.memberIds.size} members • Active" else "@$otherUsername • Active now",
                                fontSize = 11.sp,
                                color = LoopinOnlineGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Spotify Music button shortcut in header
                IconButton(
                    onClick = { showSpotifyPicker = true },
                    modifier = Modifier.testTag("header_spotify_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x221DB954))
                            .border(1.dp, SpotifyGreen.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Spotify",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Conversation Details shortcut button
                IconButton(
                    onClick = onOpenDetails,
                    modifier = Modifier.testTag("chat_header_details_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Details",
                        tint = LoopinIrisGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Disappearing Messages Banner indicator
            if (room.disappearingDurationSec != 0L) {
                val bannerLabel = when (room.disappearingDurationSec) {
                    -1L -> "⚡ View Once active: messages vanish after viewing"
                    86400L -> "⚡ Disappearing messages: 24 hours"
                    604800L -> "⚡ Disappearing messages: 7 days"
                    else -> "⚡ Disappearing messages: ${room.disappearingDurationSec / 3600} hours"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x287E74F1))
                        .border(0.5.dp, LoopinIrisGlow.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { onOpenDetails() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = bannerLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = LoopinIrisGlow
                    )
                }
            }

            val lastSentByMeId = remember(messages, currentUserId) {
                messages.filter { it.senderId == currentUserId }.lastOrNull()?.id
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isMe = message.senderId == currentUserId
                    val isLastSentByMe = isMe && (message.id == lastSentByMeId)
                    FrostedMessageBubble(
                        message = message,
                        isMe = isMe,
                        isLastSentByMe = isLastSentByMe,
                        recipientName = if (!room.isGroup) effectiveDisplayName else "",
                        currentUserId = currentUserId,
                        chatTheme = chatTheme,
                        onImageClick = { fullScreenImageUrl = it },
                        onReactionSelected = { emoji ->
                            onToggleReaction(message.id, message.reactions, emoji)
                        },
                        onMessageAction = { activeActionMessage = message },
                        onUserClick = { username -> onOpenUserProfile(username) },
                        onMarkViewOnceOpened = onMarkMessageViewed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (otherTypingUsernames.isNotEmpty()) {
                    item {
                        FrostedTypingBubble(
                            typingUsers = otherTypingUsernames,
                            isGroup = room.isGroup
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Message Effect selector
            AnimatedVisibility(visible = showEffectPicker) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x26000000))
                        .border(0.5.dp, GlassBorder, RoundedCornerShape(0.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Message Effect:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoopinIrisGlow,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MESSAGE_EFFECTS) { effect ->
                            val isSelected = selectedEffect == effect.first
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0x337E74F1) else Color(0x18FFFFFF))
                                    .border(
                                        1.dp,
                                        if (isSelected) LoopinIrisGlow else GlassBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedEffect = effect.first
                                        showEffectPicker = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = effect.second,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Live Voice Recording State Banner
            if (isRecording) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33FF4869))
                        .border(1.dp, Color(0x55FF4869), RoundedCornerShape(0.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(LoopinLikeRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Recording Voice Note... 0:${recordingDurationSec.toString().padStart(2, '0')}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = { LoopinAudioController.cancelRecording() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Cancel Recording",
                                tint = LoopinLikeRed
                            )
                        }

                        IconButton(
                            onClick = {
                                val result = LoopinAudioController.stopRecording()
                                if (result != null) {
                                    onSendMessage(
                                        "Voice Message",
                                        "voice",
                                        result.filePath,
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        result.durationSeconds,
                                        replyingToMessage?.id ?: "",
                                        replyingToMessage?.text ?: "",
                                        replyingToMessage?.senderName ?: ""
                                    )
                                    replyingToMessage = null
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Voice Note",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Keyboard-aware Bottom Input Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Reply Preview Bar
                AnimatedVisibility(visible = replyingToMessage != null) {
                    replyingToMessage?.let { replyTarget ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x287E74F1))
                                .border(0.5.dp, LoopinIrisGlow.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(28.dp)
                                        .background(chatTheme.accentColor, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Replying to ${replyTarget.senderName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LoopinIrisGlow
                                    )
                                    Text(
                                        text = replyTarget.text.ifBlank { "Media attachment" },
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { replyingToMessage = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel reply",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // More Media Tools drawer (when "+" tapped)
                AnimatedVisibility(visible = showMoreTools) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1F000000))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Camera
                        IconButton(
                            onClick = {
                                showMoreTools = false
                                val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permission == PackageManager.PERMISSION_GRANTED) {
                                    cameraLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = LoopinIrisGlow
                            )
                        }

                        // Gallery
                        IconButton(
                            onClick = {
                                showMoreTools = false
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = LoopinIrisGlow
                            )
                        }

                        // Mic
                        IconButton(
                            onClick = {
                                showMoreTools = false
                                val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                if (permission == PackageManager.PERMISSION_GRANTED) {
                                    LoopinAudioController.startRecording(context)
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice",
                                tint = LoopinSilver
                            )
                        }

                        // Effects
                        IconButton(
                            onClick = {
                                showMoreTools = false
                                showEffectPicker = !showEffectPicker
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Effects",
                                tint = if (selectedEffect.isNotBlank()) LoopinIrisGlow else TextMuted
                            )
                        }

                        // Spotify
                        IconButton(
                            onClick = {
                                showMoreTools = false
                                showSpotifyPicker = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Spotify",
                                tint = SpotifyGreen
                            )
                        }
                    }
                }

                // Frosted Bottom Input Bar (auto-scaling with keyboard)
                Divider(color = Color(0x18FFFFFF), thickness = 0.5.dp)
                if (isRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x28000000))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0x33FF334B))
                                .border(1.dp, Color(0x66FF334B), RoundedCornerShape(22.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val recTransition = rememberInfiniteTransition(label = "recording_pulse")
                            val recAlpha by recTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "rec_alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = recAlpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val min = recordingDurationSec / 60
                            val sec = recordingDurationSec % 60
                            Text(
                                text = String.format("%d:%02d", min, sec),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val waveHeights = listOf(6, 14, 22, 10, 18, 24, 12, 16, 8, 20, 14, 6)
                                waveHeights.forEachIndexed { idx, h ->
                                    val waveAlpha by recTransition.animateFloat(
                                        initialValue = 0.4f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(400, delayMillis = idx * 40, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "wave_$idx"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.5.dp)
                                            .height(h.dp)
                                            .background(LoopinIrisGlow.copy(alpha = waveAlpha), RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { LoopinAudioController.cancelRecording() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel recording",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val result = LoopinAudioController.stopRecording()
                                if (result != null) {
                                    onSendMessage(
                                        "Voice message (${result.durationSeconds}s)",
                                        "voice",
                                        result.filePath,
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        result.durationSeconds,
                                        replyingToMessage?.id ?: "",
                                        replyingToMessage?.text ?: "",
                                        replyingToMessage?.senderName ?: ""
                                    )
                                    replyingToMessage = null
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(chatTheme.bubbleBrush)
                                .testTag("send_voice_note_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send voice note",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x28000000))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Expand/Collapse Tools Button
                        IconButton(
                            onClick = { showMoreTools = !showMoreTools },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("chat_tools_toggle")
                        ) {
                            Icon(
                                imageVector = if (showMoreTools) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Attach media",
                                tint = LoopinIrisGlow,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Direct Quick Camera shortcut (only when input is blank to preserve width)
                        if (inputText.isBlank()) {
                            IconButton(
                                onClick = {
                                    val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    if (permission == PackageManager.PERMISSION_GRANTED) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Take Photo",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Pick Image",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Input TextField - takes full remaining width, supports multiline
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = {
                                inputText = it
                                onUserTyping(it.isNotBlank())
                            },
                            placeholder = {
                                Text(
                                    text = if (selectedEffect.isNotBlank()) "Message with effect..." else "Message...",
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            maxLines = 4,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0x1AFFFFFF),
                                unfocusedContainerColor = Color(0x0EFFFFFF),
                                focusedBorderColor = LoopinIrisGlow,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Send Button or Mic button when empty
                        if (inputText.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onUserTyping(false)
                                    onSendMessage(
                                        inputText.trim(),
                                        "text",
                                        "",
                                        selectedEffect,
                                        "",
                                        "",
                                        "",
                                        "",
                                        0,
                                        replyingToMessage?.id ?: "",
                                        replyingToMessage?.text ?: "",
                                        replyingToMessage?.senderName ?: ""
                                    )
                                    inputText = ""
                                    selectedEffect = ""
                                    showEffectPicker = false
                                    replyingToMessage = null
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(chatTheme.bubbleBrush)
                                    .testTag("send_chat_message_button")
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_send_up_arrow),
                                    contentDescription = "Send message",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                    if (permission == PackageManager.PERMISSION_GRANTED) {
                                        LoopinAudioController.startRecording(context)
                                    } else {
                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice note",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Spotify Track Picker Dialog
        if (showSpotifyPicker) {
            SpotifyTrackPickerDialog(
                onTrackSelected = { track ->
                    onSendMessage(
                        "Shared Spotify track: ${track.title}",
                        "spotify",
                        track.albumArt,
                        "",
                        track.title,
                        track.artist,
                        track.albumArt,
                        track.spotifyUri,
                        0,
                        replyingToMessage?.id ?: "",
                        replyingToMessage?.text ?: "",
                        replyingToMessage?.senderName ?: ""
                    )
                    replyingToMessage = null
                    showSpotifyPicker = false
                },
                onDismiss = { showSpotifyPicker = false }
            )
        }

        // Full Screen Image Lightbox with Download functionality
        if (fullScreenImageUrl != null) {
            val targetUrl = fullScreenImageUrl!!
            Dialog(onDismissRequest = { fullScreenImageUrl = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xE608090E)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { downloadChatImage(context, targetUrl) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .testTag("download_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Image",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { fullScreenImageUrl = null },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        AsyncImage(
                            model = targetUrl,
                            contentDescription = "Full image",
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlassButton(
                            onClick = { downloadChatImage(context, targetUrl) },
                            text = "Download to Photos 💾",
                            leadingIcon = Icons.Default.Download,
                            isPrimary = true
                        )
                    }
                }
            }
        }

        // Message Actions Bottom Sheet (Reply, Forward, Unsend, Delete for you, Copy)
        if (activeActionMessage != null) {
            val targetMsg = activeActionMessage!!
            val isMyMsg = targetMsg.senderId == (currentUserProfile?.userId ?: "")
            val canUnsend = isMyMsg && !targetMsg.isUnsent

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { activeActionMessage = null },
                sheetState = sheetState,
                containerColor = Color(0xFF141728)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Message Actions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Reactions row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QUICK_REACTIONS.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        onToggleReaction(targetMsg.id, targetMsg.reactions, emoji)
                                        activeActionMessage = null
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Divider(
                        color = Color(0x18FFFFFF),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // 1. Reply
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                replyingToMessage = targetMsg
                                activeActionMessage = null
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = "Reply",
                            tint = LoopinIrisGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Reply", fontSize = 15.sp, color = TextPrimary)
                    }

                    // 2. Forward
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showForwardDialog = true
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward,
                            contentDescription = "Forward",
                            tint = LoopinIrisGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Forward message", fontSize = 15.sp, color = TextPrimary)
                    }

                    // 3. Copy text
                    if (targetMsg.text.isNotBlank() && !targetMsg.isUnsent) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(targetMsg.text))
                                    activeActionMessage = null
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = LoopinSilver,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Copy text", fontSize = 15.sp, color = TextPrimary)
                        }
                    }

                    // 4. Unsend (only available within 30 min)
                    if (canUnsend) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onUnsendMessage(targetMsg.id)
                                    activeActionMessage = null
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Unsend",
                                tint = LoopinLikeRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Unsend message", fontSize = 15.sp, color = LoopinLikeRed, fontWeight = FontWeight.SemiBold)
                                Text("Removes this message for everyone", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }

                    // 5. Delete for you
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onDeleteForYou(targetMsg.id)
                                activeActionMessage = null
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Delete for you", fontSize = 15.sp, color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Forward Dialog
        if (showForwardDialog && activeActionMessage != null) {
            val targetMsg = activeActionMessage!!
            Dialog(onDismissRequest = {
                showForwardDialog = false
                activeActionMessage = null
            }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141728))
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Forward to...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            IconButton(onClick = {
                                showForwardDialog = false
                                activeActionMessage = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (availableRooms.isEmpty()) {
                            Text(
                                text = "No other chat rooms found to forward to.",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        } else {
                            LazyColumn(modifier = Modifier.height(240.dp)) {
                                items(availableRooms) { r ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                onForwardMessage(r, targetMsg)
                                                showForwardDialog = false
                                                activeActionMessage = null
                                            }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x287E74F1)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = r.name.take(2).uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LoopinIrisGlow
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = r.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
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
}

fun downloadChatImage(context: Context, imageUrl: String) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val loader = coil.ImageLoader(context)
            val request = coil.request.ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = (loader.execute(request) as? coil.request.SuccessResult)?.drawable
            val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap
            if (bitmap != null) {
                val filename = "loopin_chat_${System.currentTimeMillis()}.jpg"
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Loopin")
                        put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                    }
                }
            }
        } catch (_: Exception) {}
    }
}

@Composable
fun FrostedTypingBubble(
    typingUsers: List<String>,
    isGroup: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 0, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 360, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            if (isGroup && typingUsers.isNotEmpty()) {
                Text(
                    text = "${typingUsers.joinToString(", ")} is typing...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = LoopinIrisGlow,
                    modifier = Modifier.padding(start = 10.dp, bottom = 3.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .background(Color(0x287E74F1))
                    .border(1.dp, LoopinIrisGlow.copy(alpha = 0.45f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .graphicsLayer {
                                scaleX = dot1Scale
                                scaleY = dot1Scale
                            }
                            .clip(CircleShape)
                            .background(LoopinIrisGlow)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .graphicsLayer {
                                scaleX = dot2Scale
                                scaleY = dot2Scale
                            }
                            .clip(CircleShape)
                            .background(LoopinIrisGlow)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .graphicsLayer {
                                scaleX = dot3Scale
                                scaleY = dot3Scale
                            }
                            .clip(CircleShape)
                            .background(LoopinIrisGlow)
                    )
                }
            }
        }
    }
}

@Composable
fun FrostedMessageBubble(
    message: RoomMessage,
    isMe: Boolean,
    isLastSentByMe: Boolean = false,
    recipientName: String = "",
    currentUserId: String,
    chatTheme: ChatThemeDefinition = ChatThemeRegistry.IrisGlow,
    onImageClick: (String) -> Unit,
    onReactionSelected: (String) -> Unit,
    onMessageAction: () -> Unit,
    onUserClick: (username: String) -> Unit = {},
    onMarkViewOnceOpened: (messageId: String) -> Unit = {}
) {
    val context = LocalContext.current
    var isRevealed by remember { mutableStateOf(false) }

    val playingId by LoopinAudioController.playingId.collectAsState()
    val isPlayingThis = playingId == message.id

    val effect = message.effect
    val isSparkle = effect == "sparkle"
    val isFire = effect == "fire"
    val isIrisGlow = effect == "iris_glow"
    val isWhisper = effect == "whisper"
    val isLoud = effect == "loud"
    val isInvisible = effect == "invisible"

    val infiniteTransition = rememberInfiniteTransition(label = "bubble")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe && message.senderName.isNotBlank()) {
            Text(
                text = message.senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LoopinIrisGlow,
                modifier = Modifier
                    .padding(start = 4.dp, bottom = 2.dp)
                    .clickable { onUserClick(message.senderName) }
            )
        }

        Row(
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isMe) {
                AsyncImage(
                    model = message.senderAvatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, GlassBorder, CircleShape)
                        .padding(bottom = 2.dp)
                        .clickable { onUserClick(message.senderName) },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Bubble container
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMe) 18.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 18.dp
                        )
                    )
                    .background(
                        when {
                            message.isUnsent -> Color(0x14FFFFFF)
                            isFire -> Color(0x40FF6A00)
                            isSparkle -> Color(0x3D9B51E0)
                            isIrisGlow -> chatTheme.accentColor.copy(alpha = 0.45f)
                            isMe -> chatTheme.accentColor.copy(alpha = 0.35f)
                            else -> Color(0x18FFFFFF)
                        }
                    )
                    .border(
                        width = if (isSparkle || isFire || isIrisGlow || isMe) 1.5.dp else 1.dp,
                        brush = when {
                            message.isUnsent -> Brush.linearGradient(listOf(Color(0x22FFFFFF), Color(0x11FFFFFF)))
                            isFire -> Brush.linearGradient(listOf(Color(0xFFFF9500), Color(0xFFFF3B30)))
                            isSparkle -> Brush.linearGradient(listOf(Color(0xFFFFD700), chatTheme.glowColor))
                            isIrisGlow -> chatTheme.bubbleBrush
                            isMe -> chatTheme.bubbleBrush
                            else -> GlassStrokeBrush
                        },
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMe) 18.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 18.dp
                        )
                    )
                    .clickable {
                        if (isInvisible && !isRevealed) {
                            isRevealed = true
                        } else {
                            onMessageAction()
                        }
                    }
                    .padding(
                        horizontal = if (isLoud) 18.dp else 14.dp,
                        vertical = if (isLoud) 14.dp else 10.dp
                    )
            ) {
                Column {
                    // Forwarded badge
                    if (message.isForwarded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward,
                                contentDescription = "Forwarded",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Forwarded",
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color = TextMuted
                            )
                        }
                    }

                    // Replied quote container
                    if (message.replyToText.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22000000))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(24.dp)
                                    .background(LoopinIrisGlow, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSenderName.ifBlank { "Original" },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LoopinIrisGlow
                                )
                                Text(
                                    text = message.replyToText,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Unsent message notification
                    if (message.isUnsent) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "This message was unsent",
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = TextMuted
                            )
                        }
                    } else {
                        // Effect badge if applicable
                        if (effect.isNotBlank()) {
                            Text(
                                text = when (effect) {
                                    "sparkle" -> "✨ SPARKLE"
                                    "fire" -> "🔥 FLAME"
                                    "iris_glow" -> "💜 GLOW"
                                    "whisper" -> "💬 WHISPER"
                                    "loud" -> "📢 SHOUT"
                                    "invisible" -> "🤫 SECRET"
                                    else -> ""
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (effect) {
                                    "fire" -> Color(0xFFFF9500)
                                    "sparkle" -> Color(0xFFFFD700)
                                    else -> LoopinIrisGlow
                                },
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        // 1. Instant / Image Message
                        if ((message.type == "instant" || message.type == "image") && message.mediaUrl.isNotBlank()) {
                            if (message.isViewOnce) {
                                if (message.isViewed) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x18FFFFFF))
                                            .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isMe) "Opened" else "Opened • View once",
                                            fontSize = 13.sp,
                                            color = TextMuted,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x287E74F1))
                                            .border(1.dp, LoopinIrisGlow, RoundedCornerShape(12.dp))
                                            .clickable {
                                                onImageClick(message.mediaUrl)
                                                if (!isMe) {
                                                    onMarkViewOnceOpened(message.id)
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = LoopinIrisGlow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isMe) "Photo • View once" else "Photo • Tap to view",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(0.75.dp, GlassBorder, RoundedCornerShape(12.dp))
                                        .clickable { onImageClick(message.mediaUrl) }
                                        .padding(bottom = 4.dp)
                                ) {
                                    AsyncImage(
                                        model = message.mediaUrl,
                                        contentDescription = "Image preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (message.type == "instant") {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xCC7E74F1))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                .align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "INSTANT ⚡",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { downloadChatImage(context, message.mediaUrl) },
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x88000000))
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Download Image",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Spotify Music Message
                        if (message.type == "spotify") {
                            Column(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x24000000))
                                    .border(1.dp, Color(0x331DB954), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = message.songCoverUrl.ifBlank { message.mediaUrl },
                                        contentDescription = message.songTitle,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(0.75.dp, GlassBorder, RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = message.songTitle.ifBlank { "Track" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = message.songArtist.ifBlank { "Spotify Artist" },
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x221DB954))
                                        .clickable {
                                            SpotifyMusicService.openInSpotify(
                                                context,
                                                message.spotifyTrackUri,
                                                "https://open.spotify.com"
                                            )
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Play on Spotify",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreen
                                    )
                                }
                            }
                        }

                        // 3. Voice Message Bubble
                        if (message.type == "voice") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .width(200.dp)
                                    .padding(vertical = 4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        LoopinAudioController.togglePlay(
                                            id = message.id,
                                            songTitle = "Voice Note",
                                            artist = message.senderName,
                                            audioUrl = message.mediaUrl,
                                            context = context
                                        )
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlayingThis) LoopinIrisGlow else Color(0x33FFFFFF))
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause Voice",
                                        tint = if (isPlayingThis) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val heights = listOf(8, 16, 22, 14, 28, 18, 12, 24, 16, 10, 20, 14)
                                    heights.forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(h.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (isPlayingThis) LoopinIrisGlow else Color(0x66FFFFFF))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "0:${message.audioDurationSec.toString().padStart(2, '0')}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // 4. Regular Text Message
                        if (message.type == "text" || message.text.isNotBlank()) {
                            if (isInvisible && !isRevealed) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x33FFFFFF))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Tap to reveal secret message 🤫",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = LoopinSilver
                                    )
                                }
                            } else {
                                Text(
                                    text = message.text,
                                    fontSize = when {
                                        isLoud -> 18.sp
                                        isWhisper -> 11.sp
                                        else -> 14.sp
                                    },
                                    fontWeight = if (isLoud) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (isWhisper) FontStyle.Italic else FontStyle.Normal,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // Timestamp and Sent / Seen status row
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val timeStr = formatMessageTime(message.createdAt)
                        if (timeStr.isNotBlank()) {
                            Text(
                                text = timeStr,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        if (isMe && !message.isUnsent) {
                            val isSeen = message.status == "seen" || message.seenByUserIds.any { it != currentUserId }
                            if (isSeen) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Seen",
                                        tint = LoopinIrisGlow,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    val seenTime = formatMessageTime(message.seenAt)
                                    Text(
                                        text = if (seenTime.isNotBlank()) "Seen $seenTime" else "Seen",
                                        fontSize = 9.sp,
                                        color = LoopinIrisGlow
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sent",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Sent",
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reactions Display below bubble
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = if (isMe) 0.dp else 34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x28FFFFFF))
                    .border(0.75.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                message.reactions.distinct().forEach { reaction ->
                    Text(text = reaction, fontSize = 11.sp)
                }
            }
        }

        // Prominent Read Receipt for latest sent message
        if (isLastSentByMe && isMe && !message.isUnsent) {
            val isSeen = message.status == "seen" || message.seenByUserIds.any { it != currentUserId }
            if (isSeen) {
                val seenTime = formatMessageTime(message.seenAt)
                val readLabel = if (recipientName.isNotBlank()) {
                    if (seenTime.isNotBlank()) "Seen by $recipientName • $seenTime" else "Seen by $recipientName"
                } else {
                    if (seenTime.isNotBlank()) "Seen • $seenTime" else "Seen"
                }
                Row(
                    modifier = Modifier
                        .padding(top = 3.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Read receipt",
                        tint = LoopinIrisGlow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = readLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = LoopinIrisGlow
                    )
                }
            }
        }
    }
}
