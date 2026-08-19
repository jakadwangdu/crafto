package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CutoutMaskShape
import com.example.data.model.FrameStyle
import com.example.data.model.ProfileType
import com.example.data.model.TemplateAspectRatio

class Converters {
    @TypeConverter
    fun fromProfileType(value: ProfileType): String = value.name

    @TypeConverter
    fun toProfileType(value: String): ProfileType = try {
        ProfileType.valueOf(value)
    } catch (e: Exception) {
        ProfileType.PERSONAL
    }

    @TypeConverter
    fun fromFrameStyle(value: FrameStyle): String = value.name

    @TypeConverter
    fun toFrameStyle(value: String): FrameStyle = try {
        FrameStyle.valueOf(value)
    } catch (e: Exception) {
        FrameStyle.MODERN_PILL
    }

    @TypeConverter
    fun fromCutoutMaskShape(value: CutoutMaskShape): String = value.name

    @TypeConverter
    fun toCutoutMaskShape(value: String): CutoutMaskShape = try {
        CutoutMaskShape.valueOf(value)
    } catch (e: Exception) {
        CutoutMaskShape.TRANSPARENT_CUTOUT
    }

    @TypeConverter
    fun fromTemplateAspectRatio(value: TemplateAspectRatio): String = value.name

    @TypeConverter
    fun toTemplateAspectRatio(value: String): TemplateAspectRatio = try {
        TemplateAspectRatio.valueOf(value)
    } catch (e: Exception) {
        TemplateAspectRatio.SQUARE_1_1
    }
}
