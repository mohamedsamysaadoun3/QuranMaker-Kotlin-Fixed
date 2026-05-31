package hazem.nurmontage.videoquran.ui.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.media.MediaPlayer
import com.arthenica.ffmpegkit.FFmpegKit
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.model.Template
import hazem.nurmontage.videoquran.utils.UtilsBitmap
import hazem.nurmontage.videoquran.utils.audio.AudioUtils
import hazem.nurmontage.videoquran.utils.FileUtils
import hazem.nurmontage.videoquran.views.BlurredImageView
import java.io.File
import java.io.IOException

/**
 * BackgroundManager
 *
 * Encapsulates all background (image / video) handling for the engine screen.
 * In the original code this was spread across:
 *   - iniTypeImg()                  (EngineUIHelper.kt — init image bg from template)
 *   - initTypeVideo()               (EngineActivity.kt — init video bg from template)
 *   - handleVideo(uri)              (EngineUIHelper.kt — user picks video from gallery)
 *   - handleVideoRunnable(uri)      (EngineUIHelper.kt — bg thread for video extraction)
 *   - changeBitmap(str)             (EngineUIHelper.kt — swap bg bitmap from frame path)
 *   - setupOriginalBitmap(uri)      (EngineUIHelper.kt — scale bitmap from Uri)
 *   - setupOriginalBitmap(bitmap,i) (EngineUIHelper.kt — scale bitmap to size)
 *   - handleImg(uri)                (EngineUIHelper.kt — user picks image from gallery)
 *   - pickVideoFromGallery()        (EngineUIHelper.kt — permission check + launcher)
 *   - pickImageFromGallery()        (EngineUIHelper.kt — permission check + launcher)
 *
 * This class provides the skeleton for consolidating all background operations.
 * Full implementation will be wired in a later refactoring pass once
 * cross-references to BlurredImageView, TrackEntityView, Template etc. are
 * untangled.
 */
