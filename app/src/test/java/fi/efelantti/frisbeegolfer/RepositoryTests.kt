package fi.efelantti.frisbeegolfer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.InvalidObjectException

class RepositoryTests {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: Repository

    @Before
    fun createRepository() {
        repository = Repository(FakePlayerDao(), FakeCourseDao(), FakeRoundDao())
    }

    @Test
    fun getAllPlayersWhenEmpty() = runBlocking {
        val allPlayers = repository.allPlayers
        val result = allPlayers.getValueBlocking() ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(0))
    }
}