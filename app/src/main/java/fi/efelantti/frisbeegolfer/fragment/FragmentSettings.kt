package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import fi.efelantti.frisbeegolfer.R

class FragmentSettings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}