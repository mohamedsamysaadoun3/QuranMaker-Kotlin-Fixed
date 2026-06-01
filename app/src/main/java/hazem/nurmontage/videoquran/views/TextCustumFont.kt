package hazem.nurmontage.videoquran.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

open class TextCustumFont : AppCompatTextView {

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        val typeface = TypefaceCache.get(resources.assets, FONT_PATH)
        typeface?.let { setTypeface(it) }
    }

    companion object {
        private const val FONT_PATH = "fonts/ReadexPro_Medium.ttf"
    }
}
