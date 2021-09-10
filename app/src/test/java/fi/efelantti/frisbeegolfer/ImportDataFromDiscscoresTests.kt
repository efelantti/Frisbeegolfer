package fi.efelantti.frisbeegolfer

import fi.efelantti.frisbeegolfer.managediscscoresdata.DiscscoresDataHandler
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
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

    @Test
    fun readStringToOffsetDateTime() {
        val time = DiscscoresDataHandler.convertDiscscoresTimeStampToOffsetDateTime(1624027719052)
        assertThat(time.toString(), equalTo("2021-06-18T17:48:39+03:00"))
    }
}
