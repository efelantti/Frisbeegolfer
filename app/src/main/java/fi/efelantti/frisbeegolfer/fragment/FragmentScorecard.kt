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
import fi.efelantti.frisbeegolfer.databinding.FragmentScorecardBinding
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory
import ir.androidexception.datatable.model.DataTableHeader
import ir.androidexception.datatable.model.DataTableRow
import java.time.OffsetDateTime
import java.util.*


class FragmentScorecard : Fragment() {

    private var _binding: FragmentScorecardBinding? = null
    private val binding get() = _binding!!
    private lateinit var scoreViewModel: ScoreViewModel
    private lateinit var scoreViewModelFactory: ScoreViewModelFactory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScorecardBinding.inflate(inflater, container, false)
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
        //binding.scorecardText.text = "Score card"

        scoreViewModel.currentRound.observe(viewLifecycleOwner) { currentRound ->
            if (currentRound.scores.count() > 0)
                displayScoreCard(currentRound.scores)
        }
    }

    private fun displayScoreCard(scores: List<ScoreWithPlayerAndHole>) {
        val players = scores.distinctBy { it.player }.map { it.player }.sortedBy { it.name }
        val holes = scores.distinctBy { it.hole }.map { it.hole }.sortedBy { it.holeNumber }
        val dataTable = binding.dataTable

        val header = DataTableHeader.Builder()
        header.item(getString(R.string.score_card_hole_number), 1)
        players.forEach { player ->
            header.item(player.name, 2)
        }

        val rows = ArrayList<DataTableRow>()
        // define 200 fake rows for table

        holes.forEach { hole ->
            val row = DataTableRow.Builder()
            row.value(hole.holeNumber.toString())
            players.forEach { player ->
                row.value("3")
            }
            rows.add(row.build())
        }

        //dataTable.typeface = typeface
        dataTable.header = header.build()
        dataTable.rows = rows
        dataTable.headerTextSize = 14.0f
        dataTable.rowTextSize = 14.0f
        dataTable.inflate(requireContext())
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
            FragmentScorecard().apply {
                arguments = bundleOf(
                    ROUND_ID to roundId,
                    PLAYER_IDS to playerIds,
                    HOLE_IDS to holeIds
                )
            }
    }
}