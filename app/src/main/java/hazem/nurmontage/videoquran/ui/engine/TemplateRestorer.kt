package hazem.nurmontage.videoquran.ui.engine

import android.content.Context
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.model.*
import hazem.nurmontage.videoquran.utils.NetworkUtils
import hazem.nurmontage.videoquran.utils.UtilsFileLast
import hazem.nurmontage.videoquran.utils.audio.AudioUtils
import hazem.nurmontage.videoquran.views.BlurredImageView
import hazem.nurmontage.videoquran.views.TrackEntityView
import java.io.File

/**
 * TemplateRestorer
 *
 * Encapsulates template restoration logic for the engine screen.
 * In the original code this was spread across:
 *   - addEntityFromTemplate()                (EngineEntityManager.kt:747)
 *   - extractAudioFromVideoRecursive()        (EngineAudioManager.kt:1153)
 *   - extractAudioFromVideo()                 (EngineAudioManager.kt:1190)
 *   - addAudioFromVideoWithExtention()        (EngineAudioManager.kt:1138)
 *   - addAudioTemplateHttp()                  (EngineAudioManager.kt:850)
 *   - addAudioRecitersTemplate()              (EngineAudioManager.kt:307)
 *
 * The restoration flow is:
 *   1. [addEntityFromTemplate] — re-creates all visual entities (Quran, translation,
 *      bismilah, isti3adha, surah name) from the serialised Template data
 *   2. Media restoration — handles three cases:
 *      a. Video with local path → extract audio via [extractAudioFromVideoRecursive]
 *      b. HTTP audio URLs → download via [addAudioTemplateHttp] / [addAudioRecitersTemplate]
 *      c. Local audio URI → add directly
 *
 * This class provides the skeleton for consolidating template restoration.
 * Full implementation will be wired in a later refactoring pass.
 */
