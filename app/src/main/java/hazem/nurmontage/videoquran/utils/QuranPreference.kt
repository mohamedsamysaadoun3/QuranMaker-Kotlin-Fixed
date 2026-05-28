package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Quran search and reader preferences — persistence layer for surah selection,
 * ayah ranges, last search query, and reader/translation choices.
 *
 * Originally: QuranPreference.java
 * Converted to: QuranPreference.kt — idiomatic Kotlin, full logic preserved
 *
 * Architecture:
 * - Stores Quran-related preferences in SharedPreferences under "QuranPrefs_"
 * - Tracks: surah index, from/to ayah range, last search query,
 *   selected reader index, and selected translation index
 * - Instance-based API for reading preferences (via constructor with Context)
 * - Static API for writing search results and last search query
 * - Two overloads of [savePreferencesSearch] for different use cases:
 *   - Full: saves surah + from + to + search query
 *   - Simple: saves surah + single ayah (from == to)
 *
 * This class is the "surah loader" in the sense that it persists and retrieves
 * the user's last Quran browsing state (which surah, which ayah range, which
 * reader/reciter, which translation) so the app can restore state between
 * sessions.
 *
 * @property sharedPreferences The SharedPreferences instance for reading
 */
class QuranPreference(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "QuranPrefs_"
        private const val KEY_FROM = "from"
        private const val KEY_TO = "to"
        private const val KEY_SURAH = "surah"
        private const val KEY_SEARCH = "search"
        private const val KEY_NAME_READER = "name_reader_"
        private const val KEY_TRANSLATION = "translation_select"

        /**
         * Saves the full search result to SharedPreferences.
         * Called when the user selects an ayah range from search results.
         *
         * @param context The application context
         * @param surah The selected surah index (0-based)
         * @param from The starting ayah position in the adapter
         * @param to The ending ayah position in the adapter
         * @param searchQuery The search query string that led to this result
         */
        fun savePreferencesSearch(
            context: Context,
            surah: Int,
            from: Int,
            to: Int,
            searchQuery: String
        ) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_FROM, from)
                .putInt(KEY_TO, to)
                .putInt(KEY_SURAH, surah)
                .putString(KEY_SEARCH, searchQuery)
                .apply()
        }

        /**
         * Saves a simple single-ayah search result.
         * Sets both from and to to the same ayah position.
         *
         * @param context The application context
         * @param surah The selected surah index (0-based)
         * @param ayah The selected ayah position (used for both from and to)
         */
        fun savePreferencesSearch(context: Context, surah: Int, ayah: Int) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_FROM, ayah)
                .putInt(KEY_TO, ayah)
                .putInt(KEY_SURAH, surah)
                .apply()
        }

        /**
         * Saves the last search query string for auto-restoration.
         * Used by QuranSearchActivity to pre-fill the search field
         * when the user returns to the search screen.
         *
         * @param context The application context
         * @param query The search query to persist
         */
        fun saveLastSearch(context: Context, query: String?) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SEARCH, query)
                .apply()
        }

        /**
         * Retrieves the last search query string.
         * Returns empty string if no previous search exists.
         *
         * @param context The application context
         * @return The last search query, or empty string
         */
        fun getLastSearch(context: Context): String {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SEARCH, "") ?: ""
        }
    }

    /**
     * Saves the complete Quran editing state: surah, ayah range, reader, and translation.
     * Called when the user finishes configuring the Quran section in the editor.
     *
     * @param surah The selected surah index (0-based)
     * @param from The starting ayah position
     * @param to The ending ayah position
     * @param nameReader The selected reader/reciter index
     * @param translation The selected translation file index
     */
    fun savePreferences(surah: Int, from: Int, to: Int, nameReader: Int, translation: Int) {
        sharedPreferences.edit()
            .putInt(KEY_FROM, from)
            .putInt(KEY_TO, to)
            .putInt(KEY_SURAH, surah)
            .putInt(KEY_NAME_READER, nameReader)
            .putInt(KEY_TRANSLATION, translation)
            .apply()
    }

    /** Returns the last selected surah index (0-based), defaulting to 0. */
    fun getSurah(): Int = sharedPreferences.getInt(KEY_SURAH, 0)

    /** Returns the last selected translation file index, defaulting to 0. */
    fun getTranslation(): Int = sharedPreferences.getInt(KEY_TRANSLATION, 0)

    /** Returns the starting ayah position of the last selection, defaulting to 0. */
    fun getFrom(): Int = sharedPreferences.getInt(KEY_FROM, 0)

    /** Returns the ending ayah position of the last selection, defaulting to 0. */
    fun getTo(): Int = sharedPreferences.getInt(KEY_TO, 0)

    /**
     * Returns the last selected reader/reciter index, defaulting to 0.
     * Wrapped in try-catch to handle potential type mismatch in stored data.
     */
    fun getNameReader(): Int {
        return try {
            sharedPreferences.getInt(KEY_NAME_READER, 0)
        } catch (_: Exception) {
            0
        }
    }
}
