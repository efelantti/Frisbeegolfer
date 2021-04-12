package fi.efelantti.frisbeegolfer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.model.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.io.InvalidObjectException
import java.time.OffsetDateTime
import java.time.ZoneOffset


@RunWith(MockitoJUnitRunner::class)
/*
Using Mockito and MockK for mocking purposes. Couldn't get Mockito to work properly with suspend functions, so MockK is used in those tests instead. Every test
could be refactored to use MockK for contistency.
 */
class RepositoryTests {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    @Mock
    private lateinit var fakePlayerDao: PlayerDao
    @Mock
    private lateinit var fakeCourseDao : CourseDao
    @Mock
    private lateinit var fakeRoundDao: RoundDao

    @Test
    fun getAllPlayersWhenEmpty() = runBlockingTest {
        `when`(fakePlayerDao.getPlayers())
            .thenReturn(MutableLiveData(emptyList()))
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        val allPlayers = repository.allPlayers
        val result = allPlayers.getValueBlocking() ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(0))
    }

    @Test
    fun getAllPlayersWhenNonEmpty() = runBlockingTest {
        `when`(fakePlayerDao.getPlayers())
            .thenReturn(MutableLiveData(listOf(Player(name = "Tester1"), Player(name= "Tester2"), Player(name="Tester3"))))
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        val allPlayers = repository.allPlayers
        val result = allPlayers.getValueBlocking() ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(3))
        assertThat(result[0].name, equalTo("Tester1"))
        assertThat(result[1].name, equalTo("Tester2"))
        assertThat(result[2].name, equalTo("Tester3"))
    }

    @Test
    fun getAllCoursesWhenEmpty() = runBlockingTest {
        `when`(fakeCourseDao.getCoursesWithHoles())
            .thenReturn(MutableLiveData(emptyList()))
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        val allCourses = repository.allCourses
        val result = allCourses.getValueBlocking() ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(0))
    }

    @Test
    fun getAllCoursesWhenNonEmpty() = runBlockingTest {
        `when`(fakeCourseDao.getCoursesWithHoles())
                .thenReturn(MutableLiveData(listOf(
                    CourseWithHoles(Course(name = "Course1", city= "City1"), holes=listOf(Hole(), Hole())),
                    CourseWithHoles(Course(name = "Course2", city= "City2"), holes=listOf(Hole(), Hole(), Hole()))
                )))
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        val allCourses = repository.allCourses
        val result = allCourses.getValueBlocking() ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(2))
        assertThat(result[0].course.name, equalTo("Course1"))
        assertThat(result[0].course.city, equalTo("City1"))
        assertThat(result[1].course.name, equalTo("Course2"))
        assertThat(result[1].course.city, equalTo("City2"))
    }

    @Test
    fun getAllRoundsWhenEmpty() = runBlockingTest {
        `when`(fakeRoundDao.getRounds())
            .thenReturn(MutableLiveData(emptyList()))
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        val allRounds = repository.allRounds
        val result = allRounds.getValueBlocking() ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(0))
    }

    @Test
    fun getAllRoundsWhenNonEmpty() = runBlockingTest {
        val roundId = OffsetDateTime.of(2020, 12,31,12,0,0,0, ZoneOffset.UTC)
        val roundId2 = OffsetDateTime.of(2010, 6,1,12,0,0,0, ZoneOffset.UTC)

        `when`(fakeRoundDao.getRounds())
            .thenReturn(MutableLiveData(listOf(
                RoundWithCourseAndScores(Round(courseId=0, dateStarted= roundId), listOf(ScoreWithPlayerAndHole(hole=Hole(), player=Player(), score=Score(parentRoundId = roundId))), Course()),
                RoundWithCourseAndScores(Round(courseId=1, dateStarted= roundId2), listOf(ScoreWithPlayerAndHole(hole=Hole(), player=Player(), score=Score(parentRoundId = roundId2))), Course())
            )))
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        val allRounds = repository.allRounds
        val result = allRounds.getValueBlocking() ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(2))
        assertThat(result[0].round.dateStarted, equalTo(roundId))
        assertThat(result[0].round.courseId, equalTo(0L))
        assertThat(result[1].round.dateStarted, equalTo(roundId2))
        assertThat(result[1].round.courseId, equalTo(1L))
    }

    @Test
    fun insertPlayer() = runBlockingTest {
        val player = Player(name="Tester")
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        repository.insert(player)
        verify(fakePlayerDao).insert(player)
    }

    @Test
    fun insertRound() = runBlockingTest {
        val round = Round(dateStarted = OffsetDateTime.now(), courseId = 0)
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        repository.insert(round)
        verify(fakeRoundDao).insert(round)
    }

    @Test
    fun insertScore() = runBlockingTest {
        val score = Score(parentRoundId = OffsetDateTime.now())
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        repository.insert(score)
        verify(fakeRoundDao).insert(score)
    }

    @Test
    fun updatePlayer() = runBlockingTest {
        val player = Player(name="Tester")
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        repository.update(player)
        verify(fakePlayerDao).update(player)
    }

    @Test
    fun updateScore() = runBlockingTest {
        val score = Score(parentRoundId = OffsetDateTime.now())
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        repository.update(score)
        verify(fakeRoundDao).update(score)
    }

    @Test
    fun deleteHole() = runBlockingTest {
        val hole = Hole()
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        repository.delete(hole)
        verify(fakeCourseDao).delete(hole)
    }

    @Test
    fun deleteRoundWithScores() = runBlockingTest {
        val roundId = OffsetDateTime.of(2020,1,1,12,0,0,0, ZoneOffset.UTC)
        val round = Round(roundId , courseId = 0)
        val roundWithCourseAndScores = RoundWithCourseAndScores(round, listOf(ScoreWithPlayerAndHole(hole=Hole(), player=Player(), score=Score(parentRoundId = roundId))), Course())

        val alternativeMock = mockk<RoundDao>()
        every { alternativeMock.getRounds() } returns MutableLiveData<List<RoundWithCourseAndScores>>(emptyList())
        coEvery { alternativeMock.delete(roundWithCourseAndScores.round) } returns Unit
        for (score in roundWithCourseAndScores.scores)
        {
            coEvery { alternativeMock.delete(score.score) } returns Unit
        }

        repository = Repository(fakePlayerDao, fakeCourseDao, alternativeMock)
        repository.delete(roundWithCourseAndScores)

        coVerify(exactly = 1) { alternativeMock.delete(roundWithCourseAndScores.round) }
        for (score in roundWithCourseAndScores.scores)
        {
            coVerify(exactly = 1) { alternativeMock.delete(score.score)}
        }
    }

    @Test
    fun updateCourseWithHoles() = runBlockingTest {
        val courseWithHoles = CourseWithHoles(Course(), listOf(Hole()))
        val alternativeMock = mockk<CourseDao>()
        every { alternativeMock.getCoursesWithHoles() } returns MutableLiveData<List<CourseWithHoles>>(emptyList())
        coEvery { alternativeMock.update(courseWithHoles.course) } returns Unit
        coEvery { alternativeMock.updateAll(courseWithHoles.holes) } returns Unit

        repository = Repository(fakePlayerDao, alternativeMock, fakeRoundDao)
        repository.update(courseWithHoles)
        coVerify(exactly = 1) { alternativeMock.update(courseWithHoles.course)}
        coVerify(exactly = 1) { alternativeMock.updateAll(courseWithHoles.holes)}
    }

    @Test
    fun insertCourseWithHoles() = runBlockingTest {
        val courseWithHoles = CourseWithHoles(Course(), listOf(Hole()))
        val alternativeMock = mockk<CourseDao>()
        every { alternativeMock.getCoursesWithHoles() } returns MutableLiveData<List<CourseWithHoles>>(emptyList())
        coEvery { alternativeMock.insert(courseWithHoles.course)} returns 0
        coEvery { alternativeMock.insertAll(courseWithHoles.holes)} returns Unit
        repository = Repository(fakePlayerDao, alternativeMock, fakeRoundDao)
        repository.insertCourseWithHoles(courseWithHoles)

        coVerify(exactly = 1) { alternativeMock.insert(courseWithHoles.course) }
        coVerify(exactly = 1) { alternativeMock.insertAll(courseWithHoles.holes) }
    }

    @Test
    fun getCourseWithHolesById() = runBlockingTest {
        val courseWithHoles = CourseWithHoles(Course(), listOf(Hole()))

        val alternativeMock = mockk<CourseDao>()
        every { alternativeMock.getCoursesWithHoles() } returns MutableLiveData<List<CourseWithHoles>>(emptyList())
        every { alternativeMock.getCourseWithHolesWithId(0)} returns MutableLiveData(courseWithHoles)
        repository = Repository(fakePlayerDao, alternativeMock, fakeRoundDao)

        repository.getCourseWithHolesById(0)

        verify ( exactly = 1 ) { alternativeMock.getCourseWithHolesWithId(0)}
    }

    @Test
    fun getRoundWithRoundId() = runBlockingTest {
        val roundId = OffsetDateTime.of(2020,1,1,12,0,0,0, ZoneOffset.UTC)
        val round = Round(roundId , courseId = 0)
        val roundWithCourseAndScores = RoundWithCourseAndScores(round, listOf(ScoreWithPlayerAndHole(hole=Hole(), player=Player(), score=Score(parentRoundId = roundId))), Course())

        val alternativeMock = mockk<RoundDao>()
        every { alternativeMock.getRounds() } returns MutableLiveData<List<RoundWithCourseAndScores>>(emptyList())
        every { alternativeMock.getRoundWithId(roundId)} returns MutableLiveData(roundWithCourseAndScores)

        repository = Repository(fakePlayerDao, fakeCourseDao, alternativeMock)
        repository.getRoundWithRoundId(roundId)

        verify ( exactly = 1 ) { alternativeMock.getRoundWithId(roundId)}
    }

    @Test
    fun getHoleStatistics() = runBlocking {
        val alternativeMock = mockk<RoundDao>()
        val holeId = 0L
        val playerId = 0L
        every { alternativeMock.getRounds() } returns MutableLiveData<List<RoundWithCourseAndScores>>(emptyList())
        every { alternativeMock.getHoleStatistics(playerId, holeId)} returns MutableLiveData<HoleStatistics>(HoleStatistics(bestResult=1, avgResult=1.5f, latestResult=2))

        repository = Repository(fakePlayerDao, fakeCourseDao, alternativeMock)
        repository.getHoleStatistics(playerId, holeId)

        verify(exactly = 1) { alternativeMock.getHoleStatistics(playerId, holeId)}
    }
}