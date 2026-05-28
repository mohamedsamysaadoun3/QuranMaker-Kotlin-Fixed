package hazem.nurmontage.videoquran.layers

import android.widget.ImageView

/**
 * UI controller for per-layer eye icon visibility toggle.
 * Works with LayerManager to toggle layer visibility when eye icons are tapped.
 */
class LayerVisibilityController(
    private val layerManager: LayerManager
) {

    private val eyeIcons: MutableList<ImageView?> = ArrayList()

    /**
     * Bind an eye icon ImageView to a layer index.
     */
    fun bindEyeIcon(layerIndex: Int, eyeIcon: ImageView) {
        if (layerIndex < 0 || layerIndex >= layerManager.layerCount) return

        while (eyeIcons.size <= layerIndex) {
            eyeIcons.add(null)
        }
        eyeIcons[layerIndex] = eyeIcon

        // Set initial state
        val layer = layerManager.getLayer(layerIndex)
        if (layer != null) {
            updateEyeIcon(eyeIcon, layer.isVisible)
        }

        eyeIcon.setOnClickListener {
            layerManager.toggleVisibility(layerIndex)
            val l = layerManager.getLayer(layerIndex)
            if (l != null) {
                updateEyeIcon(eyeIcon, l.isVisible)
            }
        }
    }

    /**
     * Update all eye icons to reflect current layer states.
     */
    fun refreshAll() {
        var i = 0
        while (i < eyeIcons.size && i < layerManager.layerCount) {
            val icon = eyeIcons[i]
            val layer = layerManager.getLayer(i)
            if (icon != null && layer != null) {
                updateEyeIcon(icon, layer.isVisible)
            }
            i++
        }
    }

    /**
     * Update a single eye icon to reflect a visibility state.
     */
    private fun updateEyeIcon(icon: ImageView?, visible: Boolean) {
        if (icon == null) return
        if (visible) {
            icon.imageAlpha = 255 // Full opacity for visible
        } else {
            icon.imageAlpha = 80  // Dimmed for hidden
        }
        icon.isSelected = visible
    }

    /**
     * Clear all bindings.
     */
    fun clear() {
        eyeIcons.clear()
    }
}
