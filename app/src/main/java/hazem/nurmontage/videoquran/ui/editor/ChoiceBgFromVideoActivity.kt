package hazem.nurmontage.videoquran.ui.editor

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.core.common.Common
import hazem.nurmontage.videoquran.databinding.ActivityChoiceBgFromVideoBinding
import hazem.nurmontage.videoquran.utils.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for selecting a video frame as a background image.
 * Faithful port of original ChoiceBgFromVideoActivity.java.
 *
 * Flow:
 *   1. Receives a video URI via intent data
 *   2. Shows the video preview and a frame scrubber (VideoFrameSelectorView)
 *   3. User scrubs to the desired frame
 *   4. On "Done", stores the frame bitmap in Common.bitmap and returns RESULT_OK
 *   5. On "Cancel" or back-press, returns RESULT_CANCELED
 */
class ChoiceBgFromVideoActivity : BaseActivity() {

    private lateinit var binding: ActivityChoiceBgFromVideoBinding

    private var videoUri: String? = null
    private var videoDurationUs: Long = 0L
    private var currentFrameTimeUs: Long = 0L
    private var currentBitmap: Bitmap? = null
    private var retriever: MediaMetadataRetriever? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            cancel()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private fun cancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChoiceBgFromVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setStatusBarColor(ViewCompat.MEASURED_STATE_MASK)
        setNavigationBarColor(ViewCompat.MEASURED_STATE_MASK)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        binding.tvTittleFragment.text = getString(R.string.choice_bg)

        binding.btnCancel.setOnClickListener {
            cancel()
        }

        if (intent != null) {
            init(intent.data)
        }
    }

    private fun init(uri: android.net.Uri?) {
        if (uri == null) {
            return
        }

        videoUri = uri.toString()

        binding.btnDone.setOnClickListener {
            val bitmap = currentBitmap
            if (bitmap != null && !bitmap.isRecycled) {
                // Match Java: store bitmap in Common.bitmap, return RESULT_OK with empty Intent
                Common.bitmap = bitmap
                setResult(RESULT_OK, Intent())
                finish()
            }
        }

        initVideoRetriever()
    }

    /**
     * Initialize the MediaMetadataRetriever and set up the frame selector.
     */
    private fun initVideoRetriever() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                retriever = MediaMetadataRetriever()
                retriever?.setDataSource(videoUri)

                val durationStr = retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                videoDurationUs = (durationStr?.toLongOrNull() ?: 0L) * 1000L // ms to us

                withContext(Dispatchers.Main) {
                    setupFrameSelector()
                    showFrameAt(0L)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChoiceBgFromVideoActivity,
                        "Failed to load video",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    /**
     * Set up the VideoFrameSelectorView with callback for frame scrubbing.
     */
    private fun setupFrameSelector() {
        binding.frameSelectorView.setOnFrameSeekListener(object : hazem.nurmontage.videoquran.views.VideoFrameSelectorView.OnFrameSeekListener {
            override fun onSeekTo(timeUs: Long) {
                currentFrameTimeUs = timeUs.coerceIn(0L, videoDurationUs)
                showFrameAt(currentFrameTimeUs)
            }
        })
        binding.frameSelectorView.setDuration(videoDurationUs)
        videoUri?.let { binding.frameSelectorView.setVideoPath(it) }
    }

    /**
     * Display the video frame at the specified time position.
     */
    private fun showFrameAt(timeUs: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bitmap = retriever?.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                bitmap?.let {
                    val oldBitmap = currentBitmap
                    currentBitmap = it
                    oldBitmap?.recycle()
                    withContext(Dispatchers.Main) {
                        binding.ivView.setImageBitmap(it)
                    }
                }
            } catch (_: Exception) {
                // Frame extraction failed for this timestamp — keep previous frame
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            retriever?.release()
        } catch (_: Exception) {
            // Retriever may already be released
        }
        retriever = null
        // Note: do NOT recycle currentBitmap here — it may have been stored in Common.bitmap
        // and will be used by the caller (EngineActivity.onChoiceBgResult)
        currentBitmap = null
    }
}
