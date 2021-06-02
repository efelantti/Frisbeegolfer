package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.FragmentScoreBinding
import fi.efelantti.frisbeegolfer.observeOnce
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory
import fi.efelantti.frisbeegolfer.viewmodel.ScoringTerm
import java.time.OffsetDateTime
import kotlin.properties.Delegates


class FragmentScore : Fragment() {

    private var _binding: FragmentScoreBinding? = null
    private val binding get() = _binding!!
    private lateinit var scoreViewModel: ScoreViewModel
    private lateinit var scoreViewModelFactory: ScoreViewModelFactory
    private var numberOfHoles by Delegates.notNull<Int>()

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
        numberOfHoles = holeIds.count()
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        scoreViewModel.currentRound.observeOnce(viewLifecycleOwner) { currentRound ->
            if (currentRound.scores.count() > 0) scoreViewModel.initializeScore(currentRound.scores)
        }

        // TODO - Show skeleton view before data has been loaded.
        scoreViewModel.currentScore.observe(viewLifecycleOwner) {
            it?.let { currentScore ->
                binding.fragmentScoreCurrentPlayer.text = currentScore.player.name

                val holeNumber = currentScore.hole.holeNumber
                val previousHoleNumber = if (holeNumber >= 2) holeNumber - 1 else numberOfHoles
                val nextHoleNumber = if (holeNumber < numberOfHoles) holeNumber + 1 else 1

                binding.fragmentScoreCurrentHole.text = holeNumber.toString()
                // These are not required if there is only one hole.
                if (numberOfHoles >= 1) {
                    binding.fragmentScorePreviousHole.text = previousHoleNumber.toString()
                    binding.fragmentScoreNextHole.text = nextHoleNumber.toString()
                }
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
                controlScoreButtonActivationState(currentScore.score.result)
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

        setScoreButtonClickListeners()
    }

    private fun controlScoreButtonActivationState(result: Int?) {
        val scoreButtons = listOf(
            binding.fragmentScoreButton1,
            binding.fragmentScoreButton2,
            binding.fragmentScoreButton3,
            binding.fragmentScoreButton4,
            binding.fragmentScoreButton5,
            binding.fragmentScoreButton6,
            binding.fragmentScoreButton7,
            binding.fragmentScoreButton8,
            binding.fragmentScoreButton9
        )
        scoreButtons.forEach { it.button.isActivated = false }
        binding.fragmentScoreButtonMore.button.isActivated = false

        val resultToScoringButtonMap = mapOf(
            1 to binding.fragmentScoreButton1,
            2 to binding.fragmentScoreButton2,
            3 to binding.fragmentScoreButton3,
            4 to binding.fragmentScoreButton4,
            5 to binding.fragmentScoreButton5,
            6 to binding.fragmentScoreButton6,
            7 to binding.fragmentScoreButton7,
            8 to binding.fragmentScoreButton8,
            9 to binding.fragmentScoreButton9
        )
        result?.let { resultInt ->
            if (resultInt > 0) {
                if (resultInt > 9) binding.fragmentScoreButtonMore.button.isActivated = true
                else resultToScoringButtonMap[resultInt]?.button?.isActivated = true
            }
        }
    }

    private fun setScoreButtonClickListeners() {
        val scoreButtons = listOf(
            binding.fragmentScoreButton1,
            binding.fragmentScoreButton2,
            binding.fragmentScoreButton3,
            binding.fragmentScoreButton4,
            binding.fragmentScoreButton5,
            binding.fragmentScoreButton6,
            binding.fragmentScoreButton7,
            binding.fragmentScoreButton8,
            binding.fragmentScoreButton9
        )
        scoreButtons.forEachIndexed { index, buttonScoreResultBinding ->
            buttonScoreResultBinding.button.setOnClickListener() {
                it.isActivated = true
                scoreViewModel.setResult(index + 1)
                scoreViewModel.nextScore()
            }
        }
    }

    /*
    Function that sets the scoring term texts for all scoring buttons.
     */
    private fun setScoringTermForButtons(par: Int) {
        val scoreButtons = listOf(
            binding.fragmentScoreButton1,
            binding.fragmentScoreButton2,
            binding.fragmentScoreButton3,
            binding.fragmentScoreButton4,
            binding.fragmentScoreButton5,
            binding.fragmentScoreButton6,
            binding.fragmentScoreButton7,
            binding.fragmentScoreButton8,
            binding.fragmentScoreButton9
        )
        val scoringTermToResIdMap = mapOf(
            ScoringTerm.Ace to R.string.scoring_term_ace,
            ScoringTerm.Condor to R.string.scoring_term_condor,
            ScoringTerm.Albatross to R.string.scoring_term_albatross,
            ScoringTerm.Eagle to R.string.scoring_term_eagle,
            ScoringTerm.Birdie to R.string.scoring_term_birdie,
            ScoringTerm.Par to R.string.scoring_term_Par,
            ScoringTerm.Bogey to R.string.scoring_term_bogey,
            ScoringTerm.DoubleBogey to R.string.scoring_term_double_bogey,
            ScoringTerm.TripleBogey to R.string.scoring_term_triple_bogey,
            ScoringTerm.NoName to R.string.scoring_term_noname
        )

        scoreButtons.forEachIndexed { index, buttonScoreResultBinding ->
            val result = index + 1
            val scoringTerm = ScoreViewModel.getScoringTerm(result, par)
            val scoringTermTextResId = scoringTermToResIdMap[scoringTerm]
                ?: throw IndexOutOfBoundsException("Invalid key $scoringTerm.")
            val scoringTermText = getString(scoringTermTextResId)
            buttonScoreResultBinding.scoringTerm.text = scoringTermText
        }
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