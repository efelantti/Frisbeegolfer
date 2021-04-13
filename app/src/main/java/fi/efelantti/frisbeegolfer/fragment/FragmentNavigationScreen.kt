package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import fi.efelantti.frisbeegolfer.R

class FragmentNavigationScreen : Fragment() {

    private lateinit var buttonNewRound: Button
    private lateinit var buttonContinueRound: Button
    private lateinit var buttonCourses: Button
    private lateinit var buttonPlayers: Button
    private lateinit var listener: FragmentNavigationScreenListener

    interface FragmentNavigationScreenListener {

        fun navigateToNewRound()
        fun navigateToContinueRound()
        fun navigateToCourses()
        fun navigatePlayers()
    }

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

        listener = activity as FragmentNavigationScreenListener

        buttonNewRound = view.findViewById(R.id.button_newRound)
        buttonNewRound.setOnClickListener {
            listener.navigateToNewRound()
        }

        buttonContinueRound = view.findViewById(R.id.button_continue_round)
        buttonContinueRound.setOnClickListener {
            listener.navigateToContinueRound()
        }
        buttonCourses = view.findViewById(R.id.button_courses)
        buttonCourses.setOnClickListener {
            listener.navigateToCourses()
        }

        buttonPlayers = view.findViewById(R.id.button_players)
        buttonPlayers.setOnClickListener {
            listener.navigatePlayers()
        }
    }
}