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
    private lateinit var courseWithHoles: CourseWithHoles
    private lateinit var holes: List<Hole>
    private lateinit var players: List<Player>
    private lateinit var playerIds: LongArray
    private lateinit var scores: List<ScoreWithPlayerAndHole>
    private lateinit var holeIds: LongArray
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
        holeIds = holes.map { it.holeId }.sorted().toLongArray()
        courseWithHoles = CourseWithHoles(course, holes)
        players = listOf(
            Player(id = 0, name = "Andy"),
            Player(id = 1, name = "Bert")
        )
        playerIds = players.map { it.id }.sorted().toLongArray()
        scores = listOf(
            ScoreWithPlayerAndHole(
                hole = holes[0],
                player = players[0],
                score = Score(
                    id = 0,
                    holeId = holes[0].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[0],
                player = players[1],
                score = Score(
                    id = 1,
                    holeId = holes[0].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[1],
                player = players[0],
                score = Score(
                    id = 2,
                    holeId = holes[1].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[1],
                player = players[1],
                score = Score(
                    id = 3,
                    holeId = holes[1].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[2],
                player = players[0],
                score = Score(
                    id = 4,
                    holeId = holes[2].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[2],
                player = players[1],
                score = Score(
                    id = 5,
                    holeId = holes[2].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            )
        )

        roundWithCourseAndScores =
            RoundWithCourseAndScores(round, scores, courseWithHoles)

        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData(
            roundWithCourseAndScores
        )
        every { repository.getScore(roundId, 0, 0) } returns MutableLiveData(scores[0])

        scoreViewModel = ScoreViewModel(testScope, repository, roundId, playerIds, holeIds)
        // This is needed for the helper values in scoreViewModel to initialize - the transformations don't take place if the LiveData is not observed.
        val initialCurrentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        initialCurrentScore = initialCurrentScoreNullable
            ?: throw InvalidObjectException("Null returned as current score.")

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
    fun nextScoreOnFirstHole() {
        every { repository.getScore(roundId, 1, 0) } returns MutableLiveData(scores[1])
        scoreViewModel = ScoreViewModel(testScope, repository, roundId, playerIds, holeIds)
        scoreViewModel.nextScore()
        val currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        val currentScore =
            currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore, equalTo(scores[1]))
    }

    @Test
    fun incrementIndexOnLastHole() {
        every { repository.getScore(roundId, 1, 0) } returns MutableLiveData(scores[1])
        every { repository.getScore(roundId, 0, 1) } returns MutableLiveData(scores[2])
        every { repository.getScore(roundId, 1, 1) } returns MutableLiveData(scores[3])
        every { repository.getScore(roundId, 0, 2) } returns MutableLiveData(scores[4])
        every { repository.getScore(roundId, 1, 2) } returns MutableLiveData(scores[5])

        scoreViewModel = ScoreViewModel(testScope, repository, roundId, playerIds, holeIds)
        repeat(5) { scoreViewModel.nextScore() }
        var currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        var currentScore =
            currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")
        assertThat(currentScore, equalTo(scores.last()))

        scoreViewModel.nextScore()
        currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        currentScore =
            currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore, equalTo(scores.first()))
    }

    @Test
    fun decrementIndexOnFirstHole() {
        every { repository.getScore(roundId, 1, 0) } returns MutableLiveData(scores[1])
        every { repository.getScore(roundId, 0, 1) } returns MutableLiveData(scores[2])
        every { repository.getScore(roundId, 1, 1) } returns MutableLiveData(scores[3])
        every { repository.getScore(roundId, 0, 2) } returns MutableLiveData(scores[4])
        every { repository.getScore(roundId, 1, 2) } returns MutableLiveData(scores[5])

        scoreViewModel = ScoreViewModel(testScope, repository, roundId, playerIds, holeIds)

        scoreViewModel.previousScore()
        val currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        val currentScore =
            currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore, equalTo(scores.last()))
    }

    @Test
    fun decrementIndexOnLastHole() {
        every { repository.getScore(roundId, 1, 0) } returns MutableLiveData(scores[1])
        every { repository.getScore(roundId, 0, 1) } returns MutableLiveData(scores[2])
        every { repository.getScore(roundId, 1, 1) } returns MutableLiveData(scores[3])
        every { repository.getScore(roundId, 0, 2) } returns MutableLiveData(scores[4])
        every { repository.getScore(roundId, 1, 2) } returns MutableLiveData(scores[5])

        scoreViewModel = ScoreViewModel(testScope, repository, roundId, playerIds, holeIds)
        repeat(2) { scoreViewModel.previousScore() }
        val currentScoreNullable = scoreViewModel.currentScore.getValueBlocking()
        val currentScore =
            currentScoreNullable ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore, equalTo(scores[4]))
    }

    @Test
    fun setResult() {
        coEvery { repository.update(any<Score>()) } just runs

        var currentScore = scoreViewModel.currentScore.getValueBlocking()
            ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore.score.result, equalTo(0))

        scoreViewModel.setResult(currentScore.score, 3)
        currentScore = scoreViewModel.currentScore.getValueBlocking()
            ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(currentScore.score.result, equalTo(3))
        coVerify(exactly = 1) { repository.update(currentScore.score) }
    }

    @Test
    fun currentScoreIsInitializedCorrectlyWhenNoHolesHaveBeenPlayed() {
        val initialCurrentScore = scoreViewModel.currentScore.getValueBlocking()
            ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(initialCurrentScore, equalTo(scores.first()))
    }

    @Test
      fun currentScoreIsInitializedCorrectlyWhenSomeHolesHaveBeenPlayed() {
          scores = listOf(
              ScoreWithPlayerAndHole(
                  hole = holes[0],
                  player = players[0],
                  score = Score(
                      id = 0,
                      holeId = holes[0].holeId,
                      result = 3,
                      parentRoundId = roundId,
                      playerId = players[0].id
                  )
              ),
              ScoreWithPlayerAndHole(
                  hole = holes[0],
                  player = players[1],
                  score = Score(
                      id = 1,
                      holeId = holes[0].holeId,
                      result = 2,
                      parentRoundId = roundId,
                      playerId = players[1].id
                  )
              ),
              ScoreWithPlayerAndHole(
                  hole = holes[1],
                  player = players[0],
                  score = Score(
                      id = 2,
                      holeId = holes[1].holeId,
                      result = 1,
                      parentRoundId = roundId,
                      playerId = players[0].id
                  )
              ),
              ScoreWithPlayerAndHole(
                  hole = holes[1],
                  player = players[1],
                  score = Score(
                      id = 3,
                      holeId = holes[1].holeId,
                      result = 0,
                      parentRoundId = roundId,
                      playerId = players[1].id
                  )
              ),
              ScoreWithPlayerAndHole(
                  hole = holes[2],
                  player = players[0],
                  score = Score(
                      id = 4,
                      holeId = holes[2].holeId,
                      result = 0,
                      parentRoundId = roundId,
                      playerId = players[0].id
                  )
              ),
              ScoreWithPlayerAndHole(
                  hole = holes[2],
                  player = players[1],
                  score = Score(
                      id = 5,
                      holeId = holes[2].holeId,
                      result = 0,
                      parentRoundId = roundId,
                      playerId = players[1].id
                  )
              )
          )
        roundWithCourseAndScores =
            RoundWithCourseAndScores(round, scores, courseWithHoles)

        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData(
            roundWithCourseAndScores
        )
        every { repository.getScore(roundId, 0, 0) } returns MutableLiveData(
            scores[3]
        )

        scoreViewModel = ScoreViewModel(testScope, repository, roundId, playerIds, holeIds)

        val initialCurrentScore = scoreViewModel.currentScore.getValueBlocking()
            ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(initialCurrentScore, equalTo(scores[3]))
    }

    @Test
    fun currentScoreIsInitializedCorrectlyWhenAllHolesAreScored() {
        scores = listOf(
            ScoreWithPlayerAndHole(
                hole = holes[0],
                player = players[0],
                score = Score(
                    id = 0,
                    holeId = holes[0].holeId,
                    result = 3,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[0],
                player = players[1],
                score = Score(
                    id = 1,
                    holeId = holes[0].holeId,
                    result = 2,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[1],
                player = players[0],
                score = Score(
                    id = 2,
                    holeId = holes[1].holeId,
                    result = 1,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[1],
                player = players[1],
                score = Score(
                    id = 3,
                    holeId = holes[1].holeId,
                    result = 3,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[2],
                player = players[0],
                score = Score(
                    id = 4,
                    holeId = holes[2].holeId,
                    result = 3,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[2],
                player = players[1],
                score = Score(
                    id = 5,
                    holeId = holes[2].holeId,
                    result = 4,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            )
        )
        roundWithCourseAndScores =
            RoundWithCourseAndScores(round, scores, courseWithHoles)

        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData(
            roundWithCourseAndScores
        )
        every { repository.getScore(roundId, 0, 0) } returns MutableLiveData(scores.last())

        scoreViewModel = ScoreViewModel(testScope, repository, roundId, playerIds, holeIds)

        val initialCurrentScore = scoreViewModel.currentScore.getValueBlocking()
            ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(initialCurrentScore, equalTo(scores.last()))
    }

    @Test
    fun scoresListIsSortedProperlyWhenInitiallyNotInSortedOrder() {
        scores = listOf(
            ScoreWithPlayerAndHole(
                hole = holes[1],
                player = players[0],
                score = Score(
                    id = 2,
                    holeId = holes[1].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[1],
                player = players[1],
                score = Score(
                    id = 3,
                    holeId = holes[1].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[0],
                player = players[0],
                score = Score(
                    id = 0,
                    holeId = holes[0].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[0],
                player = players[1],
                score = Score(
                    id = 1,
                    holeId = holes[0].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[2],
                player = players[0],
                score = Score(
                    id = 4,
                    holeId = holes[2].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[0].id
                )
            ),
            ScoreWithPlayerAndHole(
                hole = holes[2],
                player = players[1],
                score = Score(
                    id = 5,
                    holeId = holes[2].holeId,
                    result = 0,
                    parentRoundId = roundId,
                    playerId = players[1].id
                )
            )
        )
        roundWithCourseAndScores =
            RoundWithCourseAndScores(round, scores, courseWithHoles)

        repository = mockk()
        every { repository.getRoundWithRoundId(roundId) } returns MutableLiveData(
            roundWithCourseAndScores
        )

        scoreViewModel = ScoreViewModel(testScope, repository, roundId, playerIds, holeIds)
        assertThat(initialCurrentScore, equalTo(scores[2]))
    }

    @Test
    fun getHoleStatistics() {
        val desiredHoleStatistics = HoleStatistics(1, 2f, 3)
        every { repository.getHoleStatistics(0, 0) } returns MutableLiveData(desiredHoleStatistics)

        val holeStatistics = scoreViewModel.getHoleStatistics(0, 0).getValueBlocking()
            ?: throw InvalidObjectException("Null returned as current score.")

        assertThat(holeStatistics.latestResult, equalTo(desiredHoleStatistics.latestResult))
        assertThat(holeStatistics.bestResult, equalTo(desiredHoleStatistics.bestResult))
        assertThat(holeStatistics.avgResult, equalTo(desiredHoleStatistics.avgResult))
    }
}
