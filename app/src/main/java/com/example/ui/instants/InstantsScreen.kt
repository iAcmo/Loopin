package com.example.ui.instants

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.InstantItem
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassHeader
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.LocalAppAccentColor
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinLikeRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File
import java.io.FileOutputStream

@Composable
fun InstantsScreen(
    userProfile: UserProfile?,
    instants: List<InstantItem>,
    onPostInstant: (imageUrl: String, caption: String) -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val accentColor = LocalAppAccentColor.current
    var showDropModal by remember { mutableStateOf(false) }

    LiquidGlassBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter)
                    .padding(bottom = 70.dp)
            ) {
                // Liquid Glass Header with Fresnel specular curve, chromatic aberration dispersion, and z-radius
                LiquidGlassHeader(
                    zRadius = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Instants ⚡",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) DarkTextPrimary else Color(0xFF10151C)
                                )
                                Text(
                                    text = "Fleeting 24h photo drops",
                                    fontSize = 11.sp,
                                    color = if (isDark) DarkTextSecondary else Color(0xFF5B6178)
                                )
                            }
                        }

                        GlassButton(
                            onClick = { showDropModal = true },
                            text = "Drop Instant",
                            leadingIcon = Icons.Default.FlashOn,
                            isPrimary = true,
                            modifier = Modifier.testTag("top_drop_instant_button")
                        )
                    }
                }

                if (instants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            isElevated = true
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Instants Yet",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Instants are authentic, fleeting photo drops shared with your squad. Drop yours now!",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                GlassButton(
                                    onClick = { showDropModal = true },
                                    text = "Drop First Instant ⚡",
                                    leadingIcon = Icons.Default.FlashOn,
                                    isPrimary = true,
                                    modifier = Modifier.testTag("drop_first_instant_button")
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(instants, key = { it.id }) { instant ->
                            FrostedInstantCard(
                                instant = instant,
                                currentUserId = userProfile?.userId ?: ""
                            )
                        }
                    }
                }
            }

            // Floating Quick Drop Button styled with active accent color
            FloatingActionButton(
                onClick = { showDropModal = true },
                containerColor = accentColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 90.dp)
                    .testTag("floating_drop_instant_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Drop Instant",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (showDropModal) {
            DropInstantGlassModal(
                onDismiss = { showDropModal = false },
                onDrop = { imageUrl, caption ->
                    onPostInstant(imageUrl, caption)
                    showDropModal = false
                }
            )
        }
    }
}

@Composable
private fun FrostedInstantCard(
    instant: InstantItem,
    currentUserId: String
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDark = LocalIsDarkTheme.current
    val accentColor = LocalAppAccentColor.current

    var isLiked by remember { mutableStateOf(instant.likedByUserIds.contains(currentUserId)) }
    var likesCount by remember { mutableIntStateOf(instant.likesCount.toInt().coerceAtLeast(if (isLiked) 1 else 0)) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "like_scale"
    )

    // Local in-memory comments for instant engagement
    val instantComments = remember { mutableStateListOf<Pair<String, String>>() }
    var commentInput by remember { mutableStateOf("") }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("instant_card_${instant.id}"),
        isElevated = true,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column {
            // Header Row: Avatar, Username, and ... Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (instant.userAvatar.isNotBlank()) {
                        AsyncImage(
                            model = instant.userAvatar,
                            contentDescription = instant.username,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, FrostedStoryRingGradient, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .border(1.5.dp, FrostedStoryRingGradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = instant.username,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = instant.username,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Instant ⚡",
                            fontSize = 11.sp,
                            color = accentColor
                        )
                    }
                }

                // More Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("instant_menu_button_${instant.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Instant Options",
                            tint = if (isDark) DarkTextSecondary else Color(0xFF64748B)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy Link") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                clipboardManager.setText(AnnotatedString("https://loopin.app/instant/${instant.id}"))
                                Toast.makeText(context, "Instant link copied!", Toast.LENGTH_SHORT).show()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share via DM") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                            onClick = {
                                Toast.makeText(context, "Shared to Direct Messages!", Toast.LENGTH_SHORT).show()
                                showMenu = false
                            }
                        )
                        if (instant.userId == currentUserId) {
                            DropdownMenuItem(
                                text = { Text("Delete Instant", color = LoopinLikeRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = LoopinLikeRed) },
                                onClick = {
                                    Toast.makeText(context, "Instant deleted", Toast.LENGTH_SHORT).show()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = instant.imageUrl,
                    contentDescription = "Instant image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (instant.caption.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xB308090E))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = instant.caption,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Interactive Actions Row: Like, Comments, Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like button with heart animation & counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isLiked = !isLiked
                                likesCount = if (isLiked) likesCount + 1 else (likesCount - 1).coerceAtLeast(0)
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("instant_like_button_${instant.id}")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like Instant",
                            tint = if (isLiked) LoopinLikeRed else (if (isDark) DarkTextSecondary else Color(0xFF64748B)),
                            modifier = Modifier
                                .size(22.dp)
                                .scale(likeScale)
                        )
                        if (likesCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$likesCount",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLiked) LoopinLikeRed else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Comment button with count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showCommentsDialog = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("instant_comment_button_${instant.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comment on Instant",
                            tint = if (isDark) DarkTextSecondary else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                        val totalComments = instant.commentsCount + instantComments.size
                        if (totalComments > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalComments",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Share button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("Check out this instant on Loopin: ${instant.caption}"))
                            Toast.makeText(context, "Instant shared to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Share Instant",
                            tint = if (isDark) DarkTextSecondary else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Instant Comments Dialog / Sheet
    if (showCommentsDialog) {
        AlertDialog(
            onDismissRequest = { showCommentsDialog = false },
            title = {
                Text(
                    text = "Comments for @${instant.username}'s Instant",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (instantComments.isEmpty()) {
                        Text(
                            text = "No comments yet. Say something nice!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            items(instantComments) { comment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${comment.first}: ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Text(
                                        text = comment.second,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = "Add a comment...",
                        modifier = Modifier.fillMaxWidth().testTag("instant_comment_input")
                    )
                }
            },
            confirmButton = {
                GlassButton(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            instantComments.add("You" to commentInput.trim())
                            commentInput = ""
                            Toast.makeText(context, "Comment added!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    text = "Post",
                    isPrimary = true
                )
            },
            dismissButton = {
                TextButton(onClick = { showCommentsDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DropInstantGlassModal(
    onDismiss: () -> Unit,
    onDrop: (imageUrl: String, caption: String) -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    var caption by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "instant_${System.currentTimeMillis()}.jpg")
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()
                selectedImage = file.absolutePath
            } catch (_: Exception) {}
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission needed to snap Instant", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Drop Instant ⚡",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedImage.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = selectedImage,
                            contentDescription = "Selected instant",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            text = "Gallery",
                            leadingIcon = Icons.Default.PhotoLibrary,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )

                        GlassButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    cameraLauncher.launch(null)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            text = "Camera",
                            leadingIcon = Icons.Default.PhotoCamera,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                GlassTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = "Add a quick caption...",
                    modifier = Modifier.fillMaxWidth().testTag("instant_caption_input")
                )
            }
        },
        confirmButton = {
            GlassButton(
                onClick = {
                    val finalImage = selectedImage.ifBlank {
                        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800"
                    }
                    onDrop(finalImage, caption.trim())
                },
                text = "Drop Now",
                isPrimary = true,
                modifier = Modifier.testTag("confirm_drop_instant_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
