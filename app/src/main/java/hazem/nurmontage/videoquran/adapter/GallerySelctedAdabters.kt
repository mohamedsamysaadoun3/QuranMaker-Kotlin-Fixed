package hazem.nurmontage.videoquran.adapter

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.model.GallerySelected
import hazem.nurmontage.videoquran.views.SquareImageView
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * RecyclerView adapter for displaying currently selected gallery items
 * (photos and/or videos) in a horizontal strip.
 *
 * Originally: GallerySelctedAdabters.java (133 lines)
 * Converted to: GallerySelctedAdabters.kt — idiomatic Kotlin, full logic preserved
 *
 * This adapter shows a compact horizontal list of the user's selections.
 * Each item displays a thumbnail with:
 * - A **delete button** (X) in the top-right corner to remove the selection
 * - A **duration label** for video items (hidden for photos)
 *
 * When the delete button is tapped, the adapter:
 * 1. Removes the item from the internal [gallerySelecteds] list
 * 2. Notifies the [IGallerySelected] callback so the parent can deselect
 *    the corresponding item in the grid picker ([GalleryPickerAdabters] or
 *    [GalleryVideoAdabters])
 *
 * The callback distinguishes between photo and video deselections because
 * each grid adapter has its own [inselectItem] method with different
 * position tracking.
 *
 * @see IGallerySelected
 * @see GallerySelected
 */
class GallerySelctedAdabters(
    resources: Resources,
    private val iGallerySelected: IGallerySelected,
    size: Int
) : RecyclerView.Adapter<GallerySelctedAdabters.MyViewHolder>() {

    private val size: Int
    private val bitmapPlaceHolder: BitmapDrawable

    /** The list of currently selected gallery items (photos and/or videos). */
    private val gallerySelecteds: MutableList<GallerySelected> = ArrayList()

    init {
        this.size = size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        bitmap.eraseColor(ViewCompat.MEASURED_STATE_MASK)
        this.bitmapPlaceHolder = BitmapDrawable(resources, bitmap)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Inner interface
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Callback interface for deselection events.
     *
     * The parent Activity/Fragment implements this to deselect the
     * corresponding item in the grid picker when the user removes it
     * from the selected-items strip.
     *
     * Two separate methods are needed because photos and videos are
     * displayed in separate grid adapters ([GalleryPickerAdabters] and
     * [GalleryVideoAdabters] respectively), each with its own position
     * tracking.
     */
    interface IGallerySelected {
        /**
         * Called when a photo is removed from the selected-items strip.
         * @param index The [GallerySelected.index] of the removed photo
         */
        fun inselectPhoto(index: Int)

        /**
         * Called when a video is removed from the selected-items strip.
         * @param index The [GallerySelected.index] of the removed video
         */
        fun inselectVideo(index: Int)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Accessors
    // ──────────────────────────────────────────────────────────────────────

    /** Returns the mutable list of selected items. */
    fun getGallerySelecteds(): MutableList<GallerySelected> = gallerySelecteds

    /** Returns the configured thumbnail size. */
    fun getSize(): Int = size

    // ──────────────────────────────────────────────────────────────────────
    // Item manipulation
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Add a video selection to the strip.
     *
     * Also sets the bidirectional link: [VideoItem.gallerySelected] → this
     * [GallerySelected] wrapper, so that the grid adapter can retrieve
     * the [GallerySelected] when the user deselects a video from the grid.
     *
     * @param gallerySelected The wrapper containing the selected video
     */
    fun addItemVideo(gallerySelected: GallerySelected) {
        gallerySelecteds.add(gallerySelected)
        gallerySelected.videoItem?.setGallerySelected(gallerySelected)
        notifyItemInserted(gallerySelecteds.size - 1)
    }

    /**
     * Add a photo selection to the strip.
     *
     * Also sets the bidirectional link: [PhotoItem.gallerySelected] → this
     * [GallerySelected] wrapper, so that the grid adapter can retrieve
     * the [GallerySelected] when the user deselects a photo from the grid.
     *
     * @param gallerySelected The wrapper containing the selected photo
     */
    fun addItemPhoto(gallerySelected: GallerySelected) {
        gallerySelecteds.add(gallerySelected)
        gallerySelected.photoItem?.gallerySelected = gallerySelected
        notifyItemInserted(gallerySelecteds.size - 1)
    }

    /**
     * Remove a selected item by its [GallerySelected] reference.
     *
     * @param gallerySelected The item to remove
     */
    fun deletedItem(gallerySelected: GallerySelected) {
        val index = gallerySelecteds.indexOf(gallerySelected)
        if (index != -1) {
            gallerySelecteds.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * Remove a selected item by its position in the list.
     *
     * @param position The index to remove
     */
    fun deletedItem(position: Int) {
        gallerySelecteds.removeAt(position)
        notifyItemRemoved(position)
    }

    // ──────────────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_gallery_select, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val gallerySelected = gallerySelecteds[position]
        val path: String

        if (gallerySelected.videoItem != null) {
            // Video item: show the duration label
            path = gallerySelected.videoItem!!.path
            holder.tvTime.visibility = View.VISIBLE
            holder.tvTime.text = gallerySelected.videoItem!!.time
        } else {
            // Photo item: hide the duration label
            path = gallerySelected.photoItem!!.path
            holder.tvTime.visibility = View.GONE
        }

        // Load the thumbnail (no version signature needed for the selection strip)
        Glide.with(holder.itemView)
            .load(path)
            .override(size, size)
            .centerCrop()
            .placeholder(bitmapPlaceHolder)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = gallerySelecteds.size

    // ──────────────────────────────────────────────────────────────────────
    // ViewHolder
    // ──────────────────────────────────────────────────────────────────────

    /**
     * ViewHolder for a single selected-item cell in the horizontal strip.
     *
     * Contains:
     * - [imageView]: The thumbnail (SquareImageView)
     * - [tvTime]: Duration label (visible for videos only)
     * - [btnDeleted]: Delete button (X icon) to remove the selection
     *
     * When the delete button is tapped, the adapter removes the item from
     * [gallerySelecteds] and calls the appropriate [IGallerySelected] callback
     * so the parent can deselect the item in the grid picker.
     */
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: SquareImageView = itemView.findViewById(R.id.img)
        val tvTime: TextCustumFont = itemView.findViewById(R.id.tv_time)
        val btnDeleted: ImageButton = itemView.findViewById(R.id.btn_deleted)

        init {
            // Make the delete button and time label visible for the selection strip
            btnDeleted.visibility = View.VISIBLE
            tvTime.visibility = View.VISIBLE

            btnDeleted.setOnClickListener {
                val pos = adapterPosition
                if (pos < 0 || pos >= gallerySelecteds.size) return@setOnClickListener

                val gallerySelected = gallerySelecteds[pos]
                deletedItem(pos)

                // Notify the parent which type of item was deselected
                if (gallerySelected.videoItem != null) {
                    iGallerySelected.inselectVideo(gallerySelected.index)
                } else {
                    iGallerySelected.inselectPhoto(gallerySelected.index)
                }
            }
        }
    }
}
