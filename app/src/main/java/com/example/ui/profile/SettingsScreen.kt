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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
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
    onToggleOnlineStatus: (Boolean) -> Unit = {},
    onToggleBlock: (String) -> Unit = {},
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
    var isPrivate by remember(userProfile) { mutableStateOf(userProfile?.isPrivate ?: false) }
    var isPublicDirectory by remember(userProfile) { mutableStateOf(userProfile?.isPublicDirectory ?: true) }
    var usernameError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUrl = uri.toString()
        }
    }

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
                    text = "Profile & Account Settings",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )

                IconButton(
                    onClick = {
                        val clean = username.lowercase().trim()
                        if (clean.length < 3) {
                            usernameError = "Username must be at least 3 characters"
                            return@IconButton
                        }
                        usernameError = null
                        onSaveProfile(
                            displayName.trim(),
                            bio.trim(),
                            clean,
                            avatarUrl,
                            isPublicDirectory,
                            isPrivate
                        )
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
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Avatar Edit
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            com.example.ui.components.LoopinAvatar(
                                avatarUrl = avatarUrl,
                                size = 92.dp,
                                borderWidth = 2.5.dp,
                                borderBrush = FrostedStoryRingGradient
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    text = "Upload",
                                    leadingIcon = Icons.Default.PhotoLibrary,
                                    isPrimary = false,
                                    modifier = Modifier.height(36.dp)
                                )

                                GlassButton(
                                    onClick = { cameraLauncher.launch(null) },
                                    text = "Camera",
                                    leadingIcon = Icons.Default.PhotoCamera,
                                    isPrimary = false,
                                    modifier = Modifier.height(36.dp)
                                )

                                if (avatarUrl.isNotBlank()) {
                                    GlassButton(
                                        onClick = { avatarUrl = "" },
                                        text = "Remove",
                                        leadingIcon = Icons.Default.Delete,
                                        isPrimary = false,
                                        modifier = Modifier.height(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (avatarUrl.isBlank()) "Default profile picture active" else "Custom photo set. Tap Remove to revert to default.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Section 2: Profile Info Fields (Display Name, Unique Username, Bio)
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
                                onValueChange = {
                                    username = it.lowercase().trim().replace(" ", "_")
                                    usernameError = null
                                },
                                label = "Unique Username (@username)",
                                modifier = Modifier.testTag("settings_username_input")
                            )

                            usernameError?.let { err ->
                                Text(
                                    text = err,
                                    fontSize = 11.sp,
                                    color = LoopinLikeRed,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                            }

                            Text(
                                text = "Usernames are strictly unique across all Loopin members.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            GlassTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = "Bio / Status",
                                singleLine = false,
                                modifier = Modifier.height(84.dp).testTag("settings_bio_input")
                            )
                        }
                    }
                }

                // Section 3: Privacy Control (Public vs Private Account)
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
                                        .background(if (isPrivate) Color(0x337E74F1) else Color(0x18FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPrivate) Icons.Default.Lock else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = if (isPrivate) LoopinIrisGlow else TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isPrivate) "Private Account" else "Public Account",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = if (isPrivate)
                                            "Only approved followers can see your posts and stories"
                                        else
                                            "Anyone on Loopin can view your posts and profile",
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
                                modifier = Modifier.testTag("toggle_private_account_switch")
                            )
                        }
                    }
                }

                // Section 4: Interface Theme (Light vs Dark)
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
                                        text = "Theme Appearance",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = if (isDarkTheme) "Dark Liquid Obsidian" else "Light Pearl Crystal",
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
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color(0x33FFFFFF)
                                ),
                                modifier = Modifier.testTag("toggle_dark_theme_switch")
                            )
                        }
                    }
                }

                // Section 5: Blocked Accounts Management
                item {
                    val blockedList = userProfile?.blockedUsers ?: emptyList()
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = LoopinLikeRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Blocked Accounts",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                }
                                Text(
                                    text = "${blockedList.size} blocked",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }

                            if (blockedList.isEmpty()) {
                                Text(
                                    text = "You haven't blocked any accounts.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            } else {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    blockedList.forEach { blockedId ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "User ID: ${blockedId.take(12)}...",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                            GlassButton(
                                                onClick = { onToggleBlock(blockedId) },
                                                text = "Unblock",
                                                isPrimary = false,
                                                modifier = Modifier.height(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 6: Online Presence Status
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
                                        text = "Active Status",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = "Automatically active while Loopin is running",
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

                // Section 7: Save Changes Button
                item {
                    GlassButton(
                        onClick = {
                            val clean = username.lowercase().trim()
                            if (clean.length < 3) {
                                usernameError = "Username must be at least 3 characters"
                                return@GlassButton
                            }
                            usernameError = null
                            onSaveProfile(
                                displayName.trim(),
                                bio.trim(),
                                clean,
                                avatarUrl,
                                isPublicDirectory,
                                isPrivate
                            )
                            onBack()
                        },
                        text = "Save Profile Changes",
                        leadingIcon = Icons.Default.Check,
                        isPrimary = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_button")
                    )
                }

                // Section 8: Sign Out
                item {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                } catch (_: Exception) {}
                                Firebase.auth.signOut()
                                onSignOut()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("sign_out_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LoopinLikeRed.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = LoopinLikeRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out of Account",
                            color = LoopinLikeRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
