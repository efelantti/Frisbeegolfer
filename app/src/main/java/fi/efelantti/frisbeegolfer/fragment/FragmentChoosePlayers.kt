package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.EmptyRecyclerView
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.PlayerListAdapterMultiSelect
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.FragmentChoosePlayersBinding
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.*
import java.time.OffsetDateTime


class FragmentChoosePlayers : Fragment(), PlayerListAdapterMultiSelect.ListItemClickListener {

    private var _binding: FragmentChoosePlayersBinding? = null
    private val binding get() = _binding!!
    private val playerViewModel: PlayerViewModel by activityViewModels {
        PlayerViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private val roundViewModel: RoundViewModel by activityViewModels {
        RoundViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private val courseViewModel: CourseViewModel by activityViewModels {
        CourseViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private val args: FragmentChoosePlayersArgs by navArgs()
    private lateinit var adapter: PlayerListAdapterMultiSelect
    private var actionMode: ActionMode? = null
    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var emptyView: TextView
    private lateinit var fab: FloatingActionButton

    override fun onPrepareOptionsMenu(menu: Menu) {
        val menuItemsToHide = listOf(
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

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_choose_course_or_players, menu)
            mode.title = getString(R.string.player_selected)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return false
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            fab.isEnabled = false
            adapter.resetSelectedPlayers()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChoosePlayersBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlayerListAdapterMultiSelect(activity as Context, this)
        recyclerView = binding.recyclerviewChoosePlayers
        emptyView = binding.emptyViewChoosePlayers
        recyclerView.setEmptyView(emptyView)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        playerViewModel.allPlayers.observe(viewLifecycleOwner, { list ->
            list?.let { players ->
                val sortedPlayers = players.sortedBy { it.name }
                adapter.setPlayers(sortedPlayers)
            }
        })

        fab = binding.fabChoosePlayers
        fab.setOnClickListener {
            val players = chooseSelectedPlayers()
            val playerIds = players.sortedBy { it.name }.map { it.id }
            val courseId = args.courseId
            courseViewModel.getCourseWithHolesById(courseId).observe(viewLifecycleOwner, { course ->
                val roundId = OffsetDateTime.now()
                val holeIds = course.holes.sortedBy { it.holeNumber }.map { it.holeId }
                val roundName =
                    course.course.name ?: throw IllegalStateException("Course has no name!")
                roundViewModel.addRoundToDatabase(course, playerIds, roundId)
                navigateToGameFragment(
                    roundId,
                    holeIds.toLongArray(),
                    playerIds.toLongArray(),
                    roundName
                )
            })
        }
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
            fab.isEnabled = false
        } else {
            fab.isEnabled = true
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = activity?.startActionMode(actionModeCallback)
                }
            }
            val selectedPlayersCount = adapter.selectedIndeces.count()
            val title = resources.getQuantityString(
                R.plurals.numberPlayersSelected,
                selectedPlayersCount,
                selectedPlayersCount
            )
            actionMode?.title = title
        }
    }

    private fun chooseSelectedPlayers(): List<Player> {
        val players = adapter.getSelectedPlayers()
        actionMode?.finish()
        return players
    }

    private fun navigateToGameFragment(
        roundId: OffsetDateTime,
        holeIds: LongArray,
        playerIds: LongArray,
        roundName: String
    ) {
        val action =
            FragmentChoosePlayersDirections.actionFragmentChoosePlayersToFragmentGame(
                roundId,
                holeIds,
                playerIds,
                false,
                roundName
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}