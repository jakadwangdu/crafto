package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TemplateAspectRatio {
    SQUARE_1_1,
    STORY_9_16
}

@Entity(tableName = "poster_templates")
data class PosterTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "Good Morning", "Festivals", "Motivation", "Birthday", "Business", "Devotional", "Special Days", "Good Night"
    val aspectRatio: TemplateAspectRatio = TemplateAspectRatio.SQUARE_1_1,
    val quoteText: String,
    val quoteSubText: String? = null,
    val authorText: String? = null,
    val bgGradientStart: String = "#1E1B4B",
    val bgGradientEnd: String = "#4338CA",
    val bgGradientMiddle: String? = null,
    val bgGradientAngle: Float = 135f,
    val bgPatternType: String = "GEOMETRIC", // RANGOLI, SUNRISE, STARS, DIWALI_LAMPS, GEOMETRIC, SPARKS, WAVES, FLORAL, MODERN_GRID, HERO_IMAGE
    val bgImageResName: String? = null,
    val quoteTextColor: String = "#FFFFFF",
    val quoteFontSize: Int = 22,
    val quoteFontWeight: String = "BOLD",
    val quoteYPercent: Float = 0.32f,
    val quoteAlignment: String = "CENTER",
    val cutoutDefaultXPercent: Float = 0.78f,
    val cutoutDefaultYPercent: Float = 0.75f,
    val cutoutDefaultScale: Float = 1.0f,
    val badgePosition: String = "BOTTOM",
    val festiveEmoji: String? = "✨",
    val tags: String = "",
    val isFavorite: Boolean = false
)
