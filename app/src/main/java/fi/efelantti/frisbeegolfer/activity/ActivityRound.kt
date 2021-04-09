package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.FragmentChooseCourse
import fi.efelantti.frisbeegolfer.fragment.FragmentChoosePlayers
import fi.efelantti.frisbeegolfer.fragment.FragmentScore
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.Score
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModelFactory
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory
import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import kotlin.properties.Delegates


class ActivityRound : AppCompatActivity(), FragmentChooseCourse.FragmentChooseCourseListener,
    FragmentChoosePlayers.FragmentChoosePlayersListener {

    private val TAG = "ActivityRound"
    private val chooseCourseTag = "FragmentChooseCourse"
    private val choosePlayersTag = "FragmentChoosePlayers"
    private val scoreTag = "FragmentScore"
    private var selectedCourseId by Delegates.notNull<Long>()
    private lateinit var selectedPlayerIds: List<Long>
    private val roundViewModel: RoundViewModel by viewModels {
        RoundViewModelFactory((applicationContext as FrisbeegolferApplication).repository)
    }
    private val courseViewModel: CourseViewModel by viewModels {
        CourseViewModelFactory((applicationContext as FrisbeegolferApplication).repository)
    }

    // TODO - https://guides.codepath.com/android/creating-and-using-fragments
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round)
        if (savedInstanceState == null) {
            displayChooseCourseFragment()
        }
    }

    private fun displayChooseCourseFragment() {
        supportActionBar?.title = getString(R.string.choose_a_course_title)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.round_fragment_container_view, FragmentChooseCourse(), chooseCourseTag)
        }
    }

    private fun displayChoosePlayersFragment() {
        supportActionBar?.title = getString(R.string.choose_players_title)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.round_fragment_container_view, FragmentChoosePlayers(), choosePlayersTag)
        }
    }

    override fun onCourseSelected(chosenCourseId: Long) {
        selectedCourseId = chosenCourseId
        displayChoosePlayersFragment()
    }

    override fun onPlayersSelected(chosenPlayerIds: List<Long>) {
        selectedPlayerIds = chosenPlayerIds
        val roundId = addRoundToDatabase(selectedCourseId, selectedPlayerIds)
        displayScoreFragment(roundId)
    }

    private fun displayScoreFragment(roundId: OffsetDateTime) {
        supportActionBar?.title = "Score"
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            val fragmentScore: FragmentScore =
                FragmentScore.newInstance(roundId)
            replace(R.id.round_fragment_container_view, fragmentScore, scoreTag)
        }
    }

    /**
     * Adds an entry to the database for the round. Creates all the necessary scores, that are
     * then later to be edited when playing the round.
     */
    private fun addRoundToDatabase(
        selectedCourseId: Long,
        selectedPlayerIds: List<Long>
    ): OffsetDateTime {
        val roundId = now()
        courseViewModel.getCourseWithHolesById(selectedCourseId).observe(this, Observer {
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
}