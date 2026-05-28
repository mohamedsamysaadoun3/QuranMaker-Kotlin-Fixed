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
import hazem.nurmontage.videoquran.model.VideoItem
import hazem.nurmontage.videoquran.views.SquareImageView
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * RecyclerView adapter for displaying video items in a grid gallery picker.
 *
 * Originally: GalleryVideoAdabters.java (191 lines)
 * Converted to: GalleryVideoAdabters.kt — idiomatic Kotlin, full logic preserved
 *
 * This adapter is the video counterpart of [GalleryPickerAdabters] and supports
 * the same two selection modes:
 * 1. **Single-select** (gallerySelectedList == null): Tapping a video selects it
 *    and deselects the previously selected one. Used in GalleryPickerVideo where
 *    only one video can be chosen for the timeline background.
 * 2. **Multi-select** (gallerySelectedList != null): Tapping a video toggles its
 *    selection state. A number badge shows the selection order. Used for
 *    multi-video compositions.
 *
 * Each grid cell shows a video thumbnail with a duration overlay ([tvTime]).
 * The duration text is set from [VideoItem.time] which is pre-formatted
 * by the Activity that queries the MediaStore (e.g., "02:35").
 *
 * Folder filtering works identically to [GalleryPickerAdabters]:
 * [update] filters by folder path, [updateAll] resets to show all videos.
 *
 * @see IPicker
 * @see VideoItem
 * @see GallerySelected
 * @see SquareImageView
 */
