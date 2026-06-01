package hazem.nurmontage.videoquran.views.image

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.common.Common

open class SquareImageView : AppCompatImageView {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintRect: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var isSelect: Boolean = false
    private var number: String? = null
    private var anInt: Int = 0
    private var cx: Float = 0f
    private var cy: Float = 0f
    private var radius: Float = 0f
    private var x: Float = 0f
    private var y: Float = 0f
    private var drawableDone: Drawable? = null

    fun getAnInt(): Int = anInt

    fun setNumber(number: Int) {
        if (number == 0) return
        this.anInt = number
        this.number = number.toString()
        this.cx = (width * 0.5f) - (textPaint.measureText(this.number) * 0.5f)
    }

    fun isMSelect(): Boolean = isSelect

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        textPaint.color = -1
        textPaint.typeface = Typeface.createFromAsset(resources.assets, "fonts/${Common.FONT_ENGLISH_APP}")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        setMeasuredDimension(width, width)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        paintRect.color = -1056964608
        val size = w.toFloat()
        paint.strokeWidth = 0.02f * size
        if (!isSelect) {
            paint.color = -8355712
            paint.style = Paint.Style.STROKE
        } else {
            paint.color = -12190534
            paint.style = Paint.Style.FILL
        }
        textPaint.textSize = 0.25f * size
        radius = 0.1f * size
        x = size - (1.2f * radius)
        y = radius + paint.strokeWidth
        if (number != null) {
            cx = (width * 0.5f) - (textPaint.measureText(number) * 0.5f)
        }
        cy = height * 0.5f
        val checkSize = (size * 0.3f).toInt()
        val centerX = (width * 0.5f).toInt()
        val checkRect = Rect(
            centerX - checkSize,
            (cy - checkSize).toInt(),
            centerX + checkSize,
            (cy + checkSize).toInt()
        )
        val drawable = ContextCompat.getDrawable(context, R.drawable.check_24px)
        drawableDone = drawable
        drawable?.bounds = checkRect
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isSelect) return
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintRect)
        drawableDone?.draw(canvas)
        number?.let {
            canvas.drawText(it, cx, cy, textPaint)
        }
    }

    fun onSelect(selected: Boolean) {
        isSelect = selected
        if (!selected) {
            paint.color = -8355712
            paint.style = Paint.Style.STROKE
        } else {
            paint.color = -12190534
            paint.style = Paint.Style.FILL
        }
        invalidate()
    }
}
