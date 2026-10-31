package com.example.ui.profile

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LoopinAvatar
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinLikeRed
import com.example.ui.theme.LoopinOnlineGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Full-size user profile page:
 * - Opened on demand or expanded from smaller profile cards across the app
 * - Sticky glass header with username, squad badge, and overflow options
 * - Dynamic stat counters (Posts, Followers, Following)
 * - Bio and online status
 * - Action buttons: Follow / Following (animated), Send Message (opens 1-on-1 DM), Share Profile
 * - 3-column Instagram grid for posts, Reels/Instants, and Tagged items
 * - Tapping any post opens a full post detail viewer
 */
@Composable
fun FullUserProfileScreen(
    user: UserProfile,
    currentUser: UserProfile?,
    userPosts: List<Post>,
    isDarkTheme: Boolean = true,
    onToggleFollow: (String) -> Unit,
    onToggleBlock: (String) -> Unit,
    onStartDirectMessage: (userId: String, username: String) -> Unit,
    onLikePost: (Post) -> Unit = {},
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDark = LocalIsDarkTheme.current
    val isMe = currentUser?.userId == user.userId
    val isFollowing = currentUser?.following?.contains(user.userId) == true
    val isBlocked = currentUser?.blockedUsers?.contains(user.userId) == true

    var selectedTab by remember { mutableIntStateOf(0) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var selectedPostForDetail by remember { mutableStateOf<Post?>(null) }

    LiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("full_user_profile_screen")
        ) {
            // Sticky Glass Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(if (isDark) Color(0x33090B12) else Color(0xEEFFFFFF))
                    .border(0.5.dp, if (isDark) Color(0x18FFFFFF) else Color(0x12000000), RoundedCornerShape(0.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("full_profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isDark) TextPrimary else Color(0xFF141728)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = user.username,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimary else Color(0xFF141728)
                    )

                    if (user.squadId.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x287E74F1))
                                .border(0.5.dp, LoopinIrisGlow, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = user.squadId,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LoopinIrisGlow
                            )
                        }
                    }
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier.testTag("full_profile_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = if (isDark) TextPrimary else Color(0xFF141728)
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy profile link") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                clipboardManager.setText(AnnotatedString("https://loopin.app/@${user.username}"))
                                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                                showOptionsMenu = false
                            }
                        )
                        if (!isMe) {
                            DropdownMenuItem(
                                text = { Text(if (isBlocked) "Unblock user" else "Block user", color = LoopinLikeRed) },
                                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = LoopinLikeRed) },
                                onClick = {
                                    onToggleBlock(user.userId)
                                    showOptionsMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Scrollable Profile Content (3-column grid)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Span Item
                item(span = { GridItemSpan(3) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        // Avatar & Statistics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Avatar
                            Box(contentAlignment = Alignment.BottomEnd) {
                                LoopinAvatar(
                                    avatarUrl = user.avatarUrl,
                                    size = 86.dp,
                                    borderWidth = 2.5.dp,
                                    borderBrush = FrostedStoryRingGradient
                                )

                                // Active online pulse dot
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (user.isOnline) LoopinOnlineGreen else Color(0xFF6B7280))
                                        .border(2.dp, if (isDark) Color(0xFF090B12) else Color.White, CircleShape)
                                )
                            }

                            // 3 Stat Counters (Posts, Followers, Following)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 24.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatCounter(count = userPosts.size.toString(), label = "Posts")
                                StatCounter(count = user.followers.size.toString(), label = "Followers")
                                StatCounter(count = user.following.size.toString(), label = "Following")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Display Name, Squad & Bio
                        Text(
                            text = user.displayName.ifBlank { user.username },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextPrimary else Color(0xFF141728)
                        )

                        if (user.bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.bio,
                                fontSize = 13.sp,
                                color = if (isDark) TextSecondary else Color(0xFF4B5563),
                                lineHeight = 18.sp
                            )
                        }

                        // Online Status Label
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (user.isOnline) LoopinOnlineGreen else Color(0xFF9CA3AF))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (user.isOnline) "Active now" else "Offline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (user.isOnline) LoopinOnlineGreen else TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Primary Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isMe) {
                                // Current user actions
                                GlassButton(
                                    onClick = { Toast.makeText(context, "Edit your profile in Settings", Toast.LENGTH_SHORT).show() },
                                    text = "Edit Profile",
                                    modifier = Modifier.weight(1f)
                                )
                                GlassButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("https://loopin.app/@${user.username}"))
                                        Toast.makeText(context, "Profile link copied", Toast.LENGTH_SHORT).show()
                                    },
                                    text = "Share Profile",
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Other user actions: Follow + Message
                                val followScale by animateFloatAsState(
                                    targetValue = if (isFollowing) 0.98f else 1.0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                    label = "followScale"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .scale(followScale)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isFollowing) {
                                                if (isDark) Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x33FFFFFF)))
                                                else Brush.linearGradient(listOf(Color(0x18000000), Color(0x18000000)))
                                            } else {
                                                Brush.linearGradient(listOf(LoopinIris, LoopinIrisGlow))
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isFollowing) GlassBorder else LoopinIrisGlow,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onToggleFollow(user.userId) }
                                        .testTag("full_profile_follow_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isFollowing) "Following" else "Follow",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFollowing) (if (isDark) TextPrimary else Color(0xFF141728)) else Color.White
                                    )
                                }

                                // Message button (opens direct private DM)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) Color(0x28FFFFFF) else Color(0x12000000))
                                        .border(1.dp, if (isDark) GlassBorder else Color(0x18000000), RoundedCornerShape(12.dp))
                                        .clickable {
                                            onDismiss()
                                            onStartDirectMessage(user.userId, user.username)
                                        }
                                        .testTag("full_profile_message_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = null,
                                            tint = if (isDark) TextPrimary else Color(0xFF141728),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Message",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) TextPrimary else Color(0xFF141728)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Profile Tabs (Posts, Instants, Tagged)
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = LoopinIrisGlow,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = LoopinIrisGlow,
                                    height = 2.dp
                                )
                            },
                            divider = {
                                Divider(color = if (isDark) Color(0x18FFFFFF) else Color(0x12000000))
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.GridOn,
                                        contentDescription = "Posts Grid",
                                        tint = if (selectedTab == 0) LoopinIrisGlow else TextMuted
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = "Instants",
                                        tint = if (selectedTab == 1) LoopinIrisGlow else TextMuted
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentInd,
                                        contentDescription = "Tagged",
                                        tint = if (selectedTab == 2) LoopinIrisGlow else TextMuted
                                    )
                                }
                            )
                        }
                    }
                }

                // Grid Content Based on Active Tab
                when (selectedTab) {
                    0 -> {
                        // TAB 0: Posts Grid
                        if (userPosts.isEmpty()) {
                            item(span = { GridItemSpan(3) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridOn,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No posts yet",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) TextPrimary else Color(0xFF141728)
                                    )
                                    Text(
                                        text = "When @${user.username} shares photos, you'll see them here.",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            items(userPosts, key = { it.id }) { post ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .padding(1.dp)
                                        .background(if (isDark) Color(0x33141728) else Color(0xFFF1F3F9))
                                        .clickable { selectedPostForDetail = post },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (post.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = post.imageUrl,
                                            contentDescription = post.caption,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Text-only post thumbnail
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(listOf(Color(0x287E74F1), Color(0x187E74F1)))
                                                )
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = post.caption,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isDark) TextPrimary else Color(0xFF141728),
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // TAB 1: Instants / Reels
                        item(span = { GridItemSpan(3) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = LoopinIrisGlow,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Instants yet",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) TextPrimary else Color(0xFF141728)
                                )
                                Text(
                                    text = "24h flash updates and stories by @${user.username}.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    2 -> {
                        // TAB 2: Tagged
                        item(span = { GridItemSpan(3) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentInd,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No tagged posts",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) TextPrimary else Color(0xFF141728)
                                )
                                Text(
                                    text = "Photos and squads tagging @${user.username} will appear here.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Post Detail Modal when tapping on any post in the grid
    if (selectedPostForDetail != null) {
        val post = selectedPostForDetail!!
        var postLiked by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { selectedPostForDetail = null }) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                isElevated = true
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LoopinAvatar(avatarUrl = user.avatarUrl, size = 34.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user.username,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) TextPrimary else Color(0xFF141728)
                            )
                        }

                        IconButton(
                            onClick = { selectedPostForDetail = null },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (post.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = post.caption,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.1f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Caption
                    if (post.caption.isNotBlank()) {
                        Text(
                            text = post.caption,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) TextPrimary else Color(0xFF141728),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Like action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                postLiked = !postLiked
                                if (postLiked) onLikePost(post)
                            }
                        ) {
                            Icon(
                                imageVector = if (postLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (postLiked) LoopinLikeRed else TextPrimary
                            )
                        }
                        Text(
                            text = "${post.likesCount + if (postLiked) 1 else 0} likes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) TextPrimary else Color(0xFF141728)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCounter(count: String, label: String) {
    val isDark = LocalIsDarkTheme.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) TextPrimary else Color(0xFF141728)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isDark) TextSecondary else Color(0xFF5B6178)
        )
    }
}
