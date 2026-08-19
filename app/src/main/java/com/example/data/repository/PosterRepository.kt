package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.PosterTemplate
import com.example.data.model.SavedPoster
import com.example.data.model.UserProfile
import com.example.data.sample.SampleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PosterRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val userProfileDao = database.userProfileDao()
    private val posterTemplateDao = database.posterTemplateDao()
    private val savedPosterDao = database.savedPosterDao()

    init {
        // Initialize default seed data if DB is newly created
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        if (userProfileDao.getProfileCount() == 0) {
            userProfileDao.insertProfile(SampleData.defaultPersonalProfile)
            userProfileDao.insertProfile(SampleData.defaultBusinessProfile)
        }
        if (posterTemplateDao.getTemplateCount() == 0) {
            posterTemplateDao.insertTemplates(SampleData.defaultTemplates)
        }
    }

    // Profiles
    val allProfiles: Flow<List<UserProfile>> = userProfileDao.getAllProfiles()
    val selectedProfile: Flow<UserProfile?> = userProfileDao.getSelectedProfile()

    suspend fun getProfileById(id: Long): UserProfile? = withContext(Dispatchers.IO) {
        userProfileDao.getProfileById(id)
    }

    suspend fun saveProfile(profile: UserProfile): Long = withContext(Dispatchers.IO) {
        if (profile.id == 0L) {
            val id = userProfileDao.insertProfile(profile)
            userProfileDao.setSelectedProfile(id)
            id
        } else {
            userProfileDao.updateProfile(profile)
            profile.id
        }
    }

    suspend fun selectProfile(id: Long) = withContext(Dispatchers.IO) {
        userProfileDao.setSelectedProfile(id)
    }

    suspend fun deleteProfile(id: Long) = withContext(Dispatchers.IO) {
        userProfileDao.deleteProfileById(id)
        val remaining = userProfileDao.getAllProfiles().first()
        if (remaining.isNotEmpty()) {
            userProfileDao.setSelectedProfile(remaining.first().id)
        }
    }

    // Templates
    val allTemplates: Flow<List<PosterTemplate>> = posterTemplateDao.getAllTemplates()
    val favoriteTemplates: Flow<List<PosterTemplate>> = posterTemplateDao.getFavoriteTemplates()

    fun getTemplatesByCategory(category: String): Flow<List<PosterTemplate>> {
        return if (category.equals("All", ignoreCase = true)) {
            posterTemplateDao.getAllTemplates()
        } else {
            posterTemplateDao.getTemplatesByCategory(category)
        }
    }

    suspend fun getTemplateById(id: Long): PosterTemplate? = withContext(Dispatchers.IO) {
        posterTemplateDao.getTemplateById(id)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        posterTemplateDao.setFavorite(id, isFavorite)
    }

    suspend fun updateTemplate(template: PosterTemplate) = withContext(Dispatchers.IO) {
        posterTemplateDao.updateTemplate(template)
    }

    // Saved Posters
    val allSavedPosters: Flow<List<SavedPoster>> = savedPosterDao.getAllSavedPosters()

    suspend fun savePoster(poster: SavedPoster): Long = withContext(Dispatchers.IO) {
        savedPosterDao.insertSavedPoster(poster)
    }

    suspend fun deleteSavedPoster(id: Long) = withContext(Dispatchers.IO) {
        savedPosterDao.deleteSavedPosterById(id)
    }
}
