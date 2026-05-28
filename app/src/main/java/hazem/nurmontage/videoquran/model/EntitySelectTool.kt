package hazem.nurmontage.videoquran.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.utils.FileUtils

/**
 * Renders the selection overlay (scale handle + "Apply All" button) around
 * a selected [EntityView] on the canvas.
 *
 * When the user taps an entity in the engine editor, this class draws:
 *   - A rounded-rect border around the entity bounds
 *   - A circular scale handle (with expand icon) at the bottom-left corner
 *   - An "Apply All" pill button at the top-right corner (when [isApply_all] is true)
 *
 * The scale handle bitmap is pre-rendered in the constructor using the
 * [R.drawable.ic_expand] icon, and the "Apply All" bitmap is drawn with
 * text that switches between Arabic and English based on the current locale.
 *
 * Touch-hit testing is done via [isScale] and [isApply], which check whether
 * a given (x, y) coordinate falls inside the handle/button hit rectangles.
 *
 * Converted from EntitySelectTool.java (179 lines).
 */
class EntitySelectTool(canvasSize: Int, context: Context) {

    // ── Pre-rendered bitmaps ─────────────────────────────────────────
    private val bitmapScale: Bitmap
    private val bitmapApplyAll: Bitmap

    // ── Hit-test rectangles ──────────────────────────────────────────
    private val rectFScale: RectF
    private val rectApplyAll: RectF

    // ── Paint ────────────────────────────────────────────────────────
    private val paint: Paint

    // ── Offsets for handle positioning ───────────────────────────────
    private val offsetX: Float
    private val offsetY: Float
    private val offsetYApply: Float

    // ── Corner radius for the selection border ───────────────────────
    private val round: Float

    // ── Interaction state ────────────────────────────────────────────
    var isClick_apply: Boolean = false
        private set
    var isOnProgress: Boolean = false
        private set
    var isApply_Move: Boolean = false
        private set
    var isApply_Scale: Boolean = false
        private set
    var isApply_all: Boolean = false
        private set
    var isOnScale: Boolean = false
        private set

    // ═══════════════════════════════════════════════════════════════════
    //  Constructor — pre-renders scale handle and "Apply All" bitmaps
    // ═══════════════════════════════════════════════════════════════════

    init {
        // Load the font for "Apply All" text
        val applyAllFont = FileUtils.loadFontFromAsset(context, "fonts/arabic/خط الإبل.otf")
        val applyAllText = if (LocaleHelper.getLanguage(context) == "ar") "تطبيق على الكل" else "ApplyAll"

        // Initialize paint for selection border and handle rendering
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        paint = p
        p.color = -0x6400B  // 0xFFF9BFF5 — selection color from original
        val f = canvasSize.toFloat()
        p.strokeWidth = 0.005f * f
        round = 0.02f * f

        // Calculate handle size (4.7% of canvas)
        val handleSize = (f * 0.047f).toInt()
        val handleSizeF = handleSize.toFloat()

        // Scale handle rect (circular)
        rectFScale = RectF(0f, 0f, handleSizeF, handleSizeF)

        // "Apply All" rect (4x wider than scale handle)
        rectApplyAll = RectF(0f, 0f, handleSize * 4f, rectFScale.height())

        // Positioning offsets
        offsetX = rectFScale.width() * 0.7f
        val halfStroke = paint.strokeWidth * 0.5f
        offsetY = halfStroke
        offsetYApply = halfStroke * 3.0f

        // ── Render scale handle bitmap ────────────────────────────────
        paint.style = Paint.Style.FILL
        val expandDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_expand)
        bitmapScale = Bitmap.createBitmap(handleSize, handleSize, Bitmap.Config.ARGB_8888)
        val scaleCanvas = Canvas(bitmapScale)
        scaleCanvas.drawCircle(
            scaleCanvas.width * 0.5f,
            scaleCanvas.height * 0.5f,
            handleSizeF * 0.5f,
            paint
        )
        val inset = (handleSizeF * 0.1f).toInt()
        val end = handleSize - inset
        expandDrawable?.setBounds(inset, inset, end, end)
        expandDrawable?.draw(scaleCanvas)

        // ── Render "Apply All" bitmap ─────────────────────────────────
        bitmapApplyAll = Bitmap.createBitmap(
            rectApplyAll.width().toInt(),
            rectApplyAll.height().toInt(),
            Bitmap.Config.ARGB_8888
        )
        scaleCanvas.setBitmap(bitmapApplyAll)
        val cornerRadius = (rectApplyAll.height() * 0.2f).toInt().toFloat()
        scaleCanvas.drawRoundRect(rectApplyAll, cornerRadius, cornerRadius, paint)

