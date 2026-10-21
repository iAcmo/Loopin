package com.example.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.ui.components.LoopinAvatar
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LoopinBg
import com.example.ui.theme.LoopinIris
import com.example.ui.theme.LoopinIrisGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class SearchCategory(val title: String) {
    TOP("Top"),
    ACCOUNTS("Accounts"),
    AUDIO("Audio"),
    TAGS("Tags"),
    PLACES("Places")
}

data class ExplorePlace(
    val name: String,
    val country: String,
    val imageUrl: String,
    val postCount: String
)

data class TrendingTag(
    val tag: String,
    val count: String
)

@Composable
fun SearchScreen(
    posts: List<Post>,
    publicUsers: List<UserProfile>,
    onOpenUserProfile: (String) -> Unit,
    onOpenPostComments: (Post) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchCategory.TOP) }

    val trendingTags = remember {
        listOf(
            TrendingTag("space", "48.2k posts"),
            TrendingTag("architecture", "32.1k posts"),
            TrendingTag("design", "85.4k posts"),
            TrendingTag("cinematic", "19.8k posts"),
            TrendingTag("minimal", "64.3k posts"),
            TrendingTag("photography", "112k posts"),
            TrendingTag("loopin", "15.7k posts"),
            TrendingTag("nature", "93.0k posts"),
            TrendingTag("tokyo", "28.5k posts")
        )
    }

    val samplePlaces = remember {
        listOf(
            ExplorePlace("Seceda Ridge", "Dolomites, Italy", "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800&auto=format&fit=crop&q=80", "12.4k photos"),
            ExplorePlace("Shibuya Crossing", "Tokyo, Japan", "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800&auto=format&fit=crop&q=80", "94.1k photos"),
            ExplorePlace("Kyoto Gardens", "Kyoto, Japan", "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800&auto=format&fit=crop&q=80", "41.6k photos"),
            ExplorePlace("Milky Way Observatory", "Atacama, Chile", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80", "28.0k photos")
        )
    }

    val curatedImages = remember {
        listOf(
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80", // Space galaxy
            "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80", // Neon space
            "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800&auto=format&fit=crop&q=80", // Seceda mountain
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80", // Landscape lake
            "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=800&auto=format&fit=crop&q=80", // Gradient abstract
            "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800&auto=format&fit=crop&q=80", // Tokyo
            "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80", // Concert stage
            "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800&auto=format&fit=crop&q=80", // Music studio
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&auto=format&fit=crop&q=80"  // Night party
        )
    }

    // Filter posts based on search query
    val filteredPosts = remember(posts, searchQuery) {
        if (searchQuery.isBlank()) posts else {
            val q = searchQuery.trim().lowercase().removePrefix("#")
            posts.filter { p ->
                p.caption.lowercase().contains(q) ||
                p.username.lowercase().contains(q) ||
                p.communityName.lowercase().contains(q)
            }
        }
    }

    // Filter accounts based on search query
    val filteredAccounts = remember(publicUsers, searchQuery) {
        if (searchQuery.isBlank()) publicUsers else {
            val q = searchQuery.trim().lowercase().removePrefix("@")
            publicUsers.filter { u ->
                u.username.lowercase().contains(q) ||
                u.displayName.lowercase().contains(q) ||
                u.bio.lowercase().contains(q)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) LoopinBg else Color(0xFFF6F8FC))
    ) {
        // Aesthetic Grid Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 40.dp.toPx()
            val lineColor = if (isDark) Color(0x0CFFFFFF) else Color(0x0C000000)
            val dotColor = if (isDark) Color(0x187E74F1) else Color(0x187E74F1)

            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.75f
                )
                x += gridSpacing
            }

            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.75f
                )
                y += gridSpacing
            }

            // Cross / dots at grid intersections for technical aesthetic
            x = 0f
            while (x < size.width) {
                y = 0f
                while (y < size.height) {
                    drawCircle(
                        color = dotColor,
                        radius = 1.5f,
                        center = Offset(x, y)
                    )
                    y += gridSpacing
                }
                x += gridSpacing
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar with Search Input (matches attached image 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search space, tags, accounts...",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = LoopinIrisGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x24FFFFFF) else Color(0xEEFFFFFF),
                        unfocusedContainerColor = if (isDark) Color(0x14FFFFFF) else Color(0xDDFFFFFF),
                        focusedBorderColor = LoopinIrisGlow,
                        unfocusedBorderColor = if (isDark) GlassBorder else Color(0x18000000),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("search_text_input")
                )
            }

            // Categories horizontal tabs: Top, Accounts, Audio, Tags, Places (matches image 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(LoopinIris, LoopinIrisGlow))
                                } else {
                                    if (isDark) Brush.horizontalGradient(listOf(Color(0x14FFFFFF), Color(0x14FFFFFF)))
                                    else Brush.horizontalGradient(listOf(Color(0xEEFFFFFF), Color(0xEEFFFFFF)))
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) LoopinIrisGlow else if (isDark) GlassBorder else Color(0x14000000),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                            .testTag("search_category_${category.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextPrimary
                        )
                    }
                }
            }

            // Interactive Tags chips bar (quick filter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                trendingTags.forEach { t ->
                    val isTagActive = searchQuery.equals(t.tag, ignoreCase = true) ||
                            searchQuery.equals("#${t.tag}", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isTagActive) Color(0x337E74F1) else if (isDark) Color(0x14FFFFFF) else Color(0xD8FFFFFF))
                            .border(0.75.dp, if (isTagActive) LoopinIrisGlow else if (isDark) GlassBorder else Color(0x10000000), RoundedCornerShape(12.dp))
                            .clickable {
                                searchQuery = if (isTagActive) "" else "#${t.tag}"
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "#${t.tag}",
                            fontSize = 12.sp,
                            fontWeight = if (isTagActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isTagActive) LoopinIrisGlow else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Tab Content
            when (selectedCategory) {
                SearchCategory.TOP -> {
                    // Staggered Explore Grid with mosaic tiles
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_explore_grid")
                    ) {
                        // Combined posts and curated explore tiles
                        val totalItems = (filteredPosts.size + curatedImages.size).coerceAtLeast(12)
                        items(totalItems) { index ->
                            val isLargeSpan = (index % 7 == 1) // occasional 2x2 hero card like Instagram
                            val post = filteredPosts.getOrNull(index % filteredPosts.size.coerceAtLeast(1))
                            val imageUrl = post?.imageUrl?.ifBlank { null }
                                ?: curatedImages[index % curatedImages.size]

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(if (isLargeSpan) 1f else 1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color(0x22141724) else Color(0xFFE2E8F0))
                                    .clickable {
                                        if (post != null) onOpenPostComments(post)
                                    }
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Explore media",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Overlay badge with like count
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x77000000))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${(index * 137 + 42) % 900 + 50}",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SearchCategory.ACCOUNTS -> {
                    // Accounts listing with LoopinAvatar
                    if (filteredAccounts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No creators found matching '$searchQuery'",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredAccounts, key = { it.userId }) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isDark) Color(0x14FFFFFF) else Color(0xEEFFFFFF))
                                        .border(0.75.dp, if (isDark) GlassBorder else Color(0x12000000), RoundedCornerShape(16.dp))
                                    .clickable { onOpenUserProfile(user.username) }
                                    .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LoopinAvatar(
                                        avatarUrl = user.avatarUrl,
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.displayName.ifBlank { user.username },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "@${user.username}",
                                            fontSize = 12.sp,
                                            color = LoopinIrisGlow
                                        )
                                        if (user.bio.isNotBlank()) {
                                            Text(
                                                text = user.bio,
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0x287E74F1))
                                            .border(0.75.dp, LoopinIrisGlow, RoundedCornerShape(14.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "View",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LoopinIrisGlow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SearchCategory.TAGS -> {
                    // Popular Tags list with counts
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(trendingTags) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color(0x14FFFFFF) else Color(0xEEFFFFFF))
                                    .border(0.75.dp, if (isDark) GlassBorder else Color(0x10000000), RoundedCornerShape(14.dp))
                                    .clickable {
                                        searchQuery = "#${item.tag}"
                                        selectedCategory = SearchCategory.TOP
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x287E74F1))
                                        .border(1.dp, LoopinIrisGlow, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tag,
                                        contentDescription = null,
                                        tint = LoopinIrisGlow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "#${item.tag}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = item.count,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                SearchCategory.AUDIO -> {
                    // Audio & Soundtracks with music icon
                    val songs = remember {
                        listOf(
                            Triple("Midnight City", "M83", "Electronic"),
                            Triple("Starboy", "The Weeknd, Daft Punk", "Synthpop"),
                            Triple("Resonance", "HOME", "Chillwave"),
                            Triple("After Dark", "Mr.Kitty", "Darkwave"),
                            Triple("Interstellar Theme", "Hans Zimmer", "Soundtrack"),
                            Triple("Space Song", "Beach House", "Dream Pop")
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(songs) { (title, artist, genre) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color(0x14FFFFFF) else Color(0xEEFFFFFF))
                                    .border(0.75.dp, if (isDark) GlassBorder else Color(0x10000000), RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x281DB954))
                                        .border(1.dp, Color(0xFF1DB954), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color(0xFF1DB954),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$artist • $genre",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                SearchCategory.PLACES -> {
                    // Explore Places
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(samplePlaces) { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color(0x14FFFFFF) else Color(0xEEFFFFFF))
                                    .border(0.75.dp, if (isDark) GlassBorder else Color(0x10000000), RoundedCornerShape(14.dp))
                                    .clickable {
                                        searchQuery = place.name
                                        selectedCategory = SearchCategory.TOP
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = place.imageUrl,
                                    contentDescription = place.name,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = place.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${place.country} • ${place.postCount}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = LoopinIrisGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
