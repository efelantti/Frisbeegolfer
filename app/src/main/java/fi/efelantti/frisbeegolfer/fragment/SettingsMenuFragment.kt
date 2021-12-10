package fi.efelantti.frisbeegolfer.fragment

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.ui.onNavDestinationSelected
import fi.efelantti.frisbeegolfer.R


open class SettingsMenuFragment : Fragment() {

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.fragmentSettings -> {
                val navController =
                    Navigation.findNavController(requireActivity(), R.id.main_content)
                item.onNavDestinationSelected(navController)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}