package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.FragmentChooseCourse

class ActivityRound : AppCompatActivity() {

    private val TAG = "ActivityRound"

    // TODO - https://guides.codepath.com/android/creating-and-using-fragments
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.round_fragment_container_view, FragmentChooseCourse())
            }
        }
            // TODO - Choose a course and players by using separate fragments.
        // TODO - Score the holes by a new fragment.

        /*var round = Round()
var scores = emptyList<ScoreWithPlayerAndHole>()
round.dateStarted = now()
var roundWithScores: RoundWithScores = RoundWithScores(round, scores)
roundViewModel.insert(roundWithScores)*/
    }
}