package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.*
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
    FragmentNavigationScreen.FragmentNavigationScreenListener,
    FragmentChooseRound.FragmentChooseRoundListener,
    FragmentChooseCourse.FragmentChooseCourseListener,
    FragmentChoosePlayers.FragmentChoosePlayersListener {

    private val navigationScreenTag = "FragmentNavigationScreen"
    private val chooseCourseTag = "FragmentChooseCourse"
    private val choosePlayersTag = "FragmentChoosePlayers"
    private val coursesTag = "FragmentCourses"
    private val playersTag = "FragmentPlayers"
    private val continueRoundTag = "FragmentContinueRound"
    private val scoreTag = "FragmentScore"

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
        setContentView(R.layout.activity_main_refactored)
        if (savedInstanceState == null) {
            displayNavigationScreenFragment()
        }
    }

    private fun displayNavigationScreenFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(
                R.id.fragment_container_view,
                FragmentNavigationScreen(),
                navigationScreenTag
            )
        }
    }

    override fun navigateToNewRound() {
        displayChooseCourseFragment()
    }

    private fun displayChooseCourseFragment() {
        supportActionBar?.title = getString(R.string.choose_a_course_title)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, FragmentChooseCourse(), chooseCourseTag)
        }
    }

    override fun onCourseSelected(chosenCourseId: Long) {
        selectedCourseId = chosenCourseId
        displayChoosePlayersFragment()
    }

    private fun displayChoosePlayersFragment() {
        supportActionBar?.title = getString(R.string.choose_players_title)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, FragmentChoosePlayers(), choosePlayersTag)
        }
    }

    override fun onPlayersSelected(chosenPlayerIds: List<Long>) {
        selectedPlayerIds = chosenPlayerIds
        val roundId = addRoundToDatabase(selectedCourseId, selectedPlayerIds)
        displayScoreFragment(roundId)
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

    override fun navigateToContinueRound()
    {
        supportActionBar?.title = getString(R.string.continue_round_activity_title)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, FragmentChooseRound(), continueRoundTag)
        }
    }

    override fun onRoundSelected(chosenRoundId: OffsetDateTime) {
        displayScoreFragment(chosenRoundId)
    }

    private fun displayScoreFragment(roundId: OffsetDateTime) {
        supportActionBar?.title = "Score"
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            val fragmentScore: FragmentScore =
                FragmentScore.newInstance(roundId)
            replace(R.id.fragment_container_view, fragmentScore, scoreTag)
        }
    }

    override fun navigateToCourses() {
        //supportActionBar?.title = getString(R.string.courses_activity_title)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, FragmentCourses(), coursesTag)
        }
    }

    override fun navigatePlayers() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, FragmentPlayers(), playersTag)
        }
    }
}