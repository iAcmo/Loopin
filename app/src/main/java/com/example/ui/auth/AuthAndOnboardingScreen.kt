package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LoopinBrandHeader
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderStrong
import com.example.ui.theme.GlassFill
import com.example.ui.theme.GlassFillElevated
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinSilver
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

val POPULAR_SQUADS = listOf("ALPHA", "STUDIO", "CAMPUS", "CREATIVE", "TECH", "VIBE")

@Composable
fun AuthAndOnboardingScreen(
    isSignedIn: Boolean,
    onAuthSuccess: () -> Unit,
    onSaveProfile: (username: String, displayName: String, bio: String, avatarUrl: String, squadId: String) -> Unit
) {
    var onboardingStep by remember { mutableIntStateOf(if (isSignedIn) 1 else 0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Profile form state (Default to plain user icon as requested)
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var squadId by remember { mutableStateOf("ALPHA") }
    var bio by remember { mutableStateOf("Connected on Loopin ✨") }
    var selectedAvatar by remember { mutableStateOf("") }

    LiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Official Loopin Logo & Brand
            LoopinBrandHeader(
                logoSize = 56.dp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (!isSignedIn) {
                // Frosted Glass Authentication Card with Email Login & "Login via username" hyperlink
                GlassSignInCard(
                    errorMessage = errorMessage,
                    onAuthSuccess = {
                        errorMessage = null
                        onAuthSuccess()
                        onboardingStep = 1
                    },
                    onAuthError = { err -> errorMessage = err },
                    onUsernameLoginMode = { prefillUsername ->
                        username = prefillUsername
                        onAuthSuccess()
                        onboardingStep = 2
                    }
                )
            } else if (onboardingStep == 1) {
                // Step 1: Sleek Frosted Onboarding Walkthrough
                GlassOnboardingTutorialCard(
                    onNext = { onboardingStep = 2 }
                )
            } else {
                // Step 2: Liquid Frosted Glass Profile & Squad Setup
                GlassProfileSetupCard(
                    username = username,
                    onUsernameChange = { username = it },
                    displayName = displayName,
                    onDisplayNameChange = { displayName = it },
                    squadId = squadId,
                    onSquadIdChange = { squadId = it },
                    bio = bio,
                    onBioChange = { bio = it },
                    selectedAvatar = selectedAvatar,
                    onSelectAvatar = { selectedAvatar = it },
                    onSubmit = {
                        if (username.trim().length >= 2 && squadId.trim().isNotEmpty()) {
                            onSaveProfile(
                                username.trim(),
                                displayName.trim().ifBlank { username.trim() },
                                bio.trim(),
                                selectedAvatar,
                                squadId.trim().uppercase()
                            )
                        } else {
                            errorMessage = "Please enter a username and squad ID"
                        }
                    },
                    errorMessage = errorMessage
                )
            }
        }
    }
}

@Composable
private fun GlassSignInCard(
    errorMessage: String?,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    onUsernameLoginMode: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isUsernameLoginMode by remember { mutableStateOf(false) }
    var inputUsername by remember { mutableStateOf("") }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isElevated = true
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isUsernameLoginMode) "Log in via Username" else "Sign In to Loopin",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = if (isUsernameLoginMode)
                    "Enter your Loopin username to access your squad room."
                else
                    "Frosted communication rooms, stories, instants & music notes.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            if (!isUsernameLoginMode) {
                // Custom Email Login fields
                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email address",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Email Login Action Button
                GlassButton(
                    onClick = {
                        val trimmedEmail = email.trim()
                        val trimmedPass = password.trim()
                        if (trimmedEmail.contains("@") && trimmedPass.length >= 6) {
                            isLoading = true
                            scope.launch {
                                try {
                                    Firebase.auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                                    isLoading = false
                                    onAuthSuccess()
                                } catch (signInErr: Exception) {
                                    try {
                                        Firebase.auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                                        isLoading = false
                                        onAuthSuccess()
                                    } catch (createErr: Exception) {
                                        isLoading = false
                                        onAuthError(createErr.localizedMessage ?: signInErr.localizedMessage ?: "Authentication failed")
                                    }
                                }
                            }
                        } else if (!trimmedEmail.contains("@")) {
                            onAuthError("Please enter a valid email address (e.g. user@domain.com)")
                        } else {
                            onAuthError("Password must be at least 6 characters")
                        }
                    },
                    text = "Sign In with Email",
                    isPrimary = true,
                    isLoading = isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_login_button")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Google Sign In option
                GlassButton(
                    onClick = {
                        isLoading = true
                        handleGoogleSignIn(
                            context = context,
                            scope = scope,
                            onSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onError = { err ->
                                isLoading = false
                                onAuthError(err)
                            },
                            onCancelled = { isLoading = false }
                        )
                    },
                    text = "Continue with Google",
                    isPrimary = false,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_signin_button")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Tiny hyperlink below: "Login via username"
                Text(
                    text = "Login via username",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = LoopinIrisGlow,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { isUsernameLoginMode = true }
                        .padding(4.dp)
                        .testTag("login_via_username_hyperlink")
                )
            } else {
                // Username Fast Login Form
                GlassTextField(
                    value = inputUsername,
                    onValueChange = { inputUsername = it },
                    label = "Username (e.g. alex)",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassButton(
                    onClick = {
                        val uName = inputUsername.trim().lowercase()
                        if (uName.length >= 2) {
                            isLoading = true
                            val syntheticEmail = "$uName@loopin.app"
                            val syntheticPass = "LoopinPass@$uName"
                            scope.launch {
                                try {
                                    Firebase.auth.signInWithEmailAndPassword(syntheticEmail, syntheticPass).await()
                                    isLoading = false
                                    onUsernameLoginMode(uName)
                                } catch (_: Exception) {
                                    try {
                                        Firebase.auth.createUserWithEmailAndPassword(syntheticEmail, syntheticPass).await()
                                        isLoading = false
                                        onUsernameLoginMode(uName)
                                    } catch (err: Exception) {
                                        isLoading = false
                                        onAuthError(err.localizedMessage ?: "Failed to log in with username")
                                    }
                                }
                            }
                        } else {
                            onAuthError("Please enter a username (at least 2 characters)")
                        }
                    },
                    text = "Enter via Username",
                    isPrimary = true,
                    isLoading = isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_login_submit_button")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Back to Email Login",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = LoopinIrisGlow,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { isUsernameLoginMode = false }
                        .padding(4.dp)
                        .testTag("login_via_email_hyperlink")
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FF4B6E))
                        .border(1.dp, Color(0x55FF4B6E), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF7E96),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassOnboardingTutorialCard(
    onNext: () -> Unit
) {
    var tutorialIndex by remember { mutableIntStateOf(0) }

    val tutorials = listOf(
        Triple(
            "Squad Glass Rooms",
            "Enter a shared Squad ID. You and everyone in your squad get bundled directly into private communication rooms and group DMs.",
            Icons.Default.Group
        ),
        Triple(
            "Stories & Instants ⚡",
            "Share fleeting 24-hour visual stories and drop rapid 'Instants' directly to squad peers without feed clutter.",
            Icons.Default.PhotoCamera
        ),
        Triple(
            "Music Notes 🎵",
            "Pin your current soundtrack and mood. Your 24-hour note with song title & artist floats directly above your messages.",
            Icons.Default.MusicNote
        )
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isElevated = true
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Frosted Glass Orb Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0x247E74F1))
                    .border(1.5.dp, GlassStrokeBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tutorials[tutorialIndex].third,
                    contentDescription = null,
                    tint = LoopinIrisGlow,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step dots
            Row(horizontalArrangement = Arrangement.Center) {
                tutorials.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == tutorialIndex) 16.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(if (index == tutorialIndex) LoopinIrisGlow else Color(0x33FFFFFF))
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = tutorials[tutorialIndex].first,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tutorials[tutorialIndex].second,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            GlassButton(
                onClick = {
                    if (tutorialIndex < tutorials.size - 1) {
                        tutorialIndex++
                    } else {
                        onNext()
                    }
                },
                text = if (tutorialIndex < tutorials.size - 1) "Next" else "Setup Profile & Squad",
                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                isPrimary = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_next_button")
            )
        }
    }
}

@Composable
private fun GlassProfileSetupCard(
    username: String,
    onUsernameChange: (String) -> Unit,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    squadId: String,
    onSquadIdChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    selectedAvatar: String,
    onSelectAvatar: (String) -> Unit,
    onSubmit: () -> Unit,
    errorMessage: String?
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
                text = "Profile & Squad Setup",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Input a Squad ID to get included in group rooms and DMs with peers.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            val context = LocalContext.current
            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) onSelectAvatar(uri.toString())
            }

            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicturePreview()
            ) { bitmap: Bitmap? ->
                if (bitmap != null) {
                    try {
                        val file = File(context.cacheDir, "avatar_onboard_${System.currentTimeMillis()}.jpg")
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        onSelectAvatar(file.toURI().toString())
                    } catch (_: Exception) {}
                }
            }

            // Profile Picture
            Text(
                text = "Profile Picture (Custom or Plain User Icon)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Avatar Preview Circle
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
                    .border(2.dp, FrostedStoryRingGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selectedAvatar.isNotBlank()) {
                    AsyncImage(
                        model = selectedAvatar,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Plain User Icon",
                        tint = LoopinSilver,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                GlassButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    text = "Gallery 🖼️",
                    isPrimary = false,
                    modifier = Modifier.testTag("onboard_avatar_gallery_button")
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlassButton(
                    onClick = { cameraLauncher.launch(null) },
                    text = "Camera 📷",
                    isPrimary = false,
                    modifier = Modifier.testTag("onboard_avatar_camera_button")
                )
                if (selectedAvatar.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassButton(
                        onClick = { onSelectAvatar("") },
                        text = "Plain Icon 👤",
                        isPrimary = false,
                        modifier = Modifier.testTag("onboard_avatar_plain_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            GlassTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = "Username (e.g. alex)",
                modifier = Modifier.testTag("username_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            GlassTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = "Display Name",
                modifier = Modifier.testTag("display_name_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Squad ID Field
            Column(modifier = Modifier.fillMaxWidth()) {
                GlassTextField(
                    value = squadId,
                    onValueChange = { onSquadIdChange(it.uppercase()) },
                    label = "Squad ID (e.g. ALPHA, STUDIO, CAMPUS)",
                    modifier = Modifier.testTag("squad_id_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    POPULAR_SQUADS.take(4).forEach { tag ->
                        val isSelected = squadId.equals(tag, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x337E74F1) else Color(0x14FFFFFF))
                                .border(1.dp, if (isSelected) LoopinIrisGlow else GlassBorder, RoundedCornerShape(8.dp))
                                .clickable { onSquadIdChange(tag) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) LoopinIrisGlow else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            GlassTextField(
                value = bio,
                onValueChange = onBioChange,
                label = "Bio",
                singleLine = false,
                modifier = Modifier.testTag("bio_input")
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassButton(
                onClick = onSubmit,
                text = "Enter Loopin",
                isPrimary = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enter_squad_button")
            )
        }
    }
}

private fun handleGoogleSignIn(
    context: Context,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val credentialManager = CredentialManager.create(context)
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onSuccess()
            } else {
                onError("Unexpected credential format received.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled: ${e.message}", e)
            onCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onError(e.localizedMessage ?: "Sign in failed")
        }
    }
}
