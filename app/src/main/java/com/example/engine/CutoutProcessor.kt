package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import com.example.R
import com.example.data.model.CutoutMaskShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayDeque
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object CutoutProcessor {

    /**
     * Loads a Bitmap from either a resource string identifier, a content Uri, or a file path.
     */
    suspend fun loadSourceBitmap(context: Context, source: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            when {
                source == "sample_portrait" -> {
                    BitmapFactory.decodeResource(context.resources, R.drawable.img_sample_portrait)
                }
                source == "sample_business" -> {
                    BitmapFactory.decodeResource(context.resources, R.drawable.img_sample_business)
                }
                source.startsWith("content://") || source.startsWith("file://") -> {
                    val uri = Uri.parse(source)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                File(source).exists() -> {
                    BitmapFactory.decodeFile(source)
                }
                else -> {
                    // Try by resource identifier
                    val resId = context.resources.getIdentifier(source, "drawable", context.packageName)
                    if (resId != 0) {
                        BitmapFactory.decodeResource(context.resources, resId)
                    } else null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Automated AI/Edge-aware background removal engine with shape masking.
     */
    suspend fun processCutout(
        context: Context,
        source: String,
        maskShape: CutoutMaskShape = CutoutMaskShape.TRANSPARENT_CUTOUT,
        applyBgRemoval: Boolean = true
    ): String? = withContext(Dispatchers.Default) {
        val original = loadSourceBitmap(context, source) ?: return@withContext null

        // Scale down large images for optimal processing speed (< 1000px)
        val maxDim = 800
        val scale = if (original.width > maxDim || original.height > maxDim) {
            maxDim.toFloat() / max(original.width, original.height)
        } else 1.0f

        val workingBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * scale).toInt(),
                (original.height * scale).toInt(),
                true
            )
        } else {
            original.copy(Bitmap.Config.ARGB_8888, true)
        }

        val processedBitmap = when (maskShape) {
            CutoutMaskShape.TRANSPARENT_CUTOUT -> {
                if (applyBgRemoval) {
                    removeBackgroundFast(workingBitmap)
                } else {
                    workingBitmap
                }
            }
            CutoutMaskShape.CIRCLE_RING -> {
                createCircularCutout(workingBitmap, withRing = true)
            }
            CutoutMaskShape.GOLDEN_FRAME -> {
                createGoldenFrameCutout(workingBitmap)
            }
            CutoutMaskShape.ROUNDED_SQUARE -> {
                createRoundedSquareCutout(workingBitmap)
            }
            CutoutMaskShape.SHIELD -> {
                createShieldCutout(workingBitmap)
            }
        }

        // Save to cache directory
        saveCutoutToCache(context, processedBitmap)
    }

    /**
     * Fast on-device background segmentation using edge sampling and flood fill color distance.
     */
    private fun removeBackgroundFast(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Sample background colors from 4 corners and borders
        val cornerSamples = listOf(
            pixels[0], // Top-Left
            pixels[width - 1], // Top-Right
            pixels[(height - 1) * width], // Bottom-Left
            pixels[width * height - 1], // Bottom-Right
            pixels[width / 2], // Top-Center
            pixels[(height - 1) * width + width / 2] // Bottom-Center
        )

        // Find average background color components
        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        for (c in cornerSamples) {
            totalR += Color.red(c)
            totalG += Color.green(c)
            totalB += Color.blue(c)
        }
        val avgBgR = (totalR / cornerSamples.size).toInt()
        val avgBgG = (totalG / cornerSamples.size).toInt()
        val avgBgB = (totalB / cornerSamples.size).toInt()

        // Flood fill from borders to identify continuous background pixels
        val isBackground = BooleanArray(width * height)
        val visited = BooleanArray(width * height)
        val queue = ArrayDeque<Int>()

        // Color distance tolerance
        val tolerance = 52.0

        fun isColorSimilarToBg(pixelColor: Int): Boolean {
            val r = Color.red(pixelColor)
            val g = Color.green(pixelColor)
            val b = Color.blue(pixelColor)
            val dist = sqrt(((r - avgBgR) * (r - avgBgR) + (g - avgBgG) * (g - avgBgG) + (b - avgBgB) * (b - avgBgB)).toDouble())
            if (dist < tolerance) return true

            // Also check distance to individual corner samples
            for (sample in cornerSamples) {
                val sr = Color.red(sample)
                val sg = Color.green(sample)
                val sb = Color.blue(sample)
                val d = sqrt(((r - sr) * (r - sr) + (g - sg) * (g - sg) + (b - sb) * (b - sb)).toDouble())
                if (d < tolerance * 0.85) return true
            }
            return false
        }

        // Seed with perimeter pixels
        for (x in 0 until width) {
            val topIdx = x
            val btmIdx = (height - 1) * width + x
            if (isColorSimilarToBg(pixels[topIdx])) {
                queue.add(topIdx)
                visited[topIdx] = true
            }
            if (isColorSimilarToBg(pixels[btmIdx])) {
                queue.add(btmIdx)
                visited[btmIdx] = true
            }
        }
        for (y in 0 until height) {
            val leftIdx = y * width
            val rightIdx = y * width + (width - 1)
            if (!visited[leftIdx] && isColorSimilarToBg(pixels[leftIdx])) {
                queue.add(leftIdx)
                visited[leftIdx] = true
            }
            if (!visited[rightIdx] && isColorSimilarToBg(pixels[rightIdx])) {
                queue.add(rightIdx)
                visited[rightIdx] = true
            }
        }

        // BFS flood fill
        while (!queue.isEmpty()) {
            val idx = queue.poll() ?: break
            isBackground[idx] = true
            val x = idx % width
            val y = idx / width

            // 4-neighborhood
            val neighbors = intArrayOf(
                if (x > 0) idx - 1 else -1,
                if (x < width - 1) idx + 1 else -1,
                if (y > 0) idx - width else -1,
                if (y < height - 1) idx + width else -1
            )

            for (n in neighbors) {
                if (n >= 0 && !visited[n]) {
                    visited[n] = true
                    if (isColorSimilarToBg(pixels[n])) {
                        queue.add(n)
                    }
                }
            }
        }

        // Set alpha channel for output
        val outputPixels = IntArray(width * height)
        for (i in 0 until width * height) {
            if (isBackground[i]) {
                outputPixels[i] = Color.TRANSPARENT
            } else {
                outputPixels[i] = pixels[i]
            }
        }

        output.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun createCircularCutout(source: Bitmap, withRing: Boolean): Bitmap {
        val size = min(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius - 4f, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(
            (source.width - size) / 2,
            (source.height - size) / 2,
            (source.width + size) / 2,
            (source.height + size) / 2
        )
        val destRect = Rect(0, 0, size, size)
        canvas.drawBitmap(source, srcRect, destRect, paint)

        if (withRing) {
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = size * 0.04f
                color = Color.parseColor("#4F46E5")
            }
            canvas.drawCircle(radius, radius, radius - (ringPaint.strokeWidth / 2f), ringPaint)
        }

        return output
    }

    private fun createGoldenFrameCutout(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius - 10f, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(
            (source.width - size) / 2,
            (source.height - size) / 2,
            (source.width + size) / 2,
            (source.height + size) / 2
        )
        val destRect = Rect(0, 0, size, size)
        canvas.drawBitmap(source, srcRect, destRect, paint)

        // Outer Golden Ring
        val goldOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.05f
            color = Color.parseColor("#F59E0B")
        }
        canvas.drawCircle(radius, radius, radius - (goldOuter.strokeWidth / 2f), goldOuter)

        // Inner Golden Accent
        val goldInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.015f
            color = Color.parseColor("#FDE68A")
        }
        canvas.drawCircle(radius, radius, radius - (goldOuter.strokeWidth) - 4f, goldInner)

        return output
    }

    private fun createRoundedSquareCutout(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }

        val rectF = RectF(6f, 6f, size - 6f, size - 6f)
        val cornerRadius = size * 0.22f
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(
            (source.width - size) / 2,
            (source.height - size) / 2,
            (source.width + size) / 2,
            (source.height + size) / 2
        )
        val destRect = Rect(0, 0, size, size)
        canvas.drawBitmap(source, srcRect, destRect, paint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.035f
            color = Color.WHITE
        }
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, borderPaint)

        return output
    }

    private fun createShieldCutout(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
        }

        val path = Path().apply {
            moveTo(size * 0.5f, size * 0.05f)
            lineTo(size * 0.92f, size * 0.22f)
            lineTo(size * 0.92f, size * 0.65f)
            quadTo(size * 0.5f, size * 0.98f, size * 0.5f, size * 0.98f)
            quadTo(size * 0.08f, size * 0.65f, size * 0.08f, size * 0.65f)
            lineTo(size * 0.08f, size * 0.22f)
            close()
        }

        canvas.drawPath(path, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(
            (source.width - size) / 2,
            (source.height - size) / 2,
            (source.width + size) / 2,
            (source.height + size) / 2
        )
        val destRect = Rect(0, 0, size, size)
        canvas.drawBitmap(source, srcRect, destRect, paint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = size * 0.035f
            color = Color.parseColor("#F59E0B")
        }
        canvas.drawPath(path, borderPaint)

        return output
    }

    private suspend fun saveCutoutToCache(context: Context, bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val cutoutsDir = File(context.cacheDir, "cutouts").apply { mkdirs() }
            val file = File(cutoutsDir, "cutout_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
