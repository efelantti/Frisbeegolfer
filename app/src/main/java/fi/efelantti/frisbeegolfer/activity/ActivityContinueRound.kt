package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.FragmentChooseRound
import fi.efelantti.frisbeegolfer.fragment.FragmentScore
import java.time.OffsetDateTime


class ActivityContinueRound : AppCompatActivity(), FragmentChooseRound.FragmentChooseRoundListener {

    private val TAG = "ActivityContinueRound"
    private val scoreTag = "FragmentScore"
    private val roundTag = "FragmentChooseRound"
    /*private val roundViewModel: RoundViewModel by viewModels{
        RoundViewModelFactory((applicationContext as FrisbeegolferApplication).repository)
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_continue_round)

        displayChooseRoundFragment()
    }

    private fun displayChooseRoundFragment()
    {
        supportActionBar?.title = getString(R.string.continue_round_activity_title)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.continue_round_fragment_container_view, FragmentChooseRound(), roundTag)
        }
    }

    private fun displayScoreFragment(roundId: OffsetDateTime) {
        supportActionBar?.title = "Score"
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            val fragmentScore: FragmentScore =
                FragmentScore.newInstance(roundId)
            replace(R.id.continue_round_fragment_container_view, fragmentScore, scoreTag)
        }
    }

    override fun onRoundSelected(chosenRoundId: OffsetDateTime) {
        displayScoreFragment(chosenRoundId)
    }
}