package com.example.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CommentsDisabled
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassStrokeBrush
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.LoopinLikeRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Full Instagram-grade Post Options Bottom Sheet:
 * - Top quick-action icon buttons (Save, Share to DM, Copy Link)
 * - Authorship-aware contextual menu:
 *   - Author: Edit caption, Delete post, Hide like count, Turn off comments, Pin to profile, Archive
 *   - Others: View full profile, Add to favorites, Hide post, Unfollow, About account, Block
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOptionsBottomSheet(
    post: Post,
    currentUser: UserProfile?,
    onEditCaption: (Post, String) -> Unit,
    onDeletePost: (Post) -> Unit,
    onSharePostToDM: (Post) -> Unit,
    onViewUserProfile: (username: String) -> Unit,
    onToggleFollow: (userId: String) -> Unit,
    onToggleBlock: (userId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDark = LocalIsDarkTheme.current
    val isMyPost = currentUser?.userId == post.userId
    val isFollowing = currentUser?.following?.contains(post.userId) == true

    var isSaved by remember { mutableStateOf(false) }
    var hideLikeCount by remember { mutableStateOf(false) }
    var commentsDisabled by remember { mutableStateOf(false) }
    var isPinned by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editCaptionText by remember { mutableStateOf(post.caption) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sheetBg = if (isDark) Color(0xF2141724) else Color(0xF9FFFFFF)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x38FFFFFF) else Color(0x28000000))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("post_options_bottom_sheet")
        ) {
            // Post Mini Header Summary Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Post by @${post.username}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimary else Color(0xFF141728)
                    )
                    Text(
                        text = post.caption.ifBlank { "Squad update" },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Top Quick Action Row (Save, Share, Copy Link)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. Bookmark / Save
                QuickActionButton(
                    icon = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    label = if (isSaved) "Saved" else "Save",
                    tint = if (isSaved) LoopinIrisGlow else (if (isDark) TextPrimary else Color(0xFF141728)),
                    onClick = {
                        isSaved = !isSaved
                        Toast.makeText(context, if (isSaved) "Saved to your bookmarks" else "Removed from saved", Toast.LENGTH_SHORT).show()
                    }
                )

                // 2. Share to DM
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    label = "Share",
                    tint = if (isDark) TextPrimary else Color(0xFF141728),
                    onClick = {
                        onSharePostToDM(post)
                        onDismiss()
                    }
                )

                // 3. Copy Link
                QuickActionButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Link",
                    tint = if (isDark) TextPrimary else Color(0xFF141728),
                    onClick = {
                        clipboardManager.setText(AnnotatedString("https://loopin.app/post/${post.id}"))
                        Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = if (isDark) Color(0x18FFFFFF) else Color(0x12000000), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Contextual Actions List
            if (isMyPost) {
                // MY POST ACTIONS
                PostMenuActionRow(
                    icon = Icons.Default.Edit,
                    label = "Edit caption",
                    onClick = { showEditDialog = true }
                )

                PostMenuActionRow(
                    icon = if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                    label = if (isPinned) "Unpin from profile" else "Pin to your profile",
                    onClick = {
                        isPinned = !isPinned
                        Toast.makeText(context, if (isPinned) "Pinned to profile" else "Unpinned", Toast.LENGTH_SHORT).show()
                    }
                )

                PostMenuActionRow(
                    icon = if (isArchived) Icons.Default.Archive else Icons.Default.Archive,
                    label = if (isArchived) "Show in feed" else "Archive post",
                    onClick = {
                        isArchived = !isArchived
                        Toast.makeText(context, if (isArchived) "Post moved to archive" else "Restored to feed", Toast.LENGTH_SHORT).show()
                    }
                )

                PostMenuActionRow(
                    icon = if (hideLikeCount) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                    label = if (hideLikeCount) "Show like count" else "Hide like count to others",
                    onClick = {
                        hideLikeCount = !hideLikeCount
                        Toast.makeText(context, if (hideLikeCount) "Like counts hidden" else "Like counts visible", Toast.LENGTH_SHORT).show()
                    }
                )

                PostMenuActionRow(
                    icon = Icons.Default.CommentsDisabled,
                    label = if (commentsDisabled) "Turn on commenting" else "Turn off commenting",
                    onClick = {
                        commentsDisabled = !commentsDisabled
                        Toast.makeText(context, if (commentsDisabled) "Commenting turned off" else "Commenting turned on", Toast.LENGTH_SHORT).show()
                    }
                )

                Divider(color = if (isDark) Color(0x18FFFFFF) else Color(0x12000000), thickness = 0.5.dp)

                // Delete Post (Destructive in Red)
                PostMenuActionRow(
                    icon = Icons.Default.Delete,
                    label = "Delete post",
                    textColor = LoopinLikeRed,
                    iconTint = LoopinLikeRed,
                    onClick = { showDeleteConfirmDialog = true }
                )
            } else {
                // OTHER USER'S POST ACTIONS
                PostMenuActionRow(
                    icon = Icons.Default.Person,
                    label = "About this account & full profile",
                    onClick = {
                        onDismiss()
                        onViewUserProfile(post.username)
                    }
                )

                PostMenuActionRow(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                    onClick = {
                        isFavorite = !isFavorite
                        Toast.makeText(context, if (isFavorite) "Added @${post.username} to Favorites" else "Removed from Favorites", Toast.LENGTH_SHORT).show()
                    }
                )

                PostMenuActionRow(
                    icon = Icons.Default.VisibilityOff,
                    label = "Hide post / Not interested",
                    onClick = {
                        Toast.makeText(context, "We'll show fewer posts like this", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                PostMenuActionRow(
                    icon = Icons.Default.PersonRemove,
                    label = if (isFollowing) "Unfollow @${post.username}" else "Follow @${post.username}",
                    onClick = {
                        onToggleFollow(post.userId)
                        onDismiss()
                    }
                )

                Divider(color = if (isDark) Color(0x18FFFFFF) else Color(0x12000000), thickness = 0.5.dp)

                PostMenuActionRow(
                    icon = Icons.Default.Block,
                    label = "Block @${post.username}",
                    textColor = LoopinLikeRed,
                    iconTint = LoopinLikeRed,
                    onClick = {
                        onToggleBlock(post.userId)
                        Toast.makeText(context, "Blocked @${post.username}", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Caption Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit Caption",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )
            },
            text = {
                OutlinedTextField(
                    value = editCaptionText,
                    onValueChange = { editCaptionText = it },
                    label = { Text("Caption") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) TextPrimary else Color(0xFF141728),
                        unfocusedTextColor = if (isDark) TextPrimary else Color(0xFF141728),
                        focusedBorderColor = LoopinIrisGlow,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEditCaption(post, editCaptionText)
                        showEditDialog = false
                        onDismiss()
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = LoopinIrisGlow)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = if (isDark) Color(0xFF1E2130) else Color.White
        )
    }

    // Delete Post Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Post?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimary else Color(0xFF141728)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this post? This action cannot be undone.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePost(post)
                        showDeleteConfirmDialog = false
                        onDismiss()
                    }
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = LoopinLikeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = if (isDark) Color(0xFF1E2130) else Color.White
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val buttonBg = if (isDark) Color(0x24FFFFFF) else Color(0x10000000)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(buttonBg)
                .border(1.dp, if (isDark) Color(0x1FFFFFFF) else Color(0x14000000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) TextSecondary else Color(0xFF4B5563)
        )
    }
}

@Composable
private fun PostMenuActionRow(
    icon: ImageVector,
    label: String,
    textColor: Color? = null,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val defaultText = if (isDark) TextPrimary else Color(0xFF141728)
    val defaultIcon = if (isDark) TextSecondary else Color(0xFF5B6178)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint ?: defaultIcon,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor ?: defaultText,
            modifier = Modifier.weight(1f)
        )
    }
}