        // Draw "Apply All" text centered on the pill
        paint.style = Paint.Style.FILL
        paint.color = -0xDE4E4E  // 0xFF21B1B1 — dark text color
        paint.typeface = applyAllFont
        val maxTextWidth = rectApplyAll.width() * 0.8f
        val maxTextHeight = rectApplyAll.height() * 0.6f
        paint.textSize = 100f
        val textBounds = Rect()
        paint.getTextBounds(applyAllText, 0, applyAllText.length, textBounds)
        paint.textSize = Math.min(maxTextWidth / textBounds.width(), maxTextHeight / textBounds.height()) * 100f
        paint.getTextBounds(applyAllText, 0, applyAllText.length, textBounds)
        scaleCanvas.drawText(
            applyAllText,
            rectApplyAll.centerX() - textBounds.width() * 0.5f,
            rectApplyAll.centerY() - textBounds.exactCenterY(),
            paint
        )

        // Reset paint to stroke style for border drawing
        paint.color = -0x6400B
        paint.style = Paint.Style.STROKE
    }

    // ═══════════════════════════════════════════════════════════════════
    //  State setters (mutually exclusive for Move/Scale)
    // ═══════════════════════════════════════════════════════════════════

    fun setClick_apply(click: Boolean) { isClick_apply = click }

    fun setOnProgress(onProgress: Boolean) { isOnProgress = onProgress }

    fun setApply_Move(move: Boolean) {
        isApply_Move = move
        if (move) setApply_Scale(false)
    }

    fun setApply_Scale(scale: Boolean) {
        isApply_Scale = scale
        if (scale) setApply_Move(false)
    }

    fun setApply_all(applyAll: Boolean) { isApply_all = applyAll }

    // ═══════════════════════════════════════════════════════════════════
    //  Hit testing
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Tests whether the (x, y) touch point hits the "Apply All" button
     * for the given [entityView]. The button is positioned at the
     * top-right corner of the entity's bounding rect.
     */
    fun isApply(entityView: EntityView, x: Float, y: Float): Boolean {
        if (!isApply_all) return false
        rectApplyAll.left = entityView.getRect().right - bitmapApplyAll.width
        rectApplyAll.right = entityView.getRect().right
        rectApplyAll.top = entityView.getRect().top - bitmapApplyAll.height - offsetYApply
        rectApplyAll.bottom = entityView.getRect().top
        return rectApplyAll.contains(x, y)
    }

    /**
     * Tests whether the (x, y) touch point hits the scale handle
     * for the given [entityView].
     *
     * For [TranslationQuranEntity], the handle is positioned at the
     * top-left corner; for all other entities, it is at the bottom-left.
     */
    fun isScale(entityView: EntityView, x: Float, y: Float): Boolean {
        if (entityView is TranslationQuranEntity) {
            rectFScale.top = entityView.getRect().top - offsetY * 2.0f
            rectFScale.left = entityView.getRect().left - offsetX
        } else {
            rectFScale.left = entityView.getRect().left - offsetX * 2.0f
            rectFScale.top = entityView.getRect().bottom - offsetY * 2.0f
        }
        rectFScale.right = rectFScale.left + bitmapScale.width * 1.5f
        rectFScale.bottom = rectFScale.top + bitmapScale.height * 1.5f
        val contains = rectFScale.contains(x, y)
        isOnScale = contains
        setApply_Scale(contains)
        return isOnScale
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Drawing
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Draws the selection overlay on [canvas] around [entityView]:
     *   1. Rounded-rect border
     *   2. Scale handle bitmap
     *   3. "Apply All" button bitmap (if [isApply_all] is true)
     */
    fun draw(canvas: Canvas, entityView: EntityView) {
        val rect = entityView.getRect()
        canvas.drawRoundRect(rect, round, round, paint)

        // Scale handle position depends on entity type
        if (entityView is TranslationQuranEntity) {
            canvas.drawBitmap(
                bitmapScale,
                entityView.getRect().left,
                entityView.getRect().top - offsetY,
                null
            )
        } else {
            canvas.drawBitmap(
                bitmapScale,
                entityView.getRect().left - offsetX,
                entityView.getRect().bottom - offsetY,
                null
            )
        }

        // "Apply All" button at top-right
        if (isApply_all) {
            canvas.drawBitmap(
                bitmapApplyAll,
                entityView.getRect().right - bitmapApplyAll.width,
                entityView.getRect().top - bitmapApplyAll.height - offsetYApply,
                null
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Reset
    // ═══════════════════════════════════════════════════════════════════

    /** Clears all selection state (move, scale, apply-all). */
    fun reset() {
        setApply_Move(false)
        setApply_Scale(false)
        setApply_all(false)
    }
}
