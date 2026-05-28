package hazem.nurmontage.videoquran.views

import android.content.Context
import android.util.AttributeSet

/**
 * Alias class for [hazem.nurmontage.videoquran.views.text.TextCustumFont].
 *
 * This class exists at the `views` package path because some layout XML files
 * reference `hazem.nurmontage.videoquran.views.TextCustumFont` directly.
 * The canonical implementation lives in the `views.text` subpackage.
 *
 * Without this alias, layout inflation of `row_gallery.xml` and
 * `row_gallery_select.xml` would crash with a ClassNotFoundException
 * since the XML references `views.TextCustumFont` but the actual class
 * is at `views.text.TextCustumFont`.
 *
 * @see hazem.nurmontage.videoquran.views.text.TextCustumFont
 */
class TextCustumFont : hazem.nurmontage.videoquran.views.text.TextCustumFont {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)
}