class BackgroundManager(
    private val context: Context,
    private val onBackgroundChanged: () -> Unit,
    private val onError: (String) -> Unit
) {
    // Reference to the shared template — set externally
    var template: Template? = null

    // Reference to the preview surface — set externally
    var blurredImageView: BlurredImageView? = null

    // FFmpeg session IDs for cancellation
    private val ffmpegSessionIds = mutableListOf<Long>()

    // Background URI string
    var uriBg: String? = null

    // End-frame counter for video extraction
    var endFrame: Int = 0

    // ──────────────────────────────────────────────
    //  Image background
    // ──────────────────────────────────────────────

    /**
     * Initialize image background from the current template.
     *
     * Corresponds to `EngineUIHelper.iniTypeImg()`.
     *
     * Loads the background bitmap via Glide, crops it to the correct aspect
     * ratio, sets up the iPad frame, and notifies [onBackgroundChanged].
     *
     * TODO: Move full body from EngineUIHelper.iniTypeImg()
     */
    fun iniTypeImg() {
        // TODO: Implement — see EngineUIHelper.kt → iniTypeImg()
        //  1. Load bg bitmap from template.uri_bg via Glide
        //  2. Crop to 16:9 / 9:16 / 1:1 based on template resize type
        //  3. Set up iPad square bitmap (neomorphic / classic / rounded)
        //  4. Apply blurred background + gradient/color
        //  5. Call onBackgroundChanged()
    }

    /**
     * Handle a user-selected image URI as the new background.
     *
     * Corresponds to `EngineUIHelper.handleImg(uri)`.
     *
     * TODO: Move full body from EngineUIHelper.kt → handleImg()
     */
    fun handleImg(uri: Uri) {
        // TODO: Implement — see EngineUIHelper.kt → handleImg()
        //  1. Take persistable URI permission
        //  2. Update template.uri_bg and uriBg
        //  3. Load & scale bitmap via setupOriginalBitmap()
        //  4. Crop, set iPad frame, apply blur + gradient
        //  5. Call onBackgroundChanged()
    }

    // ──────────────────────────────────────────────
    //  Video background
    // ──────────────────────────────────────────────

    /**
     * Initialize video background from the current template.
     *
     * Corresponds to `EngineActivity.initTypeVideo()`.
     *
     * Copies the video to local storage, extracts frames via FFmpeg, and sets
     * the first frame as the background bitmap.
     *
     * TODO: Move full body from EngineActivity.initTypeVideo()
     */
    fun initTypeVideo() {
        // TODO: Implement — see EngineActivity.kt → initTypeVideo()
        //  1. Copy video to local via AudioUtils.copyToLocalAsync()
        //  2. Extract initial frames via FFmpegKit
        //  3. Load first frame as bg bitmap
        //  4. Crop, set iPad frame, apply blur + gradient
        //  5. Call onBackgroundChanged()
    }

    /**
     * Handle a user-selected video URI as the new background.
     *
     * Corresponds to `EngineUIHelper.handleVideo(uri)`.
     *
     * @param uri Content URI of the selected video
     */
    fun handleVideo(uri: Uri) {
        ffmpegSessionIds.clear()
        // TODO: Execute handleVideoRunnable on a background thread
    }

    /**
     * Build the Runnable that extracts frames from a video URI.
     *
     * Corresponds to `EngineUIHelper.handleVideoRunnable(uri)`.
     *
     * TODO: Move full body from EngineUIHelper.kt → handleVideoRunnable()
     */
    fun handleVideoRunnable(uri: Uri): Runnable = Runnable {
        try {
            val tmpl = template ?: return@Runnable
            val copyFromUri = AudioUtils.copyFromUri(context, uri, tmpl.folder_template!!) ?: return@Runnable
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.setOnPreparedListener { mp ->
                if (mp == null) return@setOnPreparedListener
                val biv = blurredImageView ?: return@setOnPreparedListener
                val height = biv.getH()

                tmpl.isVideoSquare = true
                biv.isVideo = true
                tmpl.name_drawable = null
                tmpl.uri_original_upload_video = uri.toString()
                tmpl.uri_media_video = copyFromUri
                tmpl.duration_video_media = mp.duration / 1000

                val fileVideo = FileUtils.getFileVideo(tmpl.folder_template!!)!!
                val file = File(fileVideo, "frame_%04d.jpg")
                val file2 = File(fileVideo, "frame_0001.jpg")
                tmpl.frame_bg = file2.absolutePath

                endFrame = Math.min(Math.round(0f / 1000.0f), 3) // TODO: use trackView maxTime
                if (endFrame == 0) endFrame = 3

                ffmpegSessionIds.add(
                    FFmpegKit.executeWithArgumentsAsync(
                        arrayOf(
                            "-i", copyFromUri, "-ss", "0", "-t", "$endFrame",
                            "-r", "25", "-vf",
                            "scale=$height:$height:force_original_aspect_ratio=increase",
                            "-q:v", "0", "-threads", "4", "-an", "-y",
                            file.absolutePath
                        )
                    ) { _ ->
                        changeBitmap(file2.absolutePath)
                        onBackgroundChanged()
                        // TODO: Start second FFmpeg pass for remaining frames
                    }.sessionId
                )
            }
            mediaPlayer.prepare()
        } catch (e: Exception) {
            Log.e(TAG, "handleVideoRunnable failed", e)
            onError(e.message ?: "Video processing failed")
        }
    }

    // ──────────────────────────────────────────────
    //  Bitmap manipulation
    // ──────────────────────────────────────────────

    /**
     * Swap the background bitmap from a frame file path.
     *
     * Corresponds to `EngineUIHelper.changeBitmap(str)`.
     *
     * TODO: Move full body from EngineUIHelper.kt → changeBitmap()
     */
    fun changeBitmap(framePath: String) {
        // TODO: Implement — see EngineUIHelper.kt → changeBitmap()
        //  1. Load bitmap from framePath via Glide
        //  2. Crop based on resize type
        //  3. Update iPad frame + blur
        //  4. Call onBackgroundChanged()
    }

    /**
     * Load and scale a bitmap from a content [uri] to fit the preview height.
     *
     * Corresponds to `EngineUIHelper.setupOriginalBitmap(uri: Uri)`.
     */
    @Throws(IOException::class)
    fun setupOriginalBitmap(uri: Uri): Bitmap {
        val biv = blurredImageView ?: throw IllegalStateException("blurredImageView not set")
        val height = biv.getH()
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        val min = height / Math.min(bitmap.width, bitmap.height).toFloat()
        return Bitmap.createScaledBitmap(
            bitmap, Math.round(bitmap.width * min), Math.round(bitmap.height * min), true
        )
    }

    /**
     * Scale an existing [bitmap] so its shortest side equals [targetSize].
     *
     * Corresponds to `EngineUIHelper.setupOriginalBitmap(bitmap, i)`.
     */
    fun setupOriginalBitmap(bitmap: Bitmap, targetSize: Int): Bitmap {
        val min = targetSize / Math.min(bitmap.width, bitmap.height).toFloat()
        return Bitmap.createScaledBitmap(
            bitmap, Math.round(bitmap.width * min), Math.round(bitmap.height * min), true
        )
    }

    // ──────────────────────────────────────────────
    //  FFmpeg session management
    // ──────────────────────────────────────────────

    /**
     * Cancel all running FFmpeg sessions.
     */
    fun cancelFFmpeg() {
        for (id in ffmpegSessionIds) {
            FFmpegKit.cancel(id)
        }
        ffmpegSessionIds.clear()
    }

    companion object {
        private const val TAG = "BackgroundManager"
    }
}
