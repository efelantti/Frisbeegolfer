package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import androidx.room.*

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