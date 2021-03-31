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
    Gets the best (minimum) result, average and latest result from the score table.
    Latest result is determined by ordering scoreId's and then choosing the result from the score with the highest scoreId.
     */
    @Transaction
    @Query("SELECT" +
            "(SELECT MIN(result) AS bestResult FROM Score WHERE playerId=:playerId AND holeId=:holeId) AS bestResult," +
            "(SELECT AVG(result) AS avgResult FROM Score WHERE playerId=:playerId AND holeId=:holeId) AS avgResult," +
            "(SELECT result AS latestResult FROM Score WHERE playerId=:playerId AND holeId=:holeId ORDER BY datetime(parentRoundId) desc LIMIT 1) AS latestResult")
    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics>
}