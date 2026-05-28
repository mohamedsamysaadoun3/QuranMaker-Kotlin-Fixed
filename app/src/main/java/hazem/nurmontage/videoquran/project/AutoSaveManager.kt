package hazem.nurmontage.videoquran.project

import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Periodic auto-save using Handler.
 * Saves project state every 30 seconds when changes are detected.
 */
class AutoSaveManager(
    private val callback: AutoSaveCallback?,
    private val intervalMs: Long = DEFAULT_INTERVAL_MS
) {

    interface AutoSaveCallback {
        fun hasUnsavedChanges(): Boolean
        fun onAutoSave()
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var hasUnsavedChanges = false

    private val saveRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return

            // Check if there are unsaved changes
            if (callback != null && callback.hasUnsavedChanges()) {
                Log.d(TAG, "Auto-saving project...")
                callback.onAutoSave()
                hasUnsavedChanges = false
            }

            // Schedule next check
            if (isRunning) {
                handler.postDelayed(this, intervalMs)
            }
        }
    }

    /**
     * Start periodic auto-save.
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        hasUnsavedChanges = false
        handler.postDelayed(saveRunnable, intervalMs)
        Log.d(TAG, "Auto-save started with interval $intervalMs ms")
    }

    /**
     * Stop periodic auto-save.
     */
    fun stop() {
        isRunning = false
        handler.removeCallbacks(saveRunnable)
        Log.d(TAG, "Auto-save stopped")
    }

    /**
     * Mark that there are unsaved changes.
     * Triggers an immediate save check.
     */
    fun markDirty() {
        hasUnsavedChanges = true
        // Reset the timer so we don't save too frequently
        if (isRunning) {
            handler.removeCallbacks(saveRunnable)
            handler.postDelayed(saveRunnable, intervalMs)
        }
    }

    /**
     * Force an immediate save regardless of timer.
     */
    fun forceSave() {
        callback?.let {
            it.onAutoSave()
            hasUnsavedChanges = false
        }
    }

    fun isRunning(): Boolean = isRunning

    fun hasUnsavedChanges(): Boolean {
        return hasUnsavedChanges || (callback != null && callback.hasUnsavedChanges())
    }

    fun getIntervalMs(): Long = intervalMs

    /**
     * Release resources. After calling this, the manager cannot be restarted.
     */
    fun release() {
        stop()
    }

    companion object {
        private const val TAG = "AutoSaveManager"
        private const val DEFAULT_INTERVAL_MS = 30_000L // 30 seconds
    }
}
