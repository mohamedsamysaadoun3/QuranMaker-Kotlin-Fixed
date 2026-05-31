package hazem.nurmontage.videoquran.ui.engine

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.ViewCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback
import com.arthenica.ffmpegkit.StreamInformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.utils.BitmapCropper
import hazem.nurmontage.videoquran.utils.AudioUtils
import hazem.nurmontage.videoquran.utils.DrawableHelper
import hazem.nurmontage.videoquran.utils.FileUtils
import hazem.nurmontage.videoquran.utils.LocalPersistence
import hazem.nurmontage.videoquran.utils.MyPreferences
import hazem.nurmontage.videoquran.utils.NetworkUtils
import hazem.nurmontage.videoquran.utils.PCMWaveformExtractor
import hazem.nurmontage.videoquran.utils.UtilsBitmap
import hazem.nurmontage.videoquran.utils.UtilsFileLast
import hazem.nurmontage.videoquran.adapter.DimensionAdabters
import hazem.nurmontage.videoquran.core.common.Common
import hazem.nurmontage.videoquran.core.common.Constants
import hazem.nurmontage.videoquran.constant.AyaTextPreset
import hazem.nurmontage.videoquran.constant.EffectAudioType
import hazem.nurmontage.videoquran.constant.EntityAction
import hazem.nurmontage.videoquran.constant.IpadType
import hazem.nurmontage.videoquran.constant.ResizeType
import hazem.nurmontage.videoquran.constant.SurahNameStyle
import hazem.nurmontage.videoquran.entity_timeline.Entity
import hazem.nurmontage.videoquran.entity_timeline.EntityAudio
import hazem.nurmontage.videoquran.entity_timeline.EntityBismilahTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityQuranTimeline
import hazem.nurmontage.videoquran.entity_timeline.EntityTrslTimeline
import hazem.nurmontage.videoquran.fragment.*
import hazem.nurmontage.videoquran.fragment.audio_effect.*
import hazem.nurmontage.videoquran.model.BgItem
import hazem.nurmontage.videoquran.model.data.BismilahEntity
import hazem.nurmontage.videoquran.model.EffectAudio
import hazem.nurmontage.videoquran.model.EntityMedia
import hazem.nurmontage.videoquran.model.EntityView
import hazem.nurmontage.videoquran.model.Gradient
import hazem.nurmontage.videoquran.model.MRectF
import hazem.nurmontage.videoquran.model.data.QuranEntity
import hazem.nurmontage.videoquran.model.RecitersModel
import hazem.nurmontage.videoquran.model.SurahNameEntity
import hazem.nurmontage.videoquran.model.Transition
import hazem.nurmontage.videoquran.model.data.TranslationQuranEntity
import hazem.nurmontage.videoquran.adapter.TransitionBismilahAdabters
import hazem.nurmontage.videoquran.adapter.TransitionEntityAdabters
import hazem.nurmontage.videoquran.views.BlurredImageView
import hazem.nurmontage.videoquran.views.ButtonCustumFont
import hazem.nurmontage.videoquran.views.TextCustumFont
import hazem.nurmontage.videoquran.views.text.TextCustumFont
import hazem.nurmontage.videoquran.views.TrackEntityView
import hazem.nurmontage.videoquran.utils.AspectRatioCalculator
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.core.base.BaseActivity
import java.io.File
import java.lang.ref.WeakReference
import java.util.Locale

// ==========================================================================
// EngineCallbacks.kt
// All callback interface implementations and activity result launchers
// for EngineActivity, extracted as factory extension functions.
// ==========================================================================

@Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM")
fun EngineActivity.createITrimLineCallback(): TrackEntityView.ITrimLineCallback {
    return object : TrackEntityView.ITrimLineCallback {
        override fun fadeInAudio(f: Float) {}

        override fun fadeOutAudio(f: Float) {}

        override fun onMove() {}

        override fun onUpdatePlayerAudio(entityAudio: EntityAudio) {}

        override fun onSelectMultiple(i: Int) {
            this@EngineActivity.showEditMultipleEntity(i)
        }

        override fun onDelete(entityView: EntityView) {
            try {
                this@EngineActivity.blurredImageView.entity_select = null
                this@EngineActivity.blurredImageView.postInvalidate()
                this@EngineActivity.hideFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onEmptySelect() {
            this@EngineActivity.blurredImageView.entity_select = null
            this@EngineActivity.blurredImageView.postInvalidate()
            this@EngineActivity.pausePlayer()
            this@EngineActivity.hideFragment()
        }

        override fun onUpdate() {
            if (this@EngineActivity.blurredImageView.isInitialized) {
                this@EngineActivity.blurredImageView.postInvalidate()
            }
        }

        override fun onUp() {
            this@EngineActivity.isOnScroll = false
            this@EngineActivity.updateBtnCutState()
        }

        override fun onAddStack(entityAction: EntityAction) {
            this@EngineActivity.enableUndoBtn()
        }

        override fun onSeekPlayer(f: Float) {
            try {
                this@EngineActivity.isOnScroll = true
                for (entityAudio in this@EngineActivity.trackViewEntity.entityListAudio) {
                    try {
                        if (entityAudio.mediaPlayer != null && entityAudio.mediaPlayer!!.isPlaying) {
                            entityAudio.mediaPlayer!!.pause()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (this@EngineActivity.mIsPlaying) {
                    if (this@EngineActivity.btnPlayPause.isInitialized) {
                        this@EngineActivity.btnPlayPause.setImageResource(R.drawable.play_btn)
                    }
                    this@EngineActivity.mIsPlaying = false
                    this@EngineActivity.trackViewEntity.isPlaying = false
                    this@EngineActivity.blurredImageView.isPlaying = false
                }
                this@EngineActivity.pauseTimelineAnimation()
                this@EngineActivity.stop()
                val round = Math.round(Math.abs((f / this@EngineActivity.trackViewEntity.getSecond_in_screen()) * (-1000.0f))).toInt()
                if (this@EngineActivity.blurredImageView.isInitialized && (round <= this@EngineActivity.trackViewEntity.maxTime || this@EngineActivity.blurredImageView.progress < 1.0f)) {
                    val min = Math.min(1.0f, round.toFloat() / this@EngineActivity.trackViewEntity.maxTime)
                    this@EngineActivity.updateTime(round.toLong())
                    this@EngineActivity.blurredImageView.progress = min
                }
                this@EngineActivity.trackViewEntity.update_current_cursur_position(round)
                this@EngineActivity.current_position_time = System.currentTimeMillis().toInt()
                this@EngineActivity.startCursur = this@EngineActivity.trackViewEntity.current_cursur_position
                this@EngineActivity.updateViewTime(this@EngineActivity.trackViewEntity.maxTime, this@EngineActivity.trackViewEntity.current_cursur_position)
                this@EngineActivity.updateBtnCutState()
                this@EngineActivity.updateBtnToStart()
                this@EngineActivity.updateBtnToEnd()
                this@EngineActivity.updateFrame()
            } catch (unused: Exception) {
            }
        }

        override fun pause() {
            this@EngineActivity.pausePlayer()
        }

        override fun onPlayVibration() {
            this@EngineActivity.pausePlayer()
            this@EngineActivity.runOnUiThread {
                if (this@EngineActivity.vibrationHelper != null) {
                    this@EngineActivity.vibrationHelper!!.vibrate()
                }
            }
        }

        override fun onSelectEntity(entity: Entity, f: Float) {
            this@EngineActivity.stop()
            if (entity is EntityQuranTimeline) {
                this@EngineActivity.blurredImageView.entity_select = entity.getEntityView()
                this@EngineActivity.blurredImageView.invalidate()
                if (EditEntityFragment.instance != null) {
                    EditEntityFragment.instance!!.checkSplitEntity(entity, -this@EngineActivity.trackViewEntity.getCurrentPosition())
                    EditEntityFragment.instance!!.checkIcon(entity)
                    return
                } else if (EditTextFragment.instance != null) {
                    EditTextFragment.instance!!.update((entity as EntityQuranTimeline).quranEntity)
                    return
                } else {
                    this@EngineActivity.showEditEntity(entity)
                    return
                }
            }
            if (entity is EntityTrslTimeline) {
                this@EngineActivity.blurredImageView.entity_select = entity.getEntityView()
                this@EngineActivity.blurredImageView.invalidate()
                if (EditTrslEntityFragment.instance != null) {
                    EditTrslEntityFragment.instance!!.checkSplitEntity(entity, -this@EngineActivity.trackViewEntity.getCurrentPosition())
                    return
                } else {
                    this@EngineActivity.showEditTrslEntity(entity)
                    return
                }
            }
            if (entity is EntityBismilahTimeline) {
                this@EngineActivity.blurredImageView.entity_select = entity.getEntityView()
                this@EngineActivity.blurredImageView.invalidate()
                this@EngineActivity.showEditBismilahEntity(entity)
            } else if (entity is EntityAudio) {
                val entityAudio = entity as EntityAudio
                if (EditMediaFragment.instance != null) {
                    EditMediaFragment.instance!!.checkSplit(entityAudio, -this@EngineActivity.trackViewEntity.getCurrentPosition())
                } else {
                    this@EngineActivity.showEditAudioEntity(entityAudio)
                }
            }
        }

        override fun enableRedo(z: Boolean) {
            if (z) {
                this@EngineActivity.enableRedoBtn()
            } else {
                this@EngineActivity.disableRedoBtn()
            }
        }

        override fun enableUndo(z: Boolean) {
            if (z) {
                this@EngineActivity.enableUndoBtn()
            } else {
                this@EngineActivity.disableUndoBtn()
            }
        }

        override fun progress(z: Boolean) {
            this@EngineActivity.runOnUiThread {
                if (z) {
                    this@EngineActivity.showProgress()
                } else {
                    this@EngineActivity.hideProgressFragment()
                }
            }
        }

        override fun onUpdateTime() {
            this@EngineActivity.startCursur = this@EngineActivity.trackViewEntity.current_cursur_position
            this@EngineActivity.updateTime()
        }
    }
}

fun EngineActivity.createIAddQuran(): AddQuranFragment.IAddQuran {
    return object : AddQuranFragment.IAddQuran {
        override fun onBismilah() {
            val addEntityIste3adha = this@EngineActivity.addEntityIste3adha()
            val addEntityBissmilah = this@EngineActivity.addEntityBissmilah()
            if (!addEntityIste3adha || !addEntityBissmilah) {
                this@EngineActivity.trackViewEntity.translateToRight(addEntityIste3adha)
            } else {
                this@EngineActivity.trackViewEntity.translateToRight()
            }
        }

        override fun onVuCopyRight() {
            this@EngineActivity.dialogCopyRight()
        }

        override fun progress() {
            this@EngineActivity.runOnUiThread {
                this@EngineActivity.showProgress()
            }
        }

        override fun onSearch() {
            this@EngineActivity.isToCrop = true
            this@EngineActivity.searchAyaResult.launch(Intent(this@EngineActivity, hazem.nurmontage.videoquran.ui.search.QuranSearchActivity::class.java))
        }

        override fun uploadRecitation() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = AddAudioFragment.getInstance(this@EngineActivity.iAudioCallback, this@EngineActivity.mResources!!)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.audio))
            } catch (unused: Exception) {
            }
        }

        override fun onAddTranslation(str: String, i: Int, z: Boolean) {
            this@EngineActivity.addTranslationEntity(str, i, z)
        }

        override fun onAdd(str: String, str2: String, str3: String?, str4: String?, i: Int, i2: Int, str5: String, i3: Int, i4: Int) {
            this@EngineActivity.addEntity(str!!, "$str2 $i2", str3!!, str4!!, i, i2, str5!!, i3, i4)
        }

        override fun onDone(str: String, i: Int, str2: String?, uri: Uri?, str3: String?) {
            this@EngineActivity.runOnUiThread {
                this@EngineActivity.hideFragment()
            }
            this@EngineActivity.blurredImageView.updateSizeAya()
            this@EngineActivity.blurredImageView.updateSizeAyaTrsl()
            this@EngineActivity.blurredImageView.setSurahNameEntity(
                str!!, str2!!, "", 1.0f, "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf",
                this@EngineActivity.blurredImageView.clr_aya, 0,
                if (this@EngineActivity.blurredImageView.surahNameEntity != null) this@EngineActivity.blurredImageView.surahNameEntity!!.style else SurahNameStyle.NONE.ordinal,
                i,
                this@EngineActivity.blurredImageView.surahNameEntity != null && this@EngineActivity.blurredImageView.surahNameEntity!!.isHaveBg,
                if (this@EngineActivity.blurredImageView.surahNameEntity != null) this@EngineActivity.blurredImageView.surahNameEntity!!.clrBg else ViewCompat.MEASURED_STATE_MASK
            )
            if (str3 == null) {
                this@EngineActivity.addAudio(uri!!)
            } else {
                this@EngineActivity.addAudioFromVideo(uri!!, str3!!)
            }
        }

        override fun onDone(str: String, i: Int, str2: String?, list: List<RecitersModel>?) {
            this@EngineActivity.runOnUiThread {
                this@EngineActivity.hideFragment()
            }
            this@EngineActivity.blurredImageView.updateSizeAya()
            this@EngineActivity.blurredImageView.updateSizeAyaTrsl()
            this@EngineActivity.blurredImageView.setSurahNameEntity(
                str!!, str2!!, "", 1.0f, "\u062E\u0637 \u0627\u0644\u0625\u0628\u0644.otf",
                this@EngineActivity.blurredImageView.clr_aya, 0,
                if (this@EngineActivity.blurredImageView.surahNameEntity != null) this@EngineActivity.blurredImageView.surahNameEntity!!.style else SurahNameStyle.NONE.ordinal,
                i,
                this@EngineActivity.blurredImageView.surahNameEntity != null && this@EngineActivity.blurredImageView.surahNameEntity!!.isHaveBg,
                if (this@EngineActivity.blurredImageView.surahNameEntity != null) this@EngineActivity.blurredImageView.surahNameEntity!!.clrBg else ViewCompat.MEASURED_STATE_MASK
            )
            if (NetworkUtils.isNetworkAvailable(this@EngineActivity) && list != null && list.isNotEmpty()) {
                this@EngineActivity.addAudioReciters(list)
            } else {
                this@EngineActivity.runOnUiThread {
                    this@EngineActivity.updateTimeToEndAya()
                    this@EngineActivity.updateBtnToEnd()
                    this@EngineActivity.updateBtnToStart()
                    this@EngineActivity.hideProgressFragment()
                }
            }
        }

        override fun onCancel() {
            this@EngineActivity.hideFragment()
        }

        override fun onErrorLimitation() {
            this@EngineActivity.runOnUiThread {
                Toast.makeText(this@EngineActivity, this@EngineActivity.mResources!!.getString(R.string.error_limit), Toast.LENGTH_SHORT).show()
            }
        }

        override fun onAddReaderName(str: String?, str2: String?, uri: Uri?) {
            this@EngineActivity.isToCrop = true
            val intent = Intent(this@EngineActivity, hazem.nurmontage.videoquran.ui.editor.audio.AddReaderNameActivity::class.java)
            intent.putExtra("name", str)
            if (uri != null) {
                intent.putExtra(android.media.MediaMimeTypes.BASE_TYPE_AUDIO, uri.toString())
            }
            intent.putExtra("path_video_copy", str2)
            this@EngineActivity.nameReaderResult.launch(intent)
        }
    }
}

fun EngineActivity.createIChangeBgCallback(): ChangeBgFragment.IChangeBgCallback {
    return object : ChangeBgFragment.IChangeBgCallback {
        override fun onCancel() {
            this@EngineActivity.hideFragment()
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
        }

        override fun onCrop() {
            this@EngineActivity.toCrop()
        }

        override fun onAdd(bgItem: BgItem) {
            if (bgItem.name_drawable == this@EngineActivity.mTemplate!!.name_drawable) {
                return
            }
            if (ChangeBgFragment.instance != null) {
                ChangeBgFragment.instance!!.scrollToSelected()
            }
            this@EngineActivity.mTemplate!!.name_drawable = bgItem.name_drawable
            this@EngineActivity.uri_bg = "android.resource://" + this@EngineActivity.packageName + "/drawable/" + bgItem.id
            this@EngineActivity.showProgressSimple()
            this@EngineActivity.executor.execute {
                var engineActivity: EngineActivity
                var runnable: Runnable
                var cropTo16x9: Bitmap? = null
                var bitmap: Bitmap
                var bitmap2: Bitmap
                var rect: Rect
                try {
                    try {
                        try {
                            this@EngineActivity.mTemplate!!.uri_bg = this@EngineActivity.uri_bg
                            var i = 0
                            this@EngineActivity.mTemplate!!.isVideoSquare = false
                            this@EngineActivity.blurredImageView.isVideo = false
                            val height = this@EngineActivity.blurredImageView.getHeight()
                            this@EngineActivity.blurredImageView.bitmapOriginal = Glide.with(this@EngineActivity as androidx.fragment.app.FragmentActivity)
                                    .asBitmap()
                                    .load(this@EngineActivity.uri_bg)
                                    .override(height, height)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .submit()
                                    .get()
                            cropTo16x9 = if (this@EngineActivity.mTemplate!!.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.bitmapOriginal, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            } else if (this@EngineActivity.mTemplate!!.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.bitmapOriginal, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            } else {
                                BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.bitmapOriginal, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            }
                            this@EngineActivity.blurredImageView.updatePosCanvas(cropTo16x9)
                            this@EngineActivity.blurredImageView.updateIpad(cropTo16x9, this@EngineActivity.mTemplate!!.ipad_type, this@EngineActivity.mTemplate!!.geTypeResize())
                            if (this@EngineActivity.mTemplate!!.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                val width = (this@EngineActivity.blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                                var round = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.x_square)
                                var round2 = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.y_square)
                                var i2 = width + round
                                if (i2 > this@EngineActivity.blurredImageView.bitmapOriginal!!.width) {
                                    round -= i2 - this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                    i2 = this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                }
                                var i3 = width + round2
                                if (i3 > this@EngineActivity.blurredImageView.bitmapOriginal!!.height) {
                                    round2 -= i3 - this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                    i3 = this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i = round2
                                }
                                val rect2 = Rect(round, i, i2, i3)
                                this@EngineActivity.blurredImageView.setRadius_square(width)
                                val width2 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.width_square).toInt()
                                val height2 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.height_square).toInt()
                                val cropToSquareWithRoundCorners = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.bitmapOriginal!!, rect2, width, width2, height2)
                                rect2.right = rect2.left + width2
                                rect2.bottom = rect2.top + height2
                                this@EngineActivity.blurredImageView.rectSquare = rect2
                                bitmap2 = cropToSquareWithRoundCorners
                                rect = rect2
                            } else {
                                if (this@EngineActivity.mTemplate!!.ipad_type != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal) {
                                    val width3 = (this@EngineActivity.blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                                    val height3 = (cropTo16x9!!.height * 0.5355f).toInt()
                                    var round3 = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.x_square)
                                    var round4 = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.y_square)
                                    var i4 = width3 + round3
                                    if (i4 > this@EngineActivity.blurredImageView.bitmapOriginal!!.width) {
                                        round3 -= i4 - this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                        i4 = this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                    }
                                    var i5 = height3 + round4
                                    if (i5 > this@EngineActivity.blurredImageView.bitmapOriginal!!.height) {
                                        round4 -= i5 - this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                        i5 = this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                    }
                                    if (round3 < 0) {
                                        round3 = 0
                                    }
                                    if (round4 < 0) {
                                        round4 = 0
                                    }
                                    val rect3 = Rect(round3, round4, i4, i5)
                                    val width4 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.width_square).toInt()
                                    val height4 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.height_square).toInt()
                                    val cropToSquare = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.bitmapOriginal!!, rect3, width4, height4)
                                    this@EngineActivity.blurredImageView.bitmapSquare = cropToSquare
                                    this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect3.right = rect3.left + width4
                                    rect3.bottom = rect3.top + height4
                                    this@EngineActivity.blurredImageView.rectSquare = rect3
                                    bitmap2 = cropToSquare
                                    rect = rect3
                                }
                                val width5 = (this@EngineActivity.blurredImageView.ipad_rect!!.width() * 0.87530595f).toInt()
                                val i6 = (width5 * 1.13f).toInt()
                                val min = Math.min(width5, i6)
                                var round5 = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.x_square)
                                var round6 = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.y_square)
                                var i7 = width5 + round5
                                if (i7 > this@EngineActivity.blurredImageView.bitmapOriginal!!.width) {
                                    round5 -= i7 - this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                    i7 = this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                }
                                var i8 = i6 + round6
                                if (i8 > this@EngineActivity.blurredImageView.bitmapOriginal!!.height) {
                                    round6 -= i8 - this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                    i8 = this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                }
                                if (round5 < 0) {
                                    round5 = 0
                                }
                                if (round6 < 0) {
                                    round6 = 0
                                }
                                val rect4 = Rect(round5, round6, i7, i8)
                                if (this@EngineActivity.mTemplate!!.ipad_type == IpadType.IPAD_CLASSIC.ordinal) {
                                    val width6 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.width_square).toInt()
                                    val height5 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.height_square).toInt()
                                    val cropToSquare2 = UtilsBitmap.cropToSquare(this@EngineActivity.blurredImageView.bitmapOriginal!!, rect4, width6, height5)
                                    this@EngineActivity.blurredImageView.bitmapSquare = cropToSquare2
                                    this@EngineActivity.blurredImageView.setRadius_square(0)
                                    rect4.right = rect4.left + width6
                                    rect4.bottom = rect4.top + height5
                                    this@EngineActivity.blurredImageView.rectSquare = rect4
                                    bitmap = cropToSquare2
                                } else {
                                    val i9 = (min * 0.10800001f).toInt()
                                    this@EngineActivity.blurredImageView.setRadius_square(i9)
                                    val width7 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.width_square).toInt()
                                    val height6 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.height_square).toInt()
                                    val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.bitmapOriginal!!, rect4, i9, width7, height6)
                                    rect4.right = rect4.left + width7
                                    rect4.bottom = rect4.top + height6
                                    this@EngineActivity.blurredImageView.rectSquare = rect4
                                    bitmap = cropToSquareWithRoundCorners2
                                }
                                bitmap2 = bitmap
                                rect = rect4
                            }
                            if (this@EngineActivity.mTemplate!!.ipad_type == IpadType.GRADIENT.ordinal) {
                                this@EngineActivity.blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2!!, ViewCompat.MEASURED_STATE_MASK, this@EngineActivity.mTemplate!!.ipad_type, this@EngineActivity.mTemplate!!.geTypeResize(), rect)
                            } else if (this@EngineActivity.mTemplate!!.ipad_type == IpadType.BLUE_TYPE.ordinal) {
                                if (this@EngineActivity.blurredImageView.color_gradient != null) {
                                    this@EngineActivity.blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2!!, this@EngineActivity.blurredImageView.color_gradient!!, this@EngineActivity.mTemplate!!.ipad_type, this@EngineActivity.mTemplate!!.geTypeResize(), rect)
                                } else {
                                    this@EngineActivity.blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2!!, this@EngineActivity.blurredImageView.color_ipad, this@EngineActivity.mTemplate!!.ipad_type, this@EngineActivity.mTemplate!!.geTypeResize(), rect)
                                }
                            } else {
                                this@EngineActivity.blurredImageView.updateBitmap(UtilsBitmap.blur(this@EngineActivity, cropTo16x9!!, 20, 1), bitmap2!!, -1, this@EngineActivity.mTemplate!!.ipad_type, this@EngineActivity.mTemplate!!.geTypeResize(), rect)
                            }
                            this@EngineActivity.mTemplate!!.color_ipad = this@EngineActivity.blurredImageView.colorIpad()
                            this@EngineActivity.runOnUiThread {
                                this@EngineActivity.blurredImageView.invalidate()
                            }
                            engineActivity = this@EngineActivity
                            runnable = Runnable { this@EngineActivity.hideProgressFragment() }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            engineActivity = this@EngineActivity
                            runnable = Runnable { this@EngineActivity.hideProgressFragment() }
                        }
                        engineActivity.runOnUiThread(runnable)
                    } catch (unused: Exception) {
                    }
                } finally {
                }
            }
        }

        override fun onUploadVideo() {
            this@EngineActivity.pickVideoFromGallery()
        }

        override fun onUploadImg() {
            this@EngineActivity.pickImageFromGallery()
        }
    }
}

