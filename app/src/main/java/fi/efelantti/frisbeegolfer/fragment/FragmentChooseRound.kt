package fi.efelantti.frisbeegolfer.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import fi.efelantti.frisbeegolfer.EmptyRecyclerView
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.RoundListAdapter
import fi.efelantti.frisbeegolfer.databinding.FragmentChooseRoundBinding
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory

class FragmentChooseRound : Fragment(), RoundListAdapter.ListItemClickListener {

    private var _binding: FragmentChooseRoundBinding? = null
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
                    chooseSelectedRound()
                    mode.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            adapter.resetSelectedPosition()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseRoundBinding.inflate(inflater, container, false)
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

        roundViewModel.allRounds.observe(viewLifecycleOwner, { round ->
            round?.let { adapter.setRounds(it) }
        })
    }

    private fun chooseSelectedRound() {
        val round = adapter.getSelectedRound()
        actionMode?.finish()
        if (round == null) throw java.lang.IllegalArgumentException("No round was selected.")
        val holeIds = round.course.holes.map { it.holeId }.toLongArray()
        val playerIds = round.scores.distinctBy { it.player.id }.map { it.player.id }.toLongArray()
        val action =
            FragmentChooseRoundDirections.actionFragmentChooseRoundToFragmentScore(
                round.round.dateStarted,
                holeIds,
                playerIds
            )
        findNavController().navigate(action)
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
        } else {
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = activity?.startActionMode(actionModeCallback)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}