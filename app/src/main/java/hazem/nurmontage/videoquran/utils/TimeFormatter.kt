package hazem.nurmontage.videoquran.utils

import android.util.Pair
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Formats time values for the timeline and player UI.
 *
 * Provides two formatting modes:
 * - **Static**: [timeToString] — full H:MM:SS.mmm format for detailed display
 * - **Instance**: [formatTime] — compact M:SS format for elapsed/remaining display
 *
 * The instance-based [formatTime] method returns a [Pair] of (elapsed, remaining)
 * time strings, which is used by the video player to show both the current
 * position and the remaining duration simultaneously.
 *
 * All time calculations use [TimeUnit] for clarity and correctness, avoiding
 * manual millisecond arithmetic.
 *
 * Converted from TimeFormatter.java — logic preserved exactly.
 */
class TimeFormatter(totalDurationMs: Long = 0L) {

    companion object {
        /**
         * Convert milliseconds to a full time string in H:MM:SS.mmm format.
         *
         * Example: `62345` -> `"0:0:2.345"`
         *
         * Note: This format includes a dot before the milliseconds and does
         * NOT zero-pad the hours, minutes, or seconds. This matches the
         * original Java implementation's output exactly.
         *
         * @param ms Time in milliseconds
         * @return Formatted time string
         */
        fun timeToString(ms: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(ms)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(hours)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(ms)
            )
            val millis = TimeUnit.MILLISECONDS.toMillis(ms) - TimeUnit.SECONDS.toMillis(
                TimeUnit.MILLISECONDS.toSeconds(ms)
            )
            return "$hours:$minutes:$seconds.$millis"
        }
    }

    private var totalDurationMs: Long = totalDurationMs

    /**
     * Update the total duration used for remaining-time calculations.
     *
     * @param totalDurationMs The total duration in milliseconds
     */
    fun setTotalDurationMs(totalDurationMs: Long) {
        this.totalDurationMs = totalDurationMs
    }

    /**
     * Format the current position as (elapsed, remaining) time strings.
     *
     * Both values are in compact M:SS format:
     * - **Elapsed**: `formatMsToTime(currentMs)`
     * - **Remaining**: `formatMsToTime(totalDurationMs - currentMs)`
     *
     * The remaining time is clamped to a minimum of 0 to avoid negative
     * values when the current position exceeds the total duration (which
     * can happen during live streaming or inaccurate duration metadata).
     *
     * @param currentMs The current playback position in milliseconds
     * @return A [Pair] of (elapsed time string, remaining time string)
     */
    fun formatTime(currentMs: Long): Pair<String, String> {
        return Pair(formatMsToTime(currentMs), formatMsToTime(totalDurationMs - currentMs))
    }

    /**
     * Convert milliseconds to a compact time string in M:SS format.
     *
     * Example: `125000` -> `"2:05"`
     *
     * The seconds field is always zero-padded to 2 digits.
     * Minutes are not padded (can grow to any number of digits).
     *
     * @param ms Time in milliseconds (negative values are clamped to 0)
     * @return Formatted time string
     */
    private fun formatMsToTime(ms: Long): String {
        var time = ms
        if (time < 0) time = 0
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
    }
}
