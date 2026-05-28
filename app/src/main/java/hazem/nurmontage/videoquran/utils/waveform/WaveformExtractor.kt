package hazem.nurmontage.videoquran.utils.waveform

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat

/**
 * Extracts waveform amplitude data from an audio file using [MediaCodec]
 * for full PCM decoding, then averages the amplitudes per time bucket.
 *
 * This extractor produces a higher-quality waveform than [FastWaveformExtractor]
 * because it averages all decoded samples within each time bucket rather than
 * taking a single peak per bucket.
 *
 * Extraction pipeline:
 * 1. Open the audio file with [MediaExtractor] and find the first audio track.
 * 2. Configure [MediaCodec] to decode the audio track.
 * 3. Feed input buffers and read output buffers in a synchronous loop.
 * 4. For each decoded sample, accumulate the absolute amplitude and a counter
 *    into the appropriate time bucket (based on presentationTimeUs).
 * 5. After all samples are decoded, divide each bucket's accumulated amplitude
 *    by its sample count to get the average.
 *
 * Converted from WaveformExtractor.java — logic preserved exactly.
 */
object WaveformExtractor {

    /**
     * Extract [targetSamples] amplitude values from the audio file at [filePath].
     *
     * @param filePath      Path to the audio file
     * @param targetSamples Number of amplitude buckets to produce
     * @return Float array of normalized amplitudes (0.0–1.0), or all zeros on error
     */
    fun extractAmplitudes(filePath: String, targetSamples: Int): FloatArray {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(filePath)
            val audioTrackIndex = selectAudioTrack(extractor)
            if (audioTrackIndex < 0) {
                return FloatArray(targetSamples)
            }

            extractor.selectTrack(audioTrackIndex)
            val format = extractor.getTrackFormat(audioTrackIndex)
            @Suppress("UNUSED_VARIABLE")
            val sampleRate = format.getInteger("sample-rate") // kept for potential future use

            val codec = MediaCodec.createDecoderByType(format.getString("mime")!!)
            codec.configure(format, null, null, 0)
            codec.start()

            @Suppress("DEPRECATION")
            var inputBuffers = codec.inputBuffers
            @Suppress("DEPRECATION")
            var outputBuffers = codec.outputBuffers

            val amplitudes = FloatArray(targetSamples)
            val counts = FloatArray(targetSamples)
            val bucketDuration = (format.getLong("durationUs") / 1000000.0f) / targetSamples

            val bufferInfo = MediaCodec.BufferInfo()
            var eos = false

            while (true) {
                // Feed input
                if (!eos) {
                    val inputIndex = codec.dequeueInputBuffer(10000L)
                    if (inputIndex >= 0) {
                        val readBytes = extractor.readSampleData(inputBuffers[inputIndex], 0)
                        if (readBytes < 0) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            eos = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, readBytes, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                // Read output
                val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000L)
                if (outputIndex >= 0) {
                    val outputBuffer = outputBuffers[outputIndex]
                    outputBuffer.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)

                    val shortBuffer = outputBuffer.asShortBuffer()
                    val remaining = shortBuffer.remaining()
                    for (i in 0 until remaining) {
                        val absValue = Math.abs(shortBuffer[i].toInt()) / 32768.0f
                        val bucket = (bufferInfo.presentationTimeUs / 1000000.0f / bucketDuration).toInt()
                        if (bucket < targetSamples) {
                            amplitudes[bucket] += absValue
                            counts[bucket] += 1.0f
                        }
                    }

                    codec.releaseOutputBuffer(outputIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                }
            }

            // Normalize: divide accumulated amplitude by sample count per bucket
            for (i in 0 until targetSamples) {
                if (counts[i] > 0f) {
                    amplitudes[i] /= counts[i]
                }
            }

            codec.stop()
            codec.release()
            extractor.release()
            return amplitudes

        } catch (_: Exception) {
            return FloatArray(targetSamples)
        }
    }

    /**
     * Find the index of the first audio track in the media file.
     * @return Track index, or -1 if no audio track is found
     */
    private fun selectAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            if (extractor.getTrackFormat(i).getString("mime")?.startsWith("audio/") == true) {
                return i
            }
        }
        return -1
    }
}
