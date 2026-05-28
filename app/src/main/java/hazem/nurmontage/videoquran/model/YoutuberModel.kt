package hazem.nurmontage.videoquran.model

/**
 * Represents a YouTuber/content creator entry displayed on the home screen.
 *
 * Each [YoutuberModel] stores:
 *   - [lnk]: The URL or identifier linking to the YouTuber's channel
 *     or social media profile (the field name "lnk" is preserved from
 *     the original JADX source for serialization compatibility)
 *   - [img]: The drawable resource ID for the YouTuber's profile image
 *     or avatar thumbnail
 *
 * These models are displayed in the [YoutuberActivity] grid/list,
 * allowing users to browse and follow Quran reciters who have
 * YouTube channels. Tapping an item typically opens the link in
 * the YouTube app or a web browser.
 *
 * Field names preserved from original JADX source for compatibility.
 *
 * Converted from YoutuberModel.java (20 lines).
 */
data class YoutuberModel(
    val lnk: String,
    val img: Int
)
