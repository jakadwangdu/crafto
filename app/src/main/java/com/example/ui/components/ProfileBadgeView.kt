package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FrameStyle
import com.example.data.model.ProfileType
import com.example.data.model.UserProfile
import com.example.ui.theme.CraftoGold
import com.example.ui.theme.CraftoPrimary

@Composable
fun ProfileBadgeView(
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    val isBusiness = profile.profileType == ProfileType.BUSINESS

    val (bgModifier, primaryTextColor, secondaryTextColor, accentColor) = when (profile.frameStyle) {
        FrameStyle.MODERN_PILL -> {
            Quadruple(
                Modifier
                    .background(Color(0xF01D1B20), RoundedCornerShape(20.dp))
                    .border(1.5.dp, Color(0xFF6750A4), RoundedCornerShape(20.dp)),
                Color.White,
                Color(0xFFE6E0E9),
                Color(0xFFD0BCFF)
            )
        }
        FrameStyle.GOLDEN_ROYAL -> {
            Quadruple(
                Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xF81E1B4B), Color(0xF80F172A))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, CraftoGold, RoundedCornerShape(16.dp)),
                Color.White,
                Color(0xFFFEF08A),
                CraftoGold
            )
        }
        FrameStyle.DARK_GLASS -> {
            Quadruple(
                Modifier
                    .background(Color(0xD9000000), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp)),
                Color.White,
                Color(0xFFE2E8F0),
                Color(0xFF38BDF8)
            )
        }
        FrameStyle.VIBRANT_BANNER -> {
            Quadruple(
                Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(CraftoPrimary, Color(0xFF7C3AED))
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .border(1.5.dp, Color.White, RoundedCornerShape(18.dp)),
                Color.White,
                Color(0xFFEEF2FF),
                Color(0xFFFDE68A)
            )
        }
        else -> {
            Quadruple(
                Modifier
                    .background(Color(0xF5FFFFFF), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp)),
                Color(0xFF0F172A),
                Color(0xFF475569),
                CraftoPrimary
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(bgModifier)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            // Name / Business Name Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isBusiness) Icons.Default.Business else Icons.Default.Person,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBusiness) profile.businessName else profile.name,
                    color = primaryTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Subtitle / Designation / Tagline
            val subText = if (isBusiness) profile.tagline else profile.designation
            if (subText.isNotBlank() && profile.showDesignation) {
                Text(
                    text = subText,
                    color = secondaryTextColor,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Contact Info Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (profile.phoneNumber.isNotBlank() && profile.showPhone) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = profile.phoneNumber,
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                if (profile.socialHandle.isNotBlank() && profile.showSocial) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = profile.socialHandle,
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
