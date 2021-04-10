package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.getViewModelScope
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.model.Score
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RoundViewModel(
    private val coroutineScopeProvider: CoroutineScope? = null,
    private val repository: IRepository
) : ViewModel() {

    private val coroutineScope = getViewModelScope(coroutineScopeProvider)
    val allRounds: LiveData<List<RoundWithCourseAndScores>> = repository.allRounds

    fun delete(round: RoundWithCourseAndScores) = coroutineScope.launch {
        repository.delete(round)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * Round should be inserted without the scores.
     */
    fun insert(round: Round) = coroutineScope.launch {
        repository.insert(round)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * Round should be inserted without the scores.
     */
    fun insert(score: Score) = coroutineScope.launch {
        repository.insert(score)
    }

    fun update(score: Score) = coroutineScope.launch {
        repository.update(score)
    }

}

@Suppress("UNCHECKED_CAST")
class RoundViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (RoundViewModel(null, repository) as T)
}