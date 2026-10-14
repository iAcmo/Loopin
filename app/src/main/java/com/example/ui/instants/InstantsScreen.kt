package com.example.ui.instants

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Person
import coil.compose.AsyncImage
import com.example.data.model.InstantItem
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
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
                // Header with Drop Instant action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x287E74F1))
                                .border(1.dp, LoopinIrisGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = LoopinIrisGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Instants ⚡",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Squad #${userProfile?.squadId ?: "ALPHA"} drops",
                                fontSize = 11.sp,
                                color = LoopinIrisGlow
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
                                    text = "Instants are quick authentic photo drops shared with your squad members. Capture one now!",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(instants, key = { it.id }) { instant ->
                            FrostedInstantCard(instant = instant)
                        }
                    }
                }
            }

            // Floating Quick Drop Button
            FloatingActionButton(
                onClick = { showDropModal = true },
                containerColor = LoopinIris,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 85.dp)
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
private fun FrostedInstantCard(instant: InstantItem) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("instant_card_${instant.id}"),
        isElevated = true,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = "Instant ⚡ • Squad #${instant.squadId}",
                        fontSize = 11.sp,
                        color = LoopinIrisGlow
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
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
                            .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(0.dp))
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
        }
    }
}

@Composable
private fun DropInstantGlassModal(
    onDismiss: () -> Unit,
    onDrop: (imageUrl: String, caption: String) -> Unit
) {
    val context = LocalContext.current
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    var caption by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf("") }

    // Pick from device photo gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri.toString()
        }
    }

    // Capture photo with camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "instant_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                selectedImage = file.toURI().toString()
            } catch (_: Exception) {}
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x337E74F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = LoopinIrisGlow,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Drop an Instant ⚡",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedImage.isNotBlank()) {
                    // Image preview box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = selectedImage,
                            contentDescription = "Instant Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // Placeholder when no picture taken yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x12FFFFFF))
                            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = LoopinIrisGlow,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Take a photo or pick from your device",
                                fontSize = 12.sp,
                                color = if (isDark) TextSecondary else Color(0xFF5B6178)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera and Gallery capture buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = {
                            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permission == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        text = "Camera 📷",
                        leadingIcon = Icons.Default.PhotoCamera,
                        isPrimary = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("instant_camera_button")
                    )

                    GlassButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        text = "Gallery 🖼️",
                        leadingIcon = Icons.Default.PhotoLibrary,
                        isPrimary = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("instant_gallery_button")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = "Instant caption (optional)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("instant_caption_input")
                )
            }
        },
        confirmButton = {
            GlassButton(
                onClick = {
                    if (selectedImage.isNotBlank()) {
                        onDrop(selectedImage, caption.trim())
                    }
                },
                text = "Drop Instant ⚡",
                isPrimary = true,
                enabled = selectedImage.isNotBlank(),
                modifier = Modifier.testTag("submit_instant_button")
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
}
