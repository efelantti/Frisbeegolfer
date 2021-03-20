package fi.efelantti.frisbeegolfer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import fi.efelantti.frisbeegolfer.FrisbeegolferRoomDatabase
import fi.efelantti.frisbeegolfer.RefreshableLiveData
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.RoundWithScores
import fi.efelantti.frisbeegolfer.model.Score
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class ScoreViewModel(application: Application, private val roundId: OffsetDateTime) :
    AndroidViewModel(application) {

    private val repository: Repository
    private val mCurrentRound: LiveData<RoundWithScores>
    private var currentScoreIndex: Int = -1
    val currentRound: RefreshableLiveData<RoundWithScores>
    private val sortedScores: LiveData<List<ScoreWithPlayerAndHole>>
    val currentScore: LiveData<ScoreWithPlayerAndHole>


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
        sortedScores = Transformations.map(mCurrentRound,
            {
                sortRound(it.scores)
            })
        currentScore = Transformations.map(sortedScores, {
            it.get(currentScoreIndex)
        })
    }

    /*
    Used to force refresh the observers of the LiveData object.
     */
    fun refresh() {
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
        return scores.sortedWith(compareBy({ it.hole.holeNumber }, { it.player.id }))
        }

    /*
    Used to initialize current score, if it's not already initialized.
    Sets the index to either the index of the first hole that's not yet scored OR to the index of the last hole, if all are already scored.
     */
    fun initCurrentScoreIndex() {
        if(currentScoreIndex == -1) {
            val scores = sortedScores.value
            if (scores == null) throw IllegalArgumentException("Cannot init current score index - sorted scores was null.")
            val firstNotScoredHole = scores.filter { it.score.result == 0 }.firstOrNull()
            if (firstNotScoredHole == null) currentScoreIndex = scores.count() - 1
            else currentScoreIndex = scores.indexOf(firstNotScoredHole)
        }
    }
}