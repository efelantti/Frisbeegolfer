package fi.efelantti.frisbeegolfer

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.text.HtmlCompat

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

class ToastUtils {
    companion object {
        fun showErrorToast(context: Context, message: CharSequence) {
            Toast.makeText(
                context, HtmlCompat.fromHtml(
                    "<font color='" + context.getColorFromAttr(R.attr.errorTextColor) + "' ><b>" + message + "</b></font>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                ), Toast.LENGTH_LONG
            ).show()
        }
    }
}