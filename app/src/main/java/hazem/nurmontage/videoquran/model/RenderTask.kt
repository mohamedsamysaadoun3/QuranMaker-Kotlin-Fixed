package hazem.nurmontage.videoquran.model

/**
 * Represents a single step in the video rendering pipeline.
 *
 * Each [RenderTask] has:
 * - [name]: A human-readable label for the step (e.g. "Extracting audio", "Merging layers")
 * - [expectedDuration]: Estimated duration in seconds for progress calculation
 * - [weight]: Computed as a fraction of total duration, used by [RenderManager]
 *   to distribute the global progress bar proportionally across steps
 *
 * Instances are created by [RenderManager.addTask] and their [weight] is
 * computed by [RenderManager.computeWeights].
 *
 * Converted from RenderTask.java — simple data holder, no logic changes.
 */
class RenderTask {
    var name: String? = null
    var expectedDuration: Int = 0
    var weight: Float = 0f
}
