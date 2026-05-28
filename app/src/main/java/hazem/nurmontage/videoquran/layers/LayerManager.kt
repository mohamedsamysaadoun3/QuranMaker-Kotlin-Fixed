package hazem.nurmontage.videoquran.layers

import java.util.Collections

/**
 * Manages layer ordering, visibility, locking, opacity, and blend modes.
 */
class LayerManager {

    private val layers: MutableList<Layer> = ArrayList()
    private var listener: OnLayerChangeListener? = null

    interface OnLayerChangeListener {
        fun onLayerAdded(layer: Layer, index: Int)
        fun onLayerRemoved(layer: Layer, index: Int)
        fun onLayerMoved(layer: Layer, fromIndex: Int, toIndex: Int)
        fun onLayerVisibilityChanged(layer: Layer, visible: Boolean)
        fun onLayerOpacityChanged(layer: Layer, opacity: Float)
        fun onLayerBlendModeChanged(layer: Layer, blendMode: BlendMode)
        fun onLayerLockChanged(layer: Layer, locked: Boolean)
    }

    /**
     * Represents a single layer in the composition.
     */
    class Layer(
        val id: String,
        name: String
    ) {
        var name: String = name
            private set

        var isVisible: Boolean = true
            private set

        var isLocked: Boolean = false
            private set

        var opacity: Float = 1.0f
            private set

        var blendMode: BlendMode = BlendMode.NORMAL
            private set

        var zIndex: Int = 0
            private set

        fun setName(name: String) {
            this.name = name
        }

        fun setVisible(visible: Boolean) {
            this.isVisible = visible
        }

        fun setLocked(locked: Boolean) {
            this.isLocked = locked
        }

        fun setOpacity(opacity: Float) {
            this.opacity = opacity.coerceIn(0f, 1f)
        }

        fun setBlendMode(blendMode: BlendMode) {
            this.blendMode = blendMode
        }

        fun setZIndex(zIndex: Int) {
            this.zIndex = zIndex
        }
    }

    fun setOnLayerChangeListener(listener: OnLayerChangeListener?) {
        this.listener = listener
    }

    /**
     * Add a new layer at the top of the stack.
     */
    fun addLayer(layer: Layer) {
        layer.setZIndex(layers.size)
        layers.add(layer)
        listener?.onLayerAdded(layer, layers.size - 1)
    }

    /**
     * Remove a layer by index.
     */
    fun removeLayer(index: Int) {
        if (index < 0 || index >= layers.size) return
        val removed = layers.removeAt(index)
        reindexLayers()
        listener?.onLayerRemoved(removed, index)
    }

    /**
     * Remove a layer by reference.
     */
    fun removeLayer(layer: Layer) {
        val index = layers.indexOf(layer)
        if (index >= 0) {
            removeLayer(index)
        }
    }

    /**
     * Move a layer from one position to another.
     */
    fun moveLayer(fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0 || fromIndex >= layers.size) return
        if (toIndex < 0 || toIndex >= layers.size) return
        if (fromIndex == toIndex) return

        val layer = layers.removeAt(fromIndex)
        layers.add(toIndex, layer)
        reindexLayers()

        listener?.onLayerMoved(layer, fromIndex, toIndex)
    }

    /**
     * Toggle the visibility of a layer.
     */
    fun toggleVisibility(index: Int) {
        if (index < 0 || index >= layers.size) return
        val layer = layers[index]
        layer.setVisible(!layer.isVisible)
        listener?.onLayerVisibilityChanged(layer, layer.isVisible)
    }

    /**
     * Set the opacity of a layer.
     */
    fun setOpacity(index: Int, opacity: Float) {
        if (index < 0 || index >= layers.size) return
        val layer = layers[index]
        layer.setOpacity(opacity)
        listener?.onLayerOpacityChanged(layer, opacity)
    }

    /**
     * Set the blend mode of a layer.
     */
    fun setBlendMode(index: Int, blendMode: BlendMode) {
        if (index < 0 || index >= layers.size) return
        val layer = layers[index]
        layer.setBlendMode(blendMode)
        listener?.onLayerBlendModeChanged(layer, blendMode)
    }

    /**
     * Toggle the lock state of a layer.
     */
    fun toggleLock(index: Int) {
        if (index < 0 || index >= layers.size) return
        val layer = layers[index]
        layer.setLocked(!layer.isLocked)
        listener?.onLayerLockChanged(layer, layer.isLocked)
    }

    /**
     * Get an unmodifiable list of all layers.
     */
    fun getLayers(): List<Layer> = Collections.unmodifiableList(layers)

    /**
     * Get a layer by index.
     */
    fun getLayer(index: Int): Layer? {
        if (index < 0 || index >= layers.size) return null
        return layers[index]
    }

    /**
     * Get the number of layers.
     */
    val layerCount: Int
        get() = layers.size

    /**
     * Find the index of a layer by its ID.
     */
    fun findLayerIndex(id: String): Int {
        for (i in layers.indices) {
            if (layers[i].id == id) {
                return i
            }
        }
        return -1
    }

    private fun reindexLayers() {
        for (i in layers.indices) {
            layers[i].setZIndex(i)
        }
    }
}
