package hazem.nurmontage.videoquran.karaoke

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView

/**
 * Smooth fade animations for karaoke word highlighting.
 * Supports SOLID and GRADIENT color schemes.
 *
 * SOLID mode: animates from normalColor → highlightColor in one step.
 * GRADIENT mode: animates through a fadeColor intermediate step,
 * enabling a two-tone sweep effect across the word.
 */
class KaraokeAnimator(
    private val highlightMode: HighlightMode,
    private val normalColor: Int,
    private val highlightColor: Int,
    private val fadeColor: Int = highlightColor
) {

    companion object {
        private const val TAG = "KaraokeAnimator"
        private const val HIGHLIGHT_DURATION_MS = 300L
        private const val RESET_DURATION_MS = 200L
    }

    /** Highlight rendering mode. */
    enum class HighlightMode { SOLID, GRADIENT }

    private var currentAnimator: ValueAnimator? = null
    private var currentView: View? = null

    /**
     * Animate a word view to its highlighted state.
     * Uses ArgbEvaluator for smooth color transitions.
     */
    fun highlightWord(wordView: View?) {
        if (wordView == null) return
        cancelCurrent()

        currentView = wordView

        currentAnimator = if (highlightMode == HighlightMode.SOLID) {
            ValueAnimator.ofObject(ArgbEvaluator(), normalColor, highlightColor)
        } else {
            // GRADIENT mode: animate through fade color first, then to highlight
            ValueAnimator.ofObject(ArgbEvaluator(), normalColor, fadeColor, highlightColor)
        }.apply {
            duration = HIGHLIGHT_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                currentView?.let { view ->
                    val color = animation.animatedValue as Int
                    applyColor(view, color)
                }
            }
        }

        currentAnimator?.start()
    }

    /**
     * Update the word progress for partial highlighting (gradient sweep).
     *
     * In GRADIENT mode, the color is interpolated based on [progress].
     * In SOLID mode, the word is highlighted once when progress first exceeds 0.
     *
     * @param wordView The view representing the word.
     * @param progress Playback progress for this word (0.0–1.0).
     */
    fun updateWordProgress(wordView: View?, progress: Float) {
        if (wordView == null) return
        if (highlightMode == HighlightMode.GRADIENT) {
            // For gradient mode, interpolate color based on progress
            val blended = blendColors(normalColor, highlightColor, progress)
            applyColor(wordView, blended)
        } else {
            // For solid mode, just set the highlight color once progress > 0
            if (progress > 0f && wordView !== currentView) {
                highlightWord(wordView)
            }
        }
    }

    /**
     * Reset a word view back to its normal (unhighlighted) state.
     * Animates the color transition from highlightColor back to normalColor.
     */
    fun resetHighlight(wordView: View?) {
        if (wordView == null) return
        cancelCurrent()

        currentView = wordView

        currentAnimator = ValueAnimator.ofObject(ArgbEvaluator(), highlightColor, normalColor).apply {
            duration = RESET_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                currentView?.let { view ->
                    val color = animation.animatedValue as Int
                    applyColor(view, color)
                }
            }
        }

        currentAnimator?.start()
    }

    private fun cancelCurrent() {
        currentAnimator?.let {
            if (it.isRunning) it.cancel()
        }
        currentAnimator = null
        currentView = null
    }

    /**
     * Apply the given color to the view.
     * For TextViews, sets the text color; for other views, sets the background color.
     */
    private fun applyColor(view: View, color: Int) {
        if (view is TextView) {
            view.setTextColor(color)
        } else {
            view.setBackgroundColor(color)
        }
    }

    /**
     * Blend two colors based on the given ratio.
     *
     * @param from   The starting color.
     * @param to     The target color.
     * @param ratio  Blend factor from 0.0 (fully [from]) to 1.0 (fully [to]).
     * @return The blended ARGB color.
     */
    private fun blendColors(from: Int, to: Int, ratio: Float): Int {
        val r = (Color.red(from) * (1f - ratio) + Color.red(to) * ratio).toInt()
        val g = (Color.green(from) * (1f - ratio) + Color.green(to) * ratio).toInt()
        val b = (Color.blue(from) * (1f - ratio) + Color.blue(to) * ratio).toInt()
        val a = (Color.alpha(from) * (1f - ratio) + Color.alpha(to) * ratio).toInt()
        return Color.argb(a, r, g, b)
    }

    /** Release the animator and cancel any running animations. */
    fun release() {
        cancelCurrent()
    }
}
