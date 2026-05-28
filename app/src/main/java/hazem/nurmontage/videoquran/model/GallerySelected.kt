package hazem.nurmontage.videoquran.model

import java.io.Serializable

/**
 * Wraps a selected gallery item (photo or video) together with its
 * selection index.
 *
 * The original Java class had two constructors:
 *   - `GallerySelected(PhotoItem, int)` — for image selections
 *   - `GallerySelected(VideoItem, int)` — for video selections
 *
 * Only one of [photoItem] or [videoItem] will be non-null at any time,
 * depending on whether the user selected an image or a video.
 *
 * The [index] field tracks the order of selection (1st, 2nd, 3rd…)
 * and is displayed as a badge on the selected item's thumbnail.
 *
 * Field names preserved from original JADX source for compatibility.
 *
 * Converted from GallerySelected.java (30 lines).
 */
class GallerySelected : Serializable {

    var photoItem: PhotoItem? = null
        private set
    var videoItem: VideoItem? = null
        private set
    var index: Int = 0
        private set

    /**
     * Constructor for photo/image selection.
     */
    constructor(photoItem: PhotoItem, index: Int) {
        this.photoItem = photoItem
        this.index = index
    }

    /**
     * Constructor for video selection.
     */
    constructor(videoItem: VideoItem, index: Int) {
        this.videoItem = videoItem
        this.index = index
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
