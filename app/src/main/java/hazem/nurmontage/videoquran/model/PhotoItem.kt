package hazem.nurmontage.videoquran.model

/**
 * Represents a single photo/image item in the device's gallery.
 *
 * Each [PhotoItem] stores:
 *   - [folder]: The name of the folder/album containing this photo (mutable)
 *   - [path]: The absolute file path of the photo
 *   - [isSelect]: Whether this photo is currently selected by the user
 *   - [gallerySelected]: A [GallerySelected] wrapper linking this photo
 *     to its selection index in the gallery picker
 *   - [adabter_pos]: The position of this item in the adapter list
 *     (used for reverse lookup when scrolling to a selected item)
 *   - [number]: A sequential number assigned to selected items
 *     (shows the order of selection, e.g., "1", "2", "3")
 *
 * This model is used by the gallery picker activities
 * ([GalleryPickerOneImage], gallery photos fragments) to display
 * browsable image grids and track user selections.
 *
 * The [gallerySelected] field creates a bidirectional link: the
 * [GallerySelected] object holds a reference back to this [PhotoItem],
 * which allows the gallery picker to quickly resolve which photo
 * corresponds to a given selection index.
 *
 * NOT a data class because the original Java class had mutable fields
 * with getter/setter pairs that are referenced throughout the adapters.
 *
 * Field names preserved from original JADX source for compatibility.
 *
 * Converted from PhotoItem.java (61 lines).
 */
class PhotoItem(
    var folder: String,
    val path: String,
    var isSelect: Boolean
) {
    var adabter_pos: Int = 0
    var number: Int = 0
    var gallerySelected: GallerySelected? = null
}
