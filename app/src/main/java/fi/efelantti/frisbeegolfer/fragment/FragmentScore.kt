package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.FragmentScoreBinding
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory
import fi.efelantti.frisbeegolfer.viewmodel.ScoringTerm
import java.time.OffsetDateTime


class FragmentScore : Fragment() {

    private var _binding: FragmentScoreBinding? = null
    private val binding get() = _binding!!
    private lateinit var scoreViewModel: ScoreViewModel
    private lateinit var scoreViewModelFactory: ScoreViewModelFactory
    private lateinit var builder: TextDrawable.IBuilder
    private val generator = ColorGenerator.MATERIAL

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScoreBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        val roundId = requireArguments().getSerializable(ROUND_ID) as OffsetDateTime
        val playerIds = requireArguments().getLongArray(PLAYER_IDS)
            ?: throw IllegalArgumentException("List of player ids was null.")
        val holeIds = requireArguments().getLongArray(HOLE_IDS)
            ?: throw IllegalArgumentException("List of hole ids was null.")
        scoreViewModelFactory = ScoreViewModelFactory(
            (requireContext().applicationContext as FrisbeegolferApplication).repository,
            roundId,
            playerIds,
            holeIds
        )
        scoreViewModel = ViewModelProvider(this, scoreViewModelFactory)
            .get(ScoreViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        builder = TextDrawable.builder()
            .beginConfig()
            .endConfig()
            .round()

        scoreViewModel.currentRound.observe(viewLifecycleOwner) { currentRound ->
            if (currentRound.scores.count() > 0) scoreViewModel.initializeScore(currentRound.scores)
        }

        // TODO - Show skeleton view before data has been loaded.
        scoreViewModel.currentScore.observe(viewLifecycleOwner) {
            it?.let { currentScore ->
                binding.fragmentScoreCurrentPlayer.text = currentScore.player.name

                val color = generator.getColor(currentScore.player.name)
                val initial = currentScore.player.name?.take(1)
                val icon = builder.build(initial, color)
                binding.fragmentScoreCurrentPlayerAvatar.setImageDrawable(icon)

                binding.fragmentScoreCurrentHole.text = currentScore.hole.holeNumber.toString()
                binding.fragmentScoreCurrentHolePar.text = currentScore.hole.par.toString()

                scoreViewModel.getHoleStatistics(currentScore.player.id, currentScore.hole.holeId)
                    .observe(viewLifecycleOwner) { it2 ->
                        it2?.let { holeStatistics ->
                            if (holeStatistics.bestResult == null) binding.fragmentScoreCurrentHoleBest.text =
                                getString(R.string.notApplicable)
                            else binding.fragmentScoreCurrentHoleBest.text =
                                holeStatistics.bestResult.toString()
                            if (holeStatistics.avgResult == null) binding.fragmentScoreCurrentHoleAverage.text =
                                getString(R.string.notApplicable)
                            else binding.fragmentScoreCurrentHoleAverage.text =
                                holeStatistics.avgResult.toString()
                            if (holeStatistics.latestResult == null) binding.fragmentScoreCurrentHoleLatest.text =
                                getString(R.string.notApplicable)
                            else binding.fragmentScoreCurrentHoleLatest.text =
                                holeStatistics.latestResult.toString()
                        }
                    }

                setScoringTermForButtons(currentScore.hole.par)

                /*setScoreButton.setOnClickListener {
                    val scoreToSet = setScoreEditText.text.toString().toInt()
                    scoreViewModel.setResult(currentScore.score, scoreToSet)
                    scoreViewModel.nextScore()
                }*/
            }
        }

        binding.fragmentScoreButtonNextPlayer.setOnClickListener {
            scoreViewModel.nextPlayer()
        }

        binding.fragmentScoreButtonPreviousPlayer.setOnClickListener {
            scoreViewModel.previousPlayer()
        }

        binding.fragmentScoreNextHole.setOnClickListener {
            scoreViewModel.nextHole()
        }

        binding.fragmentScorePreviousHole.setOnClickListener {
            scoreViewModel.previousHole()
        }
    }

    private fun setScoringTermForButtons(par: Int) {
        val scoringTermToResIdMap = mapOf(
            ScoringTerm.Condor to R.string.scoring_term_condor,
            ScoringTerm.Albatross to R.string.scoring_term_albatross,
            ScoringTerm.Eagle to R.string.scoring_term_eagle,
            ScoringTerm.Birdie to R.string.scoring_term_birdie,
            ScoringTerm.Par to R.string.scoring_term_Par,
            ScoringTerm.Bogey to R.string.scoring_term_bogey,
            ScoringTerm.DoubleBogey to R.string.scoring_term_double_bogey,
            ScoringTerm.TripleBogey to R.string.scoring_term_triple_bogey
        )
        binding.fragmentScoreButton1.scoringTerm.text = getString(R.string.scoring_term_ace)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ROUND_ID = "round_Id"
        private const val PLAYER_IDS = "player_Ids"
        private const val HOLE_IDS = "hole_Ids"

        fun newInstance(roundId: OffsetDateTime, playerIds: LongArray, holeIds: LongArray) =
            FragmentScore().apply {
                arguments = bundleOf(
                    ROUND_ID to roundId,
                    PLAYER_IDS to playerIds,
                    HOLE_IDS to holeIds
                )
            }
    }
}