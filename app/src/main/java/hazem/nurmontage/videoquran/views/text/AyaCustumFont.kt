package hazem.nurmontage.videoquran.views.text

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class AyaCustumFont : AppCompatTextView {

    private var typeface: Typeface? = null

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(resources.assets, FONT_PATH)
            typeface?.let { setTypeface(it) }
        }
    }

    companion object {
        private const val FONT_PATH = "fonts/arabic/خط حفص.ttf"
    }
}
