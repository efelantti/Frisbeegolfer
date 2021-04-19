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
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory

class FragmentScore : Fragment() {

    private val args: FragmentScoreArgs by navArgs()
    private val scoreViewModel: ScoreViewModel by viewModels {
        ScoreViewModelFactory(
            (requireContext().applicationContext as FrisbeegolferApplication).repository,
            args.roundId
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
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_score, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        testView = view.findViewById(R.id.fragment_score_test_textview)
        playerNameView = view.findViewById(R.id.fragment_score_test_currentPlayer)
        holeNumberView = view.findViewById(R.id.fragment_score_test_currentHole)
        holeParView = view.findViewById(R.id.fragment_score_test_currentHolePar)
        holeBestView = view.findViewById(R.id.fragment_score_test_currentHoleBest)
        holeAverageView = view.findViewById(R.id.fragment_score_test_currentHoleAverage)
        holeLatestView = view.findViewById(R.id.fragment_score_test_currentHoleLatest)
        nextPlayerButton = view.findViewById(R.id.fragment_score_test_button_next_player)
        nextHoleButton = view.findViewById(R.id.fragment_score_test_button_next_hole)
        incrementIndexButton = view.findViewById(R.id.fragment_score_test_button_increment_index)
        decrementIndexButton = view.findViewById(R.id.fragment_score_test_button_decrement_index)
        setScoreEditText = view.findViewById(R.id.fragment_score_test_set_score_edittext)
        setScoreButton = view.findViewById(R.id.fragment_score_test_set_score_button)

        // TODO - Empty list doesn't contain element at index -1
        scoreViewModel.currentRound.observe(viewLifecycleOwner, { currentRound ->
            if (currentRound != null)
                testView.text = currentRound.round.dateStarted.toString()
            nextPlayerButton.isEnabled = true
            nextHoleButton.isEnabled = true
            incrementIndexButton.isEnabled = true
            decrementIndexButton.isEnabled = true
            setScoreButton.isEnabled = true

            val sortedScores = scoreViewModel.sortRound(currentRound.scores)
            scoreViewModel.initCurrentScoreIndex(sortedScores)
            val currentScore = sortedScores[scoreViewModel.currentScoreIndex]
            playerNameView.text = currentScore.player.name
            holeNumberView.text = currentScore.hole.holeNumber.toString()
            holeParView.text = currentScore.hole.par.toString()
            setScoreEditText.setText(currentScore.score.result.toString())

            scoreViewModel.getHoleStatistics(currentScore.player.id, currentScore.hole.holeId)
                .observe(viewLifecycleOwner,
                    { it2 ->
                        it2?.let { holeStatistics ->
                            holeBestView.text = holeStatistics.bestResult.toString()
                            holeAverageView.text = holeStatistics.avgResult.toString()
                            holeLatestView.text = holeStatistics.latestResult.toString()
                        }
                    })

            setScoreButton.setOnClickListener {
                val scoreToSet = setScoreEditText.text.toString().toInt()
                scoreViewModel.setResult(currentScore.score, scoreToSet)
                scoreViewModel.incrementIndex()
            }
        })

        incrementIndexButton.setOnClickListener {
            scoreViewModel.incrementIndex()
        }

        decrementIndexButton.setOnClickListener {
            scoreViewModel.decrementIndex()
        }

        nextHoleButton.setOnClickListener {
            scoreViewModel.nextHole()
        }

    }
}