package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Serializable data model representing a video/media clip on the timeline.
 *
 * Holds all metadata required for:
 * - **Timeline positioning**: [start], [end], [offset], [offsetLeft], [offsetRight], [max]
 * - **Spatial layout**: [posX], [posY], [posXFFmpeg], [topX], [topY], [x], [y], [w], [h]
 * - **FFmpeg rendering paths**: [path_ffmpeg], [path_ffmpeg_effect], [video_path]
 * - **Audio control**: [volume], [isSoundEnable], [effectAudio]
 * - **Fade transitions**: [duration_fade_in], [duration_fade_out]
 * - **Remote resources**: [paths_https]
 * - **Thumbnail indices**: [index_start_thumbnail], [index_end_thumbnail]
 *
 * The [duplicate] method creates a deep copy suitable for split operations.
 *
 * **Serialization note**: Field names [path_ffmpeg], [path_ffmpeg_effect], [video_path],
 * [offset_left], [offset_right], [duration_fade_in], [duration_fade_out], [paths_https],
 * [start_original], [index_start_thumbnail], [index_end_thumbnail], [id_raw] are preserved
 * exactly as in the original Java class to maintain backward compatibility with serialized data.
 *
 * Converted from EntityMedia.java — all fields and constructors preserved exactly.
 */
class EntityMedia : Serializable {

    // ── FFmpeg paths (serialization-critical names) ─────────────────────
    var path_ffmpeg: String? = null
    var path_ffmpeg_effect: String? = null
    var video_path: String? = null

    // ── Remote URLs ─────────────────────────────────────────────────────
    var paths_https: List<String>? = null

    // ── Timeline position ───────────────────────────────────────────────
    var start: Float = 0f
    var end: Float = 0f
    var offset: Float = 0f
    var offset_left: Float = 0f
    var offset_right: Float = 0f
    var max: Float = 0f

    // ── Spatial coordinates ─────────────────────────────────────────────
    var posX: Float = 0f
    var posY: Float = 0f
    var posXFFmpeg: Float = 0f
    var topX: Float = 0f
    var topY: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
    var w: Float = 1.0f
    var h: Float = 0f

    // ── Scale ───────────────────────────────────────────────────────────
    var mScale: Float = 1.0f

    // ── Fade transitions ────────────────────────────────────────────────
    var duration_fade_in: Float = 0f
    var duration_fade_out: Float = 0f

    // ── Audio control ───────────────────────────────────────────────────
    var volume: Float = 1.0f
    var isSoundEnable: Boolean = true

    // ── Audio effects ───────────────────────────────────────────────────
    var effectAudio: EffectAudio? = null

    // ── Preview effect flag ─────────────────────────────────────────────
    var isApplyEffectInPreview: Boolean = false

    // ── Identity / metadata ─────────────────────────────────────────────
    var uri: String? = null
    var name: String? = null
    var id_raw: Int = 0
    var start_original: Int = 0
    var time: Int = 0
    var index_start_thumbnail: Int = 0
    var index_end_thumbnail: Int = 0

    // ════════════════════════════════════════════════════════════════════
    //  Constructors — mirror the original Java overloads exactly
    // ════════════════════════════════════════════════════════════════════

    /**
     * Minimal constructor — URI only.
     * All other fields take their default values.
     */
    constructor(uri: String) {
        this.uri = uri
    }

    /**
     * Standard constructor for timeline placement with fade durations.
     *
     * @param uri        Media source URI
     * @param start      Start position in seconds
     * @param end        End position in seconds
     * @param posX       Horizontal position
     * @param posY       Vertical position
     * @param fadeIn     Fade-in duration in seconds
     * @param fadeOut    Fade-out duration in seconds
     */
    constructor(
        uri: String,
        start: Float,
        end: Float,
        posX: Float,
        posY: Float,
        fadeIn: Float,
        fadeOut: Float
    ) {
        this.uri = uri
        this.start = start
        this.end = end
        this.posX = posX
        this.posY = posY
        this.duration_fade_in = fadeIn
        this.duration_fade_out = fadeOut
    }

    /**
     * Full constructor for precise timeline positioning with offsets and fades.
     *
     * @param uri            Media source URI
     * @param startOriginal  Original start time
     * @param start          Start position in seconds
     * @param end            End position in seconds
     * @param time           Time parameter
     * @param posX           Horizontal position
     * @param posY           Vertical position
     * @param offsetRight    Right offset
     * @param offsetLeft     Left offset
     * @param offset         General offset
     * @param max            Maximum extent
     * @param fadeIn         Fade-in duration
     * @param fadeOut        Fade-out duration
     * @param posXFFmpeg     FFmpeg X position for rendering
     */
    constructor(
        uri: String,
        startOriginal: Int,
        start: Float,
        end: Float,
        time: Int,
        posX: Float,
        posY: Float,
        offsetRight: Float,
        offsetLeft: Float,
        offset: Float,
        max: Float,
        fadeIn: Float,
        fadeOut: Float,
        posXFFmpeg: Float
    ) {
        this.uri = uri
        this.offset_left = offsetLeft
        this.offset_right = offsetRight
        this.max = max
        this.offset = offset
        this.start_original = startOriginal
        this.start = start
        this.end = end
        this.posX = posX
        this.posY = posY
        this.duration_fade_in = fadeIn
        this.duration_fade_out = fadeOut
        this.time = time
        this.posXFFmpeg = posXFFmpeg
    }

    /**
     * Detailed constructor for duplicate/render operations with spatial coords.
     *
     * @param uri            Media source URI
     * @param startOriginal  Original start time
     * @param start          Start position in seconds
     * @param end            End position in seconds
     * @param time           Time parameter
     * @param x              X coordinate
     * @param y              Y coordinate
     * @param w              Width
     * @param h              Height
     * @param offset         General offset
     * @param isSoundEnable  Whether audio is enabled
     * @param max            Maximum extent
     * @param fadeIn         Fade-in duration
     * @param fadeOut        Fade-out duration
     * @param posXFFmpeg     FFmpeg X position
     */
    constructor(
        uri: String,
        startOriginal: Int,
        start: Float,
        end: Float,
        time: Int,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        offset: Float,
        isSoundEnable: Boolean,
        max: Float,
        fadeIn: Float,
        fadeOut: Float,
        posXFFmpeg: Float
    ) {
        this.uri = uri
        this.start = start
        this.offset = offset
        this.duration_fade_in = fadeIn
        this.duration_fade_out = fadeOut
        this.max = max
        this.end = end
        this.posXFFmpeg = posXFFmpeg
        this.time = time
        this.start_original = startOriginal
        this.x = x
        this.h = h
        this.y = y
        this.w = w
        this.isSoundEnable = isSoundEnable
    }

    // ════════════════════════════════════════════════════════════════════
    //  Duplicate — creates a deep copy for split operations
    // ════════════════════════════════════════════════════════════════════

    /**
     * Create a duplicate of this media entity using the full constructor
     * that preserves spatial coordinates, offsets, and fade durations.
     */
    fun duplicate(): EntityMedia {
        return EntityMedia(
            uri!!,
            start_original,
            start,
            end,
            time,
            x,
            y,
            w,
            h,
            offset,
            isSoundEnable,
            max,
            duration_fade_in,
            duration_fade_out,
            posXFFmpeg
        )
    }
}
