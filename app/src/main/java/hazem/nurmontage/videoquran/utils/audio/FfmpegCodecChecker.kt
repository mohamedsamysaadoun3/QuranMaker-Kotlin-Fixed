package hazem.nurmontage.videoquran.utils.audio

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.ReturnCode

/**
 * Detects available FFmpeg encoder codecs by running `-hide_banner -encoders`
 * and parsing the output for supported video and audio codecs.
 *
 * Results are cached after the first detection to avoid repeated queries.
 *
 * Codec priority:
 * - **Video**: libx264 (preferred, software)
 * - **Audio**: libfdk_aac (preferred) → aac (fallback)
 *
 * Converted from FfmpegCodecChecker.java — logic preserved exactly.
 */
object FfmpegCodecChecker {

    private var cachedCodecs: CodecInfo? = null

    /** Callback interface for codec detection results. */
    interface CodecCallback {
        fun onResult(codecInfo: CodecInfo)
    }

    /**
     * Holds information about the available FFmpeg encoder codecs.
     *
     * @property videoCodec          The best available video encoder (e.g. "libx264"), or null
     * @property audioCodec          The best available audio encoder (e.g. "libfdk_aac" or "aac"), or null
     * @property isVideoHwAccelerated Whether hardware-accelerated video encoding is available
     */
    data class CodecInfo(
        var videoCodec: String? = null,
        var audioCodec: String? = null,
        var isVideoHwAccelerated: Boolean = false
    )

    /**
     * Asynchronously detect available FFmpeg encoders.
     * Returns cached results immediately if available.
     */
    fun detectCodecsAsync(callback: CodecCallback) {
        cachedCodecs?.let {
            callback.onResult(it)
            return
        }

        FFmpegKit.executeAsync("-hide_banner -encoders", FFmpegSessionCompleteCallback { session ->
            val info = parseEncoders(session)
            cachedCodecs = info
            callback.onResult(info)
        })
    }

    /**
     * Parse the FFmpeg `-encoders` output to determine available codecs.
     *
     * Searches for:
     * - `libx264` → videoCodec = "libx264"
     * - `libfdk_aac` → audioCodec = "libfdk_aac"
     * - `aac` (fallback) → audioCodec = "aac"
     */
    private fun parseEncoders(session: FFmpegSession): CodecInfo {
        val info = CodecInfo()

        if (!ReturnCode.isSuccess(session.returnCode)) {
            Log.e("CodecCheck", "Failed to query FFmpeg encoders")
            return info
        }

        val output = session.output ?: return info

        var hasLibx264 = false
        var hasLibfdkAac = false
        var hasAac = false

        for (line in output.split("\n")) {
            val lower = line.trim().lowercase()
            if (!hasLibx264 && lower.contains("libx264")) hasLibx264 = true
            if (!hasLibfdkAac && lower.contains("libfdk_aac")) hasLibfdkAac = true
            if (!hasAac && lower.contains("aac")) hasAac = true
        }

        if (hasLibx264) {
            info.videoCodec = "libx264"
            info.isVideoHwAccelerated = false
        } else {
            info.videoCodec = null
            info.isVideoHwAccelerated = false
        }

        info.audioCodec = when {
            hasLibfdkAac -> "libfdk_aac"
            hasAac -> "aac"
            else -> null
        }

        return info
    }
}
