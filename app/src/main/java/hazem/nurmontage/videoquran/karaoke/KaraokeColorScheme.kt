package hazem.nurmontage.videoquran.karaoke

import android.graphics.Color

/**
 * Defines color schemes for karaoke word highlighting.
 * Supports SOLID, GRADIENT, and GLOW rendering types.
 *
 * Seven predefined schemes are available via companion factory methods:
 * GOLD, EMERALD, OCEAN, SUNSET, ROYAL, NOOR, DARK.
 * Each scheme specifies primary, secondary, background, and text colors
 * along with a default [RenderType].
 */
class KaraokeColorScheme private constructor(
    val schemeType: SchemeType,
    val primaryColor: Int,
    val secondaryColor: Int,
    val backgroundColor: Int,
    val textColor: Int,
    val renderType: RenderType
) {

    /** Available karaoke color scheme presets. */
    enum class SchemeType { GOLD, EMERALD, OCEAN, SUNSET, ROYAL, NOOR, DARK }

    /** How the highlight color is rendered on the word. */
    enum class RenderType { SOLID, GRADIENT, GLOW }

    /** Create a copy with a different render type. */
    fun withRenderType(type: RenderType): KaraokeColorScheme =
        KaraokeColorScheme(schemeType, primaryColor, secondaryColor, backgroundColor, textColor, type)

    companion object {
        /** Gold scheme — warm gradient highlight on dark background. */
        @JvmStatic
        fun GOLD(): KaraokeColorScheme = KaraokeColorScheme(
            SchemeType.GOLD,
            Color.parseColor("#FFD700"), Color.parseColor("#FFA500"),
            Color.parseColor("#1A1A2E"), Color.parseColor("#FFFFFF"),
            RenderType.GRADIENT
        )

        /** Emerald scheme — green solid highlight on dark green background. */
        @JvmStatic
        fun EMERALD(): KaraokeColorScheme = KaraokeColorScheme(
            SchemeType.EMERALD,
            Color.parseColor("#50C878"), Color.parseColor("#2E8B57"),
            Color.parseColor("#0D1B0E"), Color.parseColor("#E0F0E0"),
            RenderType.SOLID
        )

        /** Ocean scheme — blue gradient highlight on deep navy background. */
        @JvmStatic
        fun OCEAN(): KaraokeColorScheme = KaraokeColorScheme(
            SchemeType.OCEAN,
            Color.parseColor("#0077BE"), Color.parseColor("#00B4D8"),
            Color.parseColor("#0A1628"), Color.parseColor("#CCE5FF"),
            RenderType.GRADIENT
        )

        /** Sunset scheme — orange-pink gradient on dark warm background. */
        @JvmStatic
        fun SUNSET(): KaraokeColorScheme = KaraokeColorScheme(
            SchemeType.SUNSET,
            Color.parseColor("#FF6B35"), Color.parseColor("#FF1493"),
            Color.parseColor("#1A0A14"), Color.parseColor("#FFE4E1"),
            RenderType.GRADIENT
        )

        /** Royal scheme — purple glow highlight on deep violet background. */
        @JvmStatic
        fun ROYAL(): KaraokeColorScheme = KaraokeColorScheme(
            SchemeType.ROYAL,
            Color.parseColor("#7B2D8E"), Color.parseColor("#C77DFF"),
            Color.parseColor("#0F0515"), Color.parseColor("#E8D5F5"),
            RenderType.GLOW
        )

        /** Noor scheme — cream/beige on pure black, Islamic calligraphy feel. */
        @JvmStatic
        fun NOOR(): KaraokeColorScheme = KaraokeColorScheme(
            SchemeType.NOOR,
            Color.parseColor("#F5E6CA"), Color.parseColor("#E8D5B7"),
            Color.parseColor("#0A0A0A"), Color.parseColor("#1A1A1A"),
            RenderType.SOLID
        )

        /** Dark scheme — subtle gray on near-black background. */
        @JvmStatic
        fun DARK(): KaraokeColorScheme = KaraokeColorScheme(
            SchemeType.DARK,
            Color.parseColor("#CCCCCC"), Color.parseColor("#888888"),
            Color.parseColor("#121212"), Color.parseColor("#E0E0E0"),
            RenderType.SOLID
        )

        /** Get a scheme by type. Defaults to GOLD for unknown types. */
        @JvmStatic
        fun getScheme(type: SchemeType): KaraokeColorScheme = when (type) {
            SchemeType.GOLD    -> GOLD()
            SchemeType.EMERALD -> EMERALD()
            SchemeType.OCEAN   -> OCEAN()
            SchemeType.SUNSET  -> SUNSET()
            SchemeType.ROYAL   -> ROYAL()
            SchemeType.NOOR    -> NOOR()
            SchemeType.DARK    -> DARK()
        }

        /** Returns all available scheme types. */
        @JvmStatic
        fun allSchemeTypes(): Array<SchemeType> = SchemeType.values()
    }
}
