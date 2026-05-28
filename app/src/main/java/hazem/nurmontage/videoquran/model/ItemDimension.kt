package hazem.nurmontage.videoquran.model

import hazem.nurmontage.videoquran.constant.ResizeType

/**
 * Represents a video/image dimension preset (e.g., TikTok 9:16, YouTube 16:9).
 *
 * Each [ItemDimension] defines:
 *   - [name]: Display name (e.g., "TikTok", "YouTube Thumbnail")
 *   - [image]: Drawable resource ID for the preset icon
 *   - [resizeType]: The [ResizeType] enum value for this dimension
 *   - [w]: Width in pixels
 *   - [h]: Height in pixels
 *   - [id]: Short identifier string (e.g., "t" for TikTok, "y_16:9" for YouTube)
 *
 * These presets are displayed in the ResizeFragment as selectable dimension
 * options. The [id] field is used internally when constructing FFmpeg export
 * commands to set the output resolution.
 *
 * All fields are `val` (immutable) because dimension presets are static
 * configuration — they are created once by [DataDimension.getALl] and never
 * modified afterwards.
 *
 * Field names preserved from original JADX source for compatibility.
 *
 * Converted from ItemDimension.java (46 lines).
 */
data class ItemDimension(
    val name: String,
    val image: Int,
    val resizeType: ResizeType,
    val w: Int,
    val h: Int,
    val id: String
)
