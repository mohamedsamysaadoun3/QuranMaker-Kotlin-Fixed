package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Simple waveform visualization view.
 *
 * Renders a placeholder waveform graphic. Used in the audio preview
 * screen (activity_ads_tuufah) to display before/after audio waveforms.
 *
 * Originally part of the QuranMaker app, this stub preserves the
 * layout XML reference while providing basic drawing support.
 */
class WaveformView : View {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = -0x4e4e4f  // gray
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val mid = h / 2f
        // Draw simple placeholder waveform lines
        val step = w / 20f
        for (i in 0..19) {
            val x = i * step + step / 2f
            val amplitude = (Math.sin(i * 0.8) * mid * 0.6).toFloat()
            canvas.drawLine(x, mid - amplitude, x, mid + amplitude, paint)
        }
    }
}
