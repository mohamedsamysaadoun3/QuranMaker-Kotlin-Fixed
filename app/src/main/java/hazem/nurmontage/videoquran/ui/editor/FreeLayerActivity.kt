package hazem.nurmontage.videoquran.ui.editor

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import hazem.nurmontage.videoquran.databinding.ActivityFreeLayerBinding
import hazem.nurmontage.videoquran.model.FreeElement

/**
 * FreeLayerActivity — Free-form layer overlay editor.
 *
 * Allows the user to add and position decorative elements (images/text)
 * as free-form layers on top of the video frame canvas.
 *
 * Layout: `activity_free_layer.xml` (dedicated layout, NOT the timeline layout).
 *
 * Flow:
 *   1. Activity opens with the current template background shown on [ivBackground]
 *   2. User taps "Add Image" or "Add Text" to create a new [FreeElement]
 *   3. Elements are displayed on the [freeLayerCanvas] and can be dragged/resized
 *   4. "Change BG" opens the background picker
 *   5. "Delete" removes the currently selected element
 *   6. On "Done", the free elements list is returned as an activity result
 *
 * Converted from the original FreeLayerActivity concept (this activity did not
 * exist in the original JADX source — it was added during the Kotlin rewrite
 * to provide a dedicated free-layer editing screen that was previously
 * embedded inside EngineActivity).
 */
class FreeLayerActivity : AppCompatActivity() {

    // ── ViewBinding (dedicated layout, not shared with EngineActivity) ──
    private lateinit var binding: ActivityFreeLayerBinding

    // ── State ────────────────────────────────────────────────────────
    private val freeElements = mutableListOf<FreeElement>()
    private var selectedIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFreeLayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupBottomMenu()
        loadBackgroundFromIntent()
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Toolbar
    // ═══════════════════════════════════════════════════════════════════

    private fun setupToolbar() {
        // Cancel — discard all changes and return
        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Done — confirm free layer edits and return the element count
        binding.btnDone.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra(EXTRA_FREE_ELEMENTS_COUNT, freeElements.size)
                putExtra(EXTRA_FREE_ELEMENTS, ArrayList(freeElements))
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Bottom menu
    // ═══════════════════════════════════════════════════════════════════

    private fun setupBottomMenu() {
        // Add Image — creates a new image-type free element and opens gallery
        binding.btnAddImage.setOnClickListener {
            val element = FreeElement(
                x = 0.5f,
                y = 0.5f,
                type = "image"
            )
            freeElements.add(element)
            selectedIndex = freeElements.lastIndex
            // TODO: Open GalleryPickerOneImage for image selection
        }

        // Add Text — creates a new text-type free element and opens text editor
        binding.btnAddText.setOnClickListener {
            val element = FreeElement(
                x = 0.5f,
                y = 0.5f,
                type = "text"
            )
            freeElements.add(element)
            selectedIndex = freeElements.lastIndex
            // TODO: Open TextEditActivity for text input
        }

        // Change Background — opens background selection
        binding.btnChangeBg.setOnClickListener {
            // TODO: Launch background picker (GalleryPickerOneImage or ColorPicker)
        }

        // Delete selected element
        binding.btnDelete.setOnClickListener {
            if (selectedIndex in freeElements.indices) {
                freeElements.removeAt(selectedIndex)
                selectedIndex = -1
                // TODO: Redraw canvas
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Background
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Loads the template background image path from the launching intent
     * and displays it behind the free-layer canvas.
     */
    private fun loadBackgroundFromIntent() {
        val bgPath = intent.getStringExtra(EXTRA_BG_PATH)
        if (!bgPath.isNullOrEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(java.io.File(bgPath))
                .centerCrop()
                .into(binding.ivBackground)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Activity Result
    // ═══════════════════════════════════════════════════════════════════

    @Deprecated("Use Activity Result API in future refactor")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQ_GALLERY_IMAGE -> {
                    val imagePath = data.getStringExtra("image_path")
                    if (imagePath != null && selectedIndex in freeElements.indices) {
                        freeElements[selectedIndex].imagePath = imagePath
                    }
                }
                REQ_TEXT_EDIT -> {
                    val text = data.getStringExtra("text")
                    if (text != null && selectedIndex in freeElements.indices) {
                        freeElements[selectedIndex].type = "text"
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Companion
    // ═══════════════════════════════════════════════════════════════════

    companion object {
        const val EXTRA_FREE_ELEMENTS_COUNT = "free_elements_count"
        const val EXTRA_FREE_ELEMENTS = "free_elements"
        const val EXTRA_BG_PATH = "bg_path"
        const val REQ_GALLERY_IMAGE = 2001
        const val REQ_TEXT_EDIT = 2002
    }
}
