import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import fi.efelantti.frisbeegolfer.fragment.FragmentSettings
import java.util.*

/**
 * Manages setting of the app's locale.
 */
object LocaleHelper {
    fun onAttach(context: Context): Context {
        val locale = getPersistedLocale(context)
        return setLocale(context, locale)
    }

    fun getPersistedLocale(context: Context): String? {
        val preferences = getDefaultSharedPreferences(context)
        return preferences.getString(FragmentSettings.KEY_PREF_LANGUAGE, "")
    }

    /**
     * Set the app's locale to the one specified by the given String.
     *
     * @param context
     * @param localeSpec a locale specification as used for Android resources (NOTE: does not
     * support country and variant codes so far); the special string "system" sets
     * the locale to the locale specified in system settings
     * @return
     */
    fun setLocale(context: Context, localeSpec: String?): Context {
        val locale: Locale = if (localeSpec == "system") {
            Resources.getSystem().configuration.locales[0]
        } else {
            Locale(localeSpec)
        }
        Locale.setDefault(locale)
        return updateResources(context, locale)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}