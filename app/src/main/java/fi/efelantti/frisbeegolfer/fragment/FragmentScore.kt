package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.databinding.FragmentScoreBinding
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory
import fi.efelantti.frisbeegolfer.viewmodel.ScoringTerm
import java.time.OffsetDateTime
import kotlin.properties.Delegates


class FragmentScore : Fragment(), DialogScoreAmount.OnScoreAmountSelected {

    private var _binding: FragmentScoreBinding? = null
    private val binding get() = _binding!!
    private lateinit var scoreViewModel: ScoreViewModel
    private lateinit var scoreViewModelFactory: ScoreViewModelFactory
    private var numberOfHoles by Delegates.notNull<Int>()

    private fun pickDateTime() {
        DateTimePicker(requireContext(), true) {
            val pickedDateTime = it.pickedDateTime
            scoreViewModel.updateCurrentRound(pickedDateTime)
        }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_start_datetime -> {
                pickDateTime()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScoreBinding.inflate(inflater, container, false)
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
        scoreViewModel = ViewModelProvider(requireParentFragment(), scoreViewModelFactory)
            .get(ScoreViewModel::class.java)
        numberOfHoles = holeIds.count()
        return binding.root
    }

    // TODO - Disable buttons while setting score is loading.
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        scoreViewModel.currentRoundId.observe(viewLifecycleOwner) {
            Log.i("CURRENT ROUND ID", it.toString())
        }

        scoreViewModel.currentRound.observeOnce(viewLifecycleOwner) { currentRound ->
            if (currentRound.scores.count() > 0) {
                scoreViewModel.initializeScore(currentRound.scores)
            }
        }

        scoreViewModel.currentRound.observe(viewLifecycleOwner) {
            // Observing just to make LiveData to update.
        }

        scoreViewModel.currentPlayer.observe(viewLifecycleOwner) { player ->
            binding.fragmentScoreCurrentPlayer.text = player.name
        }

        scoreViewModel.currentHole.observe(viewLifecycleOwner) { hole ->
            if (hole != null) {
                val holeNumber = hole.holeNumber
                val previousHoleNumber = if (holeNumber >= 2) holeNumber - 1 else numberOfHoles
                val nextHoleNumber = if (holeNumber < numberOfHoles) holeNumber + 1 else 1

                binding.fragmentScoreCurrentHole.text = holeNumber.toString()
                // These are not required if there is only one hole.
                if (numberOfHoles >= 1) {
                    binding.fragmentScorePreviousHole.text = previousHoleNumber.toString()
                    binding.fragmentScoreNextHole.text = nextHoleNumber.toString()
                }
            }
        }

        // TODO - Show skeleton view before data has been loaded.
        scoreViewModel.currentScore.observe(viewLifecycleOwner) {
            it?.let { currentScore ->
                currentScore?.let {

                    binding.fragmentScoreCurrentHolePar.text = currentScore.hole.par.toString()

                    binding.fragmentScorePlusMinus.text =
                        scoreViewModel.plusMinus(currentScore.player)

                    setScoringTermForButtons(currentScore.hole.par)
                    controlScoreButtonActivationState(currentScore.score.result)

                    binding.fragmentScoreButtonOb.button.isActivated =
                        currentScore.score.isOutOfBounds
                    binding.fragmentScoreButtonDnf.button.isActivated =
                        currentScore.score.didNotFinish
                }
            }
        }

        scoreViewModel.holeStatistics.observe(viewLifecycleOwner) { it ->
            it?.let { holeStatistics ->
                if (holeStatistics.bestResult == null || holeStatistics.bestResult == -1) binding.fragmentScoreCurrentHoleBest.text =
                    getString(R.string.notApplicable)
                else binding.fragmentScoreCurrentHoleBest.text =
                    holeStatistics.bestResult.toString()
                val avgResult = holeStatistics.avgResult
                if (avgResult == null || holeStatistics.avgResult == -1f) binding.fragmentScoreCurrentHoleAverage.text =
                    getString(R.string.notApplicable)
                else binding.fragmentScoreCurrentHoleAverage.text =
                    avgResult.toPrettyString()
                if (holeStatistics.latestResult == null || holeStatistics.latestResult == -1) binding.fragmentScoreCurrentHoleLatest.text =
                    getString(R.string.notApplicable)
                else binding.fragmentScoreCurrentHoleLatest.text =
                    holeStatistics.latestResult.toString()
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

        binding.fragmentScoreButtonOb.button.setOnClickListener {
            scoreViewModel.toggleOb()
        }

        binding.fragmentScoreButtonDnf.button.setOnClickListener {
            scoreViewModel.toggleDnf()
            scoreViewModel.nextScore()
        }

        binding.fragmentScoreButtonMore.button.setOnClickListener {
            DialogScoreAmount(this).show(
                childFragmentManager, DialogScoreAmount.TAG
            )
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
        private const val READONLY = "readOnly"

        fun newInstance(
            roundId: OffsetDateTime,
            playerIds: LongArray,
            holeIds: LongArray,
            readOnly: Boolean
        ) =
            FragmentScore().apply {
                arguments = bundleOf(
                    ROUND_ID to roundId,
                    PLAYER_IDS to playerIds,
                    HOLE_IDS to holeIds,
                    READONLY to readOnly
                )
            }
    }

    override fun selectedScoreAmount(score: Int) {
        scoreViewModel.setResult(score)
        scoreViewModel.nextScore()
    }
}