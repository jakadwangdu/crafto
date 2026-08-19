package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.FrameStyle
import com.example.data.model.PosterTemplate
import com.example.data.model.ProfileType
import com.example.data.model.TemplateAspectRatio
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object PosterBitmapRenderer {

    /**
     * Renders a high-resolution composite poster Bitmap.
     */
    suspend fun renderPoster(
        context: Context,
        template: PosterTemplate,
        userProfile: UserProfile,
        cutoutOffsetX: Float = 0f, // -1f .. 1f adjustment from default
        cutoutOffsetY: Float = 0f,
        cutoutScaleFactor: Float = 1.0f,
        customQuoteText: String? = null,
        customQuoteColorHex: String? = null,
        showWatermark: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        val width: Int
        val height: Int
        if (template.aspectRatio == TemplateAspectRatio.STORY_9_16) {
            width = 1080
            height = 1920
        } else {
            width = 1080
            height = 1080
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Render Background Gradient
        drawBackgroundGradient(canvas, width, height, template)

        // 2. Render Decorative Patterns / Graphics
        drawDecorativePattern(canvas, width, height, template)

        // 3. Render Background Image if present
        drawBackgroundImage(context, canvas, width, height, template)

        // 4. Render Decorative Floating Glow / Frame Ornaments
        drawFrameOrnaments(canvas, width, height, template)

        // 5. Render Quote Text & Subtext
        val quoteToRender = customQuoteText ?: template.quoteText
        val quoteColorHex = customQuoteColorHex ?: template.quoteTextColor
        drawQuoteText(canvas, width, height, template, quoteToRender, quoteColorHex)

        // 6. Render User Cutout Portrait / Logo
        drawUserCutout(
            context,
            canvas,
            width,
            height,
            template,
            userProfile,
            cutoutOffsetX,
            cutoutOffsetY,
            cutoutScaleFactor
        )

        // 7. Render User Profile Footer Badge / Banner
        drawProfileBadge(canvas, width, height, userProfile)

        // 8. Render Watermark (if enabled)
        if (showWatermark && userProfile.showWatermark) {
            drawWatermark(canvas, width, height)
        }

        bitmap
    }

    private fun drawBackgroundGradient(
        canvas: Canvas,
        width: Int,
        height: Int,
        template: PosterTemplate
    ) {
        val startColor = try {
            Color.parseColor(template.bgGradientStart)
        } catch (e: Exception) {
            Color.parseColor("#1E1B4B")
        }
        val endColor = try {
            Color.parseColor(template.bgGradientEnd)
        } catch (e: Exception) {
            Color.parseColor("#4338CA")
        }
        val middleColor = template.bgGradientMiddle?.let {
            try { Color.parseColor(it) } catch (e: Exception) { null }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val shader = if (middleColor != null) {
            LinearGradient(
                0f, 0f,
                width.toFloat(), height.toFloat(),
                intArrayOf(startColor, middleColor, endColor),
                floatArrayOf(0.0f, 0.5f, 1.0f),
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f, 0f,
                width.toFloat(), height.toFloat(),
                startColor, endColor,
                Shader.TileMode.CLAMP
            )
        }
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawDecorativePattern(
        canvas: Canvas,
        width: Int,
        height: Int,
        template: PosterTemplate
    ) {
        val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.argb(40, 255, 255, 255)
        }

        when (template.bgPatternType) {
            "RANGOLI" -> {
                val cx = width / 2f
                val cy = height * 0.28f
                for (r in 40..420 step 45) {
                    canvas.drawCircle(cx, cy, r.toFloat(), patternPaint)
                }
                for (i in 0 until 12) {
                    val angle = Math.toRadians((i * 30).toDouble())
                    val x2 = cx + (cos(angle) * 440).toFloat()
                    val y2 = cy + (sin(angle) * 440).toFloat()
                    canvas.drawLine(cx, cy, x2, y2, patternPaint)
                }
            }
            "SUNRISE" -> {
                val cx = width / 2f
                val cy = height * 0.12f
                val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    shader = RadialGradient(
                        cx, cy, 380f,
                        intArrayOf(Color.argb(180, 255, 237, 74), Color.argb(60, 249, 115, 22), Color.TRANSPARENT),
                        floatArrayOf(0f, 0.4f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(cx, cy, 380f, sunPaint)

                // Sun rays
                for (i in 0 until 16) {
                    val angle = Math.toRadians((i * 22.5).toDouble())
                    val x2 = cx + (cos(angle) * 700).toFloat()
                    val y2 = cy + (sin(angle) * 700).toFloat()
                    canvas.drawLine(cx, cy, x2, y2, patternPaint)
                }
            }
            "STARS" -> {
                val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(140, 255, 255, 255)
                    style = Paint.Style.FILL
                }
                val starPositions = listOf(
                    Pair(0.15f, 0.12f), Pair(0.85f, 0.15f), Pair(0.22f, 0.28f),
                    Pair(0.78f, 0.32f), Pair(0.10f, 0.48f), Pair(0.90f, 0.50f),
                    Pair(0.30f, 0.08f), Pair(0.70f, 0.08f), Pair(0.50f, 0.18f)
                )
                for (pos in starPositions) {
                    val sx = width * pos.first
                    val sy = height * pos.second
                    canvas.drawCircle(sx, sy, 4f, starPaint)
                    // Little cross star
                    canvas.drawLine(sx - 12f, sy, sx + 12f, sy, patternPaint)
                    canvas.drawLine(sx, sy - 12f, sx, sy + 12f, patternPaint)
                }
            }
            "DIWALI_LAMPS" -> {
                // Diya glow accents
                val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    shader = RadialGradient(
                        width * 0.5f, height * 0.18f, 320f,
                        intArrayOf(Color.argb(190, 253, 224, 71), Color.argb(70, 234, 88, 12), Color.TRANSPARENT),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(width * 0.5f, height * 0.18f, 320f, glowPaint)
            }
            "MODERN_GRID" -> {
                val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    color = Color.argb(25, 255, 255, 255)
                }
                val step = 60
                for (x in 0..width step step) {
                    canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), gridPaint)
                }
                for (y in 0..height step step) {
                    canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
                }
            }
            else -> {
                // Geometric subtle arcs
                canvas.drawCircle(width * 0.9f, height * 0.1f, 220f, patternPaint)
                canvas.drawCircle(width * 0.1f, height * 0.6f, 160f, patternPaint)
            }
        }
    }

    private fun drawBackgroundImage(
        context: Context,
        canvas: Canvas,
        width: Int,
        height: Int,
        template: PosterTemplate
    ) {
        val resName = template.bgImageResName ?: return
        try {
            val resId = when (resName) {
                "bg_fest_diwali" -> R.drawable.bg_fest_diwali
                "bg_fest_holi" -> R.drawable.bg_fest_holi
                "bg_fest_eid" -> R.drawable.bg_fest_eid
                "bg_fest_ganesh" -> R.drawable.bg_fest_ganesh
                "bg_fest_newyear" -> R.drawable.bg_fest_newyear
                "img_hero_banner" -> R.drawable.img_hero_banner
                else -> context.resources.getIdentifier(resName, "drawable", context.packageName)
            }
            if (resId != 0) {
                val bgBmp = BitmapFactory.decodeResource(context.resources, resId)
                if (bgBmp != null) {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        alpha = 210
                    }
                    val srcRect = Rect(0, 0, bgBmp.width, bgBmp.height)
                    val destRect = Rect(0, 0, width, height)
                    canvas.drawBitmap(bgBmp, srcRect, destRect, paint)

                    // Draw dark gradient overlay across bottom for text readability
                    val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = LinearGradient(
                            0f, height * 0.15f, 0f, height.toFloat(),
                            intArrayOf(
                                Color.argb(40, 0, 0, 0),
                                Color.argb(120, 0, 0, 0),
                                Color.argb(200, 0, 0, 0)
                            ),
                            floatArrayOf(0f, 0.5f, 1f),
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawFrameOrnaments(
        canvas: Canvas,
        width: Int,
        height: Int,
        template: PosterTemplate
    ) {
        // Subtle top header badge
        val badgeText = template.festiveEmoji?.let { "$it ${template.title.uppercase()} $it" } ?: template.title.uppercase()
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
            setShadowLayer(8f, 0f, 4f, Color.argb(160, 0, 0, 0))
        }

        // Draw top pill
        val textWidth = badgePaint.measureText(badgeText)
        val pillRect = RectF(
            (width - textWidth) / 2f - 32f,
            48f,
            (width + textWidth) / 2f + 32f,
            110f
        )
        val pillBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val pillBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(pillRect, 32f, 32f, pillBg)
        canvas.drawRoundRect(pillRect, 32f, 32f, pillBorder)
        canvas.drawText(badgeText, width / 2f, 88f, badgePaint)
    }

    private fun drawQuoteText(
        canvas: Canvas,
        width: Int,
        height: Int,
        template: PosterTemplate,
        quoteText: String,
        quoteColorHex: String
    ) {
        val parsedColor = try {
            Color.parseColor(quoteColorHex)
        } catch (e: Exception) {
            Color.WHITE
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parsedColor
            textSize = when (template.quoteFontSize) {
                in 24..30 -> 48f
                in 20..23 -> 42f
                else -> 38f
            }
            typeface = when (template.quoteFontWeight) {
                "EXTRA_BOLD" -> Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                "MEDIUM" -> Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                else -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            textAlign = when (template.quoteAlignment) {
                "LEFT" -> Paint.Align.LEFT
                "RIGHT" -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
            setShadowLayer(12f, 0f, 6f, Color.argb(180, 0, 0, 0))
        }

        val maxTextWidth = width * 0.84f
        val lines = wrapText(quoteText, textPaint, maxTextWidth)

        val lineHeight = textPaint.textSize * 1.35f
        val startY = height * template.quoteYPercent

        var currentY = startY
        for (line in lines) {
            val drawX = when (template.quoteAlignment) {
                "LEFT" -> width * 0.08f
                "RIGHT" -> width * 0.92f
                else -> width / 2f
            }
            canvas.drawText(line, drawX, currentY, textPaint)
            currentY += lineHeight
        }

        // Subtext / Author
        template.quoteSubText?.let { sub ->
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(230, 254, 240, 138)
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                setShadowLayer(8f, 0f, 4f, Color.argb(160, 0, 0, 0))
            }
            canvas.drawText(sub, width / 2f, currentY + 24f, subPaint)
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                result.add("")
                continue
            }
            val words = paragraph.split(" ")
            val currentLine = StringBuilder()

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine.clear()
                    currentLine.append(testLine)
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine.toString())
                    }
                    currentLine.clear()
                    currentLine.append(word)
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine.toString())
            }
        }
        return result
    }

    private suspend fun drawUserCutout(
        context: Context,
        canvas: Canvas,
        width: Int,
        height: Int,
        template: PosterTemplate,
        userProfile: UserProfile,
        cutoutOffsetX: Float,
        cutoutOffsetY: Float,
        cutoutScaleFactor: Float
    ) {
        val cutoutPath = userProfile.cutoutUri
        val imageSource = cutoutPath ?: userProfile.imageUri ?: "sample_portrait"

        val bitmap = CutoutProcessor.loadSourceBitmap(context, imageSource) ?: return

        // Compute positioning
        val targetSize = (width * 0.36f * template.cutoutDefaultScale * cutoutScaleFactor).toInt()
        val targetHeight = (targetSize * (bitmap.height.toFloat() / bitmap.width.toFloat())).toInt()

        val baseX = width * template.cutoutDefaultXPercent + (cutoutOffsetX * width * 0.4f)
        val baseY = height * template.cutoutDefaultYPercent + (cutoutOffsetY * height * 0.3f)

        val left = (baseX - targetSize / 2f).toInt()
        val top = (baseY - targetHeight / 2f).toInt()
        val right = left + targetSize
        val bottom = top + targetHeight

        val destRect = Rect(left, top, right, bottom)
        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)

        // Drop shadow for cutout
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 0, 0, 0)
            maskFilter = null
        }
        val shadowRect = Rect(left - 8, top - 8, right + 8, bottom + 8)
        canvas.drawOval(RectF(shadowRect), shadowPaint)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        canvas.drawBitmap(bitmap, srcRect, destRect, paint)
    }

    private fun drawProfileBadge(
        canvas: Canvas,
        width: Int,
        height: Int,
        profile: UserProfile
    ) {
        val isBusiness = profile.profileType == ProfileType.BUSINESS
        val badgeHeight = 160f
        val badgeY = height - badgeHeight - 24f

        val rectF = RectF(24f, badgeY, width - 24f, height - 24f)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        when (profile.frameStyle) {
            FrameStyle.MODERN_PILL -> {
                bgPaint.color = Color.argb(235, 15, 23, 42) // Dark Slate
                borderPaint.color = Color.argb(180, 99, 102, 241) // Indigo accent
                canvas.drawRoundRect(rectF, 40f, 40f, bgPaint)
                canvas.drawRoundRect(rectF, 40f, 40f, borderPaint)
            }
            FrameStyle.GOLDEN_ROYAL -> {
                bgPaint.shader = LinearGradient(
                    rectF.left, rectF.top, rectF.right, rectF.bottom,
                    Color.argb(245, 30, 27, 75), Color.argb(245, 15, 23, 42),
                    Shader.TileMode.CLAMP
                )
                borderPaint.color = Color.parseColor("#F59E0B")
                borderPaint.strokeWidth = 4f
                canvas.drawRoundRect(rectF, 28f, 28f, bgPaint)
                canvas.drawRoundRect(rectF, 28f, 28f, borderPaint)
            }
            FrameStyle.DARK_GLASS -> {
                bgPaint.color = Color.argb(210, 0, 0, 0)
                borderPaint.color = Color.argb(100, 255, 255, 255)
                canvas.drawRoundRect(rectF, 32f, 32f, bgPaint)
                canvas.drawRoundRect(rectF, 32f, 32f, borderPaint)
            }
            FrameStyle.VIBRANT_BANNER -> {
                bgPaint.shader = LinearGradient(
                    rectF.left, rectF.top, rectF.right, rectF.bottom,
                    Color.parseColor("#4F46E5"), Color.parseColor("#7C3AED"),
                    Shader.TileMode.CLAMP
                )
                borderPaint.color = Color.WHITE
                canvas.drawRoundRect(rectF, 36f, 36f, bgPaint)
                canvas.drawRoundRect(rectF, 36f, 36f, borderPaint)
            }
            else -> {
                bgPaint.color = Color.argb(240, 255, 255, 255)
                borderPaint.color = Color.argb(160, 203, 213, 225)
                canvas.drawRoundRect(rectF, 24f, 24f, bgPaint)
                canvas.drawRoundRect(rectF, 24f, 24f, borderPaint)
            }
        }

        val isLightBg = profile.frameStyle == FrameStyle.SLEEK_MINIMAL
        val primaryTextColor = if (isLightBg) Color.parseColor("#0F172A") else Color.WHITE
        val secondaryTextColor = if (isLightBg) Color.parseColor("#475569") else Color.parseColor("#E2E8F0")
        val accentTextColor = if (isLightBg) Color.parseColor("#4F46E5") else Color.parseColor("#FDE047")

        // Draw Profile Info
        val titleText = if (isBusiness) profile.businessName else profile.name
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryTextColor
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        canvas.drawText(titleText, rectF.left + 36f, rectF.top + 52f, titlePaint)

        // Subtitle / Designation / Tagline
        val subText = if (isBusiness) profile.tagline else profile.designation
        if (subText.isNotBlank() && profile.showDesignation) {
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = secondaryTextColor
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val truncatedSub = if (subPaint.measureText(subText) > (width - 450f)) {
                subText.take(38) + "..."
            } else subText
            canvas.drawText(truncatedSub, rectF.left + 36f, rectF.top + 88f, subPaint)
        }

        // Contact info pills (Phone, Social Handle, Website)
        var contactX = rectF.left + 36f
        val contactY = rectF.top + 130f

        val contactPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentTextColor
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }

        if (profile.phoneNumber.isNotBlank() && profile.showPhone) {
            val phoneText = "📞 ${profile.phoneNumber}"
            canvas.drawText(phoneText, contactX, contactY, contactPaint)
            contactX += contactPaint.measureText(phoneText) + 32f
        }

        if (profile.socialHandle.isNotBlank() && profile.showSocial && contactX < (width - 250f)) {
            val socialText = "🌐 ${profile.socialHandle}"
            canvas.drawText(socialText, contactX, contactY, contactPaint)
        }
    }

    private fun drawWatermark(canvas: Canvas, width: Int, height: Int) {
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 255, 255, 255)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            setShadowLayer(4f, 0f, 2f, Color.argb(140, 0, 0, 0))
        }
        canvas.drawText("Created with Jaadu App ✨", width - 40f, height - 190f, watermarkPaint)
    }

    /**
     * Saves full-resolution bitmap directly to device MediaStore Pictures/JaaduPosters gallery.
     */
    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "Jaadu_${title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${System.currentTimeMillis()}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/JaaduPosters")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    return@withContext uri.toString()
                }
            }

            // Fallback for older devices or direct file path
            val picturesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "JaaduPosters").apply { mkdirs() }
            val file = File(picturesDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            // Trigger MediaScanner for instant gallery appearance
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/jpeg"),
                null
            )
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves a temporary bitmap to cache directory for direct sharing via Intent.
     */
    suspend fun getShareableUri(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(imagesDir, "share_poster_${System.currentTimeMillis()}.jpg")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Launches native Android Share Sheet or targets WhatsApp directly.
     */
    fun sharePoster(context: Context, uri: Uri, caption: String, targetPackage: String? = null) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (targetPackage != null) {
                setPackage(targetPackage)
            }
        }
        val chooser = Intent.createChooser(shareIntent, "Share Status Poster via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
