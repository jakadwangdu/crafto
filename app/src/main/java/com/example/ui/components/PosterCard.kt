package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CutoutMaskShape
import com.example.data.model.PosterTemplate
import com.example.data.model.TemplateAspectRatio
import com.example.data.model.UserProfile
import com.example.ui.theme.CraftoGold
import com.example.ui.theme.CraftoPrimary
import com.example.ui.theme.CraftoRose
import java.io.File

@Composable
fun PosterCard(
    template: PosterTemplate,
    userProfile: UserProfile,
    onSelect: (PosterTemplate) -> Unit,
    onFavoriteToggle: (Long) -> Unit,
    onQuickShare: (PosterTemplate) -> Unit,
    onQuickDownload: (PosterTemplate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val startColor = remember(template.bgGradientStart) {
        try { Color(android.graphics.Color.parseColor(template.bgGradientStart)) }
        catch (e: Exception) { Color(0xFF1E1B4B) }
    }
    val endColor = remember(template.bgGradientEnd) {
        try { Color(android.graphics.Color.parseColor(template.bgGradientEnd)) }
        catch (e: Exception) { Color(0xFF4338CA) }
    }
    val middleColor = remember(template.bgGradientMiddle) {
        template.bgGradientMiddle?.let {
            try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { null }
        }
    }

    val gradientBrush = remember(startColor, endColor, middleColor) {
        if (middleColor != null) {
            Brush.linearGradient(listOf(startColor, middleColor, endColor))
        } else {
            Brush.linearGradient(listOf(startColor, endColor))
        }
    }

    val isStory = template.aspectRatio == TemplateAspectRatio.STORY_9_16
    val ratio = if (isStory) 9f / 16f else 1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("poster_card_${template.id}")
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Main Poster Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(gradientBrush)
                    .clickable { onSelect(template) }
            ) {
                // Background festival image if available
                val bgDrawableId = when (template.bgImageResName) {
                    "bg_fest_diwali" -> R.drawable.bg_fest_diwali
                    "bg_fest_holi" -> R.drawable.bg_fest_holi
                    "bg_fest_eid" -> R.drawable.bg_fest_eid
                    "bg_fest_ganesh" -> R.drawable.bg_fest_ganesh
                    "bg_fest_newyear" -> R.drawable.bg_fest_newyear
                    "img_hero_banner" -> R.drawable.img_hero_banner
                    else -> null
                }
                if (bgDrawableId != null) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = bgDrawableId),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        contentScale = ContentScale.Crop,
                        alpha = 0.82f
                    )
                    // Subtle dark gradient overlay to ensure text contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x33000000),
                                        Color(0x55000000),
                                        Color(0x99000000)
                                    )
                                )
                            )
                    )
                }

                // Top Badges (Category & Favorite)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Tag
                    Surface(
                        color = Color(0x66000000),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${template.festiveEmoji ?: "✨"} ${template.category}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { onFavoriteToggle(template.id) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x66000000), CircleShape)
                            .testTag("fav_button_${template.id}")
                    ) {
                        Icon(
                            imageVector = if (template.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (template.isFavorite) CraftoRose else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Quote Typography in Center
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val quoteColor = remember(template.quoteTextColor) {
                        try { Color(android.graphics.Color.parseColor(template.quoteTextColor)) }
                        catch (e: Exception) { Color.White }
                    }
                    Text(
                        text = template.quoteText,
                        color = quoteColor,
                        fontSize = if (isStory) 15.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )

                    template.quoteSubText?.let { sub ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sub,
                            color = CraftoGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                // User Portrait / Logo Cutout at Bottom-Right or Center
                val cutoutAlignment = if (isStory) Alignment.BottomCenter else Alignment.BottomEnd
                Box(
                    modifier = Modifier
                        .align(cutoutAlignment)
                        .padding(
                            bottom = if (isStory) 72.dp else 46.dp,
                            end = if (isStory) 0.dp else 12.dp
                        )
                        .size(if (isStory) 86.dp else 70.dp)
                ) {
                    UserCutoutPreview(
                        userProfile = userProfile,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Bottom User Profile Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    ProfileBadgeView(profile = userProfile)
                }
            }

            // Bottom Action Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isStory) "9:16 Story Status" else "1:1 Square Post",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onQuickDownload(template) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quick_download_${template.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Full Image",
                            tint = CraftoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(
                        onClick = { onQuickShare(template) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quick_share_${template.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = CraftoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = { onSelect(template) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CraftoPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("customize_btn_${template.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun UserCutoutPreview(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cutoutFile = userProfile.cutoutUri?.let { File(it) }

    val shape = when (userProfile.cutoutMaskShape) {
        CutoutMaskShape.CIRCLE_RING, CutoutMaskShape.GOLDEN_FRAME -> CircleShape
        CutoutMaskShape.ROUNDED_SQUARE -> RoundedCornerShape(16.dp)
        else -> CircleShape
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = if (userProfile.cutoutMaskShape == CutoutMaskShape.GOLDEN_FRAME) 2.5.dp else 1.5.dp,
                color = if (userProfile.cutoutMaskShape == CutoutMaskShape.GOLDEN_FRAME) CraftoGold else Color.White,
                shape = shape
            )
            .shadow(4.dp, shape),
        contentAlignment = Alignment.Center
    ) {
        if (cutoutFile != null && cutoutFile.exists()) {
            AsyncImage(
                model = cutoutFile,
                contentDescription = "User Cutout",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (userProfile.imageUri == "sample_business") {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.img_sample_business),
                contentDescription = "Business Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.img_sample_portrait),
                contentDescription = "User Portrait",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
