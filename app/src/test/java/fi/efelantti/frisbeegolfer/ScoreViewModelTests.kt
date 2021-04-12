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
    private lateinit var roundId: OffsetDateTime
    private lateinit var round: Round
    private lateinit var course: Course
    private lateinit var holes: List<Hole>
    private lateinit var players: List<Player>
    private lateinit var scores: List<ScoreWithPlayerAndHole>
    private lateinit var roundWithCourseAndScores: RoundWithCourseAndScores

    private lateinit var initialCurrentScore: ScoreWithPlayerAndHole

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        roundId = OffsetDateTime.of(2020, 12, 31, 12, 0, 0, 0, ZoneOffset.UTC)
        round = Round(dateStarted = roundId, courseId = 213)
        course = Course(courseId = 123)
        holes = listOf(
            Hole(holeId = 0, parentCourseId = course.courseId, holeNumber = 1, par = 3),
            Hole(holeId = 1, parentCourseId = course.courseId, holeNumber = 2, par = 2),
            Hole(holeId = 2, parentCourseId = course.courseId, holeNumber = 3, par = 4)
        )
        players = listOf(
            Player(id = 0, name = "Andy"),
            Player(id = 1, name = "Bert")
        )
       scores = listOf(
            ScoreWithPlayerAndHole(hole = holes[0], player = players[0], score = Score(id = 0, holeId = holes[0].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[0], player = players[1], score = Score(id = 1, holeId = holes[0].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id)),
            ScoreWithPlayerAndHole(hole = holes[1], player = players[0], score = Score(id = 2, holeId = holes[1].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[1], player = players[1], score = Score(id = 3, holeId = holes[1].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id)),
            ScoreWithPlayerAndHole(hole = holes[2], player = players[0], score = Score(id = 4, holeId = holes[2].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[2], player = players[1], score = Score(id = 5, holeId = holes[2].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id))
        )
        roundWithCourseAndScores =
            RoundWithCourseAndScores(round, scores, course)

        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData<RoundWithCourseAndScores>(roundWithCourseAndScores)

        scoreViewModel = ScoreViewModel(testScope,repository, roundId)
        // This is needed for the helper values in scoreViewModel to initialize - the transformations don't take place if the LiveData is not observed.
        val initialCurrentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        initialCurrentScore = initialCurrentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

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

    @Test
    fun incrementIndexOnLastHole() {
        repeat(5) {scoreViewModel.incrementIndex()}
        var currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        var currentScore = currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")
        assertThat(currentScore, equalTo(scores.last()))

        scoreViewModel.incrementIndex()
        currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        currentScore = currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore, equalTo(scores.first()))
    }

    @Test
    fun decrementIndexOnFirstHole() {
        scoreViewModel.decrementIndex()
        val currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        val currentScore = currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore, equalTo(scores.last()))
    }

    @Test
    fun decrementIndexOnLastHole() {
        repeat(2) {scoreViewModel.decrementIndex()}
        val currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        val currentScore = currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore, equalTo(scores[4]))
    }

    @Test
    fun setResult() {
        every { repository.update(any<Score>())} just runs

        var currentScore = scoreViewModel.currentScore.getValueBlocking() ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore.score.result, equalTo(0))

        scoreViewModel.setResult(3)
        currentScore = scoreViewModel.currentScore.getValueBlocking() ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore.score.result, equalTo(3))
        verify(exactly = 1) { repository.update(currentScore.score) }
    }

    @Test
    fun currentScoreIsInitializedCorrectlyWhenNoHolesHaveBeenPlayed() {
        val initialCurrentScore = scoreViewModel.currentScore.getValueBlocking() ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(initialCurrentScore, equalTo(scores.first()))
    }

    @Test
    fun currentScoreIsInitializedCorrectlyWhenSomeHolesHaveBeenPlayed() {
        scores = listOf(
            ScoreWithPlayerAndHole(hole = holes[0], player = players[0], score = Score(id = 0, holeId = holes[0].holeId, result = 3, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[0], player = players[1], score = Score(id = 1, holeId = holes[0].holeId, result = 2, parentRoundId = roundId, playerId = players[1].id)),
            ScoreWithPlayerAndHole(hole = holes[1], player = players[0], score = Score(id = 2, holeId = holes[1].holeId, result = 1, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[1], player = players[1], score = Score(id = 3, holeId = holes[1].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id)),
            ScoreWithPlayerAndHole(hole = holes[2], player = players[0], score = Score(id = 4, holeId = holes[2].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[2], player = players[1], score = Score(id = 5, holeId = holes[2].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id))
        )
        roundWithCourseAndScores =
            RoundWithCourseAndScores(round, scores, course)

        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData<RoundWithCourseAndScores>(roundWithCourseAndScores)

        scoreViewModel = ScoreViewModel(testScope,repository, roundId)

        val initialCurrentScore = scoreViewModel.currentScore.getValueBlocking() ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(initialCurrentScore, equalTo(scores[3]))
    }

    @Test
    fun currentScoreIsInitializedCorrectlyWhenAllHolesAreScored() {
        scores = listOf(
            ScoreWithPlayerAndHole(hole = holes[0], player = players[0], score = Score(id = 0, holeId = holes[0].holeId, result = 3, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[0], player = players[1], score = Score(id = 1, holeId = holes[0].holeId, result = 2, parentRoundId = roundId, playerId = players[1].id)),
            ScoreWithPlayerAndHole(hole = holes[1], player = players[0], score = Score(id = 2, holeId = holes[1].holeId, result = 1, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[1], player = players[1], score = Score(id = 3, holeId = holes[1].holeId, result = 3, parentRoundId = roundId, playerId = players[1].id)),
            ScoreWithPlayerAndHole(hole = holes[2], player = players[0], score = Score(id = 4, holeId = holes[2].holeId, result = 3, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[2], player = players[1], score = Score(id = 5, holeId = holes[2].holeId, result = 4, parentRoundId = roundId, playerId = players[1].id))
        )
        roundWithCourseAndScores =
            RoundWithCourseAndScores(round, scores, course)

        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData<RoundWithCourseAndScores>(roundWithCourseAndScores)

        scoreViewModel = ScoreViewModel(testScope,repository, roundId)

        val initialCurrentScore = scoreViewModel.currentScore.getValueBlocking() ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(initialCurrentScore, equalTo(scores.last()))
    }

    @Test
    fun scoresListIsSortedProperlyWhenInitiallyNotInSortedOrder() {
        scores = listOf(
            ScoreWithPlayerAndHole(hole = holes[1], player = players[0], score = Score(id = 2, holeId = holes[1].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[1], player = players[1], score = Score(id = 3, holeId = holes[1].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id)),
            ScoreWithPlayerAndHole(hole = holes[0], player = players[0], score = Score(id = 0, holeId = holes[0].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[0], player = players[1], score = Score(id = 1, holeId = holes[0].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id)),
            ScoreWithPlayerAndHole(hole = holes[2], player = players[0], score = Score(id = 4, holeId = holes[2].holeId, result = 0, parentRoundId = roundId, playerId = players[0].id)),
            ScoreWithPlayerAndHole(hole = holes[2], player = players[1], score = Score(id = 5, holeId = holes[2].holeId, result = 0, parentRoundId = roundId, playerId = players[1].id))
        )
        roundWithCourseAndScores =
            RoundWithCourseAndScores(round, scores, course)

        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData<RoundWithCourseAndScores>(roundWithCourseAndScores)

        scoreViewModel = ScoreViewModel(testScope,repository, roundId)
        assertThat(initialCurrentScore, equalTo(scores[2]))
    }

    @Test
    fun getHoleStatistics() {
        var desiredHoleStatistics = HoleStatistics(1,2f,3)
        every { repository.getHoleStatistics(0,0)} returns MutableLiveData(desiredHoleStatistics)

        var holeStatistics = scoreViewModel.holeStatistics.getValueBlocking() ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(holeStatistics.latestResult, equalTo(desiredHoleStatistics.latestResult))
        assertThat(holeStatistics.bestResult, equalTo(desiredHoleStatistics.bestResult))
        assertThat(holeStatistics.avgResult, equalTo(desiredHoleStatistics.avgResult))


    }
}
