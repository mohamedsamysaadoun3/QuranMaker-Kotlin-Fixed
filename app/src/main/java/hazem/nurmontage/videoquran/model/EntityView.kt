package hazem.nurmontage.videoquran.model

import android.graphics.RectF
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityTrslTimeline

/**
 * Abstract base class for all entity views rendered on the canvas.
 *
 * Provides shared position/scale state (rect, factor sizes, canvas dimensions)
 * and defines the contract that every concrete entity must implement.
 *
 * Originally: `hazem.nurmontage.videoquran.model.EntityView`
 */
abstract class EntityView {

    // ──────────────────────────────────────────────
    //  Position & bounds
    // ──────────────────────────────────────────────
    open var rect: RectF = RectF()
    open var posX: Float = 0f
    open var posY: Float = 0f
    open var maxW: Int = 0
    open var maxH: Int = 0

    // ──────────────────────────────────────────────
    //  Scale factors
    // ──────────────────────────────────────────────
    open var factorScale: Float = 1.0f
    open var factorSize: Float = 1.0f
    open var factorSizeTrl: Float = 1.0f

    // ──────────────────────────────────────────────
    //  Canvas metadata
    // ──────────────────────────────────────────────
    private var canvasW: Int = 0
    private var canvasH: Int = 0
    var copyRect: RectF? = null

    // ──────────────────────────────────────────────
    //  Timeline associations
    // ──────────────────────────────────────────────
    open var entityQuran: EntityQuranTimeline? = null
    var entityTrslTimeline: EntityTrslTimeline? = null

    // ──────────────────────────────────────────────
    //  Test-mode flag (for preview / animation testing)
    // ──────────────────────────────────────────────
    open var isAnimTest: Boolean = false

    // ══════════════════════════════════════════════
    //  Abstract contract
    // ══════════════════════════════════════════════
    abstract fun endAnimator()
    abstract var isVisible: Boolean
    abstract fun postTranslate(dx: Float, dy: Float)
    abstract fun scale(factor: Float, canvasW: Int, canvasH: Int)

    // ══════════════════════════════════════════════
    //  Max bounds
    // ══════════════════════════════════════════════

    /** Alias for [maxW] — used by many call-sites that expect `max_w`. */
    val max_w: Int get() = maxW

    /** Alias for [maxH] — used by many call-sites that expect `max_h`. */
    val max_h: Int get() = maxH

    // ══════════════════════════════════════════════
    //  Scale factor accessors
    // ══════════════════════════════════════════════

    fun setFcSize(factorSize: Float) {
        this.factorSize = factorSize
    }

    /** Alias for [factorScale] — used by many call-sites that expect `scaleFactor`. */
    var scaleFactor: Float
        get() = factorScale
        set(value) { factorScale = value }

    fun setFactor_scale(factorScale: Float) {
        this.factorScale = factorScale
    }

    fun getFactor_scale(): Float = factorScale

    // ══════════════════════════════════════════════
    //  Rect & canvas helpers
    // ══════════════════════════════════════════════
    fun setCanvasWH(w: Int, h: Int) {
        canvasW = w
        canvasH = h
    }

    fun getCanvasW(): Int = canvasW
    fun getCanvasH(): Int = canvasH

    fun setCopyRect() {
        if (canvasW == 0 || canvasH == 0) return
        copyRect = RectF(
            rect.left / canvasW,
            rect.top / canvasH,
            rect.right / canvasW,
            rect.bottom / canvasH
        )
    }

}
