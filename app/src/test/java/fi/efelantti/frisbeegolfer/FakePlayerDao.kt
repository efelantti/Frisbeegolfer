package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.model.Player

class FakePlayerDao: PlayerDao {

    private val players: MutableLiveData<List<Player>> = MutableLiveData<List<Player>>(emptyList())

    override fun getPlayers(): LiveData<List<Player>> {
        return players
    }

    override suspend fun insert(player: Player) {
        TODO("Not yet implemented")
    }

    override suspend fun insertAndGetId(player: Player): Long {
        TODO("Not yet implemented")
    }

    override suspend fun delete(player: Player) {
        TODO("Not yet implemented")
    }

    override fun update(player: Player) {
        TODO("Not yet implemented")
    }
}