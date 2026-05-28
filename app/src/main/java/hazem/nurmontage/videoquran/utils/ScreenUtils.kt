package hazem.nurmontage.videoquran.utils

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowMetrics
import kotlin.math.round

/**
 * Utility for measuring screen dimensions, accounting for system bars.
 *
 * Provides screen width and height that exclude system bar insets (status bar,
 * navigation bar), giving the actual usable area for content layout.
 *
 * On API 30+ (Android 11+), uses [WindowMetrics] and [WindowInsets] for
 * accurate measurements that handle multi-window mode and display cutouts.
 * On older APIs, falls back to [DisplayMetrics] from the default display.
 *
 * Also provides [byScreenHeight] for proportional sizing — computing a pixel
 * value as a fraction of the screen height. This is commonly used for
 * responsive UI layouts where element sizes should scale with the device.
 *
 * Converted from ScreenUtils.java — logic preserved exactly.
 */
object ScreenUtils {

    /**
     * Get the usable screen width in pixels, excluding system bars.
     *
     * On API 30+: Uses [WindowMetrics.getBounds] minus horizontal system bar insets.
     * On older APIs: Uses [DisplayMetrics.widthPixels] from the default display.
     *
     * @param activity The activity for window manager access
     * @return The usable screen width in pixels
     */
    fun getScreenWidth(activity: Activity): Int {
        if (Build.VERSION.SDK_INT >= 30) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.width() - insets.left - insets.right
        }
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    /**
     * Get the usable screen height in pixels, excluding system bars.
     *
     * On API 30+: Uses [WindowMetrics.getBounds] minus vertical system bar insets.
     * On older APIs: Uses [DisplayMetrics.heightPixels] from the default display.
     *
     * @param activity The activity for window manager access
     * @return The usable screen height in pixels
     */
    fun getScreenHeight(activity: Activity): Int {
        if (Build.VERSION.SDK_INT >= 30) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            return windowMetrics.bounds.height() - insets.top - insets.bottom
        }
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    /**
     * Calculate a pixel value as a proportion of the screen height.
     *
     * This is useful for responsive layouts where element sizes should
     * scale with the device's screen height. For example:
     * - `byScreenHeight(activity, 0.15f)` returns 15% of the screen height
     * - `byScreenHeight(activity, 0.5f)` returns 50% of the screen height
     *
     * @param activity The activity for window manager access
     * @param fraction The fraction of the screen height (0.0 to 1.0)
     * @return The computed pixel value, rounded to the nearest integer
     */
    fun byScreenHeight(activity: Activity, fraction: Float): Int {
        val height: Int = if (Build.VERSION.SDK_INT >= 30) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
        return round(height * fraction).toInt()
    }
}
