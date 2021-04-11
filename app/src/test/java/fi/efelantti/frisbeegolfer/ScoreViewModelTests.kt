package fi.efelantti.frisbeegolfer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.model.*
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.InvalidObjectException
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExperimentalCoroutinesApi
class ScoreViewModelTests {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private lateinit var repository: IRepository
    private lateinit var scoreViewModel: ScoreViewModel
    private val roundId = OffsetDateTime.of(2020, 12, 31, 12, 0, 0, 0, ZoneOffset.UTC)
    private val round = Round(dateStarted = roundId, courseId = 213)
    private val course = Course(courseId = 123)
    private val holes = listOf(
        Hole(holeId = 0, parentCourseId = course.courseId, holeNumber = 1, par = 3),
        Hole(holeId = 1, parentCourseId = course.courseId, holeNumber = 2, par = 2),
        Hole(holeId = 2, parentCourseId = course.courseId, holeNumber = 3, par = 4)
        )
    private val players = listOf(
        Player(id = 0, name = "Andy"),
        Player(id = 1, name = "Bert")
    )
    private val scores = listOf(
        ScoreWithPlayerAndHole(hole = holes[0], player = players[0], score = Score(id = 0, holeId = holes[0].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
        ScoreWithPlayerAndHole(hole = holes[0], player = players[1], score = Score(id = 1, holeId = holes[0].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id)),
        ScoreWithPlayerAndHole(hole = holes[1], player = players[0], score = Score(id = 2, holeId = holes[1].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
        ScoreWithPlayerAndHole(hole = holes[1], player = players[1], score = Score(id = 3, holeId = holes[1].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id)),
        ScoreWithPlayerAndHole(hole = holes[2], player = players[0], score = Score(id = 4, holeId = holes[2].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
        ScoreWithPlayerAndHole(hole = holes[2], player = players[1], score = Score(id = 5, holeId = holes[2].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id))
        )
    private val roundWithCourseAndScores =
        RoundWithCourseAndScores(round, scores, course)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData<RoundWithCourseAndScores>(roundWithCourseAndScores)
        scoreViewModel = ScoreViewModel(testScope,repository, roundId)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    internal fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun updateScore() = testDispatcher.runBlockingTest {
        val score = roundWithCourseAndScores.scores[0].score
        coEvery { repository.update(score) } returns Unit
        this@ScoreViewModelTests.scoreViewModel.update(score)
        coVerify(exactly = 1) { repository.update(score) }
    }

    @Test
    fun incrementIndexOnFirstHole() {
        scoreViewModel.incrementIndex()
        val currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        val currentScore = currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore, equalTo(scores[1]))
    }
}
