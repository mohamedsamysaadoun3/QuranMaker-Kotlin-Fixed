package hazem.nurmontage.videoquran.ui.gallery_photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.core.common.Common
import hazem.nurmontage.videoquran.databinding.ActivityGalleryPickerVideoBinding
import hazem.nurmontage.videoquran.utils.AppSettingsHelper
import hazem.nurmontage.videoquran.utils.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for picking a single image from the device gallery.
 * Faithful port of original GalleryPickerOneImage.java.
 *
 * Shows images in a grid with optional folder tabs.
 * Returns the selected image URI via RESULT_OK.
 *
 * On back/cancel: clears Common.LIST_SELECT and finishes.
 */
class GalleryPickerOneImage : BaseActivity() {

    private lateinit var binding: ActivityGalleryPickerVideoBinding
    private var selectedImageUri: String? = null
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

        // Done button — return selected image URI via intent data
        binding.tvDone.setOnClickListener {
            if (selectedImageUri != null) {
                val resultIntent = Intent().apply {
                    data = Uri.parse(selectedImageUri)
                }
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }

        initViews()
        loadGalleryImages()

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
            AppSettingsHelper.openAppSettings(this@GalleryPickerOneImage)
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
     * Load all images from the device's MediaStore and display them.
     */
    private fun loadGalleryImages() {
        binding.viewProgress.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val images = mutableListOf<ImageItem>()

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val data = cursor.getString(dataColumn)
                    val uri = android.content.ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    images.add(ImageItem(uri.toString(), data))
                }
            }

            withContext(Dispatchers.Main) {
                val galleryAdapter = GalleryImageAdapter(images, object : (ImageItem, Int) -> Unit {
                    override fun invoke(imageItem: ImageItem, position: Int) {
                        selectedImageUri = imageItem.uri
                        (binding.rv.adapter as? GalleryImageAdapter)?.setSelectedPosition(position)
                    }
                })
                binding.rv.apply {
                    layoutManager = GridLayoutManager(this@GalleryPickerOneImage, 3)
                    setHasFixedSize(true)
                    setItemViewCacheSize(20)
                    itemAnimator = null
                    this.adapter = adapter
                }
                binding.viewProgress.visibility = View.GONE
            }
        }
    }

    data class ImageItem(val uri: String, val path: String)

    private class GalleryImageAdapter(
        private val images: List<ImageItem>,
        private val onSelect: (ImageItem, Int) -> Unit,
        private var selectedPosition: Int = -1
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<GalleryImageAdapter.ViewHolder>() {

        class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

        fun setSelectedPosition(position: Int) {
            val oldPos = selectedPosition
            selectedPosition = position
            if (oldPos >= 0) notifyItemChanged(oldPos)
            if (position >= 0) notifyItemChanged(position)
        }

        override fun onCreateViewHolder(
            parent: android.view.ViewGroup,
            viewType: Int
        ): ViewHolder {
            val frameLayout = android.widget.FrameLayout(parent.context).apply {
                layoutParams = GridLayoutManager.LayoutParams(
                    GridLayoutManager.LayoutParams.MATCH_PARENT,
                    300
                )
            }
            val imageView = android.widget.ImageView(parent.context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                setPadding(2, 2, 2, 2)
            }
            frameLayout.addView(imageView)

            val overlay = View(parent.context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(0x40000000.toInt())
                visibility = View.GONE
            }
            frameLayout.addView(overlay)

            return ViewHolder(frameLayout)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val frameLayout = holder.itemView as android.widget.FrameLayout
            val imageView = frameLayout.getChildAt(0) as android.widget.ImageView
            val overlay = frameLayout.getChildAt(1)

            val image = images[position]
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(image.uri)
                .centerCrop()
                .into(imageView)

            overlay.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener { onSelect(image, position) }
        }

        override fun getItemCount() = images.size
    }
}
