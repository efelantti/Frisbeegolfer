package fi.efelantti.frisbeegolfer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import fi.efelantti.frisbeegolfer.FrisbeegolferRoomDatabase
import fi.efelantti.frisbeegolfer.RefreshableLiveData
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.model.Score
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class ScoreViewModel(application: Application, private val roundId: OffsetDateTime) :
    AndroidViewModel(application) {

    private val repository: Repository
    private val mCurrentRound: LiveData<RoundWithCourseAndScores>
    private var currentScoreIndex: Int = -1
    val currentRound: RefreshableLiveData<RoundWithCourseAndScores>
    private val sortedScores: LiveData<List<ScoreWithPlayerAndHole>>
    val currentScore: LiveData<ScoreWithPlayerAndHole>
    private var numberOfPlayers = -1
    private var numberOfHoles = -1

    init {
        val database = FrisbeegolferRoomDatabase.getDatabase(
            application,
            viewModelScope
        )
        repository = Repository(database)
        mCurrentRound = RefreshableLiveData {
            repository.getRoundWithRoundId(roundId)
        }
        currentRound = mCurrentRound
        sortedScores = Transformations.map(mCurrentRound) {
            it?.let {
                sortRound(it.scores)
            }
        }
        currentScore = Transformations.map(sortedScores) {
            it?.let {
                initCurrentScoreIndex(it)
                initHelperValues(it)
                it[currentScoreIndex]
            }
        }
    }

    /*
    Used to force refresh the observers of the LiveData object.
     */
    private fun refresh() {
        currentRound.refresh()
    }

    fun update(score: Score) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(score)
    }

    /*
    Sort the scores list by (hole number, player id). TODO - Avoid this by changing @RELATION to two queries?
    (Refer to https://stackoverflow.com/questions/48315261/using-rooms-relation-with-order-by/64321494)
     */
    private fun sortRound(scores: List<ScoreWithPlayerAndHole>): List<ScoreWithPlayerAndHole> {
        return scores.sortedWith(compareBy({ it.hole.holeNumber }, { it.player.name }))
    }

    /*
    Used to initialize current score, if it's not already initialized.
    Sets the index to either the index of the first hole that's not yet scored OR to the index of the last hole, if all are already scored.
     */
    private fun initCurrentScoreIndex(sortedScores: List<ScoreWithPlayerAndHole>) {
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
        if(numberOfHoles == -1) numberOfHoles = scores.distinctBy { it.hole.holeId }.count()
        if (numberOfPlayers == -1) numberOfPlayers = scores.distinctBy { it.player.id }.count()
    }

    /*
    Adds one to the current score index. To be used after scoring a hole.
     */
    fun incrementIndex() {
        addToIndex(1)
    }

    fun decrementIndex() {
        addToIndex(-1)
    }

    /*
    Helper function used for adding a value to the [currentScoreIndex].
     */
    private fun addToIndex(valueToAdd: Int) {
        currentScoreIndex = Math.floorMod(currentScoreIndex + valueToAdd, numberOfHoles*numberOfPlayers)
        // Other option, but this does not allow to use "Previous" on zero index.
        // currentScoreIndex = (currentScoreIndex + valueToAdd).rem(numberOfHoles*numberOfPlayers)
        refresh()
    }

    /*
    Next player BUT current hole / is this needed?
     */
    fun nextPlayer() {
        refresh()
    }

    /*
    Set index to next hole BUT current player / is this needed?
     */
    fun nextHole() {
        refresh()
    }

    fun setResult(scoreToSet: Int) {
        var currentScore = this.currentScore.value
        if (currentScore == null) throw IllegalArgumentException("Cannot set score - current score was null.")
        currentScore.score.result = scoreToSet
        update(currentScore.score)
    }
}