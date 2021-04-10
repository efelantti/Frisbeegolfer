package fi.efelantti.frisbeegolfer

import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PlayerViewModelTests {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var repository: Repository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // We initialise the tasks to 3, with one active and two completed
        repository = mockk()
        every { repository.allPlayers } returns MutableLiveData<List<Player>>(emptyList())
    }

    @After
    internal fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
        // Reset Coroutine Dispatcher and Scope.
        testDispatcher.cleanupTestCoroutines()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun insertPlayer() = testDispatcher.runBlockingTest {
        playerViewModel = PlayerViewModel(testScope, repository)
        val player = Player(name = "Tester")
        coEvery { repository.insert(player) } returns Unit
        playerViewModel.insert(player)
        coVerify(exactly = 1) { repository.insert(player) }
    }

    @Test
    fun updatePlayer() = testDispatcher.runBlockingTest {
        playerViewModel = PlayerViewModel(testScope, repository)
        val player = Player(name = "Tester")
        coEvery { repository.update(player) } returns Unit
        playerViewModel.update(player)
        coVerify(exactly = 1) { repository.update(player) }
    }

}
