package hazem.nurmontage.videoquran.model.data

import android.graphics.Canvas
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.model.EntityView

/**
 * Simple text entity rendered on the canvas (chapter headings, labels, etc.).
 *
 * Unlike [QuranEntity] and [BismilahEntity], this class has no translation
 * layer and no animation support.  It is used for static, non-interactive
 * text overlays in the editor.
 *
 * Originally: `hazem.nurmontage.videoquran.model.TextEntity`
 */
class TextEntity : EntityView {

    private var entityQuranTimeline: EntityQuranTimeline? = null
    private var isVisible: Boolean = false
    private var paintAya: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var staticLayout: StaticLayout? = null
    private var txt: String? = null
    private var viewWidth: Int = 0
    private var x: Float = 0f
    private var y: Float = 0f

    // ──────────────────────────────────────────────
    //  Constructors
    // ──────────────────────────────────────────────

    /**
     * Creates a TextEntity with a timeline association.
     */
    constructor(txt: String, x: Float, y: Float, entityQuran: EntityQuranTimeline) {
        this.txt = txt
        this.x = x
        this.y = y
        setVisible(true)
        this.entityQuranTimeline = entityQuran
    }

    /**
     * Creates a TextEntity with its own paint and view width.
     */
    constructor(txt: String, x: Float, y: Float, viewWidth: Int) {
        this.txt = txt
        this.x = x
        this.y = y
        setVisible(true)
        this.viewWidth = viewWidth
        paintAya.color = -1 // 0xFFFFFFFF
        paintAya.textSize = viewWidth * 0.06f
        createStaticLayout()
    }

    // ──────────────────────────────────────────────
    //  Abstract overrides (minimal)
    // ──────────────────────────────────────────────

    override fun endAnimator() {
        // No animation support in TextEntity
    }

    override fun postTranslate(dx: Float, dy: Float) {
        // No-op for simple text
    }

    override fun scale(factor: Float, canvasW: Int, canvasH: Int) {
        // No-op for simple text
    }

    // ──────────────────────────────────────────────
    //  Text & layout
    // ──────────────────────────────────────────────

    fun setTxt(txt: String) {
        this.txt = txt
        staticLayout = StaticLayout.Builder.obtain(
            txt, 0, txt.length, paintAya, viewWidth
        ).setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
    }

    private fun createStaticLayout() {
        val txt = this.txt ?: return
        staticLayout = StaticLayout.Builder.obtain(
            txt, 0, txt.length, paintAya, viewWidth
        ).setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
    }

    fun update(canvasH: Int, canvasW: Int) {
        y = canvasH * 0.67f
        viewWidth = canvasW
        paintAya.textSize = canvasW * 0.06f
        createStaticLayout()
    }

    fun getStaticLayout(): StaticLayout? = staticLayout

    override fun setEntityQuran(entityQuran: EntityQuranTimeline?) {
        this.entityQuranTimeline = entityQuran
    }

    // ──────────────────────────────────────────────
    //  Drawing
    // ──────────────────────────────────────────────

    fun draw(canvas: Canvas) {
        val layout = staticLayout ?: return
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    fun singleDraw(canvas: Canvas) {
        staticLayout?.draw(canvas)
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    override fun getEntityQuran(): EntityQuranTimeline? = entityQuranTimeline

    override fun isVisible(): Boolean = isVisible

    override fun setVisible(visible: Boolean) {
        isVisible = visible
    }

    fun getX(): Float = x
    fun getY(): Float = y
    fun getTxt(): String? = txt
}
