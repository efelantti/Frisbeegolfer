package fi.efelantti.frisbeegolfer.activity

import LocaleHelper.getPersistedLocale
import LocaleHelper.onAttach
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Automatically recreates the activity when the locale has changed.
 */
open class BaseActivity : AppCompatActivity() {
    private var initialLocale: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialLocale = getPersistedLocale(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(onAttach(base))
    }

    override fun onResume() {
        super.onResume()
        if (initialLocale != null && initialLocale != getPersistedLocale(this)) {
            recreate()
        }
    }
}
