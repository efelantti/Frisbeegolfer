package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class Repository(private val playerDao: PlayerDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allPlayers: LiveData<List<Player>> = playerDao.getPlayers()

    suspend fun insert(player: Player) {
        playerDao.insert(player)
    }

    suspend fun update(player: Player) {
        playerDao.update(player)
    }
}