fun EngineActivity.createIDimensionCallback(): DimensionAdabters.IDimensionCallback {
    return object : DimensionAdabters.IDimensionCallback {
        override fun isCustomSize(z: Boolean, resizeType: ResizeType) {}

        override fun done() {
            this@EngineActivity.hideFragment()
        }

        override fun onCustumSize(i: Int, i2: Int, i3: Int, str: String, i4: Int) {
            this@EngineActivity.updateHitRatio(i3, str)
            if (i3 == this@EngineActivity.mTemplate!!.geTypeResize()) {
                return
            }
            if (ResizeFragment.instance != null) {
                ResizeFragment.instance!!.scrollToSelectedPosition()
            }
            this@EngineActivity.showProgressSimple()
            this@EngineActivity.executor.execute {
                var engineActivity: EngineActivity
                var runnable: Runnable
                var cropTo16x9: Bitmap? = null
                var i5: Int
                var cropToSquareWithRoundCorners: Bitmap? = null
                var bitmap: Bitmap
                var rect: Rect
                try {
                    try {
                        try {
                            this@EngineActivity.blurredImageView.invalidate()
                            this@EngineActivity.mTemplate!!.resizeType = i3
                            this@EngineActivity.mTemplate!!.imgResize = str
                            val size = AspectRatioCalculator.getSize(i3, this@EngineActivity.mTemplate!!.resolution)
                            this@EngineActivity.mTemplate!!.setWidthAndHeight(size.first, size.second)
                            this@EngineActivity.blurredImageView.initCanvasDimension(this@EngineActivity.blurredImageView.getWidth(), this@EngineActivity.blurredImageView.getHeight(), i3)
                            cropTo16x9 = if (this@EngineActivity.mTemplate!!.geTypeResize() == ResizeType.SOCIAL_STORY.ordinal) {
                                BitmapCropper.cropTo9x16(this@EngineActivity.blurredImageView.bitmapOriginal, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            } else if (this@EngineActivity.mTemplate!!.geTypeResize() == ResizeType.SQUARE.ordinal) {
                                BitmapCropper.cropTo1x1(this@EngineActivity.blurredImageView.bitmapOriginal, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            } else {
                                BitmapCropper.cropTo16x9(this@EngineActivity.blurredImageView.bitmapOriginal, this@EngineActivity.blurredImageView.getW(), this@EngineActivity.blurredImageView.getH())
                            }
                            this@EngineActivity.blurredImageView.updatePosCanvas(cropTo16x9)
                            this@EngineActivity.blurredImageView.bitmapBlured = cropTo16x9
                            this@EngineActivity.blurredImageView.updateIpad(cropTo16x9, this@EngineActivity.mTemplate!!.ipad_type, this@EngineActivity.mTemplate!!.geTypeResize())
                            i5 = 0
                        } finally {
                        }
                        if (this@EngineActivity.mTemplate!!.ipad_type != IpadType.GRADIENT.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.BLACK_LAYER.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.MASK_BRUSH.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.BLUE_TYPE.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.CASSET_IMG.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.CASSET_IMG_BLUR.ordinal) {
                            if (this@EngineActivity.mTemplate!!.ipad_type == IpadType.IPAD_NEOMORPHIC.ordinal) {
                                val width = (this@EngineActivity.blurredImageView.ipad_rect!!.width() * 0.6f).toInt()
                                var round = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.x_square)
                                var round2 = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.y_square)
                                var i6 = width + round
                                if (i6 > this@EngineActivity.blurredImageView.bitmapOriginal!!.width) {
                                    round -= i6 - this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                    i6 = this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                }
                                var i7 = width + round2
                                if (i7 > this@EngineActivity.blurredImageView.bitmapOriginal!!.height) {
                                    round2 -= i7 - this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                    i7 = this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                }
                                if (round < 0) {
                                    round = 0
                                }
                                if (round2 >= 0) {
                                    i5 = round2
                                }
                                val rect2 = Rect(round, i5, i6, i7)
                                this@EngineActivity.blurredImageView.setRadius_square(width)
                                val width2 = (this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.width_square).toInt()
                                val height = (this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.height_square).toInt()
                                val cropToSquareWithRoundCorners2 = UtilsBitmap.cropToSquareWithRoundCorners(this@EngineActivity.blurredImageView.bitmapOriginal!!, rect2, width, width2, height)
                                rect2.right = rect2.left + width2
                                rect2.bottom = rect2.top + height
                                this@EngineActivity.blurredImageView.rectSquare = rect2
                                bitmap = cropToSquareWithRoundCorners2
                                rect = rect2
                            } else {
                                if (this@EngineActivity.mTemplate!!.ipad_type != IpadType.IPAD.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.IPAD_UNBLUR.ordinal && this@EngineActivity.mTemplate!!.ipad_type != IpadType.IPAD_CLASSIC.ordinal) {
                                    val width3 = (this@EngineActivity.blurredImageView.ipad_rect!!.width() * 1.0f).toInt()
                                    val height2 = (cropTo16x9!!.height * 0.5355f).toInt()
                                    var round3 = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.width * this@EngineActivity.mTemplate!!.x_square)
                                    var round4 = Math.round(this@EngineActivity.blurredImageView.bitmapOriginal!!.height * this@EngineActivity.mTemplate!!.y_square)
                                    var i8 = width3 + round3
                                    if (i8 > this@EngineActivity.blurredImageView.bitmapOriginal!!.width) {
                                        round3 -= i8 - this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                        i8 = this@EngineActivity.blurredImageView.bitmapOriginal!!.width
                                    }
                                    var i9 = height2 + round4
                                    if (i9 > this@EngineActivity.blurredImageView.bitmapOriginal!!.height) {
                                        round4 -= i9 - this@EngineActivity.blurredImageView.bitmapOriginal!!.height
                                    }
                                }
                                bitmap = this@EngineActivity.blurredImageView.bitmapSquare!!
                                rect = this@EngineActivity.blurredImageView.rectSquare!!!!
                            }
                        } else {
                            bitmap = this@EngineActivity.blurredImageView.bitmapSquare!!
                            rect = this@EngineActivity.blurredImageView.rectSquare!!!!
                        }
                        engineActivity = this@EngineActivity
                        runnable = Runnable { this@EngineActivity.hideProgressFragment() }
                    } catch (e: Exception) {
                        android.util.Log.e("Tag resize : ", "init " + e.message)
                        engineActivity = this@EngineActivity
                        runnable = Runnable { this@EngineActivity.hideProgressFragment() }
                    }
                    engineActivity.runOnUiThread(runnable)
                } catch (unused: Exception) {
                }
            }
        }
    }
}

