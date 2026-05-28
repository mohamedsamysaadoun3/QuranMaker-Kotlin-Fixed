package hazem.nurmontage.videoquran.utils.animator

import android.os.SystemClock
import android.view.Choreographer

/**
 * High-precision timeline cursor animator using [Choreographer] for vsync-aligned updates.
 *
 * Animates a timeline cursor from [startCursorMs] to [maxTimeMs] with
 * sub-frame precision by tracking real elapsed time via [SystemClock.uptimeMillis].
 *
 * The [AnimatorListener.onUpdate] callback is invoked on every vsync frame with
 * the current time position in milliseconds. When the cursor reaches [maxTimeMs],
 * [AnimatorListener.onEnd] is called and the animation stops automatically.
 *
 * **Thread safety**: All callbacks are delivered on the main thread (Choreographer
 * posts on the Looper that created it).
 *
 * Converted from SmoothTimelineAnimator.java — timing logic preserved exactly.
 */
class SmoothTimelineAnimator(
    startCursorMs: Int,
    private val maxTimeMs: Int,
    private val listener: AnimatorListener
) {

    /**
     * Listener for timeline animation progress events.
     */
    interface AnimatorListener {
        /** Called on each vsync frame with the current cursor position in milliseconds. */
        fun onUpdate(currentTimeMs: Int)
        /** Called when the animation reaches [maxTimeMs]. */
        fun onEnd()
    }

    private var startCursorMs: Int = startCursorMs
    private var currentTimeMs: Int = 0
    private var startTimeMs: Long = 0L
    private var isRunning: Boolean = false

    private val frameCallback = Choreographer.FrameCallback { frameTimeNanos ->
        if (isRunning) {
            val elapsed = (SystemClock.uptimeMillis() - startTimeMs).toInt()
            currentTimeMs = startCursorMs + elapsed

            if (currentTimeMs >= maxTimeMs) {
                listener.onUpdate(maxTimeMs)
                listener.onEnd()
                isRunning = false
            } else {
                listener.onUpdate(currentTimeMs)
                Choreographer.getInstance().postFrameCallback(frameCallback)
            }
        }
    }

    /** Whether the animator is currently running. */
    fun isRunning(): Boolean = isRunning

    /** The current cursor position in milliseconds. */
    fun getCurrentTimeMs(): Int = currentTimeMs

    /**
     * Start the animation from [startCursorMs].
     *
     * Records the system uptime as the base timestamp and posts the first
     * frame callback to the Choreographer.
     */
    fun start() {
        isRunning = true
        startTimeMs = SystemClock.uptimeMillis()
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    /**
     * Stop the animation immediately.
     *
     * Removes the pending frame callback from the Choreographer.
     * No [AnimatorListener.onEnd] callback is invoked on manual stop.
     */
    fun stop() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}
