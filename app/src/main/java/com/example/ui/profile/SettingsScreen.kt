package com.example.ui.profile

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.auth.POPULAR_SQUADS
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidGlassBackground
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun SettingsScreen(
    userProfile: UserProfile?,
    isDarkTheme: Boolean,
    onSaveProfile: (displayName: String, bio: String, username: String, avatarUrl: String, isPublicDirectory: Boolean, isPrivate: Boolean) -> Unit,
    onToggleOnlineStatus: (Boolean) -> Unit,
    onUpdateSquad: (String) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    var displayName by remember(userProfile) { mutableStateOf(userProfile?.displayName ?: "") }
    var username by remember(userProfile) { mutableStateOf(userProfile?.username ?: "") }
    var bio by remember(userProfile) { mutableStateOf(userProfile?.bio ?: "") }
    var avatarUrl by remember(userProfile) { mutableStateOf(userProfile?.avatarUrl ?: "") }
    var isPublicDirectory by remember(userProfile) { mutableStateOf(userProfile?.isPublicDirectory ?: true) }
    var isPrivate by remember(userProfile) { mutableStateOf(userProfile?.isPrivate ?: false) }
    var isOnline by remember(userProfile) { mutableStateOf(userProfile?.isOnline ?: true) }
    var selectedSquadId by remember(userProfile) { mutableStateOf(userProfile?.squadId ?: "ALPHA") }

    // Photo picker launcher (Zero-permission Google Play compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUrl = uri.toString()
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                avatarUrl = file.toURI().toString()
            } catch (_: Exception) {}
        }
    }

    LiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDark) TextPrimary else Color(0xFF141728)
                    )
                }

                Text(
                    text = "Profile & Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )

                IconButton(
                    onClick = {
                        onSaveProfile(displayName, bio, username, avatarUrl, isPublicDirectory)
                        onBack()
                    },
                    modifier = Modifier.testTag("settings_save_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Changes",
                        tint = LoopinIrisGlow
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Profile Avatar with Camera, Gallery & Plain User Icon option
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Profile Picture",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) TextPrimary else Color(0xFF141728),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Avatar preview
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x1AFFFFFF))
                                    .border(2.dp, FrostedStoryRingGradient, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Plain user icon as requested
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Plain User Icon",
                                        tint = if (isDark) LoopinSilver else Color(0xFF5B6178),
                                        modifier = Modifier.size(52.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons: Pick from device, Click photo, or Remove picture
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("avatar_pick_gallery_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = LoopinIrisGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery", fontSize = 12.sp, color = if (isDark) TextPrimary else Color(0xFF141728))
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = { cameraLauncher.launch(null) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("avatar_take_camera_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = LoopinIrisGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera", fontSize = 12.sp, color = if (isDark) TextPrimary else Color(0xFF141728))
                                }

                                if (avatarUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { avatarUrl = "" },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("avatar_remove_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = LoopinLikeRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove", fontSize = 12.sp, color = LoopinLikeRed)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Edit Name, Username, Bio
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Account Details",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) TextPrimary else Color(0xFF141728)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            GlassTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = "Display Name",
                                modifier = Modifier.testTag("settings_display_name_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            GlassTextField(
                                value = username,
                                onValueChange = { username = it.filter { char -> char.isLetterOrDigit() || char == '_' } },
                                label = "Username (@)",
                                modifier = Modifier.testTag("settings_username_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            GlassTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = "Bio",
                                singleLine = false,
                                modifier = Modifier.testTag("settings_bio_input")
                            )
                        }
                    }
                }

                // Section 3: Publicbase Visibility Setting (User requested)
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isPublicDirectory) Color(0x227E74F1) else Color(0x18FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPublicDirectory) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = if (isPublicDirectory) LoopinIrisGlow else TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Publicbase Discovery",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = if (isPublicDirectory)
                                            "Visible in public directory & pencil chat search"
                                        else
                                            "Hidden from publicbase directory (private)",
                                        fontSize = 11.sp,
                                        color = if (isDark) TextSecondary else Color(0xFF5B6178)
                                    )
                                }
                            }

                            Switch(
                                checked = isPublicDirectory,
                                onCheckedChange = { isPublicDirectory = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = LoopinIris,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.testTag("toggle_publicbase_switch")
                            )
                        }
                    }
                }

                // Section 3b: Account Privacy (Private or Public Account)
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isPrivate) Color(0x227E74F1) else Color(0x18FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPrivate) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (isPrivate) LoopinIrisGlow else TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Private Account",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = if (isPrivate)
                                            "Only approved followers can view your posts and full profile"
                                        else
                                            "Anyone can view your posts and profile",
                                        fontSize = 11.sp,
                                        color = if (isDark) TextSecondary else Color(0xFF5B6178)
                                    )
                                }
                            }

                            Switch(
                                checked = isPrivate,
                                onCheckedChange = { isPrivate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = LoopinIris,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.testTag("toggle_account_privacy_switch")
                            )
                        }
                    }
                }

                // Section 4: Functional Light & Dark Theme
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x227E74F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = LoopinIrisGlow
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Interface Appearance",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = if (isDarkTheme) "Dark Liquid Obsidian Theme" else "Light Pearl Crystal Theme",
                                        fontSize = 11.sp,
                                        color = if (isDark) TextSecondary else Color(0xFF5B6178)
                                    )
                                }
                            }

                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onToggleTheme(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = LoopinIris,
                                    uncheckedThumbColor = LoopinIris,
                                    uncheckedTrackColor = Color(0xFFD6DBED)
                                ),
                                modifier = Modifier.testTag("toggle_dark_theme_switch")
                            )
                        }
                    }
                }

                // Section 5: Automatic Online Presence
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x2222C55E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = LoopinOnlineGreen
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Online Presence",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = "Automatic (active while app is open)",
                                        fontSize = 11.sp,
                                        color = LoopinOnlineGreen
                                    )
                                }
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

                // Section 6: Switch Squad (Moved from profile page to settings as requested)
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = LoopinIrisGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Switch Squad",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) TextPrimary else Color(0xFF141728)
                                )
                            }
                            Text(
                                text = "Current Squad: #${userProfile?.squadId ?: "ALPHA"}",
                                fontSize = 12.sp,
                                color = if (isDark) TextSecondary else Color(0xFF5B6178),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(POPULAR_SQUADS) { squad ->
                                    val isSelected = selectedSquadId.equals(squad, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) LoopinIris else Color(0x18FFFFFF))
                                            .border(
                                                1.dp,
                                                if (isSelected) LoopinIrisGlow else GlassBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                selectedSquadId = squad
                                                onUpdateSquad(squad)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "#$squad",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else if (isDark) TextPrimary else Color(0xFF141728)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 7: Save & Sign Out Buttons
                item {
                    GlassButton(
                        onClick = {
                            onSaveProfile(displayName, bio, username, avatarUrl, isPublicDirectory, isPrivate)
                            onBack()
                        },
                        text = "Save All Changes",
                        leadingIcon = Icons.Default.Check,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val credManager = CredentialManager.create(context)
                                    credManager.clearCredentialState(ClearCredentialStateRequest())
                                } catch (_: Exception) {}
                                Firebase.auth.signOut()
                                onSignOut()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = LoopinLikeRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out",
                            color = LoopinLikeRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
