package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.*
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.LiveDataState
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

    private var _state: MutableLiveData<LiveDataState> =
        MutableLiveData<LiveDataState>(LiveDataState.LOADING)
    var state: LiveData<LiveDataState> = _state

    //val allPlayers: LiveData<List<Player>> = repository.allPlayers

    // with a Transformation
    // this would be the method which returns the database LiveData
    fun allPlayers(): LiveData<List<Player>> {
        // the view should show a loading indicator
        _state.value = LiveDataState.LOADING
        // we don't actually map anything, we just use the map function to get
        // a callback of when the database's LiveData has finished loading
        return Transformations.map(repository.allPlayers) {
            // the database has just finished fetching the data from the database
            // and after this method returns it will be available to the observer
            // in the fragment.
            // we also need to dismiss the loading indicator
            _state.value = LiveDataState.SUCCESS
            return@map it
        }
    }

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

    fun delete(playerToDelete: Player) = coroutineScope.launch {
        repository.delete(playerToDelete)
    }
}

@Suppress("UNCHECKED_CAST")
class PlayerViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (PlayerViewModel(null, repository) as T)
}
