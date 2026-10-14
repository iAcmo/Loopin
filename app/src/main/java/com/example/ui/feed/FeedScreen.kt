package com.example.ui.feed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Post
import com.example.data.model.PostComment
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.SpotifyGreen
import com.example.ui.components.SpotifyTrackPickerDialog
import com.example.util.SpotifyTrack
import java.io.File
import java.io.FileOutputStream
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderStrong
import com.example.ui.theme.GlassFill
import com.example.ui.theme.GlassFillElevated
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinLikeRed
import com.example.ui.theme.LoopinSilver
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    userProfile: UserProfile?,
    posts: List<Post>,
    stories: List<Story>,
    comments: List<PostComment>,
    selectedPostForComments: Post?,
    publicUsers: List<UserProfile> = emptyList(),
    onLikePost: (Post) -> Unit,
    onOpenComments: (Post) -> Unit,
    onCloseComments: () -> Unit,
    onAddComment: (String) -> Unit,
    onOpenStory: (Story) -> Unit,
    onCreateStory: (mediaUrl: String, caption: String, songTitle: String, songArtist: String) -> Unit,
    onCreatePost: (caption: String, imageUrl: String) -> Unit,
    onNavigateToDMs: () -> Unit,
    onOpenUserProfile: (username: String) -> Unit = {}
) {
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var showSearchUsersDialog by remember { mutableStateOf(false) }

    LiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
        ) {
            // Glassmorphic Top App Bar with Search by Username and Loopin Logo
            LoopinTopAppBar(
                onNavigateToDMs = onNavigateToDMs,
                onAddStory = { showCreateStoryDialog = true },
                onOpenSearch = { showSearchUsersDialog = true }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                // Stories Shelf (Frosted Liquid Glass Rings)
                item {
                    StoriesTray(
                        currentUserProfile = userProfile,
                        stories = stories,
                        onAddStoryClick = { showCreateStoryDialog = true },
                        onStoryClick = onOpenStory
                    )
                    Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)
                }

                if (posts.isEmpty()) {
                    item {
                        EmptyFeedGlassCard(
                            squadId = userProfile?.squadId ?: "SQUAD",
                            onCreatePostClick = { showCreatePostDialog = true }
                        )
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        GlassPostCard(
                            post = post,
                            onLike = { onLikePost(post) },
                            onComment = { onOpenComments(post) },
                            onShareToDM = onNavigateToDMs,
                            onOpenAuthorProfile = { onOpenUserProfile(post.username) }
                        )
                    }
                }
            }
        }

        // Sleek Frosted Floating Action Button
        FloatingActionButton(
            onClick = { showCreatePostDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 90.dp)
                .testTag("create_post_fab"),
            containerColor = LoopinIris,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Post",
                modifier = Modifier.size(26.dp)
            )
        }

        // Search Users Dialog (Search by username feature)
        if (showSearchUsersDialog) {
            SearchUsersGlassModal(
                publicUsers = publicUsers,
                onSelectUser = { user ->
                    showSearchUsersDialog = false
                    onOpenUserProfile(user.username)
                },
                onDismiss = { showSearchUsersDialog = false }
            )
        }

        // Create Post Modal
        if (showCreatePostDialog) {
            CreatePostGlassModal(
                onDismiss = { showCreatePostDialog = false },
                onCreate = { caption, imageUrl ->
                    onCreatePost(caption, imageUrl)
                    showCreatePostDialog = false
                }
            )
        }

        // Create Story Modal
        if (showCreateStoryDialog) {
            CreateStoryGlassModal(
                onDismiss = { showCreateStoryDialog = false },
                onCreate = { mediaUrl, caption, songTitle, songArtist ->
                    onCreateStory(mediaUrl, caption, songTitle, songArtist)
                    showCreateStoryDialog = false
                }
            )
        }

        // Comments Bottom Sheet
        if (selectedPostForComments != null) {
            GlassCommentsBottomSheet(
                post = selectedPostForComments,
                comments = comments,
                onDismiss = onCloseComments,
                onAddComment = onAddComment,
                onOpenUserProfile = onOpenUserProfile
            )
        }
    }
}

