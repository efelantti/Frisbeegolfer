package fi.efelantti.frisbeegolfer.fragment

import LocaleHelper
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.ThemeProvider


// TODO - https://github.com/akexorcist/Localization/issues/112
class FragmentSettings : PreferenceFragmentCompat() {

    companion object {
        const val KEY_PREF_LANGUAGE = "pref_key_language"
    }

    private val themeProvider by lazy { ThemeProvider(requireContext()) }
    private val themePreference by lazy {
        findPreference<ListPreference>(getString(R.string.theme_preferences_key))
    }
    private val languagePreference by lazy {
        findPreference<ListPreference>(KEY_PREF_LANGUAGE)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        setThemePreference()
    }

    private fun setLanguagePreference() {
        languagePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue is String) {
                    LocaleHelper.setLocale(
                        requireContext(),
                        getDefaultSharedPreferences(requireContext()).getString(newValue, "")
                    )
                    // Setting theme seems to set language instantly... maybe that can be abused
                    // AppCompatDelegate.setDefaultNightMode()
                    requireActivity().recreate(); // necessary here because this Activity is currently running and thus a recreate() in onResume() would be too late
                }
                true
            }
    }

    private fun setThemePreference() {
        themePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue is String) {
                    val theme = themeProvider.getTheme(newValue)
                    AppCompatDelegate.setDefaultNightMode(theme)
                }
                true
            }
        themePreference?.summaryProvider = getThemeSummaryProvider()
    }

    private fun getThemeSummaryProvider() =
        Preference.SummaryProvider<ListPreference> { preference ->
            themeProvider.getThemeDescriptionForPreference(preference.value)
        }
}