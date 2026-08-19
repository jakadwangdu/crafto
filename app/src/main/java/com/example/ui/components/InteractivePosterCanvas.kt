package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CutoutMaskShape
import com.example.data.model.PosterTemplate
import com.example.data.model.TemplateAspectRatio
import com.example.data.model.UserProfile
import com.example.ui.theme.CraftoGold
import kotlin.math.roundToInt

@Composable
fun InteractivePosterCanvas(
    template: PosterTemplate,
    userProfile: UserProfile,
    cutoutOffsetX: Float,
    cutoutOffsetY: Float,
    cutoutScale: Float,
    customQuote: String?,
    customQuoteColor: String?,
    onDragCutout: (dx: Float, dy: Float) -> Unit,
    onResetPosition: () -> Unit,
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .background(gradientBrush)
            .testTag("interactive_poster_canvas")
    ) {
        val canvasWidthPx = constraints.maxWidth.toFloat()
        val canvasHeightPx = constraints.maxHeight.toFloat()

        // Background hero image if present
        if (template.bgImageResName == "img_hero_banner") {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Crop,
                alpha = 0.45f
            )
        }

        // Top Festive Header Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Surface(
                color = Color(0x66000000),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFFFFF))
            ) {
                Text(
                    text = "${template.festiveEmoji ?: "✨"} ${template.title.uppercase()} ${template.festiveEmoji ?: "✨"}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Quote Typography
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val quoteColor = remember(customQuoteColor, template.quoteTextColor) {
                val hex = customQuoteColor ?: template.quoteTextColor
                try { Color(android.graphics.Color.parseColor(hex)) }
                catch (e: Exception) { Color.White }
            }

            Text(
                text = customQuote ?: template.quoteText,
                color = quoteColor,
                fontSize = if (isStory) 20.sp else 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = if (isStory) 26.sp else 22.sp,
                modifier = Modifier.testTag("editor_quote_text")
            )

            template.quoteSubText?.let { sub ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = sub,
                    color = CraftoGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // User Draggable Portrait/Logo Cutout Layer
        val baseCutoutWidth = (canvasWidthPx * 0.32f * cutoutScale).coerceIn(80f, 340f)
        val baseCutoutHeight = baseCutoutWidth * 1.15f

        val defaultCenterX = canvasWidthPx * template.cutoutDefaultXPercent
        val defaultCenterY = canvasHeightPx * template.cutoutDefaultYPercent

        val currentCenterX = defaultCenterX + (cutoutOffsetX * canvasWidthPx * 0.45f)
        val currentCenterY = defaultCenterY + (cutoutOffsetY * canvasHeightPx * 0.40f)

        val cutoutLeft = (currentCenterX - baseCutoutWidth / 2f).coerceIn(0f, canvasWidthPx - baseCutoutWidth)
        val cutoutTop = (currentCenterY - baseCutoutHeight / 2f).coerceIn(0f, canvasHeightPx - baseCutoutHeight)

        val density = androidx.compose.ui.platform.LocalDensity.current
        val cutoutWidthDp = with(density) { baseCutoutWidth.toDp() }
        val cutoutHeightDp = with(density) { baseCutoutHeight.toDp() }

        Box(
            modifier = Modifier
                .offset { IntOffset(cutoutLeft.roundToInt(), cutoutTop.roundToInt()) }
                .size(
                    width = cutoutWidthDp,
                    height = cutoutHeightDp
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dxNormalized = dragAmount.x / (canvasWidthPx * 0.45f)
                        val dyNormalized = dragAmount.y / (canvasHeightPx * 0.40f)
                        onDragCutout(dxNormalized, dyNormalized)
                    }
                }
                .testTag("draggable_user_cutout")
        ) {
            UserCutoutPreview(
                userProfile = userProfile,
                modifier = Modifier.fillMaxSize()
            )

            // Drag affordance icon overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .background(Color(0xCC000000), CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.OpenWith,
                    contentDescription = "Drag to reposition",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Bottom User Profile Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            ProfileBadgeView(profile = userProfile)
        }

        // Reset Position Button
        if (cutoutOffsetX != 0f || cutoutOffsetY != 0f || cutoutScale != 1.0f) {
            IconButton(
                onClick = onResetPosition,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .background(Color(0x88000000), CircleShape)
                    .testTag("reset_cutout_position_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset cutout position",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
