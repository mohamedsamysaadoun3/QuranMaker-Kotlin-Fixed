package hazem.nurmontage.videoquran.model

/**
 * Represents a background item used in the template background selector.
 *
 * Each [BgItem] stores a position (x, y) on the canvas, a drawable resource
 * name for the background image, and an ID for identification within the
 * background list. The original app uses this class to populate the
 * background picker with predefined gradient and image backgrounds.
 *
 * Field names (`name_drawable`, `x`, `y`, `id`) are preserved verbatim
 * from the original JADX decompilation to maintain serialization
 * compatibility with saved project files.
 *
 * Converted from BgItem.java (37 lines).
 */
data class BgItem(
    val id: Int,
    val x: Float,
    val y: Float,
    var name_drawable: String
)
