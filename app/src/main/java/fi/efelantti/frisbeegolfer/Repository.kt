package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.model.*
import java.time.OffsetDateTime


interface IRepository {
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val database: FrisbeegolferRoomDatabase
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

    fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics?>
    fun getScore(
        roundId: OffsetDateTime,
        playerId: Long,
        holeId: Long
    ): LiveData<ScoreWithPlayerAndHole?>

    fun getPlayerById(id: Long): LiveData<Player>
    fun playerExists(name: String): LiveData<Boolean>
    fun courseExists(name: String, city: String): LiveData<Boolean>
    suspend fun delete(playerToDelete: Player)
    suspend fun delete(course: CourseWithHoles)

    fun checkpoint()
    suspend fun updateStartTimeForRoundWithId(roundId: OffsetDateTime, newRoundId: OffsetDateTime)
    suspend fun updateStartTimeForRoundAndScores(
        roundId: OffsetDateTime,
        newRoundId: OffsetDateTime
    )

    fun getHoleById(id: Long): LiveData<Hole?>

    val allData: LiveData<Triple<List<Player>, List<CourseWithHoles>, List<RoundWithCourseAndScores>>>
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class Repository(// Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    override val database: FrisbeegolferRoomDatabase,
    override val playerDao: PlayerDao,
    override val courseDao: CourseDao,
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

    override suspend fun updateStartTimeForRoundWithId(
        roundId: OffsetDateTime,
        newRoundId: OffsetDateTime
    ) {
        roundDao.updateStartTimeForRoundWithId(roundId, newRoundId)
    }

    override suspend fun updateStartTimeForRoundAndScores(
        roundId: OffsetDateTime,
        newRoundId: OffsetDateTime
    ) {
        roundDao.updateRoundIdForRoundAndScores(roundId, newRoundId)
    }

    override suspend fun delete(playerToDelete: Player) {
        playerDao.delete(playerToDelete)
    }

    override suspend fun delete(hole: Hole) {
        courseDao.delete(hole)
    }

    // Holes are deleted because of foreign key.
    override suspend fun delete(course: CourseWithHoles) {
        courseDao.delete(course.course)
    }

    override suspend fun delete(round: RoundWithCourseAndScores) {
        for (score: ScoreWithPlayerAndHole in round.scores) {
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

    override fun getRoundWithRoundId(roundId: OffsetDateTime): LiveData<RoundWithCourseAndScores> {
        return roundDao.getRoundWithId(roundId)
    }

    override fun getHoleStatistics(playerId: Long, holeId: Long): LiveData<HoleStatistics?> {
        return roundDao.getHoleStatistics(playerId, holeId)
    }

    override fun getScore(
        roundId: OffsetDateTime,
        playerId: Long,
        holeId: Long
    ): LiveData<ScoreWithPlayerAndHole?> {
        return roundDao.getScore(roundId, playerId, holeId)
    }

    override fun getPlayerById(id: Long): LiveData<Player> {
        return playerDao.getPlayerById(id)
    }

    override fun getHoleById(id: Long): LiveData<Hole?> {
        return courseDao.getHoleById(id)
    }

    // https://stackoverflow.com/questions/63528738/kotlin-wait-for-multiple-livedata-to-be-observe-before-running-function
    // TODO - Continue.
    override val allData: LiveData<Triple<List<Player>, List<CourseWithHoles>, List<RoundWithCourseAndScores>>> =
        object :
            MediatorLiveData<Triple<List<Player>, List<CourseWithHoles>, List<RoundWithCourseAndScores>>>() {
            var players: List<Player>? = null
            var courses: List<CourseWithHoles>? = null
            var scores: List<RoundWithCourseAndScores>? = null

            init {
                addSource(allPlayers) { players ->
                    this.players = players
                    if (courses != null && scores != null) {
                        value = Triple(players, courses!!, scores!!)
                    }
                }
                addSource(allCourses) { courses ->
                    this.courses = courses
                    if (players != null && scores != null) {
                        value = Triple(players!!, courses, scores!!)
                    }
                }
                addSource(allRounds) { rounds ->
                    this.scores = rounds
                    if (players != null && courses != null) {
                        value = Triple(players!!, courses!!, rounds)
                    }
                }
            }
        }

    override fun playerExists(name: String): LiveData<Boolean> {
        return playerDao.playerExists(name)
    }

    override fun courseExists(name: String, city: String): LiveData<Boolean> {
        return courseDao.courseExists(name, city)
    }

    /* Android database has three files under /data/data/com.package.app/databases/
    ** test.db, test.db-shm, test.db-wal - those extra files have recent commits.
    ** To merge data from other shm and wal files to db, run following method - useful before taking backup.
    */
    override fun checkpoint() {
        database.databaseWriteExecutor.execute { roundDao.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)")) }
    }
}