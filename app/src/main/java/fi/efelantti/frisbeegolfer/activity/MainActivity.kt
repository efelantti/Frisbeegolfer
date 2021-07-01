package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.ActivityMainWithNavigationBinding

// TODO - Set Rounds fragment as home
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainWithNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainWithNavigationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragmentChooseRound,
                R.id.fragmentCourses,
                R.id.fragmentPlayers
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.fragmentChooseCourse) {
                binding.bottomNav.visibility = View.GONE
            } else if (destination.id == R.id.fragmentChoosePlayers) {
                binding.bottomNav.visibility = View.GONE
            } else if (destination.id == R.id.fragmentGame) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_content)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}