package fi.efelantti.frisbeegolfer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.model.*
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
}