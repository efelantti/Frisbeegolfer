package fi.efelantti.frisbeegolfer.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.model.Score
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
    @Query("SELECT * FROM Round ORDER BY datetime(dateStarted) DESC")
    fun getRounds(): LiveData<List<RoundWithCourseAndScores>>

    @Transaction
    @Query("SELECT * FROM Round WHERE dateStarted =:roundId")
    fun getRoundWithId(roundId: OffsetDateTime): LiveData<RoundWithCourseAndScores>
}