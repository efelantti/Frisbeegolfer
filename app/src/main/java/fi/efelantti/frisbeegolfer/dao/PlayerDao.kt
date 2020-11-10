package fi.efelantti.frisbeegolfer.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fi.efelantti.frisbeegolfer.model.Player

@Dao
interface PlayerDao {

    @Query("SELECT * from Player")
    fun getPlayers(): LiveData<List<Player>>

    @Insert
    suspend fun insert(player: Player)

    @Delete
    suspend fun delete(player: Player)

    @Update
    fun update(player: Player)
}