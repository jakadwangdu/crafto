package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CutoutMaskShape
import com.example.data.model.FrameStyle
import com.example.data.model.PosterTemplate
import com.example.data.model.ProfileType
import com.example.data.model.SavedPoster
import com.example.data.model.TemplateAspectRatio
import com.example.data.model.UserProfile
import com.example.data.repository.PosterRepository
import com.example.data.sample.SampleData
import com.example.engine.CutoutProcessor
import com.example.engine.PosterBitmapRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorState(
    val template: PosterTemplate? = null,
    val cutoutOffsetX: Float = 0f,
    val cutoutOffsetY: Float = 0f,
    val cutoutScaleFactor: Float = 1.0f,
    val customQuoteText: String? = null,
    val customQuoteColorHex: String? = null,
    val customFrameStyle: FrameStyle? = null,
    val customMaskShape: CutoutMaskShape? = null,
    val isRendering: Boolean = false,
    val isSaving: Boolean = false,
    val isSharing: Boolean = false,
    val saveSuccessUri: String? = null,
    val errorMessage: String? = null
)

class PosterViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PosterRepository(application)

    // Profiles
    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<UserProfile?> = repository.selectedProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleData.defaultPersonalProfile)

    // Category & Filters
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow<TemplateAspectRatio?>(null) // null = all
    val selectedAspectRatio: StateFlow<TemplateAspectRatio?> = _selectedAspectRatio.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    // All templates from database
    val allTemplates: StateFlow<List<PosterTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleData.defaultTemplates)

    // Filtered templates
    val filteredTemplates: StateFlow<List<PosterTemplate>> = combine(
        allTemplates,
        _selectedCategory,
        _selectedAspectRatio,
        _searchQuery,
        _onlyFavorites
    ) { templates, category, ratio, query, favoritesOnly ->
        templates.filter { t ->
            val matchCategory = category == "All" || t.category.equals(category, ignoreCase = true)
            val matchRatio = ratio == null || t.aspectRatio == ratio
            val matchQuery = query.isBlank() || t.title.contains(query, ignoreCase = true) ||
                    t.quoteText.contains(query, ignoreCase = true) ||
                    t.tags.contains(query, ignoreCase = true)
            val matchFavorites = !favoritesOnly || t.isFavorite

            matchCategory && matchRatio && matchQuery && matchFavorites
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleData.defaultTemplates)

    // Saved Posters History
    val savedPosters: StateFlow<List<SavedPoster>> = repository.allSavedPosters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Editor State
    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    // Status Message / Toast
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // AI Cutout processing state
    private val _isProcessingCutout = MutableStateFlow(false)
    val isProcessingCutout: StateFlow<Boolean> = _isProcessingCutout.asStateFlow()

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setAspectRatioFilter(ratio: TemplateAspectRatio?) {
        _selectedAspectRatio.value = ratio
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesOnly() {
        _onlyFavorites.update { !it }
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Profile Management
    fun selectProfile(profileId: Long) {
        viewModelScope.launch {
            repository.selectProfile(profileId)
        }
    }

    fun switchProfileType(targetType: ProfileType) {
        viewModelScope.launch {
            val list = allProfiles.value
            val match = list.firstOrNull { it.profileType == targetType }
            if (match != null) {
                repository.selectProfile(match.id)
            } else {
                // Create new default for that type
                val newProfile = if (targetType == ProfileType.BUSINESS) {
                    SampleData.defaultBusinessProfile.copy(id = 0, isSelected = true)
                } else {
                    SampleData.defaultPersonalProfile.copy(id = 0, isSelected = true)
                }
                repository.saveProfile(newProfile)
            }
        }
    }

    fun saveProfile(profile: UserProfile, newImageSource: String? = null, runBgRemoval: Boolean = true) {
        viewModelScope.launch {
            _isProcessingCutout.value = true
            try {
                var cutoutPath = profile.cutoutUri
                val imageToUse = newImageSource ?: profile.imageUri ?: "sample_portrait"

                if (newImageSource != null || runBgRemoval) {
                    cutoutPath = CutoutProcessor.processCutout(
                        context = getApplication(),
                        source = imageToUse,
                        maskShape = profile.cutoutMaskShape,
                        applyBgRemoval = true
                    )
                }

                val updatedProfile = profile.copy(
                    imageUri = imageToUse,
                    cutoutUri = cutoutPath
                )
                repository.saveProfile(updatedProfile)
                _userMessage.value = "Profile updated successfully! 🎉"
            } catch (e: Exception) {
                _userMessage.value = "Failed to update profile: ${e.message}"
            } finally {
                _isProcessingCutout.value = false
            }
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteProfile(id)
            _userMessage.value = "Profile removed"
        }
    }

    // Editor Actions
    fun openEditor(template: PosterTemplate) {
        _editorState.value = EditorState(
            template = template,
            cutoutOffsetX = 0f,
            cutoutOffsetY = 0f,
            cutoutScaleFactor = 1.0f,
            customQuoteText = template.quoteText,
            customQuoteColorHex = template.quoteTextColor,
            customFrameStyle = activeProfile.value?.frameStyle ?: FrameStyle.MODERN_PILL,
            customMaskShape = activeProfile.value?.cutoutMaskShape ?: CutoutMaskShape.TRANSPARENT_CUTOUT
        )
    }

    fun swapEditorTemplate(newTemplate: PosterTemplate) {
        _editorState.update { current ->
            current.copy(
                template = newTemplate,
                customQuoteText = newTemplate.quoteText,
                customQuoteColorHex = newTemplate.quoteTextColor
            )
        }
    }

    fun updateCutoutOffset(dx: Float, dy: Float) {
        _editorState.update { current ->
            val newX = (current.cutoutOffsetX + dx).coerceIn(-1.0f, 1.0f)
            val newY = (current.cutoutOffsetY + dy).coerceIn(-1.0f, 1.0f)
            current.copy(cutoutOffsetX = newX, cutoutOffsetY = newY)
        }
    }

    fun updateCutoutScale(scale: Float) {
        _editorState.update { current ->
            current.copy(cutoutScaleFactor = scale.coerceIn(0.4f, 2.2f))
        }
    }

    fun resetCutoutTransform() {
        _editorState.update { current ->
            current.copy(cutoutOffsetX = 0f, cutoutOffsetY = 0f, cutoutScaleFactor = 1.0f)
        }
    }

    fun setCustomQuote(quote: String) {
        _editorState.update { it.copy(customQuoteText = quote) }
    }

    fun setQuoteColor(colorHex: String) {
        _editorState.update { it.copy(customQuoteColorHex = colorHex) }
    }

    fun setEditorFrameStyle(style: FrameStyle) {
        _editorState.update { it.copy(customFrameStyle = style) }
        // Also update active profile
        activeProfile.value?.let { p ->
            viewModelScope.launch {
                repository.saveProfile(p.copy(frameStyle = style))
            }
        }
    }

    fun setEditorMaskShape(maskShape: CutoutMaskShape) {
        _editorState.update { it.copy(customMaskShape = maskShape) }
        // Re-process cutout with new mask
        activeProfile.value?.let { p ->
            viewModelScope.launch {
                _isProcessingCutout.value = true
                val newCutout = CutoutProcessor.processCutout(
                    context = getApplication(),
                    source = p.imageUri ?: "sample_portrait",
                    maskShape = maskShape,
                    applyBgRemoval = maskShape == CutoutMaskShape.TRANSPARENT_CUTOUT
                )
                repository.saveProfile(p.copy(cutoutMaskShape = maskShape, cutoutUri = newCutout))
                _isProcessingCutout.value = false
            }
        }
    }

    fun toggleFavorite(templateId: Long) {
        viewModelScope.launch {
            val template = allTemplates.value.firstOrNull { it.id == templateId }
            if (template != null) {
                repository.toggleFavorite(templateId, !template.isFavorite)
            }
        }
    }

    fun savePosterToGallery(onSaved: (String) -> Unit = {}) {
        val currentEditor = _editorState.value
        val template = currentEditor.template ?: return
        val profile = activeProfile.value ?: SampleData.defaultPersonalProfile

        viewModelScope.launch {
            _editorState.update { it.copy(isSaving = true) }
            try {
                val bitmap = PosterBitmapRenderer.renderPoster(
                    context = getApplication(),
                    template = template,
                    userProfile = profile.copy(
                        frameStyle = currentEditor.customFrameStyle ?: profile.frameStyle,
                        cutoutMaskShape = currentEditor.customMaskShape ?: profile.cutoutMaskShape
                    ),
                    cutoutOffsetX = currentEditor.cutoutOffsetX,
                    cutoutOffsetY = currentEditor.cutoutOffsetY,
                    cutoutScaleFactor = currentEditor.cutoutScaleFactor,
                    customQuoteText = currentEditor.customQuoteText,
                    customQuoteColorHex = currentEditor.customQuoteColorHex
                )

                val savedPath = PosterBitmapRenderer.saveBitmapToGallery(
                    context = getApplication(),
                    bitmap = bitmap,
                    title = template.title
                )

                if (savedPath != null) {
                    // Record in Room Database
                    repository.savePoster(
                        SavedPoster(
                            title = template.title,
                            category = template.category,
                            filePath = savedPath,
                            aspectRatio = template.aspectRatio,
                            profileName = if (profile.profileType == ProfileType.BUSINESS) profile.businessName else profile.name,
                            quoteSnippet = (currentEditor.customQuoteText ?: template.quoteText).take(60)
                        )
                    )
                    _userMessage.value = "Poster saved to Gallery successfully! 📸"
                    onSaved(savedPath)
                } else {
                    _userMessage.value = "Could not save image to gallery."
                }
            } catch (e: Exception) {
                _userMessage.value = "Save error: ${e.message}"
            } finally {
                _editorState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun sharePoster(targetPackage: String? = null) {
        val currentEditor = _editorState.value
        val template = currentEditor.template ?: return
        val profile = activeProfile.value ?: SampleData.defaultPersonalProfile

        viewModelScope.launch {
            _editorState.update { it.copy(isSharing = true) }
            try {
                val bitmap = PosterBitmapRenderer.renderPoster(
                    context = getApplication(),
                    template = template,
                    userProfile = profile.copy(
                        frameStyle = currentEditor.customFrameStyle ?: profile.frameStyle,
                        cutoutMaskShape = currentEditor.customMaskShape ?: profile.cutoutMaskShape
                    ),
                    cutoutOffsetX = currentEditor.cutoutOffsetX,
                    cutoutOffsetY = currentEditor.cutoutOffsetY,
                    cutoutScaleFactor = currentEditor.cutoutScaleFactor,
                    customQuoteText = currentEditor.customQuoteText,
                    customQuoteColorHex = currentEditor.customQuoteColorHex
                )

                val shareUri = PosterBitmapRenderer.getShareableUri(getApplication(), bitmap)
                if (shareUri != null) {
                    val caption = "${currentEditor.customQuoteText ?: template.quoteText}\n\n— Created with Crafto Poster App"
                    PosterBitmapRenderer.sharePoster(getApplication(), shareUri, caption, targetPackage)
                } else {
                    _userMessage.value = "Failed to prepare image for sharing."
                }
            } catch (e: Exception) {
                _userMessage.value = "Share error: ${e.message}"
            } finally {
                _editorState.update { it.copy(isSharing = false) }
            }
        }
    }

    fun deleteSavedPoster(posterId: Long) {
        viewModelScope.launch {
            repository.deleteSavedPoster(posterId)
            _userMessage.value = "Deleted from creations."
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PosterViewModel(application) as T
        }
    }
}
