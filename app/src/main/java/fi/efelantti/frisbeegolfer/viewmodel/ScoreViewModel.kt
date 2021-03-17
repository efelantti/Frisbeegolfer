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
    val sortedScores: LiveData<List<ScoreWithPlayerAndHole>>
    private var currentScoreIndex: Int = -1

            init {
                val database = FrisbeegolferRoomDatabase.getDatabase(
                    application,
                    viewModelScope
                )
                repository = Repository(database)
                currentRound = repository.getRoundWithRoundId(roundId)
                sortedScores = sortCurrentRound()
            }

            fun update(score: Score) = viewModelScope.launch(Dispatchers.IO) {
                repository.update(score)
            }

            fun sortCurrentRound(): LiveData<List<ScoreWithPlayerAndHole>>
            {
                return Transformations.map(currentRound) {
                    it?.let{
                        it.scores.sortedWith(compareBy({ it.hole.holeNumber }, {it.player.id}))
                    }
                }
            }

            fun getCurrentScore(): ScoreWithPlayerAndHole?
            {
                return sortedScores.value?.get(currentScoreIndex)
            }

            fun initCurrentScoreIndex(scores: List<ScoreWithPlayerAndHole>)
            {
                val firstNotScoredHole= scores.filter {it.score.result == 0}.firstOrNull()
                if (firstNotScoredHole == null) currentScoreIndex = scores.count()-1
                else currentScoreIndex = scores.indexOf(firstNotScoredHole)
            }
        }