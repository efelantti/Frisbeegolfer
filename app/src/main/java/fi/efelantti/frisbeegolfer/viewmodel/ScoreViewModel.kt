package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.*
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.RefreshableLiveData
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.getViewModelScope
import fi.efelantti.frisbeegolfer.model.*
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
    val currentRound: RefreshableLiveData<RoundWithCourseAndScores> = RefreshableLiveData {
        repository.getRoundWithRoundId(roundId)
    }

    private val scoreIdLiveData = MutableLiveData(Pair(playerIds[0], holeIds[0]))
    val currentScore: LiveData<ScoreWithPlayerAndHole> =
        Transformations.switchMap(scoreIdLiveData) { pair ->
            repository.getScore(roundId, pair.first, pair.second)
        }

    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics> {
        return repository.getHoleStatistics(playerId, holeId)
    }

    private fun setScoreId(playerId: Long, holeId: Long) {
        scoreIdLiveData.value = Pair(playerId, holeId)
    }

    fun initializeScore(scores: List<ScoreWithPlayerAndHole>) {
        val sortedScores =
            scores.sortedWith(compareBy<ScoreWithPlayerAndHole> { it.hole.holeNumber }.thenBy { it.player.name })
        val firstNotScored =
            sortedScores.firstOrNull { it.score.result == 0 || it.score.result == null }
        val index = if (firstNotScored == null) sortedScores.count() - 1
        else sortedScores.indexOf(firstNotScored)
        setScoreId(sortedScores[index].player.id, sortedScores[index].hole.holeId)
    }

    /*
    /*
    Used to force refresh the observers of the LiveData object.
     */
    private fun refresh() {
        (currentRound as RefreshableLiveData).refresh()
    }*/

    fun update(score: Score) = coroutineScope.launch {
        repository.update(score)
    }


    fun previousScore() {
        val indexOfCurrentPlayer = playerIds.indexOf(scoreIdLiveData.value!!.first)
        if (indexOfCurrentPlayer == 0) { // Last player on previous hole.
            val indexOfCurrentHole = holeIds.indexOf(scoreIdLiveData.value!!.second)
            val newHoleIndex = Math.floorMod(indexOfCurrentHole - 1, holeIds.count())
            val newHoleId = holeIds[newHoleIndex]
            setScoreId(playerIds.last(), newHoleId)
        } else {
            previousPlayer()
        }
    }

    fun nextScore() {
        val indexOfCurrentPlayer = playerIds.indexOf(scoreIdLiveData.value!!.first)
        if (indexOfCurrentPlayer == playerIds.count() - 1) { // Last player on current hole
            nextHole()
        } else {
            nextPlayer()
        }
    }

    /*
    Previous player BUT current hole.
     */
    fun previousPlayer() {
        val indexOfCurrentPlayer = playerIds.indexOf(scoreIdLiveData.value!!.first)
        val newPlayerIndex = Math.floorMod(indexOfCurrentPlayer - 1, playerIds.count())
        val newPlayerId = playerIds[newPlayerIndex]
        setScoreId(newPlayerId, scoreIdLiveData.value!!.second)
    }

    /*
    Next player BUT current hole.
     */
    fun nextPlayer() {
        val indexOfCurrentPlayer = playerIds.indexOf(scoreIdLiveData.value!!.first)
        val newPlayerIndex = Math.floorMod(indexOfCurrentPlayer + 1, playerIds.count())
        val newPlayerId = playerIds[newPlayerIndex]
        setScoreId(newPlayerId, scoreIdLiveData.value!!.second)
    }

    /*
    Previous hole and first player.
     */
    fun previousHole() {
        val indexOfCurrentHole = holeIds.indexOf(scoreIdLiveData.value!!.second)
        val newHoleIndex = Math.floorMod(indexOfCurrentHole - 1, holeIds.count())
        val newHoleId = holeIds[newHoleIndex]
        setScoreId(playerIds.first(), newHoleId)
    }

    /*
    Next hole first player.
     */
    fun nextHole() {
        val indexOfCurrentHole = holeIds.indexOf(scoreIdLiveData.value!!.second)
        val newHoleIndex = Math.floorMod(indexOfCurrentHole + 1, holeIds.count())
        val newHoleId = holeIds[newHoleIndex]
        setScoreId(playerIds.first(), newHoleId)
    }

    /*
    Sets the result of the current score. Calls repository.update as well.
     */
    fun setResult(resultToSet: Int) {
        val scoreWithPLayerAndHole = currentScore.value
            ?: throw IllegalStateException("Value inside current score live data was null.")
        val score = scoreWithPLayerAndHole.score
        score.result = resultToSet
        score.didNotFinish = false
        update(score)
    }

    /*
    Toggles OB and updates the score.
     */
    fun toggleOb() {
        val scoreWithPLayerAndHole = currentScore.value
            ?: throw IllegalStateException("Value inside current score live data was null.")
        val score = scoreWithPLayerAndHole.score
        score.isOutOfBounds = !score.isOutOfBounds
        update(score)
    }

    /*
    Toggles OB and updates the score.
     */
    fun toggleDnf() {
        val scoreWithPLayerAndHole = currentScore.value
            ?: throw IllegalStateException("Value inside current score live data was null.")
        val score = scoreWithPLayerAndHole.score
        score.didNotFinish = !score.didNotFinish
        if (score.didNotFinish) score.result = null
        update(score)
    }

    fun plusMinus(player: Player): String {
        currentRound.refresh()
        val round = currentRound.value
            ?: throw IllegalStateException("Value inside current round live data was null.")
        val scores = round.scores.filter { it.player == player }
        var totalResult = 0
        scores.forEach { score ->
            val result = score.score.result
            val par = score.hole.par
            if (result != null) totalResult += result - par
        }
        return if (totalResult > 0) "+$totalResult"
        else totalResult.toString()
    }

    companion object {

        fun plusMinus(player: Player, scores: List<ScoreWithPlayerAndHole>): String {
            val scoresForPlayer = scores.filter { it.player == player }
            var totalResult = 0
            scoresForPlayer.forEach { score ->
                val result = score.score.result
                val par = score.hole.par
                if (result != null) totalResult += result - par
            }
            return if (totalResult > 0) "+$totalResult"
            else totalResult.toString()
        }

        fun plusMinus(
            player: Player,
            scores: List<ScoreWithPlayerAndHole>,
            holeNumber: Int
        ): String? {
            val scoresForPlayer =
                scores.filter { it.player == player && it.hole.holeNumber <= holeNumber }
            var totalResult = 0
            scoresForPlayer.forEach { score ->
                val result = score.score.result
                val par = score.hole.par
                if (result != null) totalResult += result - par
            }
            return if (totalResult > 0) "+$totalResult"
            else totalResult.toString()
        }

        /*
        Returns the scoring term for the hole.
        */
        fun getScoringTerm(result: Int, par: Int): ScoringTerm {
            if (result <= 0) return ScoringTerm.NoName
            if (result == 1) return ScoringTerm.Ace
            return when (result - par) {
                -4 -> ScoringTerm.Condor
                -3 -> ScoringTerm.Albatross
                -2 -> ScoringTerm.Eagle
                -1 -> ScoringTerm.Birdie
                0 -> ScoringTerm.Par
                1 -> ScoringTerm.Bogey
                2 -> ScoringTerm.DoubleBogey
                3 -> ScoringTerm.TripleBogey
                else -> ScoringTerm.NoName
            }
        }
    }
}

enum class ScoringTerm() {
    NoName,
    Ace,
    Condor,
    Albatross,
    Eagle,
    Birdie,
    Par,
    Bogey,
    DoubleBogey,
    TripleBogey
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