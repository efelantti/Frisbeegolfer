package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var selectedCourseId by Delegates.notNull<Long>()
    private lateinit var selectedPlayerIds: List<Long>
    private val roundViewModel: RoundViewModel by viewModels {
        RoundViewModelFactory((applicationContext as FrisbeegolferApplication).repository)
    }
    private val courseViewModel: CourseViewModel by viewModels {
        CourseViewModelFactory((applicationContext as FrisbeegolferApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_with_navigation)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_content)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}