package hazem.nurmontage.videoquran.utils.animator

import android.os.Handler

/**
 * Basic timeline cursor animator using [Handler.postDelayed] for frame updates.
 *
 * Animates a timeline cursor from [startTimeMs] to [maxTimeMs] at approximately
 * 60 FPS (16ms frame interval). Uses [System.currentTimeMillis] for delta calculation.
 *
 * Compared to [SmoothTimelineAnimator] which uses [android.view.Choreographer],
 * this implementation is less precise but simpler — suitable for cases where
 * vsync alignment is not critical.
 *
 * The [AnimatorListener.onUpdate] callback receives the current time in milliseconds.
 * When the cursor reaches or exceeds [maxTimeMs], [AnimatorListener.onEnd] is called
 * and the animation stops automatically.
 *
 * Converted from TimelineAnimator.java — timing logic preserved exactly.
 */
class TimelineAnimator(
    startTimeMs: Int,
    private val maxTimeMs: Int,
    private val listener: AnimatorListener
) {

    /**
     * Listener for timeline animation progress events.
     */
    interface AnimatorListener {
        /** Called on each frame with the current cursor position in milliseconds. */
        fun onUpdate(currentTimeMs: Int)
        /** Called when the animation reaches [maxTimeMs]. */
        fun onEnd()
    }

    private var startTimeMs: Int = startTimeMs
    private var currentTimeMs: Int = startTimeMs
    private var isRunning: Boolean = false
    private var lastFrameTime: Long = 0L
    private val handler = Handler()

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            val now = System.currentTimeMillis()
            val delta = (now - lastFrameTime).toInt()
            lastFrameTime = now
            currentTimeMs += delta

            if (currentTimeMs >= maxTimeMs) {
                currentTimeMs = maxTimeMs
                listener.onUpdate(currentTimeMs)
                listener.onEnd()
                isRunning = false
                return
            }

            listener.onUpdate(currentTimeMs)
            postFrame()
        }
    }

    /** Whether the animator is currently running. */
    fun isRunning(): Boolean = isRunning

    /** The current cursor position in milliseconds. */
    fun getCurrentTimeMs(): Int = currentTimeMs

    /**
     * Start the animation.
     *
     * Records the current system time as the base timestamp and posts
     * the first frame update runnable.
     */
    fun start() {
        isRunning = true
        lastFrameTime = System.currentTimeMillis()
        postFrame()
    }

    /**
     * Stop the animation immediately.
     *
     * Removes any pending frame update runnables from the Handler.
     */
    fun stop() {
        isRunning = false
        handler.removeCallbacks(updateRunnable)
    }

    /**
     * Schedule the next frame update at ~16ms (60 FPS target).
     */
    private fun postFrame() {
        handler.postDelayed(updateRunnable, 16L)
    }
}
