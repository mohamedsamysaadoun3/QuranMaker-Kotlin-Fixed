package hazem.nurmontage.videoquran.utils.video

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Applies cinematic post-processing effects to bitmaps for video rendering.
 *
 * Two effects are applied in sequence:
 * 1. **Color grading** — warm tone shift via a custom [ColorMatrix] that boosts reds
 *    and blues, combined with a slight desaturation (0.85) for a film-like look.
 * 2. **Vignette** — radial gradient overlay darkening the edges, creating a focus
 *    effect toward the center of the frame.
 *
 * Also provides [createGlassRect] for rounded-rectangle masking with glass-like
 * transparency, used for overlay frames and widgets.
 *
 * Converted from CinematicProcessor.java — color matrix values and vignette
 * parameters preserved exactly to ensure identical visual output.
 */
object CinematicProcessor {

    /**
     * Apply the full cinematic pipeline (color grade + vignette) to a bitmap.
     *
     * The input bitmap is **not** mutated; a mutable copy is created first.
     *
     * @param bitmap Source bitmap (may be immutable)
     * @return A new bitmap with cinematic effects applied, or null if input is null
     */
    fun applyCinematicEffect(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null

        val copy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(copy)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        applyColorGrade(canvas, copy, paint)
        applyVignette(canvas, copy)

        return copy
    }

    /**
     * Create a rounded-rectangle glass overlay from a source bitmap.
     *
     * The result is a new bitmap with the same dimensions as the input,
     * where only the rounded-rectangle interior is visible (everything
     * outside the rounded rect is transparent).
     *
     * @param bitmap  Source bitmap
     * @param margin  Inset margin in pixels for the rounded rectangle
     * @return A new bitmap with the glass mask applied
     */
    fun createGlassRect(bitmap: Bitmap, margin: Int): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = -1 } // White

        val rect = RectF(
            margin.toFloat(),
            margin.toFloat(),
            (bitmap.width - margin).toFloat(),
            (bitmap.height - margin).toFloat()
        )

        canvas.drawARGB(0, 0, 0, 0) // Clear to transparent
        canvas.drawRoundRect(rect, 40f, 40f, paint)

        // SRC_IN: keep the bitmap only where the rounded rect was drawn
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        paint.xfermode = null

        return result
    }

    // ════════════════════════════════════════════════════════════════════
    //  Internal pipeline stages
    // ════════════════════════════════════════════════════════════════════

    /**
     * Apply warm color grading using a two-stage ColorMatrix:
     *
     * Stage 1 — Custom matrix boosting reds and blues with slight bias:
     * ```
     * R' = 1.1R + 0.1G + 0.0B - 10
     * G' = 0.0R + 1.0G + 0.0B - 10
     * B' = 0.0R + 0.1G + 1.2B - 10
     * A' = 0.0R + 0.0G + 0.0B + 1.0
     * ```
     *
     * Stage 2 — Saturation reduction to 85% for a cinematic desaturated look.
     *
     * The two matrices are concatenated (postConcat) before application.
     */
    private fun applyColorGrade(canvas: Canvas, bitmap: Bitmap, paint: Paint) {
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1.1f, 0.1f, 0.0f, 0.0f, -10.0f,
                0.0f, 1.0f, 0.0f, 0.0f, -10.0f,
                0.0f, 0.1f, 1.2f, 0.0f, -10.0f,
                0.0f, 0.0f, 0.0f, 1.0f,   0.0f
            ))
        }

        val saturationMatrix = ColorMatrix().apply {
            setSaturation(0.85f)
        }

        colorMatrix.postConcat(saturationMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }

    /**
     * Apply a vignette effect using a radial gradient overlay.
     *
     * The gradient is centered on the bitmap with a radius of 70% of the
     * diagonal length. It transitions from fully transparent at the center
     * (stop 0.0–0.4) to a semi-transparent dark tint at the edges (stop 1.0).
     *
     * The gradient is drawn with [PorterDuff.Mode.SRC_ATOP] so it only
     * affects non-transparent pixels, preserving alpha from the color grade pass.
     *
     * Vignette color: `-1728053248` (0x98680000) — a dark warm-brown tint.
     */
    private fun applyVignette(canvas: Canvas, bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val halfW = width.toFloat()
        val halfH = height.toFloat()

        val radius = (sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0)) * 0.7).toFloat()

        val radialGradient = RadialGradient(
            halfW / 2.0f, halfH / 2.0f, radius,
            intArrayOf(0, 0, -1728053248),
            floatArrayOf(0.0f, 0.4f, 1.0f),
            Shader.TileMode.CLAMP
        )

        val paint = Paint().apply {
            isAntiAlias = true
            shader = radialGradient
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        }

        canvas.drawRect(0f, 0f, halfW, halfH, paint)
    }
}
