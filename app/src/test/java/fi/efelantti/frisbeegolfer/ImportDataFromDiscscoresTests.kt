package fi.efelantti.frisbeegolfer

import fi.efelantti.frisbeegolfer.managediscscoresdata.DiscscoresDataHandler
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ImportDataFromDiscscoresTests {

    @Test
    fun readPlayersJsonRuns() {
        val playersJson = ClassLoader.getSystemResource("players.json").readText()
        val coursesJson = ClassLoader.getSystemResource("courses.json").readText()
        val gamesJson = ClassLoader.getSystemResource("games.json").readText()

        val discscoresDataHandler = DiscscoresDataHandler(playersJson, coursesJson, gamesJson)
    }
}
