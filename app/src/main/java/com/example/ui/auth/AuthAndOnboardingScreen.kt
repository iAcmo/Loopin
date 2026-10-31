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
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Delete
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

    val currentFirebaseUser = Firebase.auth.currentUser
    androidx.compose.runtime.LaunchedEffect(isSignedIn, currentFirebaseUser) {
        if (isSignedIn && onboardingStep == 0) {
            onboardingStep = 1
        }
        if (currentFirebaseUser != null) {
            if (username.isBlank()) {
                val emailPrefix = currentFirebaseUser.email?.substringBefore("@")?.replace(".", "_")?.lowercase()
                val nameClean = currentFirebaseUser.displayName?.replace(" ", "")?.lowercase()
                username = (emailPrefix ?: nameClean ?: "user").take(20)
            }
            if (displayName.isBlank() && !currentFirebaseUser.displayName.isNullOrBlank()) {
                displayName = currentFirebaseUser.displayName ?: ""
            }
            if (selectedAvatar.isBlank() && currentFirebaseUser.photoUrl != null) {
                selectedAvatar = currentFirebaseUser.photoUrl.toString()
            }
        }
    }

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
                // Frosted Glass Authentication Card with Google Sign-In
                GlassSignInCard(
                    errorMessage = errorMessage,
                    onAuthSuccess = {
                        errorMessage = null
                        onAuthSuccess()
                        onboardingStep = 1
                    },
                    onAuthError = { err -> errorMessage = err }
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
    onAuthError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isElevated = true
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Loopin",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Liquid frosted communication rooms, stories, instants & music notes.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            // Prominent Google Sign-In Button
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
                text = "Sign in with Google",
                leadingIcon = Icons.Default.Person,
                isPrimary = true,
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_signin_button")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Google account securely authenticates your identity, squad rooms, and authentic photo drops.",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FF4B6E))
                        .border(1.dp, Color(0x55FF4B6E), RoundedCornerShape(12.dp))
                        .padding(12.dp)
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
            "Open Communities",
            "Explore open community hubs for technology, music, gaming, and creative arts. Post, comment, and connect freely without limits.",
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

            // Avatar Preview Circle using LoopinAvatar matching attached silhouette image
            com.example.ui.components.LoopinAvatar(
                avatarUrl = selectedAvatar,
                size = 80.dp,
                borderWidth = 2.dp,
                borderBrush = FrostedStoryRingGradient
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    text = "Upload",
                    leadingIcon = Icons.Default.PhotoLibrary,
                    isPrimary = false,
                    modifier = Modifier.testTag("onboard_avatar_gallery_button")
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlassButton(
                    onClick = { cameraLauncher.launch(null) },
                    text = "Camera",
                    leadingIcon = Icons.Default.PhotoCamera,
                    isPrimary = false,
                    modifier = Modifier.testTag("onboard_avatar_camera_button")
                )
                if (selectedAvatar.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassButton(
                        onClick = { onSelectAvatar("") },
                        text = "Remove",
                        leadingIcon = Icons.Default.Delete,
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

            Text(
                text = "Choose a unique username (@username). Nobody else can share this handle.",
                fontSize = 11.sp,
                color = LoopinIrisGlow,
                modifier = Modifier.padding(start = 4.dp)
            )

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

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun attemptAutoSignIn(
    context: Context,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    if (Firebase.auth.currentUser != null) {
        onSuccess()
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        onUnauthenticated()
        return
    }

    val credentialManager = CredentialManager.create(context)
    val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (_: Exception) {
            onUnauthenticated()
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

    val activity = context.findActivity()
    if (activity == null) {
        onError("Cannot initialize Sign-In: Activity context is not available.")
        return
    }

    val credentialManager = CredentialManager.create(context)
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(activity, request)
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
            val msg = e.localizedMessage ?: "Sign in failed"
            if (msg.contains("NoCredentialException") || msg.contains("no credentials", ignoreCase = true)) {
                onError("No Google Account found on this device. Please sign in to a Google account in Android system Settings.")
            } else {
                onError(msg)
            }
        }
    }
}
