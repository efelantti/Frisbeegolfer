package fi.efelantti.frisbeegolfer.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fi.efelantti.frisbeegolfer.model.HoleStatistics
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

    /*
    Gets the best (minimum) result, average, standard deviation and latest result from the score table.
     */
    @Transaction
    @Query("SELECT MIN(result) AS bestResult, AVG(result) AS avgResult, STDEVP(result) AS sdResult FROM Score WHERE playerId=:playerId AND holeId=:holeId UNION SELECT result AS latestResult FROM Score WHERE playerId=:playerId AND holeId=:holeId ORDER BY id desc")
    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics>
}