fun EngineActivity.createIAudioCallback(): AddAudioFragment.IAudioCallback {
    return object : AddAudioFragment.IAudioCallback {
        override fun upload() {
            if (this@EngineActivity.checkPermissionAudio()) {
                this@EngineActivity.pickAudio()
            }
        }

        override fun extract() {
            this@EngineActivity.pickVideoForAudio()
        }

        override fun cancel() {
            this@EngineActivity.hideFragment()
            try {
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.quran))
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val addQuranInstance = this@EngineActivity.createAddQuranFragment(this@EngineActivity.iAddQuran, this@EngineActivity.mResources!!)
                this@EngineActivity.mCurrentFragment = addQuranInstance
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (unused: Exception) {
            }
        }
    }
}

@Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM")
fun EngineActivity.createIEditSName(): EditS_NameFragment.IEditS_Name {
    return object : EditS_NameFragment.IEditS_Name {
        override fun onFont(surahNameEntity: SurahNameEntity) {
            val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
            this@EngineActivity.mCurrentFragment = FontFragment.getInstance(this@EngineActivity.iFontCallback, surahNameEntity.nameFont!!, surahNameEntity.getPaintAya().typeface)
            beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
            beginTransaction.commit()
            this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.font))
        }

        override fun onEdit(surahNameEntity: SurahNameEntity) {
            try {
                this@EngineActivity.isToCrop = true
                val intent = Intent(this@EngineActivity, hazem.nurmontage.videoquran.ui.editor.EditSNameActivity::class.java)
                intent.putExtra("surah_name", this@EngineActivity.blurredImageView.surahNameEntity!!.name)
                intent.putExtra("reader_name", this@EngineActivity.blurredImageView.surahNameEntity!!.reader)
                intent.putExtra("style", this@EngineActivity.blurredImageView.surahNameEntity!!.style)
                intent.putExtra(StreamInformation.KEY_INDEX, this@EngineActivity.blurredImageView.surahNameEntity!!.index_surah)
                intent.putExtra("isBg", this@EngineActivity.blurredImageView.surahNameEntity!!.isHaveBg)
                intent.putExtra("clrBg", this@EngineActivity.blurredImageView.surahNameEntity!!.clrBg)
                this@EngineActivity.editSurahNameResult.launch(intent)
                this@EngineActivity.overridePendingTransition(0, 0)
            } catch (unused: Exception) {
            }
        }

        override fun update() {
            this@EngineActivity.blurredImageView.postInvalidate()
        }

        override fun onDone() {
            this@EngineActivity.selectSurahName()
        }

        override fun onColor(surahNameEntity: SurahNameEntity) {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val fragSNameObj = this@EngineActivity.createColorSNameFragment(this@EngineActivity.iEditSName, surahNameEntity, this@EngineActivity.mResources!!)
                this@EngineActivity.mCurrentFragment = fragSNameObj
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.color))
            } catch (unused: Exception) {
            }
        }
    }
}

