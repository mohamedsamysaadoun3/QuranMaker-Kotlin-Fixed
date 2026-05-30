package hazem.nurmontage.videoquran.adapter

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.GallerySelected
import hazem.nurmontage.videoquran.model.PhotoItem
import hazem.nurmontage.videoquran.views.image.SquareImageView

/**
 * RecyclerView adapter for displaying photo items in a grid gallery picker.
 *
 * Originally: GalleryPickerAdabters.java (178 lines)
 * Converted to: GalleryPickerAdabters.kt — idiomatic Kotlin, full logic preserved
 *
 * This adapter supports two selection modes:
 * 1. **Single-select** (gallerySelectedList == null): Tapping a photo selects it
 *    and deselects the previously selected one. Used when only one image is needed
 *    (e.g., picking a background image for the timeline).
 * 2. **Multi-select** (gallerySelectedList != null): Tapping a photo toggles its
 *    selection state. A number badge shows the selection order. Used for
 *    multi-image timeline compositions where the user picks several photos.
 *
 * Folder filtering is supported via [update] which filters the displayed items
 * to only those belonging to a specific folder, and [updateAll] to reset the filter
 * and show all photos again.
 *
 * The adapter uses a black placeholder bitmap while images are loading via Glide,
 * and caches images with a signature based on the app version to ensure fresh
 * loads after app updates.
 *
 * @see IPicker
 * @see PhotoItem
 * @see GallerySelected
 * @see SquareImageView
 */
