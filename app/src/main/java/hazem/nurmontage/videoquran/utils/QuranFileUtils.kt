package hazem.nurmontage.videoquran.utils

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Quran file processing utilities — handles Bismillah replacement and file
 * transformations for the Quran text pipeline.
 *
 * Originally: QuranFileUtils.java
 * Converted to: QuranFileUtils.kt — idiomatic Kotlin, full logic preserved
 *
 * Architecture:
 * - The Quran simple text file contains the full Bismillah
 *   "بِّسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيمِ" at the start of every surah
 *   (except Surah At-Tawbah, surah 9, and the first surah which has it as ayah 1)
 * - For search and display purposes, these Bismillah occurrences are replaced
 *   with "*" to avoid false matches and redundant display
 * - [replacePhraseInFile] processes a File → File replacement
 * - [replacePhraseFromAssetsToFilesDir] processes an Asset → internal storage replacement
 * - [counTPhraseFromAssetsToFilesDir] counts Bismillah occurrences (debug utility)
 *
 * The replacement is done once when the app first processes the Quran text,
 * and the resulting file is stored in internal storage for subsequent use.
 * This avoids repeated string replacement on every app launch.
 */
object QuranFileUtils {

    private const val TAG = "QuranFileUtils"

    /** The full Bismillah phrase to be replaced in Quran text files. */
    private const val TARGET = "\u0628\u0651\u0650\u0633\u0652\u0645\u0650 \u0627\u0644\u0644\u0651\u064E\u0647\u0650 \u0627\u0644\u0631\u0651\u064E\u062D\u0652\u0645\u064E\u0670\u0646\u0650 \u0627\u0644\u0631\u0651\u064E\u062D\u0650\u064A\u0645\u0650"
    // = "بِّسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيمِ"

    /** The replacement string for the Bismillah phrase. */
    private const val REPLACEMENT = "*"

    /**
     * Replaces all occurrences of the Bismillah phrase in a source file
     * and writes the result to a destination file.
     *
     * Reads the entire source file into memory, performs the string replacement,
     * then writes the modified content to the destination file. Both files
     * are processed with UTF-8 encoding to preserve Arabic text.
     *
     * @param sourceFile The input file containing the original Quran text
     * @param destFile The output file to write the modified text to
     * @throws IOException If file reading or writing fails
     */
    fun replacePhraseInFile(sourceFile: File, destFile: File) {
        val sb = StringBuilder()
        BufferedReader(InputStreamReader(FileInputStream(sourceFile), StandardCharsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
        }
        val replaced = sb.toString().replace(TARGET, REPLACEMENT)
        BufferedWriter(OutputStreamWriter(FileOutputStream(destFile), StandardCharsets.UTF_8)).use { writer ->
            writer.write(replaced)
        }
    }

    /**
     * Reads a Quran text file from assets, replaces all Bismillah occurrences,
     * and writes the result to the app's internal storage directory.
     *
     * This is the primary method used during initial Quran text processing.
     * The asset file is read into memory, the Bismillah phrase is replaced
     * with "*", and the modified content is saved to the files directory
     * for subsequent use by the search engine and display components.
     *
     * @param context The application context for asset and file access
     * @param assetPath The path to the source file within assets (e.g., "quran/quran-simple.txt")
     * @param destFileName The output file name in the internal storage directory
     * @throws IOException If asset reading or file writing fails
     */
    fun replacePhraseFromAssetsToFilesDir(context: Context, assetPath: String, destFileName: String) {
        val sb = StringBuilder()
        context.assets.open(assetPath).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append('\n')
                }
            }
        }
        val replaced = sb.toString().replace(TARGET, REPLACEMENT)
        BufferedWriter(
            OutputStreamWriter(
                FileOutputStream(File(context.filesDir, destFileName)),
                StandardCharsets.UTF_8
            )
        ).use { writer ->
            writer.write(replaced)
        }
    }

    /**
     * Counts and logs Bismillah occurrences in a Quran asset file.
     * Debug/utility method for verifying the number of Bismillah phrases
     * that would be replaced by the replacement methods.
     *
     * @param context The application context for asset access
     * @param assetPath The path to the Quran text file within assets
     * @throws IOException If asset reading fails
     */
    fun counTPhraseFromAssetsToFilesDir(context: Context, assetPath: String) {
        val sb = StringBuilder()
        context.assets.open(assetPath).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // Java checks for the full phrase minus final kasra: "بَّسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيم"
                    if (line!!.contains("\u0628\u0651\u0650\u0633\u0652\u0645\u0650 \u0627\u0644\u0644\u0651\u064E\u0647\u0650 \u0627\u0644\u0631\u0651\u064E\u062D\u0652\u0645\u064E\u0670\u0646\u0650 \u0627\u0644\u0631\u0651\u064E\u062D\u0650\u064A\u0645")) {
                        Log.e("mLine", line!!)
                    }
                    sb.append(line).append('\n')
                }
            }
        }
    }
}
