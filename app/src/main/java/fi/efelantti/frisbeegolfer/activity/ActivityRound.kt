package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.FragmentChooseCourse
import fi.efelantti.frisbeegolfer.fragment.FragmentChoosePlayers
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithScores
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import java.time.OffsetDateTime.now
import kotlin.properties.Delegates


class ActivityRound : AppCompatActivity(), FragmentChooseCourse.FragmentChooseCourseListener, FragmentChoosePlayers.FragmentChoosePlayersListener {

    private val TAG = "ActivityRound"
    private val chooseCourseTag = "FragmentChooseCourse"
    private val choosePlayersTag = "FragmentChoosePlayers"
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
        // TODO - Score the holes by a new fragment.
    }

    private fun displayChooseCourseFragment()
    {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.round_fragment_container_view, FragmentChooseCourse(), chooseCourseTag)
        }
    }

    private fun displayChoosePlayersFragment() {
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
        addRoundToDatabase(selectedCourseId, selectedPlayerIds)
    }

    /**
     * Adds an entry to the database for the round. Creates all the necessary scores, that are
     * then later to be edited when playing the round.
     */
    private fun addRoundToDatabase(selectedCourseId: Long, selectedPlayerIds: List<Long>)
    {
        // TODO - Finish adding to database.
        var course = courseViewModel.getCourseWithHolesById(selectedCourseId).value
        if(course == null) throw IllegalArgumentException("No course found with id ${selectedCourseId}.")
        var round = Round()
        var scores= emptyList<ScoreWithPlayerAndHole>()
        round.dateStarted = now()
        var roundWithScores: RoundWithScores = RoundWithScores(round, scores)
        roundViewModel.insert(roundWithScores)
    }
}