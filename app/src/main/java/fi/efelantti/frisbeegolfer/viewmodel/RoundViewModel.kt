package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.model.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoundViewModel(private val repository: Repository) : ViewModel() {

    val allRounds: LiveData<List<RoundWithCourseAndScores>> = repository.allRounds

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

@Suppress("UNCHECKED_CAST")
class RoundViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (RoundViewModel(repository) as T)
}