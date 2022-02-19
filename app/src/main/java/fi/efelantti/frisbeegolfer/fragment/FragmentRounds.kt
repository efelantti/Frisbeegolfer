package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.LiveDataState
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.adapter.RoundListAdapter
import fi.efelantti.frisbeegolfer.databinding.FragmentRoundsBinding
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory

class FragmentRounds : SettingsMenuFragment(), RoundListAdapter.ListItemClickListener,
    DialogConfirmDelete.OnConfirmationSelected, SearchView.OnQueryTextListener {

    private var _binding: FragmentRoundsBinding? = null
    private val binding get() = _binding!!
    private val roundViewModel: RoundViewModel by activityViewModels {
        RoundViewModelFactory((requireActivity().applicationContext as FrisbeegolferApplication).repository)
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RoundListAdapter
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.appbar_actions, menu)
            mode.title = getString(R.string.round_selected)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_edit -> {
                    editSelectedRound()
                    mode.finish() // Action picked, so close the CAB
                    true
                }
                R.id.action_delete -> {
                    val round = adapter.getSelectedRound()
                        ?: throw java.lang.IllegalArgumentException("No round was selected.")
                    deleteRound(round)
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            adapter.resetSelectedPosition()
            binding.fabStartRound.isEnabled = true
        }
    }

    private fun deleteRound(round: RoundWithCourseAndScores) {
        DialogConfirmDelete(this, round, getString(R.string.round_type)).show(
            childFragmentManager, DialogScoreAmount.TAG
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoundsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RoundListAdapter(activity as Context, this)
        recyclerView = binding.recyclerview

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        roundViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                LiveDataState.LOADING -> binding.progressBar.visibility = View.VISIBLE
                LiveDataState.SUCCESS -> binding.progressBar.visibility = View.GONE
                null -> binding.progressBar.visibility = View.GONE
            }
        }

        roundViewModel.allRounds().observe(viewLifecycleOwner) { round ->
            round?.let { rounds ->
                if (roundViewModel.state.value == LiveDataState.SUCCESS) {
                    adapter.setRounds(rounds)

                    if (rounds.count() == 0) {
                        binding.emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.fabStartRound.setOnClickListener {
            navigateToNewRound()
        }
    }

    private fun showRoundScores() {
        val round = adapter.getSelectedRound()
        actionMode?.finish()
        if (round == null) throw java.lang.IllegalArgumentException("No round was selected.")
        val holeIds = round.course.holes.map { it.holeId }.toLongArray()
        val playerIds = round.scores.distinctBy { it.player.id }.map { it.player.id }.toLongArray()
        val bundle = bundleOf(
            FragmentScorecard.ROUND_ID to round.round.dateStarted,
            FragmentScorecard.PLAYER_IDS to playerIds,
            FragmentScorecard.HOLE_IDS to holeIds,
            FragmentScorecard.READONLY to false,
            FragmentScorecard.ROUND_NAME to round.course.course.name
        )
        findNavController().navigate(R.id.action_fragmentChooseRound_to_fragmentScoreCard, bundle)
    }

    private fun editSelectedRound() {
        val round = adapter.getSelectedRound()
        actionMode?.finish()
        if (round == null) throw java.lang.IllegalArgumentException("No round was selected.")
        val holeIds = round.course.holes.map { it.holeId }.toLongArray()
        val playerIds = round.scores.distinctBy { it.player.id }.map { it.player.id }.toLongArray()
        val action =
            FragmentRoundsDirections.actionFragmentChooseRoundToFragmentGame(
                round.round.dateStarted,
                holeIds,
                playerIds,
                false,
                round.course.course.name ?: throw IllegalStateException("Course had no name.")
            )
        findNavController().navigate(action)
    }

    override fun onListItemClick(position: Int, clickedOnSame: Boolean) {
        when (actionMode) {
            null -> {
                // Start the CAB using the ActionMode.Callback defined above
                showRoundScores()
            }
            else -> {
                onListItemLongClick(position, clickedOnSame)
            }
        }
    }

    override fun onListItemLongClick(position: Int, clickedOnSame: Boolean) {
        if (clickedOnSame) {
            actionMode?.finish()
            binding.fabStartRound.isEnabled = true
        } else {
            binding.fabStartRound.isEnabled = false
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = activity?.startActionMode(actionModeCallback)
                }
            }
        }
    }

    private fun navigateToNewRound() {
        val directions =
            FragmentRoundsDirections.actionFragmentChooseRoundToFragmentChooseCourse()
        findNavController().navigate(directions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }

    override fun returnUserConfirmation(objectToDelete: Any) {
        roundViewModel.delete(objectToDelete as RoundWithCourseAndScores)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        val resultsCount = adapter.filter(query)
        actOnFilterResults(resultsCount)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val resultsCount = adapter.filter(newText)
        actOnFilterResults(resultsCount)
        return true
    }

    private fun actOnFilterResults(resultsCount: Int) {
        if (resultsCount == 0 && adapter.getAllItemsCount() > 0 && roundViewModel.state.value == LiveDataState.SUCCESS) {
            binding.recyclerview.visibility = View.GONE
            binding.noMatches.visibility = View.VISIBLE
        } else {
            binding.recyclerview.visibility = View.VISIBLE
            binding.noMatches.visibility = View.GONE
        }
    }
}