package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProfileType {
    PERSONAL,
    BUSINESS
}

enum class FrameStyle {
    MODERN_PILL,
    DARK_GLASS,
    SLEEK_MINIMAL,
    GOLDEN_ROYAL,
    VIBRANT_BANNER,
    CORNER_STAMP
}

enum class CutoutMaskShape {
    TRANSPARENT_CUTOUT,
    CIRCLE_RING,
    GOLDEN_FRAME,
    ROUNDED_SQUARE,
    SHIELD
}

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileType: ProfileType = ProfileType.PERSONAL,
    val name: String = "Rahul Sharma",
    val designation: String = "Digital Creator & Entrepreneur",
    val businessName: String = "Apex Media & Design",
    val tagline: String = "Transforming Brands with Creative Spark",
    val phoneNumber: String = "+91 98765 43210",
    val email: String = "contact@apexmedia.in",
    val website: String = "www.apexmedia.in",
    val address: String = "Mumbai, Maharashtra",
    val socialHandle: String = "@rahul_sharma",
    val imageUri: String? = null, // Local URI or drawable name
    val cutoutUri: String? = null, // Local cached PNG with transparent BG
    val frameStyle: FrameStyle = FrameStyle.MODERN_PILL,
    val cutoutMaskShape: CutoutMaskShape = CutoutMaskShape.TRANSPARENT_CUTOUT,
    val primaryColorHex: String = "#4F46E5",
    val accentColorHex: String = "#F59E0B",
    val showPhone: Boolean = true,
    val showDesignation: Boolean = true,
    val showSocial: Boolean = true,
    val showAddress: Boolean = false,
    val showWatermark: Boolean = true,
    val isSelected: Boolean = false
)
