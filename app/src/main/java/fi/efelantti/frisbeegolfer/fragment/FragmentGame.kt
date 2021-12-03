package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.activity.MainActivity
import fi.efelantti.frisbeegolfer.databinding.FragmentGameBinding
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory
import java.time.OffsetDateTime

class FragmentGame : Fragment() {
    private lateinit var gameFragmentAdapter: GameFragmentAdapter
    private lateinit var viewPager: ViewPager2
    private val args: FragmentGameArgs by navArgs()
    private var _binding: FragmentGameBinding? = null
    private val roundViewModel: RoundViewModel by viewModels {
        RoundViewModelFactory((requireActivity().applicationContext as FrisbeegolferApplication).repository)
    }
    private val binding get() = _binding!!

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_fragment_game, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val menuItemsToHide = mutableListOf(
            R.id.action_import_data,
            R.id.action_export_data,
            R.id.action_import_data_from_discscores
        )
        menuItemsToHide.forEach {
            val item = menu.findItem(it)
            if (item != null) item.isVisible = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).supportActionBar?.title = args.roundName

        gameFragmentAdapter = GameFragmentAdapter(
            this,
            roundId = args.roundId,
            holeIds = args.holeIds,
            playerIds = args.playerIds,
            readOnly = args.shouldOpenScorecard,
            roundName = args.roundName
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
    val readOnly: Boolean,
    val roundName: String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentScore.newInstance(roundId, playerIds, holeIds, readOnly)
            1 -> FragmentScorecard.newInstance(roundId, playerIds, holeIds, readOnly, roundName)
            else -> throw IndexOutOfBoundsException("No fragment found for index value $position.")
        }
    }
}