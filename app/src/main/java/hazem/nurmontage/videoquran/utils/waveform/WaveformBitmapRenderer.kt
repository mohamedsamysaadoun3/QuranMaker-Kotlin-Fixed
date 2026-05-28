package hazem.nurmontage.videoquran.utils.waveform

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF

/**
 * Pre-renders an audio waveform into a [Bitmap] and draws it on a [Canvas]
 * with horizontal scaling and translation for smooth timeline scrolling.
 *
 * Rendering pipeline:
 * 1. **Constructor** receives the amplitude array and allocates a bitmap.
 * 2. [generateBitmap] draws vertical lines proportional to each amplitude value.
 *    The peak amplitude is normalized so the tallest bar fills 85% of the height.
 * 3. [draw] renders the bitmap onto the given canvas using a [Matrix] that applies
 *    horizontal scaling (zoom) and translation (scroll offset).
 * 4. [release] recycles the bitmap to free native memory.
 *
 * **Performance note**: The bitmap is generated once in the constructor and
 * reused for every frame. Only [setColor] triggers regeneration.
 *
 * Converted from WaveformBitmapRenderer.java — logic preserved exactly.
 */
class WaveformBitmapRenderer(
    private val amps: FloatArray?,
    bitmapWidth: Int,
    bitmapHeight: Int,
    waveColor: Int
) {
    private val bitmapWidth: Int = bitmapWidth
    private val bitmapHeight: Int = bitmapHeight
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = waveColor
        alpha = 100
    }
    private var waveformBitmap: Bitmap? = null

    init {
        generateBitmap()
    }

    /**
     * Generate the waveform bitmap by drawing one vertical line per pixel column.
     *
     * Each column maps to an amplitude sample via linear interpolation.
     * The maximum amplitude in the array is used as the normalization factor
     * so that the tallest bar fills 85% of the bitmap height.
     *
     * Lines are drawn **bottom-up** from `(x, bitmapHeight)` to `(x, bitmapHeight - barHeight)`.
     */
    private fun generateBitmap() {
        val amplitudes = amps ?: return
        if (amplitudes.isEmpty()) return

        waveformBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(waveformBitmap!!)

        val usableHeight = bitmapHeight * 0.85f

        // Find the peak amplitude for normalization
        var peak = 0f
        for (amp in amplitudes) {
            if (amp > peak) peak = amp
        }
        // Prevent division by zero — minimum normalization factor
        val normalizer = if (peak < 0.01f) 0.01f else peak

        // Draw one vertical line per pixel column
        for (x in 0 until bitmapWidth) {
            // Map pixel position to amplitude array index
            var index = (x.toFloat() / bitmapWidth * amplitudes.size).toInt()
            if (index >= amplitudes.size) {
                index = amplitudes.size - 1
            }
            val barHeight = (amplitudes[index] / normalizer) * usableHeight
            canvas.drawLine(x.toFloat(), bitmapHeight.toFloat(), x.toFloat(), bitmapHeight - barHeight, paint)
        }
    }

    /**
     * Draw the pre-rendered waveform bitmap onto the given canvas.
     *
     * The bitmap is scaled horizontally by [scaleFactor] and translated
     * so that it scrolls correctly with the timeline viewport.
     *
     * @param canvas    The target canvas
     * @param rect      The bounding rectangle of the audio entity on the timeline
     * @param scaleFactor Horizontal zoom factor (mScaleFactor + scaleEffect)
     * @param offset    Horizontal scroll offset (offset + offsetLeft + tmpOffset)
     */
    fun draw(canvas: Canvas, rect: RectF, scaleFactor: Float, offset: Float) {
        val bitmap = waveformBitmap ?: return
        val translateX = rect.left - (offset * scaleFactor)
        val matrix = Matrix().apply {
            postScale(scaleFactor, 1.0f)
            postTranslate(translateX, rect.top)
        }
        canvas.drawBitmap(bitmap, matrix, null)
    }

    /**
     * Stub for overlay rendering (reserved for future use).
     */
    fun drawOverlay(canvas: Canvas, rect: RectF, f: Float, f2: Float, paint: Paint) {
        // No-op in original
    }

    /**
     * Change the waveform color and regenerate the bitmap.
     */
    fun setColor(color: Int) {
        paint.color = color
        generateBitmap()
    }

    /**
     * Returns the pre-rendered waveform bitmap (for testing or external access).
     */
    fun getBitmap(): Bitmap? = waveformBitmap

    /**
     * Recycle the bitmap to free native memory.
     * Must be called when the parent [EntityAudio] is released.
     */
    fun release() {
        waveformBitmap?.let {
            if (!it.isRecycled) it.recycle()
        }
        waveformBitmap = null
    }
}
