package hazem.nurmontage.videoquran.utils.waveform

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Fast waveform extractor that uses [MediaExtractor.seekTo] to jump
 * between time positions, extracting one amplitude peak per bucket.
 *
 * This is significantly faster than [WaveformExtractor] because it does
 * not decode the entire file — instead it seeks to evenly-spaced time
 * positions and decodes only one frame per position.
 *
 * Trade-off: Less accurate than the full decode approach, but adequate
 * for quick waveform previews where speed is more important than precision.
 *
 * Converted from FastWaveformExtractor.java — logic preserved exactly.
 */
object FastWaveformExtractor {

    /**
     * Extract [targetSamples] amplitude values from [filePath].
     *
     * @param filePath      Path to the audio file
     * @param targetSamples Number of amplitude samples to produce
     * @return Float array of normalized amplitudes (0.0–1.0)
     * @throws Exception if the file cannot be opened or decoded
     */
    fun extract(filePath: String, targetSamples: Int): FloatArray {
        val extractor = MediaExtractor()
        extractor.setDataSource(filePath)

        // Find the first audio track
        var audioTrackIndex = -1
        for (i in 0 until extractor.trackCount) {
            if (extractor.getTrackFormat(i).getString("mime")?.startsWith("audio/") == true) {
                audioTrackIndex = i
                break
            }
        }

        extractor.selectTrack(audioTrackIndex)
        val format = extractor.getTrackFormat(audioTrackIndex)

        val codec = MediaCodec.createDecoderByType(format.getString("mime")!!)
        codec.configure(format, null, null, 0)
        codec.start()

        val result = FloatArray(targetSamples)
        val segmentDuration = format.getLong("durationUs") / targetSamples

        @Suppress("DEPRECATION")
        var inputBuffers = codec.inputBuffers
        @Suppress("DEPRECATION")
        val outputBuffers = codec.outputBuffers

        val bufferInfo = MediaCodec.BufferInfo()
        var currentTime = 0L
        var sampleIndex = 0

        while (sampleIndex < targetSamples) {
            // Seek to the start of this segment
            extractor.seekTo(currentTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            val segmentEnd = currentTime + segmentDuration

            // Feed one input buffer
            val inputIndex = codec.dequeueInputBuffer(5000L)
            if (inputIndex >= 0) {
                val readBytes = extractor.readSampleData(inputBuffers[inputIndex], 0)
                if (readBytes < 0) break
                codec.queueInputBuffer(inputIndex, 0, readBytes, extractor.sampleTime, 0)
                extractor.advance()
            }

            // Read one output buffer
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 5000L)
            if (outputIndex >= 0) {
                result[sampleIndex] = computeAmp(outputBuffers[outputIndex], bufferInfo.size)
                sampleIndex++
                codec.releaseOutputBuffer(outputIndex, false)
            }

            currentTime = segmentEnd
            @Suppress("DEPRECATION")
            inputBuffers = codec.inputBuffers // refresh after potential reconfiguration
        }

        codec.stop()
        codec.release()
        extractor.release()
        return result
    }

    /**
     * Compute the peak amplitude from a decoded PCM buffer.
     *
     * Reads 16-bit samples (2 bytes each) and returns the maximum
     * absolute value normalized to the range [0.0, 1.0].
     */
    private fun computeAmp(buffer: ByteBuffer, size: Int): Float {
        buffer.position(0)
        var peak = 0f
        var i = 0
        while (i < size - 1) {
            peak = maxOf(peak, Math.abs(buffer.getShort(i).toInt()) / 32767.0f)
            i += 2
        }
        return peak
    }
}
