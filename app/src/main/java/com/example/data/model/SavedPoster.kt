package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_posters")
data class SavedPoster(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val aspectRatio: TemplateAspectRatio = TemplateAspectRatio.SQUARE_1_1,
    val profileName: String = "",
    val quoteSnippet: String = ""
)
