package fi.efelantti.frisbeegolfer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application){
    private val repository: Repository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allPlayers: LiveData<List<Player>>

    init {
        val playerDao = FrisbeegolferRoomDatabase.getDatabase(application, viewModelScope).playerDao()
        repository = Repository(playerDao)
        allPlayers = repository.allPlayers
    }

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