package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PosterTemplate
import com.example.data.model.TemplateAspectRatio
import kotlinx.coroutines.flow.Flow

@Dao
interface PosterTemplateDao {
    @Query("SELECT * FROM poster_templates ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<PosterTemplate>>

    @Query("SELECT * FROM poster_templates WHERE category = :category ORDER BY id ASC")
    fun getTemplatesByCategory(category: String): Flow<List<PosterTemplate>>

    @Query("SELECT * FROM poster_templates WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteTemplates(): Flow<List<PosterTemplate>>

    @Query("SELECT * FROM poster_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): PosterTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<PosterTemplate>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: PosterTemplate): Long

    @Update
    suspend fun updateTemplate(template: PosterTemplate)

    @Query("UPDATE poster_templates SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM poster_templates")
    suspend fun getTemplateCount(): Int
}
