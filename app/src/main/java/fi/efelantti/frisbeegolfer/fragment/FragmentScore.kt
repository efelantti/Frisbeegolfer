package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.databinding.FragmentScoreBinding
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory

class FragmentScore : Fragment() {

    private var _binding: FragmentScoreBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentScoreArgs by navArgs()
    private val scoreViewModel: ScoreViewModel by viewModels {
        ScoreViewModelFactory(
            (requireContext().applicationContext as FrisbeegolferApplication).repository,
            args.roundId,
            args.playerIds,
            args.holeIds
        )
    }
    private lateinit var testView: TextView
    private lateinit var playerNameView: TextView
    private lateinit var holeNumberView: TextView
    private lateinit var holeParView: TextView
    private lateinit var holeBestView: TextView
    private lateinit var holeAverageView: TextView
    private lateinit var holeLatestView: TextView
    private lateinit var nextPlayerButton: Button
    private lateinit var nextHoleButton: Button
    private lateinit var incrementIndexButton: Button
    private lateinit var decrementIndexButton: Button
    private lateinit var setScoreEditText: EditText
    private lateinit var setScoreButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScoreBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        testView = binding.fragmentScoreTestTextView
        playerNameView = binding.fragmentScoreTestCurrentPlayer
        holeNumberView = binding.fragmentScoreTestCurrentHole
        holeParView = binding.fragmentScoreTestCurrentHolePar
        holeBestView = binding.fragmentScoreTestCurrentHoleBest
        holeAverageView = binding.fragmentScoreTestCurrentHoleAverage
        holeLatestView = binding.fragmentScoreTestCurrentHoleLatest
        nextPlayerButton = binding.fragmentScoreTestButtonNextPlayer
        nextHoleButton = binding.fragmentScoreTestButtonNextHole
        incrementIndexButton = binding.fragmentScoreTestButtonIncrementIndex
        decrementIndexButton = binding.fragmentScoreTestButtonDecrementIndex
        setScoreEditText = binding.fragmentScoreTestSetScoreEditText
        setScoreButton = binding.fragmentScoreTestSetScoreButton

        scoreViewModel.currentRound.observe(viewLifecycleOwner) { currentRound ->
            testView.text = currentRound.round.dateStarted.toString()
            if (currentRound.scores.count() > 0) scoreViewModel.initializeScore(currentRound.scores)
        }

        scoreViewModel.currentScore.observe(viewLifecycleOwner) {
            it?.let { currentScore ->
                nextPlayerButton.isEnabled = true
                nextHoleButton.isEnabled = true
                incrementIndexButton.isEnabled = true
                decrementIndexButton.isEnabled = true
                setScoreButton.isEnabled = true

                playerNameView.text = currentScore.player.name
                holeNumberView.text = currentScore.hole.holeNumber.toString()
                holeParView.text = currentScore.hole.par.toString()
                setScoreEditText.setText(currentScore.score.result.toString())

                scoreViewModel.getHoleStatistics(currentScore.player.id, currentScore.hole.holeId)
                    .observe(viewLifecycleOwner) { it2 ->
                        it2?.let { holeStatistics ->
                            holeBestView.text = holeStatistics.bestResult.toString()
                            holeAverageView.text = holeStatistics.avgResult.toString()
                            holeLatestView.text = holeStatistics.latestResult.toString()
                        }
                    }

                setScoreButton.setOnClickListener {
                    val scoreToSet = setScoreEditText.text.toString().toInt()
                    scoreViewModel.setResult(currentScore.score, scoreToSet)
                    scoreViewModel.nextScore()
                }
            }
        }

        incrementIndexButton.setOnClickListener {
            scoreViewModel.nextScore()
        }

        decrementIndexButton.setOnClickListener {
            scoreViewModel.previousScore()
        }

        nextPlayerButton.setOnClickListener {
            scoreViewModel.nextPlayer()
        }

        nextHoleButton.setOnClickListener {
            scoreViewModel.nextHole()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}