package fi.efelantti.frisbeegolfer.activity

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import fi.efelantti.frisbeegolfer.EmptyRecyclerView
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.RoundListAdapter
import fi.efelantti.frisbeegolfer.fragment.FragmentScore
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import java.time.OffsetDateTime


class ActivityContinueRound : AppCompatActivity(), RoundListAdapter.ListItemClickListener {

    private val TAG = "ActivityContinueRound"
    private val scoreTag = "FragmentScore"
    private val roundViewModel: RoundViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_choose_round)

        displayChooseRoundFragment()
    }

    private fun displayChooseRoundFragment()
    {
        supportActionBar?.title = getString(R.string.continue_round_activity_title)
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
}