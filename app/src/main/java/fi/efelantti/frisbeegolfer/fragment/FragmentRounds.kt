package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import fi.efelantti.frisbeegolfer.EmptyRecyclerView
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.RoundListAdapter
import fi.efelantti.frisbeegolfer.databinding.FragmentRoundsBinding
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory

// TODO - Allow to filter rounds (for example, by player or course)
class FragmentRounds : Fragment(), RoundListAdapter.ListItemClickListener,
    DialogConfirmDelete.OnConfirmationSelected {

    private var _binding: FragmentRoundsBinding? = null
    private val binding get() = _binding!!
    private val roundViewModel: RoundViewModel by activityViewModels {
        RoundViewModelFactory((requireActivity().applicationContext as FrisbeegolferApplication).repository)
    }
    private lateinit var recyclerView: EmptyRecyclerView
    private lateinit var adapter: RoundListAdapter
    private lateinit var emptyView: TextView
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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RoundListAdapter(activity as Context, this)
        recyclerView = binding.recyclerviewContinueRound
        emptyView = binding.emptyViewRounds

        recyclerView.setEmptyView(emptyView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        roundViewModel.allRounds.observe(viewLifecycleOwner, { round ->
            round?.let { adapter.setRounds(it) }
        })

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
            FragmentScorecard.READONLY to false
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
                false
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
}