package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.getViewModelScope
import fi.efelantti.frisbeegolfer.model.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PlayerViewModel(
    coroutineScopeProvider: CoroutineScope? = null,
    private val repository: IRepository
) : ViewModel() {

    private val coroutineScope = getViewModelScope(coroutineScopeProvider)

    val allPlayers: LiveData<List<Player>> = repository.allPlayers

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(player: Player) = coroutineScope.launch {
        repository.insert(player)
    }

    fun getPlayerById(id: Long): LiveData<Player> {
        return repository.getPlayerById(id)
    }

    /**
     * Launching a new coroutine to update the data in a non-blocking way
     */
    fun update(player: Player) = coroutineScope.launch {
        repository.update(player)
    }

    fun playerExists(name: String): LiveData<Boolean> {
        return repository.playerExists(name)
    }
}

@Suppress("UNCHECKED_CAST")
class PlayerViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (PlayerViewModel(null, repository) as T)
}
