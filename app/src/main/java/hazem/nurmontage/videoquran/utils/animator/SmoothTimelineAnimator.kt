package hazem.nurmontage.videoquran.utils.animator

import android.os.SystemClock
import android.view.Choreographer

class SmoothTimelineAnimator(
    startCursorMs: Int,
    private val maxTimeMs: Int,
    private val listener: AnimatorListener
) {

    interface AnimatorListener {
        fun onUpdate(currentTimeMs: Int)
        fun onEnd()
    }

    private var startCursorMs: Int = startCursorMs
    private var currentTimeMs: Int = startCursorMs
    private var startTimeMs: Long = 0L
    private var _isRunning: Boolean = false

    private lateinit var frameCallback: Choreographer.FrameCallback

    init {
        frameCallback = Choreographer.FrameCallback {
            if (_isRunning) {
                val elapsed = (SystemClock.uptimeMillis() - startTimeMs).toInt()
                currentTimeMs = startCursorMs + elapsed

                if (currentTimeMs >= maxTimeMs) {
                    listener.onUpdate(maxTimeMs)
                    listener.onEnd()
                    _isRunning = false
                } else {
                    listener.onUpdate(currentTimeMs)
                    Choreographer.getInstance().postFrameCallback(frameCallback)
                }
            }
        }
    }

    fun isRunning(): Boolean = _isRunning

    fun getCurrentTimeMs(): Int = currentTimeMs

    fun start() {
        _isRunning = true
        startTimeMs = SystemClock.uptimeMillis()
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        _isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}