class GalleryVideoAdabters(
    private val appVersion: String,
    resources: Resources,
    private val gallerySelectedList: List<GallerySelected>?,
    size: Int,
    private val iPicker: IPicker?
) : RecyclerView.Adapter<GalleryVideoAdabters.MyViewHolder>() {

    private val size: Int
    private val bitmapPlaceHolder: BitmapDrawable

    /** Currently displayed list of videos (may be filtered by folder). */
    private var videoItems: MutableList<VideoItem>? = null

    /** Complete unfiltered list of all videos (set by [doneItems]). */
    private var allVideoItems: List<VideoItem>? = null

    /** The currently selected video in single-select mode. */
    private var videoItemSelect: VideoItem? = null

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
     * Set the initial video items and notify the picker if the list is empty.
     * Called when the first batch of videos is loaded from the MediaStore.
     */
    fun addItems(list: List<VideoItem>?) {
        videoItems = list?.toMutableList()
        if (iPicker != null && list.isNullOrEmpty()) {
            iPicker.onEmptyList()
        }
    }

    /**
     * Set the complete list of all videos (unfiltered) and store a copy
     * for folder-based filtering. Called after the full MediaStore scan completes.
     */
    fun doneItems(list: List<VideoItem>) {
        videoItems = list.toMutableList()
        allVideoItems = ArrayList(list)
    }

    /**
     * Reset the filter: clear the current list and repopulate from [allVideoItems].
     * Called when the user selects the "All" folder in the folder picker.
     */
    fun updateAll() {
        if (allVideoItems == null || videoItems == null) return
        videoItems!!.clear()
        videoItems = ArrayList(allVideoItems!!)
        notifyDataSetChanged()
    }

    /**
     * Filter the displayed videos to only those in the specified folder path.
     * Called when the user selects a specific folder in the folder picker.
     *
     * @param folderPath The folder path to filter by (matches [VideoItem.folderPath])
     */
    fun update(folderPath: String) {
        val currentItems = videoItems ?: return
        currentItems.clear()
        for (videoItem in allVideoItems ?: return) {
            if (videoItem.folderPath == folderPath) {
                currentItems.add(videoItem)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * Placeholder method that triggers a data set change notification.
     * In the original Java source, this method only called notifyDataSetChanged()
     * without modifying any data. It appears to be a leftover or stub from
     * an earlier implementation. Kept for API compatibility.
     *
     * @param folder Unused folder parameter (preserved for API compatibility)
     */
    fun setFolder(folder: String) {
        notifyDataSetChanged()
    }

    /**
     * Deselect the item at the given position and update the selection numbers
     * of all items that were selected after it.
     *
     * @param position The adapter position of the item to deselect
     */
    fun inselectItem(position: Int) {
        val currentItems = videoItems ?: return
        if (position >= currentItems.size) return
        val videoItem = currentItems[position]
        videoItem.isSelect = false
        notifyItemChanged(position)
        updateNumbers(videoItem.number)
    }

    /** Clear all displayed items. */
    fun clear() {
        videoItems?.clear()
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
        val currentItems = videoItems ?: return
        val videoItem = currentItems[position]

        // Set the selection number badge and visual state
        holder.imageView.setNumber(videoItem.number)
        holder.imageView.onSelect(videoItem.isSelect)

        // Load the video thumbnail with Glide
        Glide.with(holder.itemView)
            .load(videoItem.path)
            .override(size, size)
            .centerCrop()
            .signature(ObjectKey(appVersion))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(bitmapPlaceHolder)
            .into(holder.imageView)

        // Show the video duration overlay
        holder.tvTime.text = videoItem.time
    }

    override fun getItemCount(): Int = videoItems?.size ?: 0

    // ──────────────────────────────────────────────────────────────────────
    // Selection number management
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Decrement the selection number of all items that appear after the
     * removed item in the selection order.
     *
     * Unlike [GalleryPickerAdabters.updateNumbers], this method also updates
     * [PhotoItem.number] for any photo items in the mixed selection list,
     * even though this adapter only manages videos. This is necessary because
     * the [gallerySelectedList] can contain both photos and videos in a
     * mixed selection scenario, and all selection numbers must stay consistent.
     *
     * @param removedNumber The selection number of the item that was removed
     */
    fun updateNumbers(removedNumber: Int) {
        val selectedList = gallerySelectedList ?: return
        var i = removedNumber
        while (i < selectedList.size) {
            val gallerySelected = selectedList[i]

            // Update video item number and refresh its visual state
            gallerySelected.videoItem?.let { videoItem ->
                videoItem.number -= 1
                notifyItemChanged(videoItem.adabter_pos)
            }

            // Also update photo item number for consistency in mixed selections
            gallerySelected.photoItem?.let { photoItem ->
                photoItem.number -= 1
            }
            i++
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single video grid cell.
     *
     * Contains a [SquareImageView] for the thumbnail and a [TextCustumFont]
     * for the duration overlay. The [tvTime] is made visible in the constructor
     * (it defaults to GONE in the layout XML which is shared with photo grids).
     *
     * Click logic mirrors [GalleryPickerAdabters.MyViewHolder] with two modes:
     * - **Single-select**: Clicking a new video deselects the previous one
     * - **Multi-select**: Clicking a video toggles its selection and
     *   manages number badges via [updateNumbers]
     */
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: SquareImageView = itemView.findViewById(R.id.img)
        val tvTime: TextCustumFont = itemView.findViewById(R.id.tv_time)

        init {
            // Duration label is visible for video items (hidden by default in layout)
            tvTime.visibility = View.VISIBLE

            itemView.setOnClickListener {
                if (iPicker == null || adapterPosition < 0) return@setOnClickListener

                val currentItems = videoItems ?: return@setOnClickListener

                if (gallerySelectedList == null) {
                    // ── Single-select mode ──
                    val videoItem = currentItems[adapterPosition]

                    // If the user taps the already-selected video, do nothing
                    if (videoItem === videoItemSelect) return@setOnClickListener

                    // Deselect the previously selected video
                    videoItemSelect?.let { prev ->
                        prev.isSelect = false
                        notifyItemChanged(prev.adabter_pos)
                    }

                    // Select the new video
                    videoItemSelect = videoItem
                    videoItem.isSelect = true
                    imageView.onSelect(true)
                    videoItem.adabter_pos = adapterPosition
                    iPicker.onAdd(videoItem, adapterPosition)
                } else {
                    // ── Multi-select mode ──
                    val videoItem = currentItems[adapterPosition]

                    // Toggle selection state
                    videoItem.isSelect = !videoItem.isSelect
                    imageView.onSelect(videoItem.isSelect)

                    if (videoItem.isSelect) {
                        // Assign the next selection number
                        imageView.setNumber(gallerySelectedList.size + 1)
                        videoItem.number = imageView.getAnInt()
                        videoItem.adabter_pos = adapterPosition
                        iPicker.onAdd(videoItem, adapterPosition)
                    } else {
                        // Deselecting: renumber all subsequent items
                        updateNumbers(imageView.getAnInt())
                        videoItem.gallerySelected?.let { iPicker.onDelete(it) }
                    }
                }
            }
        }
    }
}