class TemplateRestorer(
    private val context: Context
) {
    // References set externally
    var template: Template? = null
    var blurredImageView: BlurredImageView? = null
    var trackViewEntity: TrackEntityView? = null

    // Audio extraction state
    private var startExtension: Int = 0

    // Supported audio container extensions for extraction
    private val extensions = arrayOf(".mp3", ".ogg", ".acc", ".m4a", ".wav", ".mpeg")

    // Callbacks for adding entities — set by EngineActivity during wiring
    var onAddEntity: ((String, String, String, String, Float, Float, Int, Int, Int, String, Transition, Boolean, String?, Int, Int, Float, Float, Float, RectF?, Typeface, Typeface, Int, Int) -> Unit)? = null
    var onAddEntityTrsl: ((String, Float, Float, Int, Int, String, Transition, Float, Float, RectF?, Int, Int, Boolean) -> Unit)? = null
    var onAddEntityIsti3ada: ((String, Float, Float, Int, Transition, Float, Float, RectF?, Int) -> Unit)? = null
    var onAddEntityBismilah: ((String, Float, Float, Int, Transition, Float, Float, RectF?, Int) -> Unit)? = null
    var onSetSurahNameEntity: (() -> Unit)? = null
    var onAddAudioFromVideoWithExtension: ((String, String, Int) -> Unit)? = null
    var onAddAudioTemplateHttp: ((Uri?, Int, String?) -> Unit)? = null
    var onAddAudioRecitersTemplate: ((List<String>, Int, String) -> Unit)? = null
    var onProgressHide: (() -> Unit)? = null
    var onProgressShow: (() -> Unit)? = null
    var onInvalidateTrackView: (() -> Unit)? = null
    var onUpdateTime: (() -> Unit)? = null

    // ──────────────────────────────────────────────
    //  Entity restoration
    // ──────────────────────────────────────────────

    /**
     * Restore all entities from the current template.
     *
     * Corresponds to `EngineEntityManager.addEntityFromTemplate()` (line ~747).
     *
     * This method iterates the template's entity lists and re-creates
     * the corresponding visual entities on the timeline:
     *   - Quran entities  → [onAddEntity]
     *   - Translation entities → [onAddEntityTrsl]
     *   - Isti3adha entity → [onAddEntityIsti3ada]
     *   - Bismilah entity → [onAddEntityBismilah]
     *   - Surah name entity → set on BlurredImageView
     *
     * After entities are restored, it proceeds to restore audio/media.
     *
     * TODO: Move full body from EngineEntityManager.addEntityFromTemplate()
     */
    fun addEntityFromTemplate() {
        val tmpl = template ?: return
        val biv = blurredImageView ?: return

        val isEnabled = tmpl.ipad_type == IpadType.GRADIENT.ordinal ||
                tmpl.ipad_type == IpadType.MASK_BRUSH.ordinal ||
                tmpl.ipad_type == IpadType.BLACK_LAYER.ordinal

        val loadFontFromAsset = UtilsFileLast.loadFontFromAsset(context, "fonts/arabic/خط فارس الكوفي.otf")
        val createFromAsset = Typeface.createFromAsset(context.assets, "fonts/ReadexPro_Medium.ttf")

        // ── Quran entities ──
        for (entityQuranTemplate in tmpl.quranEntityList) {
            onAddEntity?.invoke(
                entityQuranTemplate.aya!!,
                entityQuranTemplate.complete_aya!!,
                entityQuranTemplate.translation ?: "",
                entityQuranTemplate.translation_complete ?: "",
                entityQuranTemplate.left,
                entityQuranTemplate.right,
                entityQuranTemplate.indexNumber,
                entityQuranTemplate.number,
                entityQuranTemplate.color,
                entityQuranTemplate.name_font ?: "hafes",
                entityQuranTemplate.transition ?: Transition(),
                isEnabled,
                entityQuranTemplate.icon,
                entityQuranTemplate.startWord_index,
                entityQuranTemplate.endWord_index,
                entityQuranTemplate.scale,
                entityQuranTemplate.factor_size,
                entityQuranTemplate.factor_sizeTrl,
                entityQuranTemplate.rectF?.let {
                    RectF(it.l, it.t, it.r, it.b)
                },
                loadFontFromAsset!!,
                createFromAsset!!,
                entityQuranTemplate.colorTrsl,
                entityQuranTemplate.preset
            )
        }

        // ── Translation entities ──
        for (translationTemplate in tmpl.translationTemplateList) {
            onAddEntityTrsl?.invoke(
                translationTemplate.aya!!,
                translationTemplate.left,
                translationTemplate.right,
                translationTemplate.number,
                translationTemplate.color,
                translationTemplate.name_font ?: "ReadexPro_Medium.ttf",
                translationTemplate.transition ?: Transition(),
                translationTemplate.scale,
                translationTemplate.factor_size,
                translationTemplate.rectF?.let {
                    RectF(it.l, it.t, it.r, it.b)
                },
                translationTemplate.preset,
                translationTemplate.clr_bg,
                translationTemplate.isHaveBg
            )
        }

        // ── Isti3adha entity ──
        if (tmpl.entityIsti3adaTemplate != null) {
            onAddEntityIsti3ada?.invoke(
                tmpl.entityIsti3adaTemplate!!.aya!!,
                tmpl.entityIsti3adaTemplate!!.left,
                tmpl.entityIsti3adaTemplate!!.right,
                tmpl.entityIsti3adaTemplate!!.color,
                tmpl.entityIsti3adaTemplate!!.transition ?: Transition(),
                tmpl.entityIsti3adaTemplate!!.scale,
                tmpl.entityIsti3adaTemplate!!.factor_size,
                tmpl.entityIsti3adaTemplate!!.rectF?.let {
                    RectF(it.l, it.t, it.r, it.b)
                },
                tmpl.entityIsti3adaTemplate!!.preset
            )
        }

        // ── Bismilah entity ──
        if (tmpl.entityBismilahTemplate != null) {
            onAddEntityBismilah?.invoke(
                tmpl.entityBismilahTemplate!!.aya!!,
                tmpl.entityBismilahTemplate!!.left,
                tmpl.entityBismilahTemplate!!.right,
                tmpl.entityBismilahTemplate!!.color,
                tmpl.entityBismilahTemplate!!.transition ?: Transition(),
                tmpl.entityBismilahTemplate!!.scale,
                tmpl.entityBismilahTemplate!!.factor_size,
                tmpl.entityBismilahTemplate!!.rectF?.let {
                    RectF(it.l, it.t, it.r, it.b)
                },
                tmpl.entityBismilahTemplate!!.preset
            )
        }

        // ── Surah name entity ──
        // TODO: Move surah name restoration from EngineEntityManager.addEntityFromTemplate()

        // ── Media / audio restoration ──
        restoreMediaFromTemplate(tmpl)
    }

    // ──────────────────────────────────────────────
    //  Media / audio restoration
    // ──────────────────────────────────────────────

    /**
     * Restore audio/media entities from the template.
     *
     * Handles three cases:
     *  1. Video with local path → extract audio via [extractAudioFromVideoRecursive]
     *  2. HTTP audio URLs → download via callbacks
     *  3. Local audio URI → add directly
     *
     * Corresponds to the media section of EngineEntityManager.addEntityFromTemplate()
     * (lines ~875-945).
     */
    private fun restoreMediaFromTemplate(tmpl: Template) {
        if (tmpl.entityMediaList.isEmpty()) {
            // No media — just refresh the view
            onInvalidateTrackView?.invoke()
            onUpdateTime?.invoke()
            if (tmpl.quranEntityList.isEmpty()) {
                blurredImageView?.invalidate()
            }
            onProgressHide?.invoke()
            return
        }

        try {
            val entityMedia = tmpl.entityMediaList[0]
            if (entityMedia.video_path != null) {
                if (tmpl.uri_upload_extract_audio_video == null) {
                    onProgressHide?.invoke()
                } else {
                    AudioUtils.copyToLocalAsync(
                        context,
                        Uri.parse(tmpl.uri_upload_extract_audio_video).toString(),
                        tmpl.folder_template!!,
                        object : AudioUtils.Callback {
                            override fun onSuccess(textValue: String) {
                                entityMedia.video_path = textValue
                                if (tmpl.extension != null) {
                                    onAddAudioFromVideoWithExtension?.invoke(
                                        tmpl.extension!!, entityMedia.video_path!!, 0
                                    )
                                } else {
                                    startExtension = 0
                                    extractAudioFromVideoRecursive(
                                        entityMedia.video_path!!, 0, true, 0
                                    )
                                }
                            }

                            override fun onError(exc: Exception) {
                                exc.printStackTrace()
                            }
                        }
                    )
                }
            } else if (entityMedia.uri != null) {
                if (entityMedia.paths_https != null) {
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        onAddAudioRecitersTemplate?.invoke(entityMedia.paths_https!!, 0, "")
                    }
                    // TODO: Show no-internet dialog
                } else if (entityMedia.uri!!.contains("http")) {
                    val parse = Uri.parse(entityMedia.uri)
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        onAddAudioTemplateHttp?.invoke(parse, 0, null)
                    }
                    // TODO: Show no-internet dialog
                } else {
                    onAddAudioTemplateHttp?.invoke(Uri.parse(entityMedia.uri), 0, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "restoreMediaFromTemplate failed", e)
            onProgressHide?.invoke()
        }
    }

    // ──────────────────────────────────────────────
    //  Audio extraction from video
    // ──────────────────────────────────────────────

    /**
     * Recursively try extracting audio from a video file using different
     * codec extensions (.mp3, .ogg, .aac, .m4a, .wav, .mpeg).
     *
     * Corresponds to `EngineAudioManager.extractAudioFromVideoRecursive()` (line ~1153).
     *
     * @param path      Local path to the video file
     * @param index     Current extension index to try
     * @param isTemplate Whether this is a template restoration (vs. user action)
     * @param retryCount Retry count for progress tracking
     */
    fun extractAudioFromVideoRecursive(
        path: String,
        index: Int,
        isTemplate: Boolean,
        retryCount: Int
    ) {
        val tmpl = template ?: return

        if (index < extensions.size) {
            try {
                val file = File(
                    File(tmpl.folder_template!!),
                    "${System.currentTimeMillis()}_audio${extensions[index]}"
                )
                FFmpegKit.executeWithArgumentsAsync(
                    arrayOf("-i", path, "-vn", "-acodec", "copy", "-y", file.absolutePath)
                ) { session ->
                    if (session.returnCode.isValueSuccess) {
                        tmpl.extension = extensions[index]
                        val fromFile = Uri.fromFile(file)
                        if (!isTemplate) {
                            // TODO: Add to Quran fragment
                        } else {
                            onAddAudioTemplateHttp?.invoke(fromFile, retryCount, path)
                        }
                        return@executeWithArgumentsAsync
                    }
                    startExtension++
                    extractAudioFromVideoRecursive(path, startExtension, isTemplate, retryCount)
                }
                return
            } catch (e: Exception) {
                Log.e(TAG, "extractAudioFromVideoRecursive failed", e)
                extractAudioFromVideo(path, isTemplate)
                return
            }
        }
        // Fallback: try transcoding to MP3
        extractAudioFromVideo(path, isTemplate)
    }

    /**
     * Fallback audio extraction — transcode to MP3.
     *
     * Corresponds to `EngineAudioManager.extractAudioFromVideo()` (line ~1190).
     */
    fun extractAudioFromVideo(path: String, isTemplate: Boolean) {
        val tmpl = template ?: return
        try {
            val file = File(
                File(tmpl.folder_template!!),
                "${System.currentTimeMillis()}_audio.mp3"
            )
            FFmpegKit.executeWithArgumentsAsync(
                arrayOf("-i", path, "-vn", "-acodec", "copy", "-y", file.absolutePath)
            ) { session ->
                if (session == null) {
                    onProgressHide?.invoke()
                    return@executeWithArgumentsAsync
                }
                if (session.returnCode.isValueSuccess) {
                    val fromFile = Uri.fromFile(file)
                    tmpl.extension = ".mp3"
                    if (!isTemplate) {
                        // TODO: Add to Quran fragment
                    } else {
                        onAddAudioTemplateHttp?.invoke(fromFile, 0, path)
                    }
                    return@executeWithArgumentsAsync
                }
                onProgressHide?.invoke()
            }
        } catch (e: Exception) {
            Log.e(TAG, "extractAudioFromVideo failed", e)
            onProgressHide?.invoke()
        }
    }

    companion object {
        private const val TAG = "TemplateRestorer"
    }
}
