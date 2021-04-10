package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerViewModel(private val repository: IRepository) : ViewModel() {

    val allPlayers: LiveData<List<Player>> = repository.allPlayers

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(player: Player) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(player)
    }

    /**
     * Launching a new coroutine to update the data in a non-blocking way
     */
    fun update(player: Player) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(player)
    }
}

@Suppress("UNCHECKED_CAST")
class PlayerViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (PlayerViewModel(repository) as T)
}
