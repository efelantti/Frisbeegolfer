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

    @Insert
    suspend fun insertAndGetId(player: Player): Long

    @Delete
    suspend fun delete(player: Player)

    @Update
    suspend fun update(player: Player)

    @Query("SELECT * from Player WHERE id=:id")
    fun getPlayerById(id: Long): LiveData<Player>
}