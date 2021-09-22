package fi.efelantti.frisbeegolfer

import fi.efelantti.frisbeegolfer.managediscscoresdata.DiscscoresDataHandler
import fi.efelantti.frisbeegolfer.model.Hole
import fi.efelantti.frisbeegolfer.model.Score
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ImportDataFromDiscscoresTests {

    private lateinit var discscoresDataHandler: DiscscoresDataHandler

    @Before
    fun setup() {
        val playersJson = ClassLoader.getSystemResource("players.json").readText()
        val coursesJson = ClassLoader.getSystemResource("courses.json").readText()
        val gamesJson = ClassLoader.getSystemResource("games.json").readText()

        discscoresDataHandler = DiscscoresDataHandler(playersJson, coursesJson, gamesJson)
    }

    @Test
    fun returnsCorrectAmountOfPlayers() {
        assertThat(discscoresDataHandler.players.count(), equalTo(7))
    }

    @Test
    fun returnsCorrectAmountOfCourses() {
        assertThat(discscoresDataHandler.courses.count(), equalTo(20))
    }

    @Test
    fun playerIdsAreUnique() {
        assertThat(
            discscoresDataHandler.players.map { it.id }.count(),
            equalTo(discscoresDataHandler.players.count())
        )
    }

    @Test
    fun returnsCorrectResultFor24052017() {
        val roundId =
            DiscscoresDataHandler.convertDiscscoresTimeStampToOffsetDateTime(1495639300610)
        val round = discscoresDataHandler.rounds.find { it.dateStarted == roundId }
            ?: throw Exception("Round with id $roundId did not exist.")
        val course =
            discscoresDataHandler.courses.find { it.courseId == round.courseId } ?: throw Exception(
                "Course with id ${round.courseId} did not exist."
            )

        assertThat(course.name, notNullValue())

        val holes = discscoresDataHandler.holes.filter { it.parentCourseId == course.courseId }

        assertTrue("Holes didn't exist.", holes.count() > 0)

        holes.sortedBy { it.holeNumber }.forEachIndexed { index, hole ->
            assertThat(hole.par, equalTo(3))
            assertThat(hole.holeNumber, equalTo(index + 1))
        }

        val scores = discscoresDataHandler.scores.filter { it.parentRoundId == round.dateStarted }

        assertTrue("Scores didn't exist.", scores.count() > 0)

        val players = scores.distinctBy { it.playerId }.map { it.playerId }

        assertThat(players.count(), equalTo(2))

        val expectedPlayers = listOf("Pelaaja", "Testeri")
        players.forEach { playerId ->
            val player = discscoresDataHandler.players.find { it.id == playerId }
                ?: throw Exception("Couldn't find player with id ${playerId}.")
            assertTrue(
                "Invalid player ${player.name} found in round.",
                expectedPlayers.contains(player.name)
            )
        }

        assertThat(scores.count(), equalTo(players.count() * holes.count()))

        val sortedScoresByOwner = scores.sortedBy { it.holeId }.filter { it.playerId == 0L }

        var scoresAndHoles = mutableListOf<Pair<Score, Hole>>()

        sortedScoresByOwner.forEach { score ->
            val hole = holes.find { it.holeId == score.holeId }
                ?: throw Exception("Couldn't find hole with id ${score.holeId}.")
            scoresAndHoles.add(Pair(score, hole))
        }

        var sortedScoresAndHole = scoresAndHoles.sortedBy { it.second.holeNumber }
        assertThat(sortedScoresAndHole[0].first.result, equalTo(2))
        assertThat(sortedScoresAndHole[1].first.result, equalTo(2))
        assertThat(sortedScoresAndHole[2].first.result, equalTo(2))
        assertThat(sortedScoresAndHole[3].first.result, equalTo(5))
        assertThat(sortedScoresAndHole[4].first.result, equalTo(5))
        assertThat(sortedScoresAndHole[5].first.result, equalTo(3))
        assertThat(sortedScoresAndHole[6].first.result, equalTo(4))
        assertThat(sortedScoresAndHole[7].first.result, equalTo(3))

        val sortedScoresByOther = scores.sortedBy { it.holeId }.filter { it.playerId == 2L }
        scoresAndHoles = mutableListOf()

        sortedScoresByOther.forEach { score ->
            val hole = holes.find { it.holeId == score.holeId }
                ?: throw Exception("Couldn't find hole with id ${score.holeId}.")
            scoresAndHoles.add(Pair(score, hole))
        }
        sortedScoresAndHole = scoresAndHoles.sortedBy { it.second.holeNumber }
        assertThat(sortedScoresAndHole[0].first.result, equalTo(4))
        assertThat(sortedScoresAndHole[1].first.result, equalTo(4))
        assertThat(sortedScoresAndHole[2].first.result, equalTo(4))
        assertThat(sortedScoresAndHole[3].first.result, equalTo(7))
        assertThat(sortedScoresAndHole[4].first.result, equalTo(6))
        assertThat(sortedScoresAndHole[5].first.result, equalTo(5))
        assertThat(sortedScoresAndHole[6].first.result, equalTo(4))
        assertThat(sortedScoresAndHole[7].first.result, equalTo(4))
    }
}
