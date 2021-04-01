package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.model.Player

class FakePlayerDao: PlayerDao {
    override fun getPlayers(): LiveData<List<Player>> {
        TODO("Not yet implemented")
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