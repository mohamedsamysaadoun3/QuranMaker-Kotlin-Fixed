package hazem.nurmontage.videoquran.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

/**
 * Custom Button that applies the ReadexPro_Medium font from assets.
 *
 * Used in billing/subscription screens for consistent typography.
 *
 * Originally: ButtonCustumFontBilling.java (34 lines)
 */
class ButtonCustumFontBilling @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var typeface: Typeface? = null

    init {
        if (this.typeface == null) {
            val tf = Typeface.createFromAsset(resources.assets, "fonts/ReadexPro_Medium.ttf")
            this.typeface = tf
            setTypeface(tf)
        }
    }
}
