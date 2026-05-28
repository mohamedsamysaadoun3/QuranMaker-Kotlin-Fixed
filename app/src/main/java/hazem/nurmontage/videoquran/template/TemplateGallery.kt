package hazem.nurmontage.videoquran.template

import android.graphics.Color

/**
 * Predefined templates for social media dimensions.
 * Supports dark mode variants.
 */
object TemplateGallery {

    private const val TAG = "TemplateGallery"

    /**
     * Represents a predefined template.
     */
    class GalleryTemplate(
        val id: String,
        val name: String,
        val width: Int,
        val height: Int,
        val category: String,
        val isDarkMode: Boolean,
        val backgroundColor: Int,
        val textColor: Int,
        val accentColor: Int,
        val description: String
    )

    /**
     * Get all predefined templates (light mode).
     */
    @JvmStatic
    fun getTemplates(): List<GalleryTemplate> = getTemplates(false)

    /**
     * Get all predefined templates with dark mode support.
     */
    @JvmStatic
    fun getTemplates(darkMode: Boolean): List<GalleryTemplate> {
        val templates = mutableListOf<GalleryTemplate>()

        // Instagram Story
        templates.add(GalleryTemplate(
            "ig_story", "Instagram Story", 1080, 1920,
            "Social", darkMode,
            if (darkMode) Color.parseColor("#1A1A2E") else Color.parseColor("#FFFFFF"),
            if (darkMode) Color.parseColor("#E0E0E0") else Color.parseColor("#1A1A2E"),
            Color.parseColor("#E1306C"),
            "9:16 vertical for Instagram Stories"
        ))

        // Instagram Post
        templates.add(GalleryTemplate(
            "ig_post", "Instagram Post", 1080, 1080,
            "Social", darkMode,
            if (darkMode) Color.parseColor("#16213E") else Color.parseColor("#FAFAFA"),
            if (darkMode) Color.parseColor("#CCCCCC") else Color.parseColor("#262626"),
            Color.parseColor("#C13584"),
            "1:1 square for Instagram Posts"
        ))

        // Instagram Reels
        templates.add(GalleryTemplate(
            "ig_reels", "Instagram Reels", 1080, 1920,
            "Social", darkMode,
            if (darkMode) Color.parseColor("#0A0A0A") else Color.parseColor("#FFFFFF"),
            if (darkMode) Color.parseColor("#F5F5F5") else Color.parseColor("#000000"),
            Color.parseColor("#833AB4"),
            "9:16 for Instagram Reels"
        ))

        // YouTube
        templates.add(GalleryTemplate(
            "yt_video", "YouTube Video", 1920, 1080,
            "Video", darkMode,
            if (darkMode) Color.parseColor("#0F0F0F") else Color.parseColor("#FFFFFF"),
            if (darkMode) Color.parseColor("#F1F1F1") else Color.parseColor("#282828"),
            Color.parseColor("#FF0000"),
            "16:9 landscape for YouTube"
        ))

        // YouTube Shorts
        templates.add(GalleryTemplate(
            "yt_shorts", "YouTube Shorts", 1080, 1920,
            "Video", darkMode,
            if (darkMode) Color.parseColor("#0F0F0F") else Color.parseColor("#FFFFFF"),
            if (darkMode) Color.parseColor("#F1F1F1") else Color.parseColor("#282828"),
            Color.parseColor("#FF0000"),
            "9:16 vertical for YouTube Shorts"
        ))

        // TikTok
        templates.add(GalleryTemplate(
            "tiktok", "TikTok", 1080, 1920,
            "Social", darkMode,
            if (darkMode) Color.parseColor("#121212") else Color.parseColor("#FFFFFF"),
            if (darkMode) Color.parseColor("#FFFFFF") else Color.parseColor("#000000"),
            Color.parseColor("#00F2EA"),
            "9:16 for TikTok videos"
        ))

        // Twitter/X
        templates.add(GalleryTemplate(
            "twitter", "X (Twitter)", 1600, 900,
            "Social", darkMode,
            if (darkMode) Color.parseColor("#15202B") else Color.parseColor("#FFFFFF"),
            if (darkMode) Color.parseColor("#D7DADC") else Color.parseColor("#14171A"),
            Color.parseColor("#1DA1F2"),
            "16:9 for X/Twitter"
        ))

        // Facebook
        templates.add(GalleryTemplate(
            "facebook", "Facebook", 1280, 720,
            "Social", darkMode,
            if (darkMode) Color.parseColor("#242526") else Color.parseColor("#FFFFFF"),
            if (darkMode) Color.parseColor("#E4E6EB") else Color.parseColor("#1C1E21"),
            Color.parseColor("#1877F2"),
            "16:9 for Facebook"
        ))

        // WhatsApp Status
        templates.add(GalleryTemplate(
            "whatsapp", "WhatsApp Status", 1080, 1920,
            "Social", darkMode,
            if (darkMode) Color.parseColor("#111B21") else Color.parseColor("#ECE5DD"),
            if (darkMode) Color.parseColor("#E9EDEF") else Color.parseColor("#303030"),
            Color.parseColor("#25D366"),
            "9:16 for WhatsApp Status"
        ))

        // Cinematic 21:9
        templates.add(GalleryTemplate(
            "cinematic", "Cinematic", 2560, 1080,
            "Cinematic", darkMode,
            if (darkMode) Color.parseColor("#000000") else Color.parseColor("#1A1A1A"),
            if (darkMode) Color.parseColor("#D4AF37") else Color.parseColor("#D4AF37"),
            Color.parseColor("#C0A060"),
            "21:9 ultrawide cinematic"
        ))

        // Square - Generic
        templates.add(GalleryTemplate(
            "square", "Square", 1080, 1080,
            "Generic", darkMode,
            if (darkMode) Color.parseColor("#1E1E1E") else Color.parseColor("#F5F5F5"),
            if (darkMode) Color.parseColor("#E0E0E0") else Color.parseColor("#333333"),
            Color.parseColor("#6200EE"),
            "1:1 square format"
        ))

        return templates
    }

    /**
     * Get a template by its ID.
     */
    @JvmStatic
    fun getTemplateById(id: String): GalleryTemplate? = getTemplateById(id, false)

    /**
     * Get a template by its ID with dark mode support.
     */
    @JvmStatic
    fun getTemplateById(id: String, darkMode: Boolean): GalleryTemplate? {
        val templates = getTemplates(darkMode)
        for (t in templates) {
            if (t.id == id) {
                return t
            }
        }
        return null
    }

    /**
     * Get template names as a string array.
     */
    @JvmStatic
    fun getTemplateNames(darkMode: Boolean): Array<String> {
        val templates = getTemplates(darkMode)
        return templates.map { it.name }.toTypedArray()
    }
}
