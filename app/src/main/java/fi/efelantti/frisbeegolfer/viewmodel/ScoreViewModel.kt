package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.RefreshableLiveData
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.getViewModelScope
import fi.efelantti.frisbeegolfer.model.HoleStatistics
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.model.Score
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class ScoreViewModel(
    coroutineScopeProvider: CoroutineScope? = null,
    private val repository: IRepository,
    private val roundId: OffsetDateTime
) :
    ViewModel() {

    private val coroutineScope = getViewModelScope(coroutineScopeProvider)
    val currentRound: LiveData<RoundWithCourseAndScores> = RefreshableLiveData {
        repository.getRoundWithRoundId(roundId)
    }
    var currentScoreIndex = -1
    private var numberOfPlayers = -1
    private var numberOfHoles = -1

    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics> {
        return repository.getHoleStatistics(playerId, holeId)
    }

    /*
    Used to force refresh the observers of the LiveData object.
     */
    private fun refresh() {
        (currentRound as RefreshableLiveData).refresh()
    }

    fun update(score: Score) = coroutineScope.launch {
        repository.update(score)
    }

    /*
    Sort the scores list by (hole number, player id). TODO - Avoid this by changing @RELATION to two queries?
    (Refer to https://stackoverflow.com/questions/48315261/using-rooms-relation-with-order-by/64321494)
     */
    fun sortRound(scores: List<ScoreWithPlayerAndHole>): List<ScoreWithPlayerAndHole> {
        return scores.sortedWith(compareBy<ScoreWithPlayerAndHole> { it.hole.holeNumber }.thenBy { it.player.name })
    }

    /*
    Used to initialize current score, if it's not already initialized.
    Sets the index to either the index of the first hole that's not yet scored OR to the index of the last hole, if all are already scored.
     */
    fun initCurrentScoreIndex(sortedScores: List<ScoreWithPlayerAndHole>) {
        if (currentScoreIndex == -1) {
            val firstNotScoredHole = sortedScores.firstOrNull { it.score.result == 0 }
            currentScoreIndex = if (firstNotScoredHole == null) sortedScores.count() - 1
            else sortedScores.indexOf(firstNotScoredHole)
        }
    }

    /*
    Used to initialize number of players & holes, which are needed for incrementing & decrementing the score index.
     */
    private fun initHelperValues(scores: List<ScoreWithPlayerAndHole>) {
        if (numberOfHoles == -1) numberOfHoles = scores.distinctBy { it.hole.holeId }.count()
        if (numberOfPlayers == -1) numberOfPlayers = scores.distinctBy { it.player.id }.count()
    }

    /*
    Adds one to the current score index. To be used after scoring a hole.
     */
    fun incrementIndex() {
        addToIndex(1)
    }

    /*
    Subtracts one from the current score index. To be used after scoring a hole.
    */
    fun decrementIndex() {
        addToIndex(-1)
    }

    /*
    Helper function used for adding a value to the [currentScoreIndex].
     */
    private fun addToIndex(valueToAdd: Int) {
        val sortedScores = currentRound.value?.scores?.let { sortRound(it) }
        if (currentScoreIndex == -1) sortedScores?.let { initCurrentScoreIndex(it) }
        if (numberOfHoles == -1 || numberOfPlayers == -1) sortedScores?.let {
            initHelperValues(it)
        }
        currentScoreIndex =
            Math.floorMod(currentScoreIndex + valueToAdd, numberOfHoles * numberOfPlayers)
        // Other option, but this does not allow to use "Previous" on zero index.
        // currentScoreIndex = (currentScoreIndex + valueToAdd).rem(numberOfHoles*numberOfPlayers)
        refresh()
    }

    /*
    TODO - Next player BUT current hole / is this needed?
     */
    fun nextPlayer() {
        refresh()
    }

    /*
    TODO - Set index to next hole BUT current player / is this needed?
     */
    fun nextHole() {
        refresh()
    }

    /*
    Sets the result of the current score. Calls repository.update as well.
     */
    fun setResult(score: Score, resultToSet: Int) {
        score.result = resultToSet
        update(score)
    }
}

@Suppress("UNCHECKED_CAST")
class ScoreViewModelFactory(
    private val repository: Repository,
    private val roundId: OffsetDateTime
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        (ScoreViewModel(null, repository, roundId) as T)
}