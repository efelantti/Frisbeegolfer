package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.model.*
import java.time.OffsetDateTime

interface IRepository {
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val playerDao: PlayerDao
    val courseDao: CourseDao
    val roundDao: RoundDao
    val allPlayers: LiveData<List<Player>>
    val allCourses: LiveData<List<CourseWithHoles>>
    val allRounds: LiveData<List<RoundWithCourseAndScores>>

    suspend fun insert(player: Player)

    suspend fun insert(round: Round)
    suspend fun insert(score: Score)
    suspend fun update(player: Player)
    suspend fun update(score: Score)

    suspend fun delete(hole: Hole)

    suspend fun delete(round: RoundWithCourseAndScores)

    suspend fun update(course: CourseWithHoles)

    suspend fun insertCourseWithHoles(course: CourseWithHoles)
    fun getCourseWithHolesById(id: Long): LiveData<CourseWithHoles>

    fun getRoundWithRoundId(roundId: OffsetDateTime): LiveData<RoundWithCourseAndScores>

    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics>
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class Repository(// Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    override val playerDao: PlayerDao, override val courseDao: CourseDao,
    override val roundDao: RoundDao
) : IRepository {

    override val allPlayers: LiveData<List<Player>> = playerDao.getPlayers()
    override val allCourses: LiveData<List<CourseWithHoles>> = courseDao.getCoursesWithHoles()
    override val allRounds: LiveData<List<RoundWithCourseAndScores>> = roundDao.getRounds()

    override suspend fun insert(player: Player) {
        playerDao.insert(player)
    }

    override suspend fun insert(round: Round) {
        roundDao.insert(round)
    }

    override suspend fun insert(score: Score) {
        roundDao.insert(score)
    }

    override suspend fun update(player: Player) {
        playerDao.update(player)
    }

    override suspend fun update(score: Score) {
        roundDao.update(score)
    }

    override suspend fun delete(hole: Hole) {
        courseDao.delete(hole)
    }

    override suspend fun delete(round: RoundWithCourseAndScores) {
        for(score: ScoreWithPlayerAndHole in round.scores)
        {
            roundDao.delete(score.score)
        }
        roundDao.delete(round.round)
    }

    override suspend fun update(course: CourseWithHoles)
    {
        courseDao.updateAll(course.holes)
        courseDao.update(course.course)
    }

    override suspend fun insertCourseWithHoles(course: CourseWithHoles) {
        val courseId = courseDao.insert(course.course)
        for (hole in course.holes) {
            hole.parentCourseId = courseId
        }
        courseDao.insertAll(course.holes)
    }

    override fun getCourseWithHolesById(id: Long): LiveData<CourseWithHoles>
    {
        return courseDao.getCourseWithHolesWithId(id)
    }

    override fun getRoundWithRoundId(roundId: OffsetDateTime): LiveData<RoundWithCourseAndScores>
    {
        return roundDao.getRoundWithId(roundId)
    }

    override fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics>
    {
        return roundDao.getHoleStatistics(playerId, holeId)
    }
}