package hazem.nurmontage.videoquran.utils

import hazem.nurmontage.videoquran.model.WordModel

/**
 * Word-level text processor for Quran ayah word splitting, reversal, and
 * phrase search highlighting.
 *
 * Originally: WordProcessor.java
 * Converted to: WordProcessor.kt — idiomatic Kotlin, full logic preserved
 *
 * Architecture:
 * - [reverseInGroupsOfFour] reverses words within groups of 4 for RTL display
 *   correction — Arabic text in some contexts needs sub-group reversal to
 *   properly handle the visual ordering of ayah segments
 * - [findAndSelectPhrase] splits an ayah into individual words and marks
 *   which words match a search phrase — this is the core of the Karaoke-style
 *   word-by-word highlighting feature, where matching words are shown in a
 *   different color/style to indicate the search match position
 * - [mapIndexAfterGroupReverse] is a static utility that maps an index from
 *   the original word order to the post-reversal order, allowing external
 *   components to track which original word maps to which displayed position
 *
 * The phrase search uses a simple sliding window approach:
 * 1. Split both the full text and the search phrase into word arrays
 * 2. Scan the text for a contiguous sequence matching the phrase
 * 3. Mark all words that fall within the match range as selected
 *
 * This produces a [WordModel] list where selected words form the highlighted
 * portion, ready for display by [WordAyaAdabter].
 */
class WordProcessor {

    /**
     * Reverses the order of words within each group of 4 in the list.
     *
     * This is used for RTL (Right-to-Left) display correction where Arabic
     * ayah words need to be visually reordered in groups to match the
     * traditional Quran reading pattern. For example, if an ayah has 7 words,
     * words 0-3 are reversed (3,2,1,0) and words 4-6 are reversed (6,5,4).
     *
     * @param list The original ordered list of word models
     * @return A new list with words reversed within each 4-word group
     */
    fun reverseInGroupsOfFour(list: List<WordModel>): List<WordModel> {
        val result = mutableListOf<WordModel>()
        var i = 0
        while (i < list.size) {
            val end = i + 4
            val group = ArrayList(list.subList(i, minOf(end, list.size())))
            group.reverse()
            result.addAll(group)
            i = end
        }
        return result
    }

    /**
     * Splits an ayah text into individual words and marks the ones that
     * match the search phrase. This is the core search highlighting method
     * used by the Karaoke word-by-word display.
     *
     * The method:
     * 1. Splits both the full text and search phrase by whitespace
     * 2. Uses a sliding window to find the first contiguous match
     * 3. Creates a [WordModel] for each word, marking matched words as selected
     *
     * If no match is found, all words are returned unselected.
     * Only the first match is highlighted (not all occurrences).
     *
     * @param fullText The complete ayah text to search within
     * @param phrase The search phrase to find and highlight
     * @return A list of [WordModel] where matching words have isSelected=true
     */
    fun findAndSelectPhrase(fullText: String, phrase: String): List<WordModel> {
        val words = fullText.trim().split("\\s+".toRegex())
        val phraseWords = phrase.trim().split("\\s+".toRegex())
        val result = mutableListOf<WordModel>()

        // Find the starting index of the first match using sliding window
        var matchStart = -1
        var i = 0
        search@ while (i <= words.size - phraseWords.size) {
            for (j in phraseWords.indices) {
                if (words[i + j] != phraseWords[j]) {
                    i++
                    continue@search
                }
            }
            matchStart = i
            break
        }
        // If no complete match found, matchStart remains -1

        // Build the result list with selection flags
        for (k in words.indices) {
            val isSelected = matchStart != -1 && k >= matchStart && k < matchStart + phraseWords.size
            result.add(WordModel(words[k], isSelected))
        }

        return result
    }

    companion object {
        /**
         * Maps an index from the original word order to its position after
         * group reversal by [reverseInGroupsOfFour].
         *
         * Given an original index, the group size (typically 4), and the total
         * number of items, this calculates where that index would end up after
         * the sub-group reversal operation.
         *
         * Formula: The group start is `(index / groupSize) * groupSize`.
         * Within the group, the reversed position is calculated by mirroring
         * the offset from the group start, limited by the group boundary
         * (which may be smaller than groupSize for the last partial group).
         *
         * @param index The original index in the pre-reversal list
         * @param groupSize The size of each reversal group (typically 4)
         * @param totalItems The total number of items in the list
         * @return The mapped index after group reversal
         */
        fun mapIndexAfterGroupReverse(index: Int, groupSize: Int, totalItems: Int): Int {
            val groupStart = (index / groupSize) * groupSize
            val offset = index % groupSize
            val actualGroupSize = minOf(groupSize, totalItems - groupStart)
            return groupStart + (actualGroupSize - 1 - offset)
        }
    }
}
