package fi.efelantti.frisbeegolfer.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import fi.efelantti.frisbeegolfer.model.clone
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import java.time.OffsetDateTime

class FragmentChooseRound : Fragment(), RoundListAdapter.ListItemClickListener {

    private val roundViewModel: RoundViewModel by viewModels()

    interface FragmentChooseRoundListener {

        fun onRoundSelected(
            chosenRoundId: OffsetDateTime
        )
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
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_choose_round, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RoundListAdapter(activity as Context, this)
        recyclerView = view.findViewById<EmptyRecyclerView>(
            R.id.recyclerview_continue_round
        )
        emptyView = view.findViewById<TextView>(R.id.empty_view_rounds)

        recyclerView.setEmptyView(emptyView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(activity)

        roundViewModel.allRounds.observe(viewLifecycleOwner, Observer { round ->
            // Update the cached copy of the words in the adapter.
            round?.let { adapter.setRounds(it) }
        })
   }

    private fun chooseSelectedRound() {
        val round = adapter.getSelectedRound()
        actionMode?.finish()
        if(round == null) throw java.lang.IllegalArgumentException("No round was selected.")
        sendBackResult(round.round.dateStarted)
    }

    // Call this method to send the data back to the parent activity
    private fun sendBackResult(chosenRoundId: OffsetDateTime) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        val listener: FragmentChooseRoundListener = activity as FragmentChooseRoundListener
        listener.onRoundSelected(chosenRoundId)
    }

    override fun onListItemClick(position: Int, shouldStartActionMode: Boolean) {
        if (!shouldStartActionMode) {
            actionMode?.finish()
        } else {
            when (actionMode) {
                null -> {
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = activity?.startActionMode(actionModeCallback)
                    true
                }
                else -> false
            }
        }
    }
}