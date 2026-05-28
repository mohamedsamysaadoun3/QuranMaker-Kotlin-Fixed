package hazem.nurmontage.videoquran.karaoke

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Manages per-word timing synchronization with MediaPlayer for karaoke playback.
 *
 * Polls MediaPlayer position every 30ms and fires callbacks for word
 * highlight/end/complete events. Uses binary search for efficient word
 * lookup in the timing list.
 *
 * Lifecycle: [start] → [pause]/[resume] → [stop] → [release]
 */
class KaraokeEngine(
    private val mediaPlayer: MediaPlayer,
    wordTimings: List<WordTiming>,
    private val callback: KaraokeCallback
) {

    companion object {
        private const val TAG = "KaraokeEngine"
        private const val POLL_INTERVAL_MS = 30L
    }

    /** Callback interface for karaoke word events. */
    interface KaraokeCallback {
        /** Called with the current word and its playback progress (0.0–1.0). */
        fun onWordHighlight(word: WordTiming, progress: Float)

        /** Called when a word's time window ends. */
        fun onWordEnd(word: WordTiming)

        /** Called when the last word has been highlighted and playback continues past it. */
        fun onKaraokeComplete()
    }

    /** Immutable copy of word timings. */
    private val wordTimings: List<WordTiming> = wordTimings.toList()

    /** Handler for polling on the main thread. */
    private val handler: Handler = Handler(Looper.getMainLooper())

    /** Index of the currently highlighted word (-1 = none). */
    private var currentWordIndex: Int = -1

    /** Whether the engine is actively running. */
    private var isRunning: Boolean = false

    /** Whether the engine is paused (running but not polling). */
    private var isPaused: Boolean = false

    /** Polling runnable that reads MediaPlayer position and updates word state. */
    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!isRunning || isPaused) return
            try {
                val position = mediaPlayer.currentPosition
                updateWord(position)
            } catch (e: IllegalStateException) {
                Log.w(TAG, "MediaPlayer in invalid state", e)
                stop()
                return
            }
            if (isRunning && !isPaused) {
                handler.postDelayed(this, POLL_INTERVAL_MS)
            }
        }
    }

    /** Start karaoke tracking from the beginning. */
    fun start() {
        if (isRunning) return
        isRunning = true
        isPaused = false
        currentWordIndex = -1
        handler.post(pollRunnable)
    }

    /** Pause karaoke tracking. */
    fun pause() {
        isPaused = true
        handler.removeCallbacks(pollRunnable)
    }

    /** Resume karaoke tracking after a pause. */
    fun resume() {
        if (!isRunning) return
        isPaused = false
        handler.post(pollRunnable)
    }

    /** Stop karaoke tracking and reset state. */
    fun stop() {
        isRunning = false
        isPaused = false
        handler.removeCallbacks(pollRunnable)
        currentWordIndex = -1
    }

    /** Seek to a specific position and update the current word. */
    fun seekTo(positionMs: Int) {
        currentWordIndex = -1
        updateWord(positionMs)
    }

    /** Release resources. After calling this, the engine cannot be restarted. */
    fun release() {
        stop()
    }

    /**
     * Find the word at the given position using binary search and fire callbacks.
     * Handles word transitions (end old → highlight new) and completion detection.
     */
    private fun updateWord(positionMs: Int) {
        val newIndex = findWordIndex(positionMs.toLong())

        if (newIndex != currentWordIndex) {
            // Fire end callback for previous word
            if (currentWordIndex in wordTimings.indices) {
                callback.onWordEnd(wordTimings[currentWordIndex])
            }
            currentWordIndex = newIndex
        }

        if (newIndex in wordTimings.indices) {
            val word = wordTimings[newIndex]
            val progress = word.getProgress(positionMs.toLong())
            callback.onWordHighlight(word, progress)
        } else if (positionMs > 0 && wordTimings.isNotEmpty()) {
            // Check if we're past all words
            val last = wordTimings.last()
            if (positionMs > last.endMs) {
                if (currentWordIndex >= 0) {
                    callback.onKaraokeComplete()
                    currentWordIndex = -1
                }
            }
        }
    }

    /**
     * Binary search to find the word index for the given timestamp.
     * Returns -1 if the timestamp does not fall within any word's time range.
     */
    private fun findWordIndex(timestampMs: Long): Int {
        if (wordTimings.isEmpty()) return -1

        var low = 0
        var high = wordTimings.size - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midWord = wordTimings[mid]

            if (midWord.containsTime(timestampMs)) {
                return mid
            } else if (timestampMs < midWord.startMs) {
                high = mid - 1
            } else {
                low = mid + 1
            }
        }

        // If we're between words, return the word we most recently left
        if (low > 0 && low <= wordTimings.size) {
            val prev = wordTimings[low - 1]
            if (timestampMs in prev.startMs..prev.endMs) {
                return low - 1
            }
        }

        return -1
    }

    /** Whether the engine is currently running. */
    fun isRunning(): Boolean = isRunning

    /** Whether the engine is currently paused. */
    fun isPaused(): Boolean = isPaused
}
