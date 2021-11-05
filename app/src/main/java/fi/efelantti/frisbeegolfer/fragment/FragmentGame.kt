package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.activity.MainActivity
import fi.efelantti.frisbeegolfer.databinding.FragmentGameBinding
import fi.efelantti.frisbeegolfer.observeOnce
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory
import java.time.OffsetDateTime

class FragmentGame : Fragment() {
    private lateinit var gameFragmentAdapter: GameFragmentAdapter
    private lateinit var viewPager: ViewPager2
    private val args: FragmentGameArgs by navArgs()
    private var _binding: FragmentGameBinding? = null
    private val roundViewModel: RoundViewModel by activityViewModels {
        RoundViewModelFactory((requireActivity().applicationContext as FrisbeegolferApplication).repository)
    }
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        roundViewModel.getRoundWithRoundId(args.roundId).observeOnce(viewLifecycleOwner) { round ->
            (requireActivity() as MainActivity).supportActionBar?.title = round.course.course.name
        }

        gameFragmentAdapter = GameFragmentAdapter(
            this,
            roundId = args.roundId,
            holeIds = args.holeIds,
            playerIds = args.playerIds,
            readOnly = args.shouldOpenScorecard
        )
        viewPager = binding.pager

        viewPager.adapter = gameFragmentAdapter
        val tabLayout: TabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.fragment_score_tab_text)
                }
                1 -> {
                    if (args.shouldOpenScorecard) viewPager.setCurrentItem(1, false)
                    tab.text = getString(R.string.fragment_scorecard_tab_text)
                }
                else -> throw IndexOutOfBoundsException("No title found for index ($position).")
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class GameFragmentAdapter(
    fragment: Fragment,
    val roundId: OffsetDateTime,
    val playerIds: LongArray,
    val holeIds: LongArray,
    val readOnly: Boolean
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentScore.newInstance(roundId, playerIds, holeIds, readOnly)
            1 -> FragmentScorecard.newInstance(roundId, playerIds, holeIds, readOnly)
            else -> throw IndexOutOfBoundsException("No fragment found for index value $position.")
        }
    }
}