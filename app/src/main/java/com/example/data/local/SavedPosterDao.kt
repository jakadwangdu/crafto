package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedPoster
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPosterDao {
    @Query("SELECT * FROM saved_posters ORDER BY timestamp DESC")
    fun getAllSavedPosters(): Flow<List<SavedPoster>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPoster(poster: SavedPoster): Long

    @Query("DELETE FROM saved_posters WHERE id = :id")
    suspend fun deleteSavedPosterById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_posters")
    suspend fun getSavedPosterCount(): Int
}
