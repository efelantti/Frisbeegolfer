package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import fi.efelantti.frisbeegolfer.NewCourseAction
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.FragmentChooseCourse
import fi.efelantti.frisbeegolfer.fragment.FragmentChoosePlayers
import fi.efelantti.frisbeegolfer.fragment.FragmentNewCourse
import fi.efelantti.frisbeegolfer.fragment.FragmentScore
import fi.efelantti.frisbeegolfer.model.*
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import kotlin.properties.Delegates


class ActivityRound : AppCompatActivity(), FragmentChooseCourse.FragmentChooseCourseListener, FragmentChoosePlayers.FragmentChoosePlayersListener {

    private val TAG = "ActivityRound"
    private val chooseCourseTag = "FragmentChooseCourse"
    private val choosePlayersTag = "FragmentChoosePlayers"
    private val scoreTag = "FragmentScore"
    private var selectedCourseId by Delegates.notNull<Long>()
    private lateinit var selectedPlayerIds: List<Long>
    private val roundViewModel: RoundViewModel by viewModels()
    private val courseViewModel: CourseViewModel by viewModels()

    // TODO - https://guides.codepath.com/android/creating-and-using-fragments
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round)
        if (savedInstanceState == null) {
            displayChooseCourseFragment()
        }
    }

    private fun displayChooseCourseFragment()
    {
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
        var roundId = addRoundToDatabase(selectedCourseId, selectedPlayerIds)
        displayScoreFragment(roundId)
    }

    private fun displayScoreFragment(roundId: OffsetDateTime)
    {
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
    private fun addRoundToDatabase(selectedCourseId: Long, selectedPlayerIds: List<Long>): OffsetDateTime
    {
        var roundId = now()
        courseViewModel.getCourseWithHolesById(selectedCourseId).observe(this, Observer {
            var course = it
            if(course == null) throw IllegalArgumentException("No course found with id ${selectedCourseId}.")
            var round = Round(dateStarted=roundId, courseId=selectedCourseId)
            roundViewModel.insert(round)
            for(hole in course.holes)
            {
                for(playerId in selectedPlayerIds)
                {
                    var score = Score(parentRoundId = roundId, holeId = hole.holeId, playerId = playerId, result = 0)
                    roundViewModel.insert(score)
                }
            }
            })
        return roundId
    }
}