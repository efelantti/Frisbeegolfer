package fi.efelantti.frisbeegolfer

import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlayerViewModelTests {

    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var repository: Repository

    @Before
    fun setupViewModel() {
        // We initialise the tasks to 3, with one active and two completed
        repository = mockk()
        every { repository.allPlayers } returns MutableLiveData<List<Player>>(emptyList())
        playerViewModel = PlayerViewModel(repository)
    }

    @After
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun insertPlayer() {
        val player = Player(name = "Tester")
        coEvery { repository.insert(player) } returns Unit
        playerViewModel.insert(player)
        coVerify(exactly = 1) { repository.insert(player) }
    }

    @Test
    fun updatePlayer() {
        val player = Player(name = "Tester")
        coEvery { repository.update(player) } returns Unit
        playerViewModel.update(player)
        coVerify(exactly = 1) { repository.update(player) }
    }
}