class GalleryPickerAdabters(
    private val appVersion: String,
    resources: Resources,
    private val gallerySelectedList: List<GallerySelected>?,
    size: Int,
    private val iPicker: IPicker?
) : RecyclerView.Adapter<GalleryPickerAdabters.MyViewHolder>() {

    private val size: Int
    private val bitmapPlaceHolder: BitmapDrawable

    /** Currently displayed list of photos (may be filtered by folder). */
    private var paths: MutableList<PhotoItem>? = null

    /** Complete unfiltered list of all photos (set by [doneItems]). */
    private var allPaths: List<PhotoItem>? = null

    /** The currently selected photo in single-select mode. */
    private var photoItemSelected: PhotoItem? = null

    init {
        this.size = size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        bitmap.eraseColor(ViewCompat.MEASURED_STATE_MASK)
        this.bitmapPlaceHolder = BitmapDrawable(resources, bitmap)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Data methods
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Set the initial photo items and notify the picker if the list is empty.
     * Called when the first batch of photos is loaded from the MediaStore.
     *
     * In the original Java, this assigns the list reference directly and
     * calls [IPicker.onEmptyList] if the list is null or empty. The Kotlin
     * version creates a mutable copy to allow in-place modifications via
     * [update] and [updateAll].
     */
    fun addItems(list: List<PhotoItem>?) {
        paths = list?.toMutableList()
        if (iPicker != null && list.isNullOrEmpty()) {
            iPicker.onEmptyList()
        }
    }

    /**
     * Set the complete list of all photos (unfiltered) and store a copy
     * for folder-based filtering. Called after the full MediaStore scan completes.
     *
     * [paths] is set to the working list, and [allPaths] stores an independent
     * copy so that [update] can filter from the original full set.
     */
    fun doneItems(list: List<PhotoItem>) {
        paths = list.toMutableList()
        allPaths = ArrayList(list)
    }

    /**
     * Reset the filter: clear the current list and repopulate from [allPaths].
     * Called when the user selects the "All" folder in the folder picker.
     */
    fun updateAll() {
        if (allPaths == null || paths == null) return
        paths!!.clear()
        paths = ArrayList(allPaths!!)
        notifyDataSetChanged()
    }

    /**
     * Filter the displayed photos to only those in the specified folder.
     * Called when the user selects a specific folder in the folder picker.
     *
     * @param folder The folder name to filter by (matches [PhotoItem.folder])
     */
    fun update(folder: String) {
        val currentPaths = paths ?: return
        currentPaths.clear()
        for (photoItem in allPaths ?: return) {
            if (photoItem.folder == folder) {
                currentPaths.add(photoItem)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * Deselect the item at the given position and update the selection numbers
     * of all items that were selected after it.
     *
     * Called by [GallerySelctedAdabters] when a previously selected item
     * is removed from the "selected" strip at the bottom of the picker.
     *
     * @param position The adapter position of the item to deselect
     */
    fun inselectItem(position: Int) {
        val currentPaths = paths ?: return
        if (position >= currentPaths.size) return
        val photoItem = currentPaths[position]
        photoItem.isSelect = false
        notifyItemChanged(position)
        updateNumbers(photoItem.number)
    }

    /** Clear all displayed items. */
    fun clear() {
        paths?.clear()
    }

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_gallery, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentPaths = paths ?: return
        val photoItem = currentPaths[position]

        // Set the selection number badge and visual state
        holder.imageView.setNumber(photoItem.number)
        holder.imageView.onSelect(photoItem.isSelect)

        // Load the thumbnail with Glide — override to exact grid cell size,
        // use app version as signature to bust cache after updates
        Glide.with(holder.itemView)
            .load(photoItem.path)
            .override(size, size)
            .centerCrop()
            .signature(ObjectKey(appVersion))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(bitmapPlaceHolder)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = paths?.size ?: 0

    // ──────────────────────────────────────────────────────────────────────
    // Selection number management
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Decrement the selection number of all items that appear after the
     * removed item in the selection order.
     *
     * When item #3 is removed from a 5-item selection, items #4 and #5
     * become #3 and #4 respectively. This method walks the [gallerySelectedList]
     * from the removed index onward and decrements each item's number.
     *
     * Only relevant in multi-select mode (gallerySelectedList != null).
     *
     * @param removedNumber The selection number of the item that was removed
     */
    fun updateNumbers(removedNumber: Int) {
        val selectedList = gallerySelectedList ?: return
        var i = removedNumber
        while (i < selectedList.size) {
            val photoItem = selectedList[i].photoItem
            if (photoItem != null) {
                photoItem.number -= 1
                notifyItemChanged(photoItem.adabter_pos)
            }
            i++
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single photo grid cell.
     *
     * Click logic implements two selection modes:
     * - **Single-select**: Clicking a new photo deselects the previous one.
     *   Only one photo can be selected at a time; the selected photo gets a
     *   teal overlay. This mode is used when gallerySelectedList is null.
     * - **Multi-select**: Clicking a photo toggles its selection. A number
     *   badge (1, 2, 3…) shows the order. Deselecting an item triggers
     *   [updateNumbers] to renumber all subsequent selections. This mode
     *   is used when gallerySelectedList is non-null.
     */
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: SquareImageView = itemView.findViewById(R.id.img)

        init {
            itemView.setOnClickListener {
                if (iPicker == null || adapterPosition < 0) return@setOnClickListener

                val currentPaths = paths ?: return@setOnClickListener

                if (gallerySelectedList == null) {
                    // ── Single-select mode ──
                    val photoItem = currentPaths[adapterPosition]

                    // If the user taps the already-selected photo, do nothing
                    if (photoItem === photoItemSelected) return@setOnClickListener

                    // Deselect the previously selected photo
                    photoItemSelected?.let { prev ->
                        prev.isSelect = false
                        notifyItemChanged(prev.adabter_pos)
                    }

                    // Select the new photo
                    photoItem.isSelect = true
                    imageView.onSelect(true)
                    photoItemSelected = photoItem
                    photoItem.adabter_pos = adapterPosition
                    iPicker.onAdd(photoItem, adapterPosition)
                } else {
                    // ── Multi-select mode ──
                    val photoItem = currentPaths[adapterPosition]

                    // Toggle selection state
                    photoItem.isSelect = !photoItem.isSelect
                    imageView.onSelect(photoItem.isSelect)

                    if (photoItem.isSelect) {
                        // Assign the next selection number
                        imageView.setNumber(gallerySelectedList.size + 1)
                        photoItem.number = imageView.getAnInt()
                        photoItem.adabter_pos = adapterPosition
                        iPicker.onAdd(photoItem, adapterPosition)
                    } else {
                        // Deselecting: renumber all subsequent items
                        updateNumbers(imageView.getAnInt())
                        photoItem.gallerySelected?.let { iPicker.onDelete(it) }
                    }
                }
            }
        }
    }
}
