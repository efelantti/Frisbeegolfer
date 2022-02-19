package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fi.efelantti.frisbeegolfer.DateTimePicker
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.activity.MainActivity
import fi.efelantti.frisbeegolfer.databinding.FragmentScorecardBinding
import fi.efelantti.frisbeegolfer.tableview.TableViewAdapter
import fi.efelantti.frisbeegolfer.tableview.model.Cell
import fi.efelantti.frisbeegolfer.tableview.model.ColumnHeader
import fi.efelantti.frisbeegolfer.tableview.model.RowHeader
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory
import java.time.OffsetDateTime

// TODO - Add skeleton UI while loading.
class FragmentScorecard : Fragment() {

    private var _binding: FragmentScorecardBinding? = null
    private val binding get() = _binding!!
    private lateinit var scoreViewModel: ScoreViewModel
    private lateinit var scoreViewModelFactory: ScoreViewModelFactory
    private var expectedScoresCount = 0

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

    override fun onPrepareOptionsMenu(menu: Menu) {
        val menuItemsToHide = mutableListOf(
            R.id.action_search,
            R.id.action_import_data,
            R.id.action_export_data,
            R.id.action_import_data_from_discscores
        )
        menuItemsToHide.forEach {
            val item = menu.findItem(it)
            if (item != null) item.isVisible = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScorecardBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        requireActivity().invalidateOptionsMenu()
        val roundId = requireArguments().getSerializable(ROUND_ID) as OffsetDateTime
        val playerIds = requireArguments().getLongArray(PLAYER_IDS)
            ?: throw IllegalArgumentException("List of player ids was null.")
        val holeIds = requireArguments().getLongArray(HOLE_IDS)
            ?: throw IllegalArgumentException("List of hole ids was null.")
        expectedScoresCount = playerIds.count() * holeIds.count()
        scoreViewModelFactory = ScoreViewModelFactory(
            (requireContext().applicationContext as FrisbeegolferApplication).repository,
            roundId,
            playerIds,
            holeIds
        )
        scoreViewModel =
            ViewModelProvider(this, scoreViewModelFactory)
                .get(ScoreViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val roundName = requireArguments().getString(ROUND_NAME)
            ?: throw IllegalArgumentException("Round name was not provided.")
        val currentTitle = (requireActivity() as MainActivity).supportActionBar?.title
        if (currentTitle != roundName)
            (requireActivity() as MainActivity).supportActionBar?.title = roundName

        val tableView = binding.contentContainer
        val adapter = TableViewAdapter()
        tableView.setAdapter(adapter)

        scoreViewModel.currentRound.observe(viewLifecycleOwner) { currentRound ->
            if (currentRound != null && currentRound.scores.count() == expectedScoresCount) {

                val playerList =
                    currentRound.scores.distinctBy { it.player.id }.sortedBy { it.player.name }
                        .map { it.player }
                val mColumnHeaderList = playerList.map {
                    ColumnHeader(
                        it.name,
                        ScoreViewModel.plusMinus(it, currentRound.scores)
                    )
                }

                val holeNumberList = currentRound.scores.distinctBy { it.hole.holeNumber }
                    .sortedBy { it.hole.holeNumber }.map { it.hole }
                val mRowHeaderList = holeNumberList.map {
                    RowHeader(
                        it.holeNumber.toString(),
                        getString(R.string.scorecard_row_header_par_count, it.par)
                    )
                }

                val mCellList = mutableListOf<List<Cell>>()
                for (hole in holeNumberList) {
                    val listToAdd = mutableListOf<Cell>()
                    for (player in playerList) {
                        val score =
                            currentRound.scores.single { it.hole.holeId == hole.holeId && it.player.id == player.id }
                        val colorInt = getColorByResult(score.score.result, score.hole.par)
                        val color = ContextCompat.getColor(requireContext(), colorInt)
                        val cell = Cell(
                            score.score.result.toString(),
                            color,
                            ScoreViewModel.plusMinus(player, currentRound.scores, hole.holeNumber),
                            score.score.didNotFinish,
                            score.score.isOutOfBounds
                        )
                        listToAdd.add(cell)
                    }
                    mCellList.add(listToAdd)
                }

                adapter.setAllItems(mColumnHeaderList, mRowHeaderList, mCellList)
            }
            if (currentRound.scores.any { it.score.didNotFinish }) {
                binding.dnfText.visibility = View.VISIBLE
                binding.dnfTextDescription.visibility = View.VISIBLE
            } else {
                binding.dnfText.visibility = View.GONE
                binding.dnfTextDescription.visibility = View.GONE
            }
            if (currentRound.scores.any { it.score.isOutOfBounds }) {
                binding.obShape.visibility = View.VISIBLE
                binding.obShapeDescription.visibility = View.VISIBLE
            } else {
                binding.obShape.visibility = View.GONE
                binding.obShapeDescription.visibility = View.GONE
            }
        }
    }

    private fun getColorByResult(result: Int?, par: Int): Int {
        if (result == null) return R.color.result_other
        else if (result == 1) return R.color.result_ace
        val plusMinus = result - par
        when (plusMinus) {
            -4 -> return R.color.result_condor
            -3 -> return R.color.result_albatross
            -2 -> return R.color.result_eagle
            -1 -> return R.color.result_birdie
            0 -> return R.color.result_par
            1 -> return R.color.result_bogey
            2 -> return R.color.result_double_bogey
            3 -> return R.color.result_triple_bogey
        }
        if (plusMinus < -4) return R.color.result_albatross
        return if (plusMinus > 3) R.color.result_triple_bogey
        else R.color.result_other
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ROUND_ID = "round_Id"
        const val PLAYER_IDS = "player_Ids"
        const val HOLE_IDS = "hole_Ids"
        const val READONLY = "readOnly"
        const val ROUND_NAME = "roundName"

        fun newInstance(
            roundId: OffsetDateTime,
            playerIds: LongArray,
            holeIds: LongArray,
            readOnly: Boolean,
            roundName: String
        ) =
            FragmentScorecard().apply {
                arguments = bundleOf(
                    ROUND_ID to roundId,
                    PLAYER_IDS to playerIds,
                    HOLE_IDS to holeIds,
                    READONLY to readOnly,
                    ROUND_NAME to roundName
                )
            }
    }
}