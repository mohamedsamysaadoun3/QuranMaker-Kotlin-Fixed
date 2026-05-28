package hazem.nurmontage.videoquran.model

/**
 * Represents a feature entry displayed in the "Features" list on the
 * Pro Version / About screen.
 *
 * Each [ModelFeatures] has:
 *   - [name]: The display name of the feature (e.g., "Remove Watermark")
 *   - [isForFree]: Whether this feature is available in the free version
 *
 * In the original app, this was used to show a comparison table of free
 * vs pro features. Since we have removed the billing/pro system, all
 * features are now treated as free ([isForFree] = true by default).
 * However, the model is retained for compatibility with the FeaturesAdapter
 * and any future feature-gating logic.
 *
 * Field names preserved from original JADX source for compatibility.
 *
 * Converted from ModelFeatures.java (24 lines).
 */
data class ModelFeatures(
    val name: String,
    val isForFree: Boolean = false
) {
    /**
     * Secondary constructor for features without a free/paid designation.
     * Defaults [isForFree] to `false`.
     */
    constructor(name: String) : this(name, false)
}