@Composable
private fun LoopinTopAppBar(
    onNavigateToDMs: () -> Unit,
    onAddStory: () -> Unit,
    onOpenSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(60.dp)
            .background(Color(0x16000000))
            .border(width = 0.5.dp, color = Color(0x14FFFFFF), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_loopin_logo),
                contentDescription = "Loopin Logo",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "loopin",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier.testTag("feed_search_users_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Users",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onAddStory,
                modifier = Modifier.testTag("add_story_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Camera",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onNavigateToDMs,
                modifier = Modifier.testTag("dms_button")
            ) {
                Box {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Direct Messages",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LoopinIrisGlow)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }


@Composable
private fun StoriesTray(
    currentUserProfile: UserProfile?,
    stories: List<Story>,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Story) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.width(4.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onAddStoryClick() }
                    .testTag("user_story_circle")
            ) {
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUserProfile?.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUserProfile!!.avatarUrl,
                            contentDescription = "Your Story",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(1.dp, GlassBorder, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .border(1.dp, GlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Plain User Icon",
                                tint = LoopinSilver,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(LoopinIris)
                            .border(1.5.dp, Color(0xFF090A10), CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your loop",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        items(stories) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onStoryClick(story) }
                    .testTag("story_item_${story.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .border(2.dp, FrostedStoryRingGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (story.userAvatar.isNotBlank()) {
                        AsyncImage(
                            model = story.userAvatar,
                            contentDescription = story.username,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Plain User Icon",
                                tint = LoopinSilver,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.username.take(9),
                    fontSize = 11.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GlassPostCard(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShareToDM: () -> Unit,
    onOpenAuthorProfile: () -> Unit = {}
) {
    var isLiked by remember { mutableStateOf(false) }
    var showBigHeart by remember { mutableStateOf(false) }

    val heartScale by animateFloatAsState(
        targetValue = if (showBigHeart) 1.2f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heartScale"
    )

    LaunchedEffect(showBigHeart) {
        if (showBigHeart) {
            delay(700)
            showBigHeart = false
        }
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.clickable { onOpenAuthorProfile() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (post.userAvatar.isNotBlank()) {
                        AsyncImage(
                            model = post.userAvatar,
                            contentDescription = post.username,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, GlassBorder, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .border(1.dp, GlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LoopinSilver,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.username,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (post.communityName.isNotBlank()) {
                            Text(
                                text = post.communityName,
                                fontSize = 11.sp,
                                color = LoopinIrisGlow
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Post Visual with Double-Tap Heart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .clip(RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!isLiked) {
                                    isLiked = true
                                    onLike()
                                }
                                showBigHeart = true
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (post.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Post photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0x287E74F1), Color(0x181DB954))))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.caption.ifBlank { "Squad update from ${post.username}" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (showBigHeart) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = LoopinLikeRed,
                        modifier = Modifier
                            .size(90.dp)
                            .scale(heartScale)
                    )
                }
            }

            // Glass Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            if (isLiked) onLike()
                        },
                        modifier = Modifier.testTag("like_post_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) LoopinLikeRed else TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onComment,
                        modifier = Modifier.testTag("comment_post_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onShareToDM,
                        modifier = Modifier.testTag("share_post_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Share",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Caption & Likes
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)) {
                val totalLikes = post.likesCount + if (isLiked) 1 else 0
                if (totalLikes > 0) {
                    Text(
                        text = "$totalLikes likes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (post.caption.isNotBlank()) {
                    Row {
                        Text(
                            text = post.username + " ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoopinIrisGlow,
                            modifier = Modifier.clickable { onOpenAuthorProfile() }
                        )
                        Text(
                            text = post.caption,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                }

                if (post.commentsCount > 0) {
                    Text(
                        text = "View all ${post.commentsCount} comments",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier
                            .padding(top = 4.dp, bottom = 10.dp)
                            .clickable { onComment() }
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedGlassCard(
    squadId: String,
    onCreatePostClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        isElevated = true
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0x227E74F1))
                    .border(1.dp, GlassStrokeBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = LoopinIrisGlow,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Squad #$squadId Feed is Fresh",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Share photos or drops to your squad loop.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            GlassButton(
                onClick = onCreatePostClick,
                text = "Create First Post",
                isPrimary = true
            )
        }
    }
}

@Composable
private fun CreatePostGlassModal(
    onDismiss: () -> Unit,
    onCreate: (caption: String, imageUrl: String) -> Unit
) {
    val context = LocalContext.current
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    var caption by remember { mutableStateOf("") }
    var selectedPhotoUrl by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedPhotoUrl = uri.toString()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "post_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                selectedPhotoUrl = file.toURI().toString()
            } catch (_: Exception) {}
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Squad Post",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) TextPrimary else Color(0xFF141728)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedPhotoUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = selectedPhotoUrl,
                            contentDescription = "Post Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
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
                                text = "Take a photo or choose from gallery",
                                fontSize = 12.sp,
                                color = if (isDark) TextSecondary else Color(0xFF5B6178)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = "Write a caption...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_caption_input")
                )
            }
        },
        confirmButton = {
            GlassButton(
                onClick = {
                    if (selectedPhotoUrl.isNotBlank()) {
                        onCreate(caption, selectedPhotoUrl)
                    }
                },
                text = "Share Post",
                isPrimary = true,
                enabled = selectedPhotoUrl.isNotBlank(),
                modifier = Modifier.testTag("submit_post_button")
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

@Composable
private fun CreateStoryGlassModal(
    onDismiss: () -> Unit,
    onCreate: (mediaUrl: String, caption: String, songTitle: String, songArtist: String) -> Unit
) {
    val context = LocalContext.current
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    var caption by remember { mutableStateOf("") }
    var selectedMediaUrl by remember { mutableStateOf("") }
    var selectedSongTitle by remember { mutableStateOf("") }
    var selectedSongArtist by remember { mutableStateOf("") }
    var showSpotifyPicker by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedMediaUrl = uri.toString()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "story_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                selectedMediaUrl = file.toURI().toString()
            } catch (_: Exception) {}
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Add to Squad Story",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedMediaUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = selectedMediaUrl,
                            contentDescription = "Story Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (selectedSongTitle.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xCC1DB954))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$selectedSongTitle • $selectedSongArtist",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
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
                                text = "Take a photo or choose story media",
                                fontSize = 12.sp,
                                color = if (isDark) TextSecondary else Color(0xFF5B6178)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Attach Spotify Soundtrack button
                GlassButton(
                    onClick = { showSpotifyPicker = true },
                    text = if (selectedSongTitle.isNotBlank()) "🎵 $selectedSongTitle (Change)" else "Attach Spotify Soundtrack 🎵",
                    leadingIcon = Icons.Default.MusicNote,
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = "Story caption (optional)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("story_caption_input")
                )
            }
        },
        confirmButton = {
            GlassButton(
                onClick = {
                    if (selectedMediaUrl.isNotBlank()) {
                        onCreate(selectedMediaUrl, caption.trim(), selectedSongTitle, selectedSongArtist)
                    }
                },
                text = "Post Story",
                isPrimary = true,
                enabled = selectedMediaUrl.isNotBlank(),
                modifier = Modifier.testTag("submit_story_button")
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

    if (showSpotifyPicker) {
        SpotifyTrackPickerDialog(
            onTrackSelected = { track ->
                selectedSongTitle = track.title
                selectedSongArtist = track.artist
                showSpotifyPicker = false
            },
            onDismiss = { showSpotifyPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassCommentsBottomSheet(
    post: Post,
    comments: List<PostComment>,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onOpenUserProfile: (String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF121420),
        scrimColor = Color(0x99000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Comments",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )

            Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(vertical = 8.dp)
            ) {
                if (comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No comments yet. Start the conversation!",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    items(comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(modifier = Modifier.clickable { onOpenUserProfile(comment.username) }) {
                                if (comment.userAvatar.isNotBlank()) {
                                    AsyncImage(
                                        model = comment.userAvatar,
                                        contentDescription = comment.username,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, GlassBorder, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x22FFFFFF))
                                            .border(1.dp, GlassBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = LoopinSilver,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comment.username,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LoopinIrisGlow,
                                    modifier = Modifier.clickable { onOpenUserProfile(comment.username) }
                                )
                                Text(
                                    text = comment.text,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = "Add a comment...",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_comment_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onAddComment(commentText.trim())
                            commentText = ""
                        }
                    },
                    modifier = Modifier.testTag("submit_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Post comment",
                        tint = if (commentText.isNotBlank()) LoopinIrisGlow else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchUsersGlassModal(
    publicUsers: List<UserProfile>,
    onSelectUser: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, publicUsers) {
        if (query.isBlank()) {
            publicUsers
        } else {
            publicUsers.filter {
                it.username.contains(query, ignoreCase = true) ||
                        it.displayName.contains(query, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = LoopinIrisGlow,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search Users",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                GlassTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "Search by @username or name...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feed_user_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (query.isBlank()) "No users found" else "No users matching \"$query\"",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        items(filtered, key = { it.userId }) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x12FFFFFF))
                                    .clickable { onSelectUser(user) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (user.avatarUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = user.avatarUrl,
                                            contentDescription = user.username,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = LoopinSilver,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(if (user.isOnline) LoopinOnlineGreen else TextMuted)
                                            .border(1.dp, Color(0xFF141724), CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.displayName.ifBlank { user.username },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        if (user.isPrivate) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Private",
                                                tint = LoopinIrisGlow,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "@${user.username}",
                                        fontSize = 11.sp,
                                        color = LoopinIrisGlow
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = Color(0xFF141724),
        shape = RoundedCornerShape(22.dp)
    )
}
