package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import fi.efelantti.frisbeegolfer.databinding.FragmentNavigationScreenBinding

class FragmentNavigationScreen : Fragment() {

    private var _binding: FragmentNavigationScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var buttonNewRound: Button
    private lateinit var buttonContinueRound: Button
    private lateinit var buttonCourses: Button
    private lateinit var buttonPlayers: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNavigationScreenBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        buttonNewRound = binding.buttonNewRound
        buttonNewRound.setOnClickListener {
            navigateToNewRound()
        }

        buttonContinueRound = binding.buttonContinueRound
        buttonContinueRound.setOnClickListener {
            navigateToContinueRound()
        }
        buttonCourses = binding.buttonCourses
        buttonCourses.setOnClickListener {
            navigateToCourses()
        }

        buttonPlayers = binding.buttonPlayers
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}