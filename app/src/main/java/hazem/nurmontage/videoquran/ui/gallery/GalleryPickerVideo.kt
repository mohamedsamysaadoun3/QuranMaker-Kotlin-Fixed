package hazem.nurmontage.videoquran.ui.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import android.provider.MediaStore
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.VideoGalleryAdapter
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.core.common.Common
import hazem.nurmontage.videoquran.databinding.ActivityGalleryPickerVideoBinding
import hazem.nurmontage.videoquran.utils.AppSettingsHelper
import hazem.nurmontage.videoquran.utils.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import hazem.nurmontage.videoquran.model.VideoItem
import kotlinx.coroutines.withContext

/**
 * Activity for picking video files from the device gallery.
 * Faithful port of original GalleryPickerVideo.java.
 *
 * Shows videos in a grid with optional folder tabs.
 * Returns the selected video URI via RESULT_OK intent data.
 *
 * On back/cancel: clears Common.LIST_SELECT and finishes.
 */
class GalleryPickerVideo : BaseActivity() {

    private lateinit var binding: ActivityGalleryPickerVideoBinding
    private var selectedVideoUri: String? = null
    private var isUpdate: Boolean = false
    private var layoutSetting: android.widget.LinearLayout? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Common.listSelect = null
            Common.indexListSelect = 1
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGalleryPickerVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        Common.listSelect = null
        Common.indexListSelect = 1

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Done button — return selected video URI via intent data (matches Java)
        binding.tvDone.setOnClickListener {
            if (selectedVideoUri != null) {
                val resultIntent = Intent().apply {
                    data = Uri.parse(selectedVideoUri)
                }
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }

        initViews()
        loadVideos()

        // Permission checking — matches Java pattern
        if (Build.VERSION.SDK_INT >= 33 &&
            (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") == 0 ||
             ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") == 0)
        ) {
            setSetting(true)
        } else if (Build.VERSION.SDK_INT >= 34 &&
            ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") == 0
        ) {
            setSetting(false)
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == 0) {
            setSetting(true)
        } else {
            setSetting(false)
        }
    }

    private fun setSetting(isFlag: Boolean) {
        if (isFlag) return
        val linearLayout = binding.toSetting.root as? android.widget.LinearLayout ?: return
        layoutSetting = linearLayout
        linearLayout.visibility = View.VISIBLE
        layoutSetting?.setOnClickListener {
            isUpdate = true
            AppSettingsHelper.openAppSettings(this@GalleryPickerVideo)
        }
    }

    private fun updateSetting() {
        if (Build.VERSION.SDK_INT >= 33 &&
            (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_IMAGES") == 0 ||
             ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") == 0)
        ) {
            recreate()
        } else if ((Build.VERSION.SDK_INT < 34 ||
            ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") != 0) &&
            ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == 0
        ) {
            recreate()
        }
        isUpdate = false
    }

    override fun onResume() {
        super.onResume()
        if (isUpdate) {
            updateSetting()
        }
    }

    private fun initViews() {
        binding.btnOnBack.setOnClickListener { finish() }
    }

    /**
     * Load video files from the device's MediaStore.
     */
    private fun loadVideos() {
        binding.viewProgress.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val videos = mutableListOf<VideoItem>()

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION
            )

            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val data = cursor.getString(dataColumn)
                    val duration = cursor.getInt(durationColumn)
                    if (duration == 0) continue // skip zero-duration videos (matches Java)
                    val uri = android.content.ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )
                    videos.add(VideoItem("", uri.toString(), formatDuration(duration), false))
                }
            }

            withContext(Dispatchers.Main) {
                val adapter = VideoGalleryAdapter { videoItem ->
                    selectedVideoUri = videoItem.path
                    // Immediately return result — matches Java behavior
                    val resultIntent = Intent().apply {
                        data = Uri.parse(selectedVideoUri)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                binding.rv.apply {
                    layoutManager = GridLayoutManager(this@GalleryPickerVideo, 3)
                    setHasFixedSize(true)
                    setItemViewCacheSize(20)
                    itemAnimator = null
                    this.adapter = adapter
                }
                adapter.submitList(videos)
                binding.viewProgress.visibility = View.GONE
            }
        }
    }

    /** Format duration in milliseconds to "MM:SS" — matches Java formatDuration */
    private fun formatDuration(durationMs: Int): String {
        val seconds = durationMs / 1000
        return String.format(java.util.Locale.ENGLISH, "%02d:%02d", (seconds / 60) % 60, seconds % 60)
    }

    data class LocalVideoItem(val uri: String, val path: String, val duration: String)
}
