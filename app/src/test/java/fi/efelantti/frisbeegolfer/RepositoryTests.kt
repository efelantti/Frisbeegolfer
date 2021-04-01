package fi.efelantti.frisbeegolfer

import org.junit.Before
import org.junit.Test

class RepositoryTests {

    private lateinit var repository: Repository

    @Before
    fun createRepository() {
        repository = Repository(FakePlayerDao(), FakeCourseDao(), FakeRoundDao())
    }

    @Test
    fun getAllPlayers() {
        val allPlayers = repository.allPlayers
    }
}