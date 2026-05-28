package hazem.nurmontage.videoquran.model

/**
 * Represents a single video item in the device's gallery.
 *
 * Each [VideoItem] stores:
 *   - [folderPath]: The path of the folder/album containing this video
 *   - [path]: The absolute file path of the video
 *   - [time]: A human-readable duration string (e.g., "01:23")
 *   - [isSelect]: Whether this video is currently selected by the user
 *   - [gallerySelected]: A [GallerySelected] wrapper linking this video
 *     to its selection index in the gallery picker
 *   - [adabter_pos]: The position of this item in the adapter list
 *   - [number]: A sequential number assigned to selected items
 *
 * This model is used by [GalleryPickerVideo] and the video gallery
 * fragments to display browsable video grids and track user selections.
 * It is the video counterpart to [PhotoItem].
 *
 * The [gallerySelected] field creates a bidirectional link: the
 * [GallerySelected] object holds a reference back to this [VideoItem],
 * which allows the gallery picker to quickly resolve which video
 * corresponds to a given selection index.
 *
 * NOT a data class because the original Java class had mutable fields
 * with getter/setter pairs that are referenced throughout the adapters.
 *
 * Field names preserved from original JADX source for compatibility.
 *
 * Converted from VideoItem.java (63 lines).
 */
class VideoItem(
    val folderPath: String,
    val path: String,
    val time: String,
    var isSelect: Boolean
) {
    var adabter_pos: Int = 0
    var number: Int = 0
    var gallerySelected: GallerySelected? = null
}
