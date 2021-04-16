package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import fi.efelantti.frisbeegolfer.R

class FragmentNavigationScreen : Fragment() {

    private lateinit var buttonNewRound: Button
    private lateinit var buttonContinueRound: Button
    private lateinit var buttonCourses: Button
    private lateinit var buttonPlayers: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_navigation_screen, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        buttonNewRound = view.findViewById(R.id.button_newRound)
        buttonNewRound.setOnClickListener {
            navigateToNewRound()
        }

        buttonContinueRound = view.findViewById(R.id.button_continue_round)
        buttonContinueRound.setOnClickListener {
            navigateToContinueRound()
        }
        buttonCourses = view.findViewById(R.id.button_courses)
        buttonCourses.setOnClickListener {
            navigateToCourses()
        }

        buttonPlayers = view.findViewById(R.id.button_players)
        buttonPlayers.setOnClickListener {
            navigateToPlayers()
        }
    }

    private fun navigateToNewRound() {
        val directions =
            FragmentNavigationScreenDirections.actionFragmentNavigationScreenToFragmentChooseCourse()
        findNavController().navigate(directions)
    }

    private fun navigateToContinueRound() {
        val directions =
            FragmentNavigationScreenDirections.actionFragmentNavigationScreenToFragmentChooseRound()
        findNavController().navigate(directions)
    }

    private fun navigateToCourses() {
        val directions =
            FragmentNavigationScreenDirections.actionFragmentNavigationScreenToFragmentCourses()
        findNavController().navigate(directions)
    }

    private fun navigateToPlayers() {
        val directions =
            FragmentNavigationScreenDirections.actionFragmentNavigationScreenToFragmentPlayers()
        findNavController().navigate(directions)
    }
}