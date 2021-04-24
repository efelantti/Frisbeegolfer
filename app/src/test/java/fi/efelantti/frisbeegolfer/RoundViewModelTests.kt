package fi.efelantti.frisbeegolfer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.model.*
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExperimentalCoroutinesApi
class RoundViewModelTests {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private lateinit var repository: IRepository
    private lateinit var roundViewModel: RoundViewModel
    private val roundId = OffsetDateTime.of(2020, 12, 31, 12, 0, 0, 0, ZoneOffset.UTC)
    private val round = Round(dateStarted = roundId, courseId = 213)
    private val roundWithCourseAndScores =
        RoundWithCourseAndScores(
            round,
            course = CourseWithHoles(Course(), emptyList()),
            scores = emptyList()
        )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        repository = mockk()
        every { repository.allRounds } returns MutableLiveData<List<RoundWithCourseAndScores>>(
            emptyList()
        )
        roundViewModel = RoundViewModel(testScope, repository)
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
    fun deleteRound() = testDispatcher.runBlockingTest {
        coEvery { repository.delete(round = roundWithCourseAndScores) } returns Unit
        roundViewModel.delete(round = roundWithCourseAndScores)
        coVerify(exactly = 1) { repository.delete(roundWithCourseAndScores) }
    }

    @Test
    fun insertRound() = testDispatcher.runBlockingTest {
        coEvery { repository.insert(round = round) } returns Unit
        roundViewModel.insert(round = round)
        coVerify(exactly = 1) { repository.insert(round) }
    }

    @Test
    fun insertScore() = testDispatcher.runBlockingTest {
        val score =
            Score(holeId = 34, playerId = 123, parentRoundId = round.dateStarted, result = 3)
        coEvery { repository.insert(score) } returns Unit
        roundViewModel.insert(score)
        coVerify(exactly = 1) { repository.insert(score) }
    }

    @Test
    fun updateScore() = testDispatcher.runBlockingTest {
        val score =
            Score(holeId = 34, playerId = 123, parentRoundId = round.dateStarted, result = 3)
        coEvery { repository.update(score) } returns Unit
        roundViewModel.update(score)
        coVerify(exactly = 1) { repository.update(score) }
    }
}
