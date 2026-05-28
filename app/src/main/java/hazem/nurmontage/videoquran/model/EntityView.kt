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
    protected var rect: RectF = RectF()
    protected var posX: Float = 0f
    protected var posY: Float = 0f
    protected var maxW: Int = 0
    protected var maxH: Int = 0

    // ──────────────────────────────────────────────
    //  Scale factors
    // ──────────────────────────────────────────────
    private var factorScale: Float = 1.0f
    private var factorSize: Float = 1.0f
    private var factorSizeTrl: Float = 1.0f

    // ──────────────────────────────────────────────
    //  Canvas metadata
    // ──────────────────────────────────────────────
    private var canvasW: Int = 0
    private var canvasH: Int = 0
    private var copyRect: RectF? = null

    // ──────────────────────────────────────────────
    //  Timeline associations
    // ──────────────────────────────────────────────
    private var entityQuran: EntityQuranTimeline? = null
    private var entityTrslTimeline: EntityTrslTimeline? = null

    // ──────────────────────────────────────────────
    //  Test-mode flag (for preview / animation testing)
    // ──────────────────────────────────────────────
    private var isAnimTest: Boolean = false

    // ══════════════════════════════════════════════
    //  Abstract contract
    // ══════════════════════════════════════════════
    abstract fun endAnimator()
    abstract fun isVisible(): Boolean
    abstract fun postTranslate(dx: Float, dy: Float)
    abstract fun scale(factor: Float, canvasW: Int, canvasH: Int)
    abstract fun setVisible(visible: Boolean)

    // ══════════════════════════════════════════════
    //  Max bounds
    // ══════════════════════════════════════════════
    open fun getMaxW(): Int = maxW
    open fun getMaxH(): Int = maxH

    // ══════════════════════════════════════════════
    //  Scale factor accessors
    // ══════════════════════════════════════════════
    fun setFactorSizeTrl(factorSizeTrl: Float) {
        this.factorSizeTrl = factorSizeTrl
    }

    fun getFactorSizeTrl(): Float = factorSizeTrl

    fun setFcSize(factorSize: Float) {
        this.factorSize = factorSize
    }

    fun getFactorSize(): Float = factorSize

    fun getFactorScale(): Float = factorScale

    fun setFactorScale(factorScale: Float) {
        this.factorScale = factorScale
    }

    // ══════════════════════════════════════════════
    //  Timeline associations
    // ══════════════════════════════════════════════
    fun setEntityTrslTimeline(entityTrslTimeline: EntityTrslTimeline?) {
        this.entityTrslTimeline = entityTrslTimeline
    }

    fun getEntityTrslTimeline(): EntityTrslTimeline? = entityTrslTimeline

    open fun getEntityQuran(): EntityQuranTimeline? = entityQuran

    open fun setEntityQuran(entityQuran: EntityQuranTimeline?) {
        this.entityQuran = entityQuran
    }

    // ══════════════════════════════════════════════
    //  Rect & canvas helpers
    // ══════════════════════════════════════════════
    fun getRect(): RectF = rect

    fun setCanvasWH(w: Int, h: Int) {
        canvasW = w
        canvasH = h
    }

    fun getCanvasW(): Int = canvasW
    fun getCanvasH(): Int = canvasH

    fun getCopyRect(): RectF? = copyRect

    fun setCopyRect() {
        if (rect == null) return
        copyRect = RectF(
            rect.left / canvasW,
            rect.top / canvasH,
            rect.right / canvasW,
            rect.bottom / canvasH
        )
    }

    // ══════════════════════════════════════════════
    //  Animation-test mode
    // ══════════════════════════════════════════════
    fun isAnimTest(): Boolean = isAnimTest

    open fun setAnimTest(animTest: Boolean) {
        isAnimTest = animTest
    }
}
