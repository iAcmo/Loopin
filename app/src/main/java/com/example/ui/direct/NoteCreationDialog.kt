package com.example.ui.direct

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MusicNote
import com.example.data.model.UserProfile
import com.example.ui.components.LoopinAvatar
import com.example.ui.components.SpotifyGreen
import com.example.ui.components.SpotifyTrackPickerDialog
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinLikeRed
import com.example.ui.theme.NoteColorPalette
import com.example.ui.theme.NoteColorTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SpotifyTrack

@Composable
fun NoteCreationDialog(
    userProfile: UserProfile?,
    existingNote: MusicNote? = null,
    onDismiss: () -> Unit,
    onSave: (
        songTitle: String,
        artist: String,
        noteText: String,
        albumCoverUrl: String,
        spotifyUri: String,
        previewAudioUrl: String,
        colorTheme: String
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val focusManager = LocalFocusManager.current

    var noteText by remember { mutableStateOf(existingNote?.noteText ?: "") }
    var selectedColorId by remember { mutableStateOf(existingNote?.colorTheme?.ifBlank { "neutral" } ?: "neutral") }
    var selectedSongTitle by remember { mutableStateOf(existingNote?.songTitle ?: "") }
    var selectedArtist by remember { mutableStateOf(existingNote?.artist ?: "") }
    var selectedAlbumCover by remember { mutableStateOf(existingNote?.albumCoverUrl ?: "") }
    var selectedSpotifyUri by remember { mutableStateOf(existingNote?.spotifyUri ?: "") }

    var showSpotifyPicker by remember { mutableStateOf(false) }
    var showColorPalette by remember { mutableStateOf(false) }

    val activeColorTheme: NoteColorTheme = remember(selectedColorId) {
        NoteColorPalette.getColorById(selectedColorId)
    }

    val bubbleBg = if (isDark) activeColorTheme.backgroundDark else activeColorTheme.backgroundLight
    val bubbleTextColor = if (isDark) activeColorTheme.textDark else activeColorTheme.textLight

    val canShare = noteText.isNotBlank() || selectedSongTitle.isNotBlank()

    fun handleShare() {
        if (canShare) {
            onSave(
                selectedSongTitle,
                selectedArtist,
                noteText.trim(),
                selectedAlbumCover,
                selectedSpotifyUri,
                "",
                selectedColorId
            )
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xF2090B12) else Color(0xF7FFFFFF))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("dismiss_note_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = "New Note",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Top Share Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (canShare) LoopinIris else Color(0x337E74F1)
                            )
                            .clickable(enabled = canShare) { handleShare() }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                            .testTag("share_note_top_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Share",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canShare) Color.White else Color(0x66FFFFFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Center Stage: Floating Thought Bubble + Avatar + Actions
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Optional Attached Song Pill
                    AnimatedVisibility(visible = selectedSongTitle.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x331DB954))
                                .border(1.dp, SpotifyGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$selectedSongTitle • $selectedArtist",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SpotifyGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Track",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        selectedSongTitle = ""
                                        selectedArtist = ""
                                        selectedAlbumCover = ""
                                        selectedSpotifyUri = ""
                                    }
                            )
                        }
                    }

                    // Floating Thought Bubble
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = bubbleBg.copy(alpha = 0.4f),
                                spotColor = bubbleBg.copy(alpha = 0.6f)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(bubbleBg)
                            .border(
                                width = 1.dp,
                                color = if (selectedColorId == "neutral") GlassBorder else Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("note_thought_bubble"),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = noteText,
                            onValueChange = {
                                if (it.length <= 60) noteText = it
                            },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = bubbleTextColor,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(bubbleTextColor),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_text_input"),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (noteText.isEmpty()) {
                                        Text(
                                            text = "What's new?",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = bubbleTextColor.copy(alpha = 0.65f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    // Thought Bubble Tail (Iconic comic dots pointing to avatar)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(bubbleBg)
                            .border(0.75.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(bubbleBg)
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // User Profile Avatar
                    LoopinAvatar(
                        avatarUrl = userProfile?.avatarUrl,
                        size = 84.dp,
                        borderWidth = 2.dp,
                        borderBrush = SolidColor(if (selectedColorId != "neutral") bubbleBg else LoopinIrisGlow)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Character counter
                    Text(
                        text = "${noteText.length}/60",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Circular Action Pills (Music, Color Palette, Delete)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Music Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (selectedSongTitle.isNotBlank()) SpotifyGreen.copy(alpha = 0.2f) else Color(0x18FFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (selectedSongTitle.isNotBlank()) SpotifyGreen else GlassBorder,
                                    shape = CircleShape
                                )
                                .clickable { showSpotifyPicker = true }
                                .testTag("note_music_picker_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Add Music",
                                tint = if (selectedSongTitle.isNotBlank()) SpotifyGreen else TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 2. Color Palette Button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (showColorPalette || selectedColorId != "neutral") bubbleBg.copy(alpha = 0.3f) else Color(0x18FFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (selectedColorId != "neutral") bubbleBg else GlassBorder,
                                    shape = CircleShape
                                )
                                .clickable { showColorPalette = !showColorPalette }
                                .testTag("note_color_palette_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Choose Note Color",
                                tint = if (selectedColorId != "neutral") bubbleBg else TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 3. Delete Note Button (if existing note is present)
                        if (existingNote != null || onDelete != null) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FF334B))
                                    .border(1.dp, LoopinLikeRed.copy(alpha = 0.5f), CircleShape)
                                    .clickable {
                                        onDelete?.invoke()
                                        onDismiss()
                                    }
                                    .testTag("delete_note_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Note",
                                    tint = LoopinLikeRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Instagram Color Swatches Picker
                    AnimatedVisibility(
                        visible = showColorPalette,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 18.dp)
                        ) {
                            Text(
                                text = "Note Color",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                items(NoteColorPalette.allColors) { paletteItem ->
                                    val isSelected = paletteItem.id == selectedColorId
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(paletteItem.swatchColor)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color(0x33FFFFFF),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedColorId = paletteItem.id
                                            }
                                            .testTag("color_swatch_${paletteItem.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Audience Indicator & Bottom Share Pill Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x14FFFFFF))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share with friends",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        // Bottom Share button matching screenshot
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    if (canShare) LoopinIris else Color(0x337E74F1)
                                )
                                .clickable(enabled = canShare) { handleShare() }
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                .testTag("share_note_bottom_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Share",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canShare) Color.White else Color(0x66FFFFFF)
                            )
                        }
                    }
                }
            }
        }
    }

    // Spotify track picker
    if (showSpotifyPicker) {
        SpotifyTrackPickerDialog(
            onTrackSelected = { track: SpotifyTrack ->
                selectedSongTitle = track.title
                selectedArtist = track.artist
                selectedAlbumCover = track.albumArt
                selectedSpotifyUri = track.spotifyUri
                showSpotifyPicker = false
            },
            onDismiss = { showSpotifyPicker = false }
        )
    }
}
