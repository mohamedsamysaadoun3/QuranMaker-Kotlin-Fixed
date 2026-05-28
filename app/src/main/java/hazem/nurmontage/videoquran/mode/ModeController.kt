package hazem.nurmontage.videoquran.mode

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Switches between BASIC and PRO mode.
 * Manages which UI elements are visible in each mode.
 */
class ModeController(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var currentMode: AppMode

    private var listener: OnModeChangedListener? = null

    enum class AppMode {
        BASIC,
        PRO
    }

    interface OnModeChangedListener {
        fun onModeChanged(newMode: AppMode)
    }

    init {
        // Load saved mode, default to BASIC
        val savedMode = prefs.getString(KEY_CURRENT_MODE, AppMode.BASIC.name)
        currentMode = try {
            AppMode.valueOf(savedMode ?: AppMode.BASIC.name)
        } catch (e: IllegalArgumentException) {
            AppMode.BASIC
        }
    }

    /**
     * Switch to the specified mode.
     */
    fun setMode(mode: AppMode) {
        if (this.currentMode == mode) return

        val previousMode = this.currentMode
        this.currentMode = mode

        // Persist the mode
        prefs.edit().putString(KEY_CURRENT_MODE, mode.name).apply()

        Log.i(TAG, "Mode changed from $previousMode to $mode")

        listener?.onModeChanged(mode)
    }

    /**
     * Toggle between BASIC and PRO mode.
     */
    fun toggleMode(): AppMode {
        val newMode = if (currentMode == AppMode.BASIC) AppMode.PRO else AppMode.BASIC
        setMode(newMode)
        return newMode
    }

    /**
     * Check if currently in PRO mode.
     */
    fun isProMode(): Boolean = currentMode == AppMode.PRO

    /**
     * Check if currently in BASIC mode.
     */
    fun isBasicMode(): Boolean = currentMode == AppMode.BASIC

    /**
     * Get the current mode.
     */
    fun getCurrentMode(): AppMode = currentMode

    /**
     * Check if a feature is available in the current mode.
     */
    fun isFeatureAvailable(featureId: String): Boolean {
        val config = ModeConfig.getConfig(currentMode)
        return config.isFeatureEnabled(featureId)
    }

    /**
     * Check if a tool is available in the current mode.
     */
    fun isToolAvailable(toolId: String): Boolean {
        val config = ModeConfig.getConfig(currentMode)
        return config.isToolEnabled(toolId)
    }

    fun setOnModeChangedListener(listener: OnModeChangedListener?) {
        this.listener = listener
    }

    companion object {
        private const val TAG = "ModeController"
        private const val PREFS_NAME = "mode_prefs"
        private const val KEY_CURRENT_MODE = "current_mode"
    }
}
