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
    var currentScoreIndex: Int = -1
    val currentRound: RefreshableLiveData<RoundWithScores>

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
    }

    fun refresh() {
        currentRound.refresh()
    }

    fun update(score: Score) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(score)
    }

    fun sortRound(scores: List<ScoreWithPlayerAndHole>): List<ScoreWithPlayerAndHole> {
        return scores.sortedWith(compareBy({ it.hole.holeNumber }, { it.player.id }))
        }

    fun getCurrentScore(): ScoreWithPlayerAndHole? {
        return currentRound.value?.scores?.get(currentScoreIndex)
    }

    fun initCurrentScoreIndex(scores: List<ScoreWithPlayerAndHole>) {
        if(currentScoreIndex == -1) {
            val firstNotScoredHole = scores.filter { it.score.result == 0 }.firstOrNull()
            if (firstNotScoredHole == null) currentScoreIndex = scores.count() - 1
            else currentScoreIndex = scores.indexOf(firstNotScoredHole)
        }
    }
}