package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.model.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.io.InvalidObjectException
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class RoundDaoTests {
    private lateinit var roundDao: RoundDao
    private lateinit var courseDao: CourseDao
    private lateinit var playerDao: PlayerDao
    private lateinit var db: FrisbeegolferRoomDatabase
    private var roundId: OffsetDateTime = OffsetDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)
    private var courseId = -1L
    private var playerId = -1L
    private lateinit var holeIds: List<Long>

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, FrisbeegolferRoomDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).allowMainThreadQueries()
            .build()
        roundDao = db.roundDao()
        courseDao = db.courseDao()
        playerDao = db.playerDao()

        val course = Course(name = "Test course", city = "Test city")
        courseId = courseDao.insert(course)
        val holes = listOf(
            Hole(parentCourseId = courseId, par = 3, holeNumber = 1, lengthMeters = 100),
            Hole(parentCourseId = courseId, par = 2, holeNumber = 2),
            Hole(parentCourseId = courseId, par = 4, holeNumber = 3)
        )

        val player = Player(name = "Tester")

        playerId = playerDao.insertAndGetId(player)
        holeIds = courseDao.insertAllAndGetIds(holes)

        val scores: List<Score> = listOf(
            Score(parentRoundId = roundId, playerId = playerId, holeId = holeIds[0]),
            Score(parentRoundId = roundId, playerId = playerId, holeId = holeIds[1]),
            Score(parentRoundId = roundId, playerId = playerId, holeId = holeIds[2])
        )

        val round = Round(roundId, courseId)
        roundDao.insert(round)
        scores.forEach { roundDao.insert(it) }
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        // TODO - Can't close the database - otherwise test crashes... See https://stackoverflow.com/questions/61044457/android-room-instrumented-tests-crashing-when-properly-closing-db-connection
        // db.close()
    }

    @Test
    @Throws(Exception::class)
    fun readRounds() = runBlocking {
        val allRounds = roundDao.getRounds()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        val result = allRounds.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        assertThat(result.count(), equalTo(1))

        val resultRound = result[0]

        assertThat(resultRound.round.dateStarted, equalTo(roundId))
        assertThat(resultRound.round.courseId, equalTo(courseId))
    }

    @Test
    @Throws(Exception::class)
    fun readRoundWithId() = runBlocking {
        val roundWithId = roundDao.getRoundWithId(roundId)

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        val result = roundWithId.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        assertThat(result.round.dateStarted, equalTo(roundId))
        assertThat(result.round.courseId, equalTo(courseId))
    }


    @Test
    @Throws(Exception::class)
    fun updateScores() = runBlocking {
        var allRounds = roundDao.getRounds()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        var result = allRounds.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        var resultRound = result[0]
        resultRound.scores[0].score.result = 3
        resultRound.scores[1].score.result = 2
        resultRound.scores[2].score.result = 4

        resultRound.scores.forEach { roundDao.update(it.score) }

        allRounds = roundDao.getRounds()
        result = allRounds.getValueBlocking()
            ?: throw InvalidObjectException("null returned as courses")
        assertThat(result.count(), equalTo(1))
        resultRound = result[0]
        assertThat(resultRound.scores.count(), equalTo(3))

        val sortedScores = resultRound.scores.sortedBy { it.hole.holeNumber }
        assertThat(sortedScores[0].score.result, equalTo(3))
        assertThat(sortedScores[1].score.result, equalTo(2))
        assertThat(sortedScores[2].score.result, equalTo(4))
    }

    @Test
    @Throws(Exception::class)
    fun deleteRounds() = runBlocking {
        var allRounds = roundDao.getRounds()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        var result = allRounds.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        val resultRound = result[0]

        roundDao.delete(resultRound.round)
        allRounds = roundDao.getRounds()
        result = allRounds.getValueBlocking()
            ?: throw InvalidObjectException("null returned as courses")
        assertThat(result.count(), equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun deleteScores() = runBlocking {
        var allRounds = roundDao.getRounds()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        var result = allRounds.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        var resultRound = result[0]

        resultRound.scores.forEach { roundDao.delete(it.score) }
        allRounds = roundDao.getRounds()
        result = allRounds.getValueBlocking()
            ?: throw InvalidObjectException("null returned as courses")
        assertThat(result.count(), equalTo(1))

        resultRound = result[0]

        assertThat(resultRound.scores.count(), equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun readRoundsWithScores() = runBlocking {
        val allRounds = roundDao.getRounds()
        val result = allRounds.getValueBlocking()
            ?: throw InvalidObjectException("null returned as courses")
        val resultRound = result[0]

        assertThat(resultRound.scores.count(), equalTo(3))

        resultRound.scores.sortedBy { it.hole.holeId }.forEach { score ->
            assertThat(score.score.parentRoundId, equalTo(roundId))
        }
    }

    @Test
    @Throws(Exception::class)
    fun getHoleStatistics() = runBlocking {
        val roundWithId = roundDao.getRoundWithId(roundId)

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        val result = roundWithId.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")
        result.scores.forEach{roundDao.delete(it.score)}

        val roundIds = listOf(
            OffsetDateTime.of(2010, 12,3, 1,0,0,0,ZoneOffset.UTC),
            OffsetDateTime.of(2020, 11,2, 14,0,0,0,ZoneOffset.UTC),
            OffsetDateTime.of(2000, 12,1, 2,0,0,0,ZoneOffset.UTC),
            OffsetDateTime.of(2015, 10,11, 7,0,0,0,ZoneOffset.UTC),
            OffsetDateTime.of(2001, 2,1, 5,0,0,0,ZoneOffset.UTC)
        )
        val rounds = listOf(
            Round(roundIds[0], courseId),
            Round(roundIds[1], courseId),
            Round(roundIds[2], courseId),
            Round(roundIds[3], courseId),
            Round(roundIds[4],courseId)
        )
        rounds.forEach{roundDao.insert(it)}

        val scores: List<Score> = listOf(
            Score(parentRoundId = roundIds[0], playerId = playerId, holeId = holeIds[0], result= 1),
            Score(parentRoundId = roundIds[1], playerId = playerId, holeId = holeIds[0], result = 3),
            Score(parentRoundId = roundIds[2], playerId = playerId, holeId = holeIds[1], result = 2),
            Score(parentRoundId = roundIds[3], playerId = playerId, holeId = holeIds[0], result = 2),
            Score(parentRoundId = roundIds[4], playerId = playerId, holeId = holeIds[0], result = 2)
        )
        scores.forEach{roundDao.insert(it)}

        val holeStatisticsLD = roundDao.getHoleStatistics(playerId, holeIds[0])
        val holeStatistics = holeStatisticsLD.getValueBlocking()
            ?: throw InvalidObjectException("null returned as courses")

        assertThat(holeStatistics.avgResult, equalTo(2F))
        assertThat(holeStatistics.bestResult, equalTo(1))
        assertThat(holeStatistics.latestResult, equalTo(3))

    }
}