fun EngineActivity.createIFontCallback(): FontFragment.IFontCallback {
    return object : FontFragment.IFontCallback {
        override fun onAdd(str: String?, typeface: Typeface?) {
            try {
                if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                    val entityView = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView()
                    if (entityView is QuranEntity) {
                        entityView.setNameFont(str!!)
                        entityView.setPaintAya(typeface!!)
                        entityView.setupScaleSave(entityView.factorSize, this@EngineActivity.blurredImageView.getmCanvas_width())
                        this@EngineActivity.blurredImageView.invalidate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDone(str: String?, typeface: Typeface?) {
            this@EngineActivity.hideFragment()
            try {
                if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                    val entityView = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView()
                    if (entityView is QuranEntity) {
                        entityView.setNameFont(str!!)
                        entityView.setPaintAya(typeface!!)
                        entityView.setupScaleSave(entityView.factorSize, this@EngineActivity.blurredImageView.getmCanvas_width())
                        this@EngineActivity.blurredImageView.invalidate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCancel(str: String?, typeface: Typeface?) {
            this@EngineActivity.hideFragment()
        }
    }
}

@Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM")
fun EngineActivity.createIBismilahEntityCallback(): EditBismilahEntityFragment.IBismilahEntityCallback {
    return object : EditBismilahEntityFragment.IBismilahEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                (this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as BismilahEntity).setPreset(ayaTextPreset)
                this@EngineActivity.blurredImageView.invalidate()
            }
        }

        override fun updateAya(i: Int) {
            if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                (this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as BismilahEntity).setColor(i)
                this@EngineActivity.blurredImageView.invalidate()
            }
        }

        override fun onAnim() {
            try {
                val bismilahEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as BismilahEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = EffectBismilahFragment.getInstance(this@EngineActivity.iTransitionBismilahCallback, this@EngineActivity.mResources, bismilahEntity.bismilahTimeline!!.getTransition(), bismilahEntity.bismilahTimeline!!)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.effect))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                this@EngineActivity.blurredImageView.entity_select = null
                this@EngineActivity.blurredImageView.postInvalidate()
                this@EngineActivity.hideFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun update() {
            this@EngineActivity.blurredImageView.postInvalidate()
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun onColor() {
            try {
                val bismilahEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as BismilahEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val fragBismilahObj = this@EngineActivity.createColorBismilahFragment(this@EngineActivity.iBismilahEntityCallback, bismilahEntity, this@EngineActivity.mResources!!)
                this@EngineActivity.mCurrentFragment = fragBismilahObj
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.color))
            } catch (unused: Exception) {
            }
        }

        override fun fromTheStart() {
            this@EngineActivity.trackViewEntity.translateToStartBismilah()
        }

        override fun fromNow() {
            this@EngineActivity.trackViewEntity.translateFromNowBismilah()
        }

        override fun untilNow() {
            this@EngineActivity.trackViewEntity.translateUntilNowBismilah()
        }

        override fun untilTheEnd() {
            this@EngineActivity.trackViewEntity.translateToEndBismilah()
        }
    }
}

@Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM")
fun EngineActivity.createIEditEntityCallback(): EditEntityFragment.IEditEntityCallback {
    return object : EditEntityFragment.IEditEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                (this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity).setPreset(ayaTextPreset)
                this@EngineActivity.blurredImageView.invalidate()
            }
        }

        override fun updateAya(i: Int) {
            if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                (this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity).setColor(i)
                this@EngineActivity.blurredImageView.invalidate()
            }
        }

        override fun updateTrsl(i: Int) {
            if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                (this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity).setColorTranslation(i)
                this@EngineActivity.blurredImageView.invalidate()
            }
        }

        override fun onFont() {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = FontFragment.getInstance(this@EngineActivity.iFontCallback, quranEntity.nameFont!!, quranEntity.getPaintAya().typeface)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.font))
            } catch (unused: Exception) {
            }
        }

        override fun onIcon() {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = EditIconQuranFragment.getInstance(this@EngineActivity.iQuranIconCallback, this@EngineActivity.mResources, quranEntity.icon)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (unused: Exception) {
            }
        }

        override fun onAnim() {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = EffectAyaFragment.getInstance(this@EngineActivity.iTransitionCallback, this@EngineActivity.mResources, quranEntity.entityQuran!!.getTransition(), quranEntity.entityQuran!!)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.effect))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                this@EngineActivity.blurredImageView.entity_select = null
                this@EngineActivity.blurredImageView.postInvalidate()
                this@EngineActivity.hideFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun onColor() {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val fragAyaObj: Any = ColorAyaFragment.getInstance(this@EngineActivity.iEditTrstEntityCallback, quranEntity, this@EngineActivity.mResources!!)
                this@EngineActivity.mCurrentFragment = fragAyaObj as androidx.fragment.app.Fragment?
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.color))
            } catch (unused: Exception) {
            }
        }

        override fun onEdit() {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = EditTextFragment.getInstance(this@EngineActivity.iEdiTextCallback, this@EngineActivity.mResources, quranEntity)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (unused: Exception) {
            }
        }

        override fun onCut() {
            this@EngineActivity.splitEntity(this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity)
        }

        override fun onDuplicate() {
            this@EngineActivity.duplicateEntity(this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity)
        }

        override fun fromTheStart() {
            this@EngineActivity.trackViewEntity.translateToStart()
        }

        override fun fromNow() {
            this@EngineActivity.trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            this@EngineActivity.trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            this@EngineActivity.trackViewEntity.translateToEnd()
        }
    }
}

