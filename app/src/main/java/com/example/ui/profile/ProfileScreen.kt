package com.example.ui.profile

import android.content.Context
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.FrostedStoryRingGradient
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinOnlineGreen
import com.example.ui.theme.LoopinSilver
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    userPosts: List<Post>,
    onOpenSettings: () -> Unit
) {
    LiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .padding(bottom = 75.dp)
        ) {
            // Profile Top Bar with Loopin mark & Settings Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0x18000000))
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
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = userProfile?.username ?: "Profile",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Settings button (Opens Settings page)
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("profile_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = LoopinIrisGlow
                    )
                }
            }

            // Profile Header Glass Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                isElevated = true
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Liquid Frosted Story Ring Avatar with Online Presence indicator
                        Box(
                            modifier = Modifier.size(82.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(78.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x18FFFFFF))
                                    .border(2.5.dp, FrostedStoryRingGradient, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatar = userProfile?.avatarUrl ?: ""
                                if (avatar.isNotBlank()) {
                                    AsyncImage(
                                        model = avatar,
                                        contentDescription = "Profile Avatar",
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Plain User Icon",
                                        tint = LoopinSilver,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }

                            // Online / Offline Status Dot Badge
                            val isOnline = userProfile?.isOnline ?: true
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) LoopinOnlineGreen else TextMuted)
                                    .border(2.dp, Color(0xFF141724), CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Real User Metrics ONLY (NO FAKE FOLLOWERS)
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            RealStatColumn(count = userPosts.size.toString(), label = "Posts")
                            RealStatColumn(count = "#${userProfile?.squadId ?: "ALPHA"}", label = "Squad")
                            val isOnline = userProfile?.isOnline ?: true
                            RealStatColumn(
                                count = if (isOnline) "Online" else "Offline",
                                label = "Status",
                                isBadge = true,
                                isOnline = isOnline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name & Bio
                    Text(
                        text = userProfile?.displayName?.ifBlank { userProfile.username } ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = userProfile?.bio ?: "Loopin squad member",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 3.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Edit Profile & Settings Action Button
                    GlassButton(
                        onClick = onOpenSettings,
                        text = "Edit Profile & Settings",
                        leadingIcon = Icons.Default.Settings,
                        isPrimary = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_settings_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Posts Grid Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Posts Grid",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My Posts (${userPosts.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

            // Posts Grid
            if (userPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Posts Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Share photos and moments directly to your squad feed.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(userPosts, key = { it.id }) { post ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(0.5.dp, GlassBorder, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = post.caption,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealStatColumn(
    count: String,
    label: String,
    isBadge: Boolean = false,
    isOnline: Boolean = true
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isBadge) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isOnline) Color(0x2822C55E) else Color(0x18FFFFFF))
                    .border(
                        0.75.dp,
                        if (isOnline) LoopinOnlineGreen.copy(alpha = 0.6f) else GlassBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) LoopinOnlineGreen else TextMuted)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = count,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOnline) LoopinOnlineGreen else TextMuted
                )
            }
        } else {
            Text(
                text = count,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
