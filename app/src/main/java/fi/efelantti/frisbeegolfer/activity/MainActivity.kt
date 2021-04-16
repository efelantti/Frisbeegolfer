package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.FragmentChooseCourse
import fi.efelantti.frisbeegolfer.fragment.FragmentChoosePlayers
import fi.efelantti.frisbeegolfer.fragment.FragmentChooseRound
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.Score
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory
import java.time.OffsetDateTime
import kotlin.properties.Delegates

// TODO - Find out why the action bar is not renamed in the fragments
class MainActivity : AppCompatActivity(),
    FragmentChooseRound.FragmentChooseRoundListener,
    FragmentChooseCourse.FragmentChooseCourseListener,
    FragmentChoosePlayers.FragmentChoosePlayersListener {

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


    override fun onCourseSelected(chosenCourseId: Long) {
        selectedCourseId = chosenCourseId
        //displayChoosePlayersFragment()
    }

    override fun onPlayersSelected(chosenPlayerIds: List<Long>) {
        selectedPlayerIds = chosenPlayerIds
        val roundId = addRoundToDatabase(selectedCourseId, selectedPlayerIds)
        //displayScoreFragment(roundId)
    }

    /**
     * Adds an entry to the database for the round. Creates all the necessary scores, that are
     * then later to be edited when playing the round.
     */
    private fun addRoundToDatabase(
        selectedCourseId: Long,
        selectedPlayerIds: List<Long>
    ): OffsetDateTime {
        val roundId = OffsetDateTime.now()
        courseViewModel.getCourseWithHolesById(selectedCourseId).observe(this, Observer<CourseWithHoles> {
            val course =
                it ?: throw IllegalArgumentException("No course found with id ${selectedCourseId}.")
            val round = Round(dateStarted = roundId, courseId = selectedCourseId)
            roundViewModel.insert(round)
            for (hole in course.holes) {
                for (playerId in selectedPlayerIds) {
                    val score = Score(
                        parentRoundId = roundId,
                        holeId = hole.holeId,
                        playerId = playerId,
                        result = 0
                    )
                    roundViewModel.insert(score)
                }
            }
        })
        return roundId
    }

    override fun onRoundSelected(chosenRoundId: OffsetDateTime) {
        //displayScoreFragment(chosenRoundId)
    }

    /*private fun displayScoreFragment(roundId: OffsetDateTime) {
        supportActionBar?.title = "Score"
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            val fragmentScore: FragmentScore =
                FragmentScore.newInstance(roundId)
            replace(R.id.fragment_container_view, fragmentScore, scoreTag)
        }
    }*/
}