@Suppress("TYPE_CHECKING_HAS_RUN_INTO_RECURSIVE_PROBLEM")
fun EngineActivity.createIEditTrstEntityCallback(): EditTrslEntityFragment.IEditEntityCallback {
    return object : EditTrslEntityFragment.IEditEntityCallback {
        override fun updatePreset(ayaTextPreset: AyaTextPreset) {
            if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                (this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity).setPreset(ayaTextPreset)
                this@EngineActivity.blurredImageView.invalidate()
            }
        }

        override fun updateAya(i: Int) {
            if (this@EngineActivity.trackViewEntity.selectedEntity != null) {
                (this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity).setColor(i)
                this@EngineActivity.blurredImageView.invalidate()
            }
        }

        override fun updateTrsl(i: Int) {
            // Translation entity doesn't have separate trsl color
        }

        override fun onFont() {
            try {
                val translationQuranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = FontFragment.getInstance(this@EngineActivity.iFontCallback, translationQuranEntity.nameFont!!, translationQuranEntity.getPaintAya().typeface)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.font))
            } catch (unused: Exception) {
            }
        }

        override fun onIcon() {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = EditIconQuranFragment.getInstance(this@EngineActivity.iQuranIconCallback, this@EngineActivity.mResources, quranEntity.icon)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (unused: Exception) {
            }
        }

        override fun onAnim() {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                this@EngineActivity.mCurrentFragment = EffectAyaFragment.getInstance(this@EngineActivity.iTransitionCallback, this@EngineActivity.mResources, quranEntity.entityQuran!!.getTransition(), quranEntity.entityQuran!!)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.effect))
            } catch (unused: Exception) {
            }
        }

        override fun onDelete() {
            try {
                this@EngineActivity.blurredImageView.entity_select = null
                this@EngineActivity.blurredImageView.postInvalidate()
                this@EngineActivity.hideFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun onColor() {
            try {
                val translationQuranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val fragTrslObj = this@EngineActivity.createColorTrslAyaFragment(this@EngineActivity.iEditTrstEntityCallback, translationQuranEntity, this@EngineActivity.mResources!!)
                this@EngineActivity.mCurrentFragment = fragTrslObj
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
                this@EngineActivity.setupShowFragment(this@EngineActivity.mResources!!.getString(R.string.color))
            } catch (unused: Exception) {
            }
        }

        override fun onEdit() {
            try {
                val translationQuranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity
                val intent = Intent(this@EngineActivity, hazem.nurmontage.videoquran.ui.editor.EditTrslTxtActivity::class.java)
                intent.putExtra("txt", translationQuranEntity.txt)
                this@EngineActivity.isToCrop = true
                this@EngineActivity.editTrslResult.launch(intent)
                this@EngineActivity.overridePendingTransition(0, 0)
            } catch (unused: Exception) {
            }
        }

        override fun onCut() {
            this@EngineActivity.splitEntity(this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity)
        }

        override fun onDuplicate() {
            this@EngineActivity.duplicateEntity(this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as TranslationQuranEntity)
        }

        override fun fromTheStart() {
            this@EngineActivity.trackViewEntity.translateToStart()
        }

        override fun fromNow() {
            this@EngineActivity.trackViewEntity.translateFromNow()
        }

        override fun untilNow() {
            this@EngineActivity.trackViewEntity.translateUntilNow()
        }

        override fun untilTheEnd() {
            this@EngineActivity.trackViewEntity.translateToEnd()
        }
    }
}

fun EngineActivity.createIEditMultipleCallback(): EditMultipleEntityFragment.IEditMultipleCallback {
    return object : EditMultipleEntityFragment.IEditMultipleCallback {
        override fun onDelete() {
            this@EngineActivity.dialogDeleteSelected()
        }
    }
}

