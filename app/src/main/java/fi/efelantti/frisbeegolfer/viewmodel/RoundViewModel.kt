package fi.efelantti.frisbeegolfer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fi.efelantti.frisbeegolfer.FrisbeegolferRoomDatabase
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.model.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoundViewModel(application: Application) : AndroidViewModel(application){
    private val repository: Repository
    val allRounds: LiveData<List<RoundWithCourseAndScores>>

    init {
        val database = FrisbeegolferRoomDatabase.getDatabase(
            application,
            viewModelScope
        )
        repository = Repository(database)
        allRounds = repository.allRounds
    }

    fun delete(round: RoundWithCourseAndScores) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(round)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * Round should be inserted without the scores.
     */
    fun insert(round: Round) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(round)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * Round should be inserted without the scores.
     */
    fun insert(score: Score) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(score)
    }

    fun update(score: Score) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(score)
    }

}