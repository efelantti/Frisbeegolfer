package fi.efelantti.frisbeegolfer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fi.efelantti.frisbeegolfer.FrisbeegolferRoomDatabase
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.RoundWithScores
import fi.efelantti.frisbeegolfer.model.Score
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

}