fun EngineActivity.createIEditMediaCallback(): EditMediaFragment.IEditMediaCallback {
    return object : EditMediaFragment.IEditMediaCallback {
        override fun onReplace() {}

        override fun updateEntity(effectAudioType: EffectAudioType, entityAudio: EntityAudio) {
            try {
                val entityAudio2 = this@EngineActivity.trackViewEntity.entityListAudio[i]
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
        }

        override fun startPreview() {
            try {
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mIsPlaying = true
                this@EngineActivity.trackViewEntity.isPlaying = true
                this@EngineActivity.blurredImageView.isPlaying = true
                this@EngineActivity.startTimelineAnimationPreview(entityAudio)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun pausePreview() {
            try {
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mIsPlaying = false
                this@EngineActivity.trackViewEntity.isPlaying = false
                this@EngineActivity.blurredImageView.isPlaying = false
                this@EngineActivity.pauseTimelineAnimation()
                try {
                    if (entityAudio.mediaPlayer != null && entityAudio.mediaPlayer!!.isPlaying) {
                        entityAudio.mediaPlayer!!.pause()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                VolumeFragment.instance?.updateButton()
                SpeedFragment.instance?.updateButton()
                FadeInOutFragment.instance?.updateButton()
                EchoEffectFragment.instance?.updateButton()
                EnhanceVoiceFragment.instance?.updateButton()
                RemoveNoiceFragment.instance?.updateButton()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCmdPlay(str: String) {
            try {
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.applyffectPlayAuto(str, entityAudio)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCmd(str: String) {
            try {
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.applyffect(str, entityAudio)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCmdAll(effectAudio: EffectAudio) {
            this@EngineActivity.applyffectAll(effectAudio, 0)
        }

        override fun onDuplicate() {
            try {
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.duplicateEntityAudio(Math.round((entityAudio.rect.right - entityAudio.rect.left) / this@EngineActivity.trackViewEntity.second_in_screen * 1000.0f), entityAudio)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDelete() {
            try {
                this@EngineActivity.blurredImageView.entity_select = null
                this@EngineActivity.blurredImageView.postInvalidate()
                this@EngineActivity.hideFragment()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCut() {
            try {
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                val abs = Math.abs(this@EngineActivity.trackViewEntity.getCurrentPosition())
                if (abs <= entityAudio.rect.left || abs >= entityAudio.rect.right) {
                    return
                }
                val second_in_screenNoScale = this@EngineActivity.trackViewEntity.second_in_screenNoScale * 0.1f
                if (abs <= entityAudio.rect.left || abs >= entityAudio.rect.left + second_in_screenNoScale) {
                    if (abs >= entityAudio.rect.right || abs <= entityAudio.rect.right - second_in_screenNoScale) {
                        val round = Math.round(
                            (abs - entityAudio.rect.left) / this@EngineActivity.trackViewEntity.second_in_screen * 1000.0f
                        )
                        val split = entityAudio.split(abs)
                        if (split != null) {
                            this@EngineActivity.trackViewEntity.stackSplit(entityAudio)
                            split.pathFfmpeg = entityAudio.getPathFfmpeg()
                            split.setAmps(entityAudio.amps)
                            this@EngineActivity.trackViewEntity.addAudio(split, entityAudio.index + 1)
                            entityAudio.setCurrentRect()
                            entityAudio.right = abs
                            entityAudio.onChange()
                        }
                        this@EngineActivity.trackViewEntity.invalidate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun reverbEffect() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mCurrentFragment = ReverbePresetFragment.getInstance(this@EngineActivity.iEditMediaCallback, this@EngineActivity.mResources, entityAudio.effectAudio)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun echoEffect() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mCurrentFragment = EchoEffectFragment.getInstance(this@EngineActivity.iEditMediaCallback, this@EngineActivity.mResources, entityAudio.effectAudio)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun noice() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mCurrentFragment = RemoveNoiceFragment.getInstance(this@EngineActivity.iEditMediaCallback, this@EngineActivity.mResources, entityAudio.effectAudio)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun enhanceVoice() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mCurrentFragment = EnhanceVoiceFragment.getInstance(this@EngineActivity.iEditMediaCallback, this@EngineActivity.mResources, entityAudio.effectAudio)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun speedEffect() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mCurrentFragment = SpeedFragment.getInstance(this@EngineActivity.iEditMediaCallback, this@EngineActivity.mResources, entityAudio.effectAudio)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun volumeEffect() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mCurrentFragment = VolumeFragment.getInstance(this@EngineActivity.iEditMediaCallback, this@EngineActivity.mResources, entityAudio.effectAudio)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun pitchEffect() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mCurrentFragment = PitchFragment.getInstance(this@EngineActivity.iEditMediaCallback, this@EngineActivity.mResources, entityAudio.effectAudio)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun fadeEffect() {
            try {
                val beginTransaction = this@EngineActivity.supportFragmentManager.beginTransaction()
                val entityAudio = this@EngineActivity.trackViewEntity.selectedEntity!! as EntityAudio
                this@EngineActivity.mCurrentFragment = FadeInOutFragment.getInstance(this@EngineActivity.iEditMediaCallback, this@EngineActivity.mResources, entityAudio.effectAudio)
                beginTransaction.replace(R.id.m_container, this@EngineActivity.mCurrentFragment!!)
                beginTransaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun EngineActivity.createIEdiTextCallback(): EditTextFragment.IEdiTextCallback {
    return object : EditTextFragment.IEdiTextCallback {
        override fun onDone(entityQuranTimeline: EntityQuranTimeline?) {
            this@EngineActivity.hideFragment()
        }

        override fun onUpdate(quranEntity: QuranEntity?) {
            this@EngineActivity.blurredImageView.invalidate()
        }
    }
}

fun EngineActivity.createITransitionCallback(): TransitionEntityAdabters.ITransition {
    return object : TransitionEntityAdabters.ITransition {
        override fun destroy(entityQuranTimeline: EntityQuranTimeline) {
            entityQuranTimeline.setTransition(null)
        }

        override fun playing(entityQuranTimeline: EntityQuranTimeline) {}

        override fun onHideFragment(entityQuranTimeline: EntityQuranTimeline) {
            this@EngineActivity.hideFragment()
        }

        override fun remove(i: Int, entityQuranTimeline: EntityQuranTimeline) {
            this@EngineActivity.addUpdateAnim(this@EngineActivity.blurredImageView.mIsti3adhaEntity?.bismilahTimeline, entityQuranTimeline)
            this@EngineActivity.addUpdateAnim(this@EngineActivity.blurredImageView.bismilahEntity?.bismilahTimeline, entityQuranTimeline)
            for (entityQuranTimeline2 in this@EngineActivity.trackViewEntity.entityListQuran) {
                if (entityQuranTimeline.getTransition() == null) {
                    entityQuranTimeline2!!.setTransition(null)
                    return
                }
                if (entityQuranTimeline2!!.getTransition() == null) {
                    entityQuranTimeline2!!.setTransition(Transition())
                }
                entityQuranTimeline2!!.getTransition()!!.isOut = entityQuranTimeline.getTransition()!!.isOut
                entityQuranTimeline2!!.getTransition()!!.type_out = entityQuranTimeline.getTransition()!!.type_out
                entityQuranTimeline2!!.getTransition()!!.duration_out = entityQuranTimeline.getTransition()!!.duration_out
                entityQuranTimeline2!!.getTransition()!!.isIn = entityQuranTimeline.getTransition()!!.isIn
                entityQuranTimeline2!!.getTransition()!!.type_in = entityQuranTimeline.getTransition()!!.type_in
                entityQuranTimeline2!!.getTransition()!!.duration_in = entityQuranTimeline.getTransition()!!.duration_in
            }
        }

        override fun updateDurationIn(f: Float, entityQuranTimeline: EntityQuranTimeline) {
            this@EngineActivity.addUpdateAnim(this@EngineActivity.blurredImageView.mIsti3adhaEntity?.bismilahTimeline, entityQuranTimeline)
            this@EngineActivity.addUpdateAnim(this@EngineActivity.blurredImageView.bismilahEntity?.bismilahTimeline, entityQuranTimeline)
            for (entityQuranTimeline2 in this@EngineActivity.trackViewEntity.entityListQuran) {
                if (entityQuranTimeline2!!.getTransition() != null) {
                    entityQuranTimeline2!!.getTransition()!!.isIn = entityQuranTimeline.getTransition()!!.isIn
                    entityQuranTimeline2!!.getTransition()!!.duration_in = entityQuranTimeline.getTransition()!!.duration_in
                    entityQuranTimeline2!!.getTransition()!!.type_in = entityQuranTimeline.getTransition()!!.type_in
                }
            }
        }

        override fun updateDurationOut(f: Float, entityQuranTimeline: EntityQuranTimeline) {
            this@EngineActivity.addUpdateAnim(this@EngineActivity.blurredImageView.mIsti3adhaEntity?.bismilahTimeline, entityQuranTimeline)
            this@EngineActivity.addUpdateAnim(this@EngineActivity.blurredImageView.bismilahEntity?.bismilahTimeline, entityQuranTimeline)
            for (entityQuranTimeline2 in this@EngineActivity.trackViewEntity.entityListQuran) {
                if (entityQuranTimeline2!!.getTransition() != null) {
                    entityQuranTimeline2!!.getTransition()!!.isOut = entityQuranTimeline.getTransition()!!.isOut
                    entityQuranTimeline2!!.getTransition()!!.duration_out = entityQuranTimeline.getTransition()!!.duration_out
                    entityQuranTimeline2!!.getTransition()!!.type_out = entityQuranTimeline.getTransition()!!.type_out
                }
            }
        }

        override fun applyAll(i: Int, entityQuranTimeline: EntityQuranTimeline) {
            this@EngineActivity.addUpdateAnim(this@EngineActivity.blurredImageView.mIsti3adhaEntity?.bismilahTimeline, entityQuranTimeline)
            this@EngineActivity.addUpdateAnim(this@EngineActivity.blurredImageView.bismilahEntity?.bismilahTimeline, entityQuranTimeline)
            for (entityQuranTimeline2 in this@EngineActivity.trackViewEntity.entityListQuran) {
                if (entityQuranTimeline.getTransition() == null) {
                    entityQuranTimeline2!!.setTransition(null)
                } else {
                    if (entityQuranTimeline2!!.getTransition() == null) {
                        entityQuranTimeline2!!.setTransition(Transition())
                    }
                    entityQuranTimeline2!!.getTransition()!!.isOut = entityQuranTimeline.getTransition()!!.isOut
                    entityQuranTimeline2!!.getTransition()!!.type_out = entityQuranTimeline.getTransition()!!.type_out
                    entityQuranTimeline2!!.getTransition()!!.duration_out = entityQuranTimeline.getTransition()!!.duration_out
                    entityQuranTimeline2!!.getTransition()!!.isIn = entityQuranTimeline.getTransition()!!.isIn
                    entityQuranTimeline2!!.getTransition()!!.type_in = entityQuranTimeline.getTransition()!!.type_in
                    entityQuranTimeline2!!.getTransition()!!.duration_in = entityQuranTimeline.getTransition()!!.duration_in
                }
            }
            this@EngineActivity.hideProgressFragment()
        }
    }
}

fun EngineActivity.createITransitionBismilahCallback(): TransitionBismilahAdabters.ITransition {
    return object : TransitionBismilahAdabters.ITransition {
        override fun destroy(entityBismilahTimeline: EntityBismilahTimeline) {
            entityBismilahTimeline.setTransition(null)
        }

        override fun playing(entityBismilahTimeline: EntityBismilahTimeline) {}

        override fun onHideFragment(entityBismilahTimeline: EntityBismilahTimeline) {
            this@EngineActivity.hideFragment()
        }

        override fun remove(i: Int, entityBismilahTimeline: EntityBismilahTimeline) {
            for (entityQuranTimeline in this@EngineActivity.trackViewEntity.entityListQuran) {
                if (entityBismilahTimeline.getTransition() == null) {
                    entityQuranTimeline!!.setTransition(null)
                    return
                }
                if (entityQuranTimeline!!.getTransition() == null) {
                    entityQuranTimeline!!.setTransition(Transition())
                }
                entityQuranTimeline!!.getTransition()!!.isOut = entityBismilahTimeline.getTransition()!!.isOut
                entityQuranTimeline!!.getTransition()!!.type_out = entityBismilahTimeline.getTransition()!!.type_out
                entityQuranTimeline!!.getTransition()!!.duration_out = entityBismilahTimeline.getTransition()!!.duration_out
                entityQuranTimeline!!.getTransition()!!.isIn = entityBismilahTimeline.getTransition()!!.isIn
                entityQuranTimeline!!.getTransition()!!.type_in = entityBismilahTimeline.getTransition()!!.type_in
                entityQuranTimeline!!.getTransition()!!.duration_in = entityBismilahTimeline.getTransition()!!.duration_in
            }
        }

        override fun updateDurationIn(f: Float, entityBismilahTimeline: EntityBismilahTimeline) {
            for (entityQuranTimeline in this@EngineActivity.trackViewEntity.entityListQuran) {
                if (entityQuranTimeline!!.getTransition() != null) {
                    entityQuranTimeline!!.getTransition()!!.isIn = entityBismilahTimeline.getTransition()!!.isIn
                    entityQuranTimeline!!.getTransition()!!.duration_in = entityBismilahTimeline.getTransition()!!.duration_in
                    entityQuranTimeline!!.getTransition()!!.type_in = entityBismilahTimeline.getTransition()!!.type_in
                }
            }
        }

        override fun updateDurationOut(f: Float, entityBismilahTimeline: EntityBismilahTimeline) {
            for (entityQuranTimeline in this@EngineActivity.trackViewEntity.entityListQuran) {
                if (entityQuranTimeline!!.getTransition() != null) {
                    entityQuranTimeline!!.getTransition()!!.isOut = entityBismilahTimeline.getTransition()!!.isOut
                    entityQuranTimeline!!.getTransition()!!.duration_out = entityBismilahTimeline.getTransition()!!.duration_out
                    entityQuranTimeline!!.getTransition()!!.type_out = entityBismilahTimeline.getTransition()!!.type_out
                }
            }
        }

        override fun applyAll(entityBismilahTimeline: EntityBismilahTimeline) {
            for (entityQuranTimeline in this@EngineActivity.trackViewEntity.entityListQuran) {
                if (entityBismilahTimeline.getTransition() == null) {
                    entityQuranTimeline!!.setTransition(null)
                    return
                }
                if (entityQuranTimeline!!.getTransition() == null) {
                    entityQuranTimeline!!.setTransition(Transition())
                }
                entityQuranTimeline!!.getTransition()!!.isOut = entityBismilahTimeline.getTransition()!!.isOut
                entityQuranTimeline!!.getTransition()!!.type_out = entityBismilahTimeline.getTransition()!!.type_out
                entityQuranTimeline!!.getTransition()!!.duration_out = entityBismilahTimeline.getTransition()!!.duration_out
                entityQuranTimeline!!.getTransition()!!.isIn = entityBismilahTimeline.getTransition()!!.isIn
                entityQuranTimeline!!.getTransition()!!.type_in = entityBismilahTimeline.getTransition()!!.type_in
                entityQuranTimeline!!.getTransition()!!.duration_in = entityBismilahTimeline.getTransition()!!.duration_in
            }
            this@EngineActivity.hideProgressFragment()
        }
    }
}

fun EngineActivity.createIIpadEditCallback(): EditIpadFragment.IIpadEditCallback {
    return object : EditIpadFragment.IIpadEditCallback {
        override fun onClick(i: Int, i2: Int) {
            this@EngineActivity.mTemplate!!.color_ipad = i
            this@EngineActivity.mTemplate!!.index_color = i2
            this@EngineActivity.mTemplate!!.gradient = null
            this@EngineActivity.blurredImageView.setColorIpad(i)
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun onClick(gradient: Gradient, i: Int) {
            this@EngineActivity.mTemplate!!.gradient = gradient
            this@EngineActivity.mTemplate!!.index_color = i
            this@EngineActivity.blurredImageView.setColorIpad(gradient)
            this@EngineActivity.blurredImageView.invalidate()
        }

        fun onDialogPremium() {
            this@EngineActivity.dialogPremium(0)
        }

        override fun onGlassType(z: Boolean) {
            this@EngineActivity.mTemplate!!.isGlass = z
            this@EngineActivity.blurredImageView.isGlass = z
            this@EngineActivity.blurredImageView.invalidate()
        }

        override fun onChangeType(i: Int) {
            if (this@EngineActivity.blurredImageView.getmIpadType() == i) {
                return
            }
            if (EditIpadFragment.instance != null) {
                EditIpadFragment.instance!!.scrollToSelectedPosition()
            }
            try {
                this@EngineActivity.mTemplate!!.ipad_type = i
                this@EngineActivity.mTemplate!!.changeTypeIpad(i)
                if (this@EngineActivity.mTemplate!!.isVideoSquare) {
                    if (i != IpadType.GRADIENT.ordinal && i != IpadType.BLACK_LAYER.ordinal && i != IpadType.MASK_BRUSH.ordinal && i != IpadType.BLUE_TYPE.ordinal && i != IpadType.CASSET_IMG.ordinal) {
                        if (this@EngineActivity.mTemplate!!.ipad_type == IpadType.CASSET_IMG_BLUR.ordinal) {
                            this@EngineActivity.blurredImageView.bitmapSquare = this@EngineActivity.blurredImageView.bitmapBlured
                            this@EngineActivity.blurredImageView.setRadius_square(0)
                        }
                    }
                    this@EngineActivity.blurredImageView.bitmapSquare = this@EngineActivity.blurredImageView.bitmapNotBlur
                    this@EngineActivity.blurredImageView.setRadius_square(0)
                }
                if (i == IpadType.IPAD.ordinal || i == IpadType.IPAD_UNBLUR.ordinal) {
                    // IPad type change handling - simplified for split
                    this@EngineActivity.blurredImageView.invalidate()
                }
                this@EngineActivity.blurredImageView.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDone() {
            this@EngineActivity.hideFragment()
        }

        override fun onCancel() {
            this@EngineActivity.hideFragment()
        }
    }
}

fun EngineActivity.createIQuranIconCallback(): EditIconQuranFragment.IQuranIconCallback {
    return object : EditIconQuranFragment.IQuranIconCallback {
        override fun add(str: String) {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                quranEntity.icon = str
                quranEntity.setVectorDrawable(DrawableHelper.getIDDrawableIconByName(str))
                this@EngineActivity.blurredImageView.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onDone(str: String) {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                quranEntity.icon = str
                quranEntity.setVectorDrawable(DrawableHelper.getIDDrawableIconByName(str))
                this@EngineActivity.blurredImageView.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            this@EngineActivity.hideFragment()
        }

        override fun onCancel(str: String) {
            try {
                val quranEntity = this@EngineActivity.trackViewEntity.selectedEntity!!.getEntityView() as QuranEntity
                quranEntity.icon = str
                quranEntity.setVectorDrawable(DrawableHelper.getIDDrawableIconByName(str))
                this@EngineActivity.blurredImageView.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            this@EngineActivity.hideFragment()
        }
    }
}

// Frame processor properties and runnable - must be created by EngineActivity
fun EngineActivity.createFrameProcessorRunnable(): Runnable {
    return Runnable {
        var str: String?
        while (true) {
            synchronized(this@EngineActivity.frameLock) {
                if (this@EngineActivity.pendingFramePath == null) {
                    this@EngineActivity.isProcessingFrame = false
                    return@Runnable
                } else {
                    str = this@EngineActivity.pendingFramePath
                    this@EngineActivity.pendingFramePath = null
                }
            }
            this@EngineActivity.processFrame(str!!)
        }
    }
}
