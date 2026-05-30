package hazem.nurmontage.videoquran.views

import android.content.Context
import android.util.AttributeSet

/**
 * Billing-specific button that extends [ButtonCustumFont].
 *
 * Originally used for the subscription/purchase flow in the Pro version screen.
 * Billing functionality has been removed from this build, but the class
 * is preserved as a simple alias to satisfy layout XML references.
 *
 * @see ButtonCustumFont
 */
class ButtonCustumFontBilling : ButtonCustumFont {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)
}
