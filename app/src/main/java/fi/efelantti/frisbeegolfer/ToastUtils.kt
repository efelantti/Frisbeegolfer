package fi.efelantti.frisbeegolfer

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat

class ToastUtils {
    companion object {
        fun showErrorToast(context: Context, message: CharSequence) {
            Toast.makeText(
                context, HtmlCompat.fromHtml(
                    "<font color='" + ContextCompat.getColor(
                        context,
                        R.color.colorErrorMessage
                    ) + "' ><b>" + message + "</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY
                ), Toast.LENGTH_LONG
            ).show()
        }
    }
}