package hazem.nurmontage.videoquran.views

import android.content.res.AssetManager
import android.graphics.Typeface

internal object TypefaceCache {

    private val cache = mutableMapOf<String, Typeface>()

    fun get(assets: AssetManager, fontPath: String): Typeface {
        return cache.getOrPut(fontPath) {
            try {
                Typeface.createFromAsset(assets, fontPath)
            } catch (_: Exception) {
                Typeface.DEFAULT
            }
        }
    }

    fun clear() {
        cache.clear()
    }

    val size: Int get() = cache.size
}
