package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.TypeConverter
import fi.efelantti.frisbeegolfer.Converters
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.RoundWithScores
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class FragmentScore : Fragment() {

    companion object {
        private val converters = Converters()
        fun newInstance(roundId: OffsetDateTime): FragmentScore {
            val frag = FragmentScore()
            val args = Bundle()
            args.putString("roundId", converters.fromOffsetDateTime(roundId))
            frag.setArguments(args)
            return frag
        }
    }
    private lateinit var scoreViewModel: ScoreViewModel
    private val converters = Converters()
    private lateinit var testView: TextView
    private lateinit var playerNameView: TextView
    private lateinit var holeNumberView: TextView
    private lateinit var holeParView: TextView
    private lateinit var nextPlayerButton: Button
    private lateinit var nextHoleButton: Button

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
        var roundIdString = requireArguments().getString("roundId")
        var roundId = converters.toOffsetDateTime(roundIdString)
        if(roundId == null) throw IllegalArgumentException("Round id was null.")

        scoreViewModel = ViewModelProvider(this, ScoreViewModelFactory(this.requireActivity().application, roundId)).get(ScoreViewModel::class.java)

        testView = view.findViewById(R.id.fragment_score_test_textview)
        playerNameView = view.findViewById(R.id.fragment_score_test_currentPlayer)
        holeNumberView = view.findViewById(R.id.fragment_score_test_currentHole)
        holeParView = view.findViewById(R.id.fragment_score_test_currentHolePar)
        nextPlayerButton = view.findViewById(R.id.fragment_score_test_button_next_player)
        nextHoleButton = view.findViewById(R.id.fragment_score_test_button_next_hole)

        scoreViewModel.currentRound.observe(viewLifecycleOwner, Observer<RoundWithScores> {
            it?.let { currentRound ->
                testView.text = currentRound.round.dateStarted.toString()
                // TODO - Move this as the observed data to VM.
                val sortedScores = scoreViewModel.sortRound(currentRound.scores)
                scoreViewModel.initCurrentScoreIndex(sortedScores)

                nextPlayerButton.isEnabled = true
                nextHoleButton.isEnabled = true

                val score = sortedScores[scoreViewModel.currentScoreIndex]
                    playerNameView.text = score.player.firstName
                    holeNumberView.text = score.hole.holeNumber.toString()
                    holeParView.text = score.hole.par.toString()
            }
        })

        nextHoleButton.setOnClickListener {
            scoreViewModel.currentScoreIndex = scoreViewModel.currentScoreIndex + 1
            scoreViewModel.refresh()
        }

    }
   }