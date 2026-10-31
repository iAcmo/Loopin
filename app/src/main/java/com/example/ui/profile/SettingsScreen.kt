package com.example.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LoopinAvatar
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalVisualThemeMode
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinLikeRed
import com.example.ui.theme.LoopinOnlineGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VisualThemeMode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun SettingsScreen(
    userProfile: UserProfile?,
    isDarkTheme: Boolean,
    visualThemeMode: VisualThemeMode = VisualThemeMode.LIQUID_GLASS,
    notificationsEnabled: Boolean = true,
    notificationSoundEnabled: Boolean = true,
    appLockEnabled: Boolean = false,
    appLockPinLength: Int = 4,
    biometricUnlockEnabled: Boolean = false,
    appLockTimeoutMins: Int = 0,
    onSaveProfile: (displayName: String, bio: String, username: String, avatarUrl: String, isPublicDirectory: Boolean, isPrivate: Boolean) -> Unit,
    onToggleOnlineStatus: (Boolean) -> Unit = {},
    onToggleBlock: (String) -> Unit = {},
    onToggleTheme: (Boolean) -> Unit,
    onSelectVisualThemeMode: (VisualThemeMode) -> Unit = {},
    onToggleNotifications: (Boolean) -> Unit = {},
    onToggleNotificationSound: (Boolean) -> Unit = {},
    onSendTestNotification: () -> Unit = {},
    onConfigureAppLock: (enabled: Boolean, pin: String, pinLength: Int, biometric: Boolean, timeoutMins: Int) -> Unit = { _, _, _, _, _ -> },
    onDisableAppLock: () -> Unit = {},
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current
    val currentVisualMode = LocalVisualThemeMode.current

    var displayName by remember(userProfile) { mutableStateOf(userProfile?.displayName ?: "") }
    var username by remember(userProfile) { mutableStateOf(userProfile?.username ?: "") }
    var bio by remember(userProfile) { mutableStateOf(userProfile?.bio ?: "") }

    var showSetPasscodeDialog by remember { mutableStateOf(false) }
    var selectedPinLength by remember(appLockPinLength) { mutableIntStateOf(appLockPinLength) }
    var passcodeText by remember { mutableStateOf("") }
    var confirmPasscodeText by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf<String?>(null) }
    var avatarUrl by remember(userProfile) { mutableStateOf(userProfile?.avatarUrl ?: "") }
    var isPrivate by remember(userProfile) { mutableStateOf(userProfile?.isPrivate ?: false) }
    var isPublicDirectory by remember(userProfile) { mutableStateOf(userProfile?.isPublicDirectory ?: true) }
    var usernameError by remember { mutableStateOf<String?>(null) }

    val areSystemNotificationsEnabled = remember {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

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
                    text = "Settings & Preferences",
                    fontSize = 18.sp,
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
                            LoopinAvatar(
                                avatarUrl = avatarUrl,
                                size = 96.dp,
                                borderWidth = 3.dp,
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
                                    modifier = Modifier.height(36.dp),
                                    testTag = "upload_avatar_button"
                                )

                                GlassButton(
                                    onClick = { cameraLauncher.launch(null) },
                                    text = "Camera",
                                    leadingIcon = Icons.Default.PhotoCamera,
                                    isPrimary = false,
                                    modifier = Modifier.height(36.dp),
                                    testTag = "camera_avatar_button"
                                )

                                if (avatarUrl.isNotBlank()) {
                                    GlassButton(
                                        onClick = { avatarUrl = "" },
                                        text = "Remove",
                                        leadingIcon = Icons.Default.Delete,
                                        isPrimary = false,
                                        modifier = Modifier.height(36.dp),
                                        testTag = "remove_avatar_button"
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

                // Section 2: Account Details Fields
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = LoopinIris,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Account Identity",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) TextPrimary else Color(0xFF141728)
                                )
                            }

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
                                text = "Your @handle is unique across all Loopin members.",
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

                // Section 3: Visual UI Theme Style Selection (Liquid Glass vs Static Colors)
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = LoopinIrisGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Interface Visual Style",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = "Choose between cosmic Liquid Glass or minimalist Static Colors",
                                        fontSize = 11.sp,
                                        color = if (isDark) TextSecondary else Color(0xFF5B6178)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Option 1: Liquid Glass UI
                            val isGlassSelected = visualThemeMode == VisualThemeMode.LIQUID_GLASS
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isGlassSelected) Color(0x247E74F1) else Color(0x0CFFFFFF))
                                    .border(
                                        width = if (isGlassSelected) 1.5.dp else 1.dp,
                                        color = if (isGlassSelected) LoopinIris else Color(0x1FFFFFFF),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onSelectVisualThemeMode(VisualThemeMode.LIQUID_GLASS) }
                                    .padding(14.dp)
                                    .testTag("theme_option_liquid_glass"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isGlassSelected,
                                    onClick = { onSelectVisualThemeMode(VisualThemeMode.LIQUID_GLASS) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = LoopinIris,
                                        unselectedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = LoopinIrisGlow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Liquid Glass UI",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextPrimary else Color(0xFF141728)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Translucent frosted glass, ambient refractions, specular pointer highlights, and border luminescence.",
                                        fontSize = 11.sp,
                                        color = if (isDark) TextSecondary else Color(0xFF5B6178)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Option 2: Static Colors UI
                            val isStaticSelected = visualThemeMode == VisualThemeMode.STATIC_COLORS
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isStaticSelected) Color(0x247E74F1) else Color(0x0CFFFFFF))
                                    .border(
                                        width = if (isStaticSelected) 1.5.dp else 1.dp,
                                        color = if (isStaticSelected) LoopinIris else Color(0x1FFFFFFF),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onSelectVisualThemeMode(VisualThemeMode.STATIC_COLORS) }
                                    .padding(14.dp)
                                    .testTag("theme_option_static_colors"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isStaticSelected,
                                    onClick = { onSelectVisualThemeMode(VisualThemeMode.STATIC_COLORS) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = LoopinIris,
                                        unselectedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = Color(0xFFA1A1AA),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Static Colors UI",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextPrimary else Color(0xFF141728)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Clean solid Material 3 surfaces with high-contrast borders, solid opaque cards, and minimalist styling.",
                                        fontSize = 11.sp,
                                        color = if (isDark) TextSecondary else Color(0xFF5B6178)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Dark Mode vs Light Mode Appearance
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
                                        text = "Dark / Light Mode",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = if (isDarkTheme) "Dark Obsidian Mode (#323232)" else "Light Pearl Mode (#FFFFFF)",
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

                // Section 4B: App Accent Color Picker (Presets + Color Mixer)
                item {
                    AccentColorPickerCard()
                }

                // Section 5: Device Push & Message Notifications
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                            .background(if (notificationsEnabled) Color(0x287E74F1) else Color(0x18FFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = if (notificationsEnabled) LoopinIrisGlow else TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Message Notifications",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextPrimary else Color(0xFF141728)
                                        )
                                        Text(
                                            text = if (notificationsEnabled)
                                                "Receive alerts when new messages or mentions arrive"
                                            else
                                                "Direct message alerts are silenced",
                                            fontSize = 11.sp,
                                            color = if (isDark) TextSecondary else Color(0xFF5B6178)
                                        )
                                    }
                                }

                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { onToggleNotifications(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = LoopinIris,
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color(0x33FFFFFF)
                                    ),
                                    modifier = Modifier.testTag("toggle_notifications_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Sound & Vibrate toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sound & Vibration Alerts",
                                        fontSize = 13.sp,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                }

                                Switch(
                                    checked = notificationSoundEnabled,
                                    onCheckedChange = { onToggleNotificationSound(it) },
                                    enabled = notificationsEnabled,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = LoopinIris,
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color(0x33FFFFFF)
                                    ),
                                    modifier = Modifier.testTag("toggle_sound_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Test Notification Button
                            GlassButton(
                                onClick = onSendTestNotification,
                                text = "Send Test Notification",
                                leadingIcon = Icons.Default.NotificationsActive,
                                isPrimary = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                testTag = "send_test_notification_button"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (areSystemNotificationsEnabled) LoopinOnlineGreen else LoopinLikeRed)
                                )
                                Text(
                                    text = if (areSystemNotificationsEnabled)
                                        "Device notification channel: Active & Allowed"
                                    else
                                        "Device notifications are blocked in Android Settings",
                                    fontSize = 11.sp,
                                    color = if (areSystemNotificationsEnabled) LoopinOnlineGreen else LoopinLikeRed
                                )
                            }
                        }
                    }
                }

                // Section 6: Privacy Control (Public vs Private Account)
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
                                        text = "Account Privacy",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            PrivacyChoiceChipRow(
                                isPrivate = isPrivate,
                                onPrivacyChange = { isPrivate = it }
                            )
                        }
                    }
                }

                // Section 7: Online Presence Status
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
                                        text = "Shows when you are active to your chats",
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

                // Section 8: Security & App Passcode Lock
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x287E74F1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = LoopinIrisGlow
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "App Passcode Lock",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextPrimary else Color(0xFF141728)
                                        )
                                        Text(
                                            text = if (appLockEnabled) "Active ($appLockPinLength digits)" else "Require passcode to open app",
                                            fontSize = 11.sp,
                                            color = if (appLockEnabled) LoopinOnlineGreen else TextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = appLockEnabled,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedPinLength = appLockPinLength
                                            passcodeText = ""
                                            confirmPasscodeText = ""
                                            passcodeError = null
                                            showSetPasscodeDialog = true
                                        } else {
                                            onDisableAppLock()
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = LoopinIris
                                    ),
                                    modifier = Modifier.testTag("toggle_app_lock_switch")
                                )
                            }

                            if (appLockEnabled) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = Color(0x18FFFFFF), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Passcode Length & Change Passcode
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Passcode Length",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "$appLockPinLength digits currently set",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    GlassButton(
                                        onClick = {
                                            selectedPinLength = appLockPinLength
                                            passcodeText = ""
                                            confirmPasscodeText = ""
                                            passcodeError = null
                                            showSetPasscodeDialog = true
                                        },
                                        text = "Change PIN",
                                        isPrimary = false
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Fingerprint / Biometric Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = null,
                                            tint = LoopinIrisGlow,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Fingerprint Unlock",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Use fingerprint sensor to unlock instantly",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = biometricUnlockEnabled,
                                        onCheckedChange = { checked ->
                                            onConfigureAppLock(
                                                true,
                                                "",
                                                appLockPinLength,
                                                checked,
                                                appLockTimeoutMins
                                            )
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = LoopinIris
                                        ),
                                        modifier = Modifier.testTag("toggle_biometric_switch")
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Lock Timing Selection (Immediately vs After 1 minute)
                                Column {
                                    Text(
                                        text = "Auto-Lock Activation",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        GlassButton(
                                            onClick = {
                                                onConfigureAppLock(
                                                    true,
                                                    "",
                                                    appLockPinLength,
                                                    biometricUnlockEnabled,
                                                    0
                                                )
                                            },
                                            text = "Immediately",
                                            isPrimary = appLockTimeoutMins == 0,
                                            modifier = Modifier.weight(1f)
                                        )
                                        GlassButton(
                                            onClick = {
                                                onConfigureAppLock(
                                                    true,
                                                    "",
                                                    appLockPinLength,
                                                    biometricUnlockEnabled,
                                                    1
                                                )
                                            },
                                            text = "After 1 minute",
                                            isPrimary = appLockTimeoutMins == 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 9: Blocked Accounts Management
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

                // Section 9: Save Changes Button
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

                // Section 10: Sign Out
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
                            .height(48.dp)
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
