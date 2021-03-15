package fi.efelantti.frisbeegolfer.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithScores
import fi.efelantti.frisbeegolfer.model.Score
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import java.time.OffsetDateTime

@Dao
interface RoundDao {

    @Insert
    suspend fun insert(round: Round)

    @Insert
    suspend fun insert(score: Score): Long

    @Delete
    suspend fun delete(round: Round)

    @Delete
    suspend fun delete(score: Score)

    @Update
    fun update(score: Score)

    @Transaction
    @Query("SELECT * FROM Round")
    fun getRounds(): LiveData<List<RoundWithScores>>

    @Transaction
    @Query("SELECT * FROM Round WHERE dateStarted =:roundId")
    fun getRoundWithId(roundId: OffsetDateTime): LiveData<RoundWithScores>
}