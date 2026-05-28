package hazem.nurmontage.videoquran.utils

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Quran text reader — loads ayah text from the bundled Quran asset files.
 *
 * Originally: QuranReader.java
 * Converted to: QuranReader.kt — idiomatic Kotlin, full logic preserved
 *
 * Architecture:
 * - Reads from `assets/quran/quran-simple.txt` (Arabic Quran text, pipe-delimited)
 * - Format per line: `surahNumber|ayahNumber|ayahText`
 * - [getAyahText] retrieves a single ayah by surah + ayah number
 * - [getTranslationAyahText] retrieves a translation ayah from localized files
 *   (e.g., `quran/en.hilali.txt`, `quran/ar.muyassar.txt`)
 * - Translation file format per line: `surahNumber|ayahNumber|translationText`
 *
 * The Quran simple text file contains the complete Uthmani script without
 * tashkeel, stored as a pipe-delimited UTF-8 text file. Each line represents
 * a single ayah with its surah and ayah numbers as the first two fields.
 *
 * Translation files follow a similar format but with the translation text
 * as the remainder of the line after the surah|ayah prefix, allowing
 * translations to contain pipe characters without escaping issues.
 *
 * @property context Android context for asset access
 */
class QuranReader(private val context: Context) {

    /**
     * Retrieves the Arabic ayah text for the specified surah and ayah number.
     *
     * Opens the `quran/quran-simple.txt` asset file and scans line by line
     * until the matching surah|ayah pair is found. Each line is split by
     * the pipe delimiter, and the first two fields are parsed as integers
     * for comparison.
     *
     * This is a linear scan — for performance-critical code, consider
     * caching the results or pre-loading the entire file into memory.
     * However, for typical single-ayah lookups this is acceptable because
     * the file is only ~1.5MB and Android's asset I/O is buffered.
     *
     * @param surahNumber The surah number (1-based, e.g., 1 for Al-Fatiha)
     * @param ayahNumber The ayah number within the surah (1-based)
     * @return The ayah text in Arabic, or "Ayah not found" if not located
     */
    fun getAyahText(surahNumber: Int, ayahNumber: Int): String {
        try {
            val reader = BufferedReader(
                InputStreamReader(
                    context.assets.open("quran/quran-simple.txt"),
                    StandardCharsets.UTF_8
                )
            )
            reader.use {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line!!.split("\\|".toRegex())
                    if (parts.size == 3) {
                        try {
                            val surah = parts[0].toInt()
                            val ayah = parts[1].toInt()
                            val text = parts[2]
                            if (surah == surahNumber && ayah == ayahNumber) {
                                return text
                            }
                        } catch (_: NumberFormatException) {
                            // Skip malformed lines
                        }
                    }
                }
            }
            return "Ayah not found"
        } catch (e: IOException) {
            e.printStackTrace()
            return "Error reading file: ${e.message}"
        }
    }

    /**
     * Retrieves a translated ayah text from a specific translation file.
     *
     * Translation files are stored in `assets/quran/` with names like
     * `en.hilali.txt`, `ar.muyassar.txt`, `fr.hamidullah.txt`, etc.
     * Each line starts with `surahNumber|ayahNumber` followed by the
     * translation text (the remainder of the line after the prefix match).
     *
     * The method scans for lines starting with the exact `surah|ayah` prefix,
     * then returns the rest of the line as the translation text. This allows
     * translation text to contain any characters (including pipes) without
     * requiring escaping.
     *
     * @param translationFileName The translation file name (e.g., "en.hilali.txt")
     * @param surahNumber The surah number (1-based)
     * @param ayahNumber The ayah number within the surah (1-based)
     * @return The translation text, or "Aya Not Found !" if not located
     */
    fun getTranslationAyahText(
        translationFileName: String,
        surahNumber: Int,
        ayahNumber: Int
    ): String {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(
                InputStreamReader(
                    context.assets.open("quran/$translationFileName"),
                    StandardCharsets.UTF_8
                )
            )
            val prefix = "$surahNumber|$ayahNumber"
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith(prefix)) {
                    return line.substring(prefix.length)
                }
            }
            reader.close()
            return "Aya Not Found !"
        } catch (e: IOException) {
            e.printStackTrace()
            return "Aya Not Found !"
        } finally {
            try {
                reader?.close()
            } catch (_: IOException) {}
        }
    }
}
