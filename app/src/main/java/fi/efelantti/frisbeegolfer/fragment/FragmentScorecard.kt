package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.databinding.FragmentScorecardBinding
import fi.efelantti.frisbeegolfer.tableview.TableViewAdapter
import fi.efelantti.frisbeegolfer.tableview.model.Cell
import fi.efelantti.frisbeegolfer.tableview.model.ColumnHeader
import fi.efelantti.frisbeegolfer.tableview.model.RowHeader
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory
import java.time.OffsetDateTime


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

        val tableView = binding.contentContainer
        val adapter = TableViewAdapter()
        tableView.setAdapter(adapter)

        scoreViewModel.currentRound.observe(viewLifecycleOwner) { currentRound ->
            if (currentRound.scores.count() > 0) {
                val playerList =
                    currentRound.scores.distinctBy { it.player.id }.sortedBy { it.player.name }
                        .map { it.player }
                val mColumnHeaderList = playerList.map {
                    ColumnHeader(
                        it.name,
                        scoreViewModel.plusMinus(it, currentRound.scores)
                    )
                }

                val holeNumberList = currentRound.scores.distinctBy { it.hole.holeNumber }
                    .sortedBy { it.hole.holeNumber }.map { it.hole }
                val mRowHeaderList = holeNumberList.map { RowHeader(it.holeNumber.toString()) }

                val mCellList = mutableListOf<List<Cell>>()
                for (hole in holeNumberList) {
                    val listToAdd = mutableListOf<Cell>()
                    for (player in playerList) {
                        val score =
                            currentRound.scores.single { it.hole == hole && it.player == player }
                        val cell = Cell(
                            score.score.result.toString(),
                            scoreViewModel.plusMinus(player, currentRound.scores, hole.holeNumber)
                        )
                        listToAdd.add(cell)
                    }
                    mCellList.add(listToAdd)
                }

                adapter.setAllItems(mColumnHeaderList, mRowHeaderList, mCellList)
            }
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
            FragmentScorecard().apply {
                arguments = bundleOf(
                    ROUND_ID to roundId,
                    PLAYER_IDS to playerIds,
                    HOLE_IDS to holeIds
                )
            }
    }
}