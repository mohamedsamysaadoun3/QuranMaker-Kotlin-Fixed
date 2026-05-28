package hazem.nurmontage.videoquran.karaoke

/**
 * Data class representing timing information for a single word in karaoke playback.
 */
data class WordTiming(
    val word: String,
    val startMs: Long,
    val endMs: Long,
    val wordIndex: Int,
    val isVerseEnd: Boolean
) {
    /** Returns the duration of this word in milliseconds. */
    fun getDuration(): Long = endMs - startMs

    /** Returns progress of this word at the given time, from 0.0 to 1.0. */
    fun getProgress(currentMs: Long): Float {
        if (currentMs <= startMs) return 0f
        if (currentMs >= endMs) return 1f
        val duration = getDuration()
        if (duration <= 0) return 1f
        return (currentMs - startMs).toFloat() / duration.toFloat()
    }

    /** Returns true if the given timestamp falls within this word's time range. */
    fun containsTime(timestampMs: Long): Boolean =
        timestampMs in startMs..endMs
}
