package hazem.nurmontage.videoquran.model

/**
 * Multi-step render progress tracker with weighted task distribution.
 *
 * Manages a sequence of [RenderTask] steps and computes a global progress
 * value (0.0–1.0) based on the relative weight of each step.
 *
 * Typical usage:
 * ```
 * val manager = RenderManager()
 * manager.addTask("Extracting audio", 5)
 * manager.addTask("Rendering text", 12)
 * manager.addTask("Encoding video", 30)
 * manager.computeWeights()
 *
 * // During step 0:
 * val progress = manager.updateLocalProgress(0.5f) // 50% of step 0
 * ```
 *
 * Tasks are inserted at the **front** of the list (index 0), so the last
 * task added is processed first. This matches the original Java behavior.
 *
 * The global progress is calculated as:
 * ```
 * globalProgress = sum(weights[0..currentStep-1]) + localProgress * weights[currentStep]
 * ```
 *
 * Converted from RenderManager.java — weight computation and progress
 * calculation preserved exactly.
 */
class RenderManager {

    private val tasks: MutableList<RenderTask> = mutableListOf()
    private var currentTaskIndex: Int = 0
    private var globalProgress: Float = 0f

    /**
     * Add a new render step at the **front** of the task list.
     *
     * @param name              Step label
     * @param expectedDuration  Estimated duration in seconds
     */
    fun addTask(name: String, expectedDuration: Int) {
        val task = RenderTask().apply {
            this.name = name
            this.expectedDuration = expectedDuration
        }
        tasks.add(0, task)
    }

    /**
     * Compute the relative weight of each task based on its expected duration.
     *
     * Weight = taskDuration / totalDuration
     *
     * Must be called after all tasks are added and before [updateLocalProgress].
     */
    fun computeWeights() {
        val totalDuration = tasks.sumOf { it.expectedDuration }
        if (totalDuration == 0) return

        tasks.forEach { task ->
            task.weight = task.expectedDuration.toFloat() / totalDuration
        }
    }

    /**
     * Get the expected duration of the current step.
     */
    fun getCurrentStepDuration(): Int {
        return tasks[currentTaskIndex].expectedDuration
    }

    /**
     * Advance to the next render step.
     *
     * Does nothing if already at the last step.
     */
    fun nextTask() {
        if (currentTaskIndex < tasks.size - 1) {
            currentTaskIndex++
        }
    }

    /**
     * Update the global progress based on the local progress of the current step.
     *
     * The global progress is the sum of:
     * 1. All completed steps' weights (steps 0 to currentTaskIndex-1)
     * 2. The current step's contribution: localProgress × currentStepWeight
     *
     * The result is clamped to a maximum of 1.0.
     *
     * @param localProgress  Progress within the current step (0.0–1.0)
     * @return Global progress across all steps (0.0–1.0)
     */
    fun updateLocalProgress(localProgress: Float): Float {
        var completedWeight = 0f
        for (i in 0 until currentTaskIndex) {
            completedWeight += tasks[i].weight
        }

        globalProgress = completedWeight + (localProgress * tasks[currentTaskIndex].weight)
        if (globalProgress > 1.0f) {
            globalProgress = 1.0f
        }

        return globalProgress
    }
}
