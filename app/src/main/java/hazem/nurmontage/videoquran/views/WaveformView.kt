package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatView

/**
 * Custom view that draws a simple audio waveform visualization
 * with a progress indicator.
 *
 * Bars before the progress point are drawn in white; bars after
 * are drawn in a muted gray. Touch/drag events update the progress
 * and notify the [OnWaveformClickListener].
 *
 * Originally: WaveformView.java (87 lines)
 */
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatView(context, attrs, defStyleAttr) {

    /** Callback for progress change events via touch. */
    interface OnWaveformClickListener {
        fun onProgressChanged(progress: Float)
    }

    private var amplitudes: IntArray = intArrayOf(30, 40, 60, 80, 50, 90, 100, 70, 40, 60, 80, 50, 30, 50, 70, 90, 60, 40)
    private var listener: OnWaveformClickListener? = null
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var progress: Float = 0.0f

    fun setOnWaveformClickListener(listener: OnWaveformClickListener?) {
        this.listener = listener
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            var x = event.x / width
            if (x < 0.0f) x = 0.0f
            if (x > 1.0f) x = 1.0f
            setProgress(x)
            listener?.onProgressChanged(x)
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val barWidth = viewWidth / (amplitudes.size * 2)

        for (i in amplitudes.indices) {
            val barHeight = (amplitudes[i] / 100.0f) * viewHeight
            val barIndex = i.toFloat()
            val left = barIndex * (barWidth + barWidth)
            val top = (viewHeight - barHeight) / 2.0f
            val barRatio = barIndex / amplitudes.size

            paint.color = if (progress > 0.0f && barRatio < progress) {
                -1 // white
            } else {
                -12303292 // 0x44777774 muted gray
            }

            canvas.drawRoundRect(left, top, left + barWidth, top + barHeight, 5.0f, 5.0f, paint)
        }
    }
}
