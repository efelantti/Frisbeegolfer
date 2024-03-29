package fi.efelantti.frisbeegolfer.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import fi.efelantti.frisbeegolfer.LocaleHelper.setLocale
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.ThemeProvider

class FragmentSettings : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val KEY_PREF_LANGUAGE = "pref_key_language"
        const val KEY_PREF_THEME = "pref_key_theme"
    }

    private val themeProvider by lazy { ThemeProvider(requireContext()) }
    private val themePreference by lazy {
        findPreference<ListPreference>(KEY_PREF_THEME)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        // No options menu is needed in the settings fragment.
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        setThemePreferenceSummaryProvider()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            KEY_PREF_LANGUAGE -> {
                setLocale(
                    requireContext(), PreferenceManager.getDefaultSharedPreferences(
                        requireContext()
                    ).getString(key, "")
                )
                requireActivity().recreate() // necessary here because this Activity is currently running and thus a recreate() in onResume() would be too late
            }
            KEY_PREF_THEME -> {
                val newValue =
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .getString(key, "")
                if (newValue != null) {
                    val theme = themeProvider.getTheme(newValue)
                    AppCompatDelegate.setDefaultNightMode(theme)
                }
            }
        }
    }

    private fun setThemePreferenceSummaryProvider() {
        themePreference?.summaryProvider = getThemeSummaryProvider()
    }

    private fun getThemeSummaryProvider() =
        Preference.SummaryProvider<ListPreference> { preference ->
            themeProvider.getThemeDescriptionForPreference(preference.value)
        }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }
}