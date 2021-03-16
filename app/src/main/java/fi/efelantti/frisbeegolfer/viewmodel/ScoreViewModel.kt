package fi.efelantti.frisbeegolfer.viewmodel

import android.app.Application
import androidx.lifecycle.*
import fi.efelantti.frisbeegolfer.FrisbeegolferRoomDatabase
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.RoundWithScores
import fi.efelantti.frisbeegolfer.model.Score
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class ScoreViewModel(application: Application, private val roundId: OffsetDateTime) : AndroidViewModel(application){

    private val repository: Repository
    val currentRound: LiveData<RoundWithScores>

            init {
                val database = FrisbeegolferRoomDatabase.getDatabase(
                    application,
                    viewModelScope
                )
                repository = Repository(database)
                currentRound = repository.getRoundWithRoundId(roundId)
            }

            fun update(score: Score) = viewModelScope.launch(Dispatchers.IO) {
                repository.update(score)
            }

            // TODO - Create with Transformations.map
            fun getCurrentScore(currentRound: RoundWithScores): ScoreWithPlayerAndHole
            {
                val sortedList = currentRound.scores.sortedWith(compareBy({ it.hole.holeNumber }, {it.player.id}))
                val firstNotScoredHole= sortedList.filter {it.score.result == 0}.firstOrNull()
                if (firstNotScoredHole == null) return sortedList.first()
                else return firstNotScoredHole
            }
        }