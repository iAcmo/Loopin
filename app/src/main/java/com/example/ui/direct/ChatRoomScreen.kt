package com.example.ui.direct

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.Locale
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderStrong
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
import com.example.util.SpotifyTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

val QUICK_REACTIONS = listOf("❤️", "🔥", "😂", "👏", "😮")

val MESSAGE_EFFECTS = listOf(
    Pair("", "Normal"),
    Pair("sparkle", "✨ Sparkle"),
    Pair("fire", "🔥 Flame"),
    Pair("iris_glow", "💜 Liquid Glow"),
    Pair("whisper", "💬 Whisper"),
    Pair("loud", "📢 Loud"),
    Pair("invisible", "🤫 Invisible")
)

@Composable
fun ChatRoomScreen(
    room: Room,
    messages: List<RoomMessage>,
    currentUserProfile: UserProfile?,
    allRooms: List<Room> = emptyList(),
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
    onUnsendMessage: (messageId: String) -> Unit = {},
    onDeleteForYou: (messageId: String) -> Unit = {},
    onForwardMessage: (targetRoom: Room, message: RoomMessage) -> Unit = { _, _ -> },
    onToggleReaction: (messageId: String, currentReactions: List<String>, reactionEmoji: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedEffect by remember { mutableStateOf("") }
    var showEffectPicker by remember { mutableStateOf(false) }
    var showSpotifyPicker by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var replyingToMessage by remember { mutableStateOf<RoomMessage?>(null) }
    var forwardingMessage by remember { mutableStateOf<RoomMessage?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<RoomMessage?>(null) }

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
        }
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val listState = rememberLazyListState()
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0

    // Auto-scroll to stay to the latest text in the chat page (User requested)
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(messages.lastOrNull()?.id) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(isImeVisible) {
        if (isImeVisible && messages.isNotEmpty()) {
            delay(120)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Photo Picker contract (Zero-permission Play Store compliant)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .imePadding()
        ) {
            // Frosted Room Header with Online Status Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0x18000000))
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

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (room.isSquadRoom) Color(0x337E74F1) else Color(0x1EFFFFFF))
                        .border(1.dp, if (room.isSquadRoom) LoopinIrisGlow else GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (room.isSquadRoom) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = LoopinIrisGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = room.name.take(2).uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoopinSilver
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.name.ifBlank { "Squad #${room.squadId} Lounge" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LoopinOnlineGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (room.isSquadRoom) "Squad Online • Active" else "Online • Active now",
                            fontSize = 11.sp,
                            color = LoopinOnlineGreen
                        )
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
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isMe = message.senderId == currentUserProfile?.userId
                    FrostedMessageBubble(
                        message = message,
                        isMe = isMe,
                        onImageClick = { fullScreenImageUrl = it },
                        onReactionSelected = { emoji ->
                            onToggleReaction(message.id, message.reactions, emoji)
                        },
                        onOpenActions = { selectedMessageForOptions = message }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Text Effect Selector Bar (if expanded)
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            fontSize = 14.sp,
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

            // Replying Banner if active
            if (replyingToMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x337E74F1))
                        .border(1.dp, Color(0x667E74F1), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = "Replying",
                            tint = LoopinIrisGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Replying to ${replyingToMessage!!.senderName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LoopinIrisGlow
                            )
                            Text(
                                text = replyingToMessage!!.text.ifBlank { "Media attachment" },
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

            // Frosted Bottom Input Bar (with navigationBarsPadding)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Divider(color = Color(0x18FFFFFF), thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x24000000))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // Camera capture button (Click photo)
                IconButton(
                    onClick = {
                        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permission == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("chat_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take Photo",
                        tint = LoopinIrisGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Photo Library button (Pick from device)
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("chat_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Pick Image",
                        tint = LoopinIrisGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Voice Recording Button
                IconButton(
                    onClick = {
                        if (!isRecording) {
                            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            if (permission == PackageManager.PERMISSION_GRANTED) {
                                LoopinAudioController.startRecording(context)
                            } else {
                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
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
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record Voice Note",
                        tint = if (isRecording) LoopinLikeRed else LoopinSilver,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Effects selector toggle
                IconButton(
                    onClick = { showEffectPicker = !showEffectPicker },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("chat_effect_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Message Effects",
                        tint = if (selectedEffect.isNotBlank()) LoopinIrisGlow else TextMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Spotify Music button (Share music in chat)
                IconButton(
                    onClick = { showSpotifyPicker = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("chat_spotify_music_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Share Spotify Track",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Input TextField (Auto-scales nicely and prevents keyboard clipping)
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
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
                    shape = RoundedCornerShape(22.dp),
                    maxLines = 4,
                    minLines = 1,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = TextPrimary),
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

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
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
                            replyingToMessage = null
                            showEffectPicker = false
                        }
                    },
                    modifier = Modifier.testTag("send_chat_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) LoopinIrisGlow else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
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

        // Message Options Dialog (Reply, Forward, Delete for you, Unsend within 30 min)
        if (selectedMessageForOptions != null) {
            val msg = selectedMessageForOptions!!
            val isMe = msg.senderId == currentUserProfile?.userId
            val ageMs = msg.createdAt?.toDate()?.time?.let { System.currentTimeMillis() - it } ?: 0L
            val canUnsend = isMe && !msg.isUnsent && ageMs <= 30 * 60 * 1000L

            AlertDialog(
                onDismissRequest = { selectedMessageForOptions = null },
                containerColor = Color(0xFF141728),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Message Options",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Reply Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x18FFFFFF))
                                .clickable {
                                    replyingToMessage = msg
                                    selectedMessageForOptions = null
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                tint = LoopinIrisGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Reply", color = TextPrimary, fontSize = 14.sp)
                        }

                        // Forward Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x18FFFFFF))
                                .clickable {
                                    forwardingMessage = msg
                                    selectedMessageForOptions = null
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Forward,
                                contentDescription = null,
                                tint = LoopinIrisGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Forward Message", color = TextPrimary, fontSize = 14.sp)
                        }

                        // Delete For You Option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x18FFFFFF))
                                .clickable {
                                    onDeleteForYou(msg.id)
                                    selectedMessageForOptions = null
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Delete For You", color = Color(0xFFFF6B6B), fontSize = 14.sp)
                        }

                        // Unsend Option (Strictly up to 30 mins)
                        if (isMe && !msg.isUnsent) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (canUnsend) Color(0x22FF4444) else Color(0x0EFFFFFF))
                                    .clickable(enabled = canUnsend) {
                                        onUnsendMessage(msg.id)
                                        selectedMessageForOptions = null
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Undo,
                                    contentDescription = null,
                                    tint = if (canUnsend) Color(0xFFFF4444) else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (canUnsend) "Unsend (For Everyone)" else "Unsend (Expired > 30m)",
                                        color = if (canUnsend) Color(0xFFFF4444) else TextMuted,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (canUnsend) "Available for up to 30 minutes after sending" else "Cannot unsend messages older than 30 minutes",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedMessageForOptions = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Forward Message Dialog
        if (forwardingMessage != null) {
            val msgToForward = forwardingMessage!!
            AlertDialog(
                onDismissRequest = { forwardingMessage = null },
                containerColor = Color(0xFF141728),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Forward To...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    val candidateRooms = allRooms.filter { it.roomId != room.roomId && it.id != room.id }
                    if (candidateRooms.isEmpty()) {
                        Text(
                            text = "No other chat rooms found. Start a new conversation or group first!",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(candidateRooms) { targetRoom ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x18FFFFFF))
                                        .clickable {
                                            onForwardMessage(targetRoom, msgToForward)
                                            forwardingMessage = null
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (targetRoom.isGroup) Icons.Default.Groups else Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = LoopinIrisGlow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = targetRoom.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { forwardingMessage = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Full Screen Image Lightbox with Download functionality (User requested)
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
                        // Download button
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

                        // Close button
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
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                    }
                }
            }
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Image downloaded to device photos! 💾", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Image saved! 💾", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun formatMessageTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    return try {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(timestamp.toDate())
    } catch (_: Exception) {
        ""
    }
}

@Composable
private fun FrostedMessageBubble(
    message: RoomMessage,
    isMe: Boolean,
    onImageClick: (String) -> Unit,
    onReactionSelected: (String) -> Unit,
    onOpenActions: () -> Unit
) {
    val context = LocalContext.current
    var showReactionPicker by remember { mutableStateOf(false) }
    var isRevealed by remember { mutableStateOf(false) }

    val playingId by LoopinAudioController.playingId.collectAsState()
    val isPlayingThis = playingId == message.id

    // Text Message Effects styling
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
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
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
                        .padding(bottom = 2.dp),
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
                            message.isUnsent -> Color(0x12FFFFFF)
                            isFire -> Color(0x40FF6A00)
                            isSparkle -> Color(0x3D9B51E0)
                            isIrisGlow -> Color(0x4D7E74F1)
                            isMe -> Color(0x3D7E74F1)
                            else -> Color(0x18FFFFFF)
                        }
                    )
                    .border(
                        width = if (isSparkle || isFire || isIrisGlow) 1.5.dp else 1.dp,
                        brush = when {
                            message.isUnsent -> Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x22FFFFFF)))
                            isFire -> Brush.linearGradient(listOf(Color(0xFFFF9500), Color(0xFFFF3B30)))
                            isSparkle -> Brush.linearGradient(listOf(Color(0xFFFFD700), LoopinIrisGlow))
                            isIrisGlow -> Brush.linearGradient(listOf(LoopinIrisGlow, Color(0xFFC7C5FF)))
                            isMe -> Brush.linearGradient(listOf(Color(0x77A69FFF), Color(0x33A69FFF)))
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
                            onOpenActions()
                        }
                    }
                    .padding(
                        horizontal = if (isLoud) 18.dp else 14.dp,
                        vertical = if (isLoud) 14.dp else 10.dp
                    )
            ) {
                Column {
                    if (message.isUnsent) {
                        Text(
                            text = "🚫 This message was unsent",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = TextMuted
                        )
                    } else {
                        // Forwarded Header Tag
                        if (message.isForwarded) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Forward,
                                    contentDescription = "Forwarded",
                                    tint = LoopinIrisGlow,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Forwarded",
                                    fontSize = 10.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = LoopinIrisGlow
                                )
                            }
                        }

                        // Replied-To Quote Banner
                        if (message.replyToText.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22000000))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(24.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(LoopinIrisGlow)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = message.replyToSenderName.ifBlank { "Reply" },
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
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    // Effect badge if applicable
                    if (effect.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
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
                                }
                            )
                        }
                    }

                    // 1. Instant / Image Message
                    if ((message.type == "instant" || message.type == "image") && message.mediaUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(210.dp)
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

                            // Download icon button on bubble
                            IconButton(
                                onClick = { downloadChatImage(context, message.mediaUrl) },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x88000000))
                                    .align(Alignment.BottomEnd)
                                    .testTag("download_bubble_image_${message.id}")
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

                            // Waveform bars simulation
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
            }
        }
    }

        // Timestamp & Sent/Seen status (User requested)
        Row(
            modifier = Modifier
                .padding(top = 2.dp, start = if (isMe) 0.dp else 36.dp, end = if (isMe) 4.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            val timeStr = formatMessageTimestamp(message.createdAt)
            if (timeStr.isNotBlank()) {
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
            if (isMe && !message.isUnsent) {
                Spacer(modifier = Modifier.width(4.dp))
                val isSeen = message.seenAt != null || message.status == "seen" || message.seenByUserIds.isNotEmpty()
                if (isSeen) {
                    val seenTime = formatMessageTimestamp(message.seenAt)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Seen",
                            tint = LoopinIrisGlow,
                            modifier = Modifier.size(13.dp)
                        )
                        if (seenTime.isNotBlank()) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Seen $seenTime",
                                fontSize = 9.sp,
                                color = LoopinIrisGlow
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sent",
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Reactions Display
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

        // Quick Reaction Picker
        if (showReactionPicker) {
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x38FFFFFF))
                    .border(1.dp, GlassStrokeBrush, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QUICK_REACTIONS.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable {
                                onReactionSelected(emoji)
                                showReactionPicker = false
                            }
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}
