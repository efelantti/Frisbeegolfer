package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.*
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
    private val roundId: OffsetDateTime,
    private val playerIds: LongArray,
    private val holeIds: LongArray
) :
    ViewModel() {

    private val coroutineScope = getViewModelScope(coroutineScopeProvider)
    val currentRound: LiveData<RoundWithCourseAndScores> = RefreshableLiveData {
        repository.getRoundWithRoundId(roundId)
    }

    private val scoreIdLiveData = MutableLiveData(Pair(0L, 0L))
    val currentScore: LiveData<ScoreWithPlayerAndHole> =
        Transformations.switchMap(scoreIdLiveData) { pair ->
            repository.getScore(roundId, pair.first, pair.second)
        }

    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics> {
        return repository.getHoleStatistics(playerId, holeId)
    }

    fun setScoreId(playerId: Long, holeId: Long) {
        scoreIdLiveData.value = Pair(playerId, holeId)
    }

    fun initializeScore(scores: List<ScoreWithPlayerAndHole>) {
        val sortedScores =
            scores.sortedWith(compareBy<ScoreWithPlayerAndHole> { it.hole.holeNumber }.thenBy { it.player.name })
        val firstNotScored = sortedScores.firstOrNull { it.score.result == 0 }
        val index = if (firstNotScored == null) sortedScores.count() - 1
        else sortedScores.indexOf(firstNotScored)
        setScoreId(sortedScores[index].player.id, sortedScores[index].hole.holeId)
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
     */

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
    private val roundId: OffsetDateTime,
    private val playerIds: LongArray,
    private val holeIds: LongArray
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        (ScoreViewModel(null, repository, roundId, playerIds, holeIds) as T)
}