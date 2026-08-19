package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.PosterTemplate
import com.example.data.model.ProfileType
import com.example.data.model.TemplateAspectRatio
import com.example.data.model.UserProfile
import com.example.data.sample.SampleData
import com.example.ui.components.PosterCard
import com.example.ui.theme.CraftoGold
import com.example.ui.theme.CraftoPrimary
import com.example.ui.theme.CraftoPrimaryContainer
import com.example.ui.theme.CraftoRose
import com.example.ui.viewmodel.PosterViewModel

val categoriesList = listOf(
    "All",
    "Good Morning",
    "Festivals",
    "Motivation",
    "Birthday",
    "Business",
    "Devotional",
    "Special Days",
    "Good Night"
)

val categoryIcons = mapOf(
    "All" to "✨",
    "Good Morning" to "🌅",
    "Festivals" to "🪔",
    "Motivation" to "💪",
    "Birthday" to "🎉",
    "Business" to "💼",
    "Devotional" to "🙏",
    "Special Days" to "🇮🇳",
    "Good Night" to "🌙"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PosterViewModel,
    onNavigateToEditor: (PosterTemplate) -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val templates by viewModel.filteredTemplates.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.onlyFavorites.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }

    val safeProfile = activeProfile ?: SampleData.defaultPersonalProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            color = CraftoPrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.img_app_icon),
                                    contentDescription = "Crafto Logo",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Jaadu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "✨",
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "Festival & Status Maker",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Profile Switcher Quick Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                val targetType = if (safeProfile.profileType == ProfileType.PERSONAL) {
                                    ProfileType.BUSINESS
                                } else {
                                    ProfileType.PERSONAL
                                }
                                viewModel.switchProfileType(targetType)
                            }
                            .border(1.dp, CraftoPrimary, RoundedCornerShape(20.dp))
                            .testTag("quick_profile_switcher_pill"),
                        color = CraftoPrimaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (safeProfile.profileType == ProfileType.BUSINESS) Icons.Default.Business else Icons.Default.Person,
                                contentDescription = null,
                                tint = CraftoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (safeProfile.profileType == ProfileType.BUSINESS) "Business" else "Personal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CraftoPrimary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch",
                                tint = CraftoPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Search Button
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("home_search_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    // Favorites Button
                    IconButton(
                        onClick = { viewModel.toggleFavoritesOnly() },
                        modifier = Modifier.testTag("home_fav_filter_btn")
                    ) {
                        Icon(
                            imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (onlyFavorites) CraftoRose else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(1), // Full width cards for rich graphic fidelity
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_templates_grid")
        ) {
            // Search Bar (Expandable)
            if (isSearchExpanded) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search morning, diwali, quotes, business...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CraftoPrimary
                        )
                    )
                }
            }

            // Featured Hero Banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                FeaturedTrendingBanner(
                    onBannerClick = {
                        val featured = templates.firstOrNull { it.id == 4L } ?: templates.firstOrNull()
                        featured?.let { onNavigateToEditor(it) }
                    }
                )
            }

            // Categories Row
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explore Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${templates.size} Templates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        items(categoriesList) { cat ->
                            val icon = categoryIcons[cat] ?: "✨"
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { viewModel.setCategory(cat) },
                                label = { Text("$icon $cat", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CraftoPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("category_chip_$cat")
                            )
                        }
                    }

                    // Aspect Ratio Filter Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    ) {
                        FilterChip(
                            selected = selectedAspectRatio == null,
                            onClick = { viewModel.setAspectRatioFilter(null) },
                            label = { Text("All Sizes") }
                        )
                        FilterChip(
                            selected = selectedAspectRatio == TemplateAspectRatio.SQUARE_1_1,
                            onClick = { viewModel.setAspectRatioFilter(TemplateAspectRatio.SQUARE_1_1) },
                            label = { Text("⬛ 1:1 Post") },
                            modifier = Modifier.testTag("filter_ratio_square")
                        )
                        FilterChip(
                            selected = selectedAspectRatio == TemplateAspectRatio.STORY_9_16,
                            onClick = { viewModel.setAspectRatioFilter(TemplateAspectRatio.STORY_9_16) },
                            label = { Text("📱 9:16 Story") },
                            modifier = Modifier.testTag("filter_ratio_story")
                        )
                    }
                }
            }

            // Empty State
            if (templates.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🎨", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No templates found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try selecting another category or clear filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Templates List
                items(templates, key = { it.id }) { template ->
                    PosterCard(
                        template = template,
                        userProfile = safeProfile,
                        onSelect = {
                            viewModel.openEditor(template)
                            onNavigateToEditor(template)
                        },
                        onFavoriteToggle = { id ->
                            viewModel.toggleFavorite(id)
                        },
                        onQuickShare = { t ->
                            viewModel.openEditor(t)
                            viewModel.sharePoster()
                        },
                        onQuickDownload = { t ->
                            viewModel.quickDownloadPoster(t)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedTrendingBanner(
    onBannerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBannerClick() }
            .shadow(10.dp, RoundedCornerShape(24.dp))
            .testTag("featured_trending_banner"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF6750A4), Color(0xFF8B5CF6), Color(0xFFB69DF8))
                    )
                )
        ) {
            // Background image overlay
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.45f
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF6750A4),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "AI MAGIC ON",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0x55000000),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Featured Today ✨",
                            color = CraftoGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Create Festival & Daily Status with Your Photo Cutout",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onBannerClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF21005D)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF6750A4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Make My Poster Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
