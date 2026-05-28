package hazem.nurmontage.videoquran.audio

import kotlin.math.max
import kotlin.math.min

/**
 * Lowers background audio when voice is detected.
 * Uses FFmpeg sidechaincompress filter for automatic ducking.
 */
class AutoDuckProcessor {

    var duckAmount: Float = 0.7f
        set(value) {
            field = max(0f, min(1f, value))
        }
    var attackTimeMs: Float = 50f
    var releaseTimeMs: Float = 300f
    var thresholdDb: Float = -30f
    var isEnabled: Boolean = true

    constructor()

    constructor(duckAmount: Float, attackTimeMs: Float, releaseTimeMs: Float, thresholdDb: Float) {
        this.duckAmount = max(0f, min(1f, duckAmount))
        this.attackTimeMs = attackTimeMs
        this.releaseTimeMs = releaseTimeMs
        this.thresholdDb = thresholdDb
        this.isEnabled = true
    }

    /**
     * Build the FFmpeg filter chain for auto-ducking.
     * @param voiceInput The voice/audio track input label (e.g., "[1:a]")
     * @param bgInput The background music input label (e.g., "[0:a]")
     * @param outputLabel The output label (e.g., "[out]")
     * @return FFmpeg filter string
     */
    fun buildFFmpegFilter(voiceInput: String, bgInput: String, outputLabel: String): String {
        if (!isEnabled) {
            return "${bgInput}acopy$outputLabel"
        }

        // sidechaincompress filter: compresses bg music when voice is present
        // attack: how fast compression starts (ms)
        // release: how fast compression releases (ms)
        // threshold: dB level to trigger compression
        // ratio: compression ratio (higher = more ducking)
        val ratio = 1f + duckAmount * 19f // 1:1 to 20:1 ratio based on duckAmount
        val makeup = duckAmount * -15f     // Makeup gain to compensate

        val sb = StringBuilder()
        sb.append(voiceInput).append("acopy[voice];")
        sb.append(bgInput).append("sidechaincompress=")
        sb.append("attack=").append(attackTimeMs.toInt()).append(":")
        sb.append("release=").append(releaseTimeMs.toInt()).append(":")
        sb.append("threshold=").append(thresholdDb.toInt()).append("dB:")
        sb.append("ratio=").append("%.1f".format(ratio)).append(":")
        sb.append("makeup=").append("%.1f".format(makeup)).append("dB")
        sb.append("[bg_ducked];")

        // Mix the ducked background with voice
        sb.append("[voice][bg_ducked]amix=inputs=2:duration=longest:dropout_transition=2")
        sb.append(outputLabel)

        return sb.toString()
    }

    /**
     * Build a simpler sidechain duck filter using volume + sidechain detection.
     * Alternative approach for when sidechaincompress is not available.
     */
    fun buildSimpleDuckFilter(voiceInput: String, bgInput: String, outputLabel: String): String {
        if (!isEnabled) {
            return "${bgInput}acopy$outputLabel"
        }

        val sb = StringBuilder()

        // Use sidechaincompress with simpler parameters
        sb.append(bgInput).append("sidechaincompress=")
        sb.append("attack=").append(attackTimeMs.toInt()).append(":")
        sb.append("release=").append(releaseTimeMs.toInt()).append(":")
        sb.append("threshold=").append(thresholdDb.toInt()).append("dB:")
        sb.append("ratio=10:")
        sb.append("makeup=0dB")
        sb.append("[ducked];")

        sb.append(voiceInput).append("acopy[voice_clean];")
        sb.append("[voice_clean][ducked]amix=inputs=2:duration=longest:dropout_transition=2")
        sb.append(outputLabel)

        return sb.toString()
    }
}
