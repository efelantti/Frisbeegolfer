package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import fi.efelantti.frisbeegolfer.model.*
import java.time.OffsetDateTime

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class Repository(database: FrisbeegolferRoomDatabase) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    private val playerDao = database.playerDao()
    private val courseDao = database.courseDao()
    private val roundDao = database.roundDao()
    val allPlayers: LiveData<List<Player>> = playerDao.getPlayers()
    val allCourses: LiveData<List<CourseWithHoles>> = courseDao.getCoursesWithHoles()
    val allRounds : LiveData<List<RoundWithCourseAndScores>> = roundDao.getRounds()

    suspend fun insert(player: Player) {
        playerDao.insert(player)
    }

    suspend fun insert(round: Round){
        roundDao.insert(round)
    }

    suspend fun insert(score: Score) {
        roundDao.insert(score)
    }

    fun update(player: Player) {
        playerDao.update(player)
    }

    fun update(score: Score) {
        roundDao.update(score)
    }

    suspend fun delete(hole: Hole) {
        courseDao.delete(hole)
    }

    suspend fun delete(round: RoundWithCourseAndScores) {
        for(score: ScoreWithPlayerAndHole in round.scores)
        {
            roundDao.delete(score.score)
        }
        roundDao.delete(round.round)
    }

    suspend fun update(course: CourseWithHoles)
    {
        courseDao.updateAll(course.holes)
        courseDao.update(course.course)
    }

    suspend fun insertCourseWithHoles(course: CourseWithHoles) {
        val courseId = courseDao.insert(course.course)
        for (hole in course.holes) {
            hole.parentCourseId = courseId
        }
        courseDao.insertAll(course.holes)
    }

    fun getCourseWithHolesById(id: Long): CourseWithHoles
    {
        return courseDao.getCourseWithHolesWithId(id)
    }

    fun getRoundWithRoundId(roundId: OffsetDateTime): LiveData<RoundWithCourseAndScores>
    {
        return roundDao.getRoundWithId(roundId)
    }

    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics>
    {
        return roundDao.getHoleStatistics(playerId, holeId)
    }
}