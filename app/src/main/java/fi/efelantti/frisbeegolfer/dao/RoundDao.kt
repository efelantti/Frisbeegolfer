package fi.efelantti.frisbeegolfer.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import fi.efelantti.frisbeegolfer.model.*
import java.time.OffsetDateTime


@Dao
interface RoundDao {

    @Insert
    suspend fun insert(round: Round)

    @Insert
    suspend fun insertAll(rounds: List<Round>)

    @Insert
    suspend fun insert(score: Score): Long

    @Insert
    suspend fun insertScores(scores: List<Score>)

    @Delete
    suspend fun delete(round: Round)

    @Delete
    suspend fun delete(score: Score)

    @Update
    suspend fun update(score: Score)

    @Transaction
    @Query("SELECT * FROM Round ORDER BY datetime(dateStarted) DESC")
    fun getRounds(): LiveData<List<RoundWithCourseAndScores>>

    @Transaction
    @Query("SELECT * FROM Round WHERE dateStarted =:roundId")
    fun getRoundWithId(roundId: OffsetDateTime): LiveData<RoundWithCourseAndScores>

    /*
    Gets the best (minimum) result, average and latest result from the score table.
    Latest result is determined by ordering scoreId's and then choosing the result from the score with the highest scoreId.
    Latest takes into account the 0, which is currently default value for score. Fix: set score default to null and have this query only take non-null result.
     */
    @Transaction
    @Query(
        "SELECT (SELECT MIN(result) AS bestResult FROM Score WHERE playerId=:playerId AND holeId=:holeId) AS bestResult, (SELECT AVG(result) AS avgResult FROM Score WHERE playerId=:playerId AND holeId=:holeId) AS avgResult,(SELECT result AS latestResult FROM Score WHERE playerId=:playerId AND holeId=:holeId AND result is not null ORDER BY datetime(parentRoundId) desc LIMIT 1) AS latestResult"
    )
    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics>

    @Transaction
    @Query("SELECT * FROM Score WHERE parentRoundId=:roundId AND playerId =:playerId AND holeId=:holeId LIMIT 1")
    fun getScore(
        roundId: OffsetDateTime,
        playerId: Long,
        holeId: Long
    ): LiveData<ScoreWithPlayerAndHole>

    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery?): Int
}