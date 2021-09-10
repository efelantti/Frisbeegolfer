package fi.efelantti.frisbeegolfer.managediscscoresdata

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat.DiscscoresCourses
import fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat.DiscscoresGames
import fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat.DiscscoresPlayers
import fi.efelantti.frisbeegolfer.model.*
import java.time.LocalDateTime
import java.time.OffsetDateTime

class DiscscoresDataHandler(
    playersJson: String,
    coursesJson: String,
    gamesJson: String
) {
    private val moshi: Moshi = Moshi.Builder().build()
    private val discscoresPlayers: DiscscoresPlayers
    private val discscoresCourses: DiscscoresCourses
    private val discscoresGames: DiscscoresGames

    val players = mutableListOf<Player>()
    val courses = mutableListOf<Course>()
    val holes = mutableListOf<Hole>()
    val rounds = mutableListOf<Round>()
    val scores = mutableListOf<Score>()

    init {
        val playersAdapter: JsonAdapter<DiscscoresPlayers> =
            moshi.adapter(DiscscoresPlayers::class.java)
        val coursesAdapter: JsonAdapter<DiscscoresCourses> =
            moshi.adapter(DiscscoresCourses::class.java)
        val gamesAdapter: JsonAdapter<DiscscoresGames> = moshi.adapter(DiscscoresGames::class.java)
        discscoresPlayers = playersAdapter.fromJson(playersJson)
            ?: throw IllegalArgumentException("Error in deserializing players JSON.")
        discscoresCourses = coursesAdapter.fromJson(coursesJson)
            ?: throw IllegalArgumentException("Error in deserializing courses JSON.")
        discscoresGames = gamesAdapter.fromJson(gamesJson)
            ?: throw IllegalArgumentException("Error in deserializing games JSON.")

        // Create players according to Player class.
        // Create new player index according to position in json file.
        // Add the new index value to DiscscoresGamePLayers & DiscscoresScores.
        discscoresPlayers.players.forEachIndexed { index, discscoresPlayer ->
            val newPlayerId = index.toLong()
            discscoresPlayer.newPlayerId = newPlayerId
            val player = Player(
                id = newPlayerId,
                name = discscoresPlayer.name
            )
            players.add(player)

            discscoresGames.discscoresGamePlayers.filter { gamePlayer -> gamePlayer.playerUuid == discscoresPlayer.uuid }
                .forEach { gamePlayer ->
                    gamePlayer.newPlayerId = newPlayerId

                    discscoresGames.discscoresScores.filter { dcScore -> dcScore.gamePlayerUuid == gamePlayer.uuid }
                        .forEach { dcScore ->
                            dcScore.newPlayerId = newPlayerId
                        }
                }

        }

        // Go through each hole and assign a new position based Id.
        discscoresCourses.discscoresHoles.forEachIndexed { holeIndex, dsHole ->
            val newHoleId = holeIndex.toLong()
            dsHole.newHoleId = newHoleId
        }

        // Create courses according to Course class.
        // Create new course index from the position in the json file.
        discscoresCourses.discscoresCourses.forEachIndexed { courseIndex, discscoresCourse ->
            val newCourseId = courseIndex.toLong()
            discscoresCourse.newCourseId = newCourseId
            val course = Course(
                courseId = newCourseId,
                name = discscoresCourse.name
            )
            courses.add(course)

            discscoresCourses.discscoresHoles.filter { it.courseUuid == discscoresCourse.uuid }
                .forEachIndexed { holeId, discscoresHole ->
                    val hole = Hole(
                        holeId = discscoresHole.newHoleId
                            ?: throw Exception("Hole id was null for hole ${discscoresHole.uuid}."),
                        parentCourseId = newCourseId,
                        holeNumber = discscoresHole.hole,
                        par = discscoresHole.par
                    )
                    holes.add(hole)
                }

            // Go through each DiscsScoresGame and assign the new course id to it.
            discscoresGames.discscoresGames.filter { it.courseUuid == discscoresCourse.uuid }
                .forEach { dcGame ->
                    dcGame.newCourseId = newCourseId
                }
        }

        // Go through each GameHole and assign the new holeId to each of the GameHoles as well.
        // In order to do it, we need to use the course uuid and hole number in order to find the corresponding hole.
        discscoresGames.discscoresGameHoles.forEach { dcGameHole ->
            val dcGame = discscoresGames.discscoresGames.find { it.uuid == dcGameHole.gameUuid }
                ?: throw Exception("Couldn't find game with uuid ${dcGameHole.gameUuid}.")
            val dcCourse = discscoresCourses.discscoresCourses.find { it.uuid == dcGame.courseUuid }
                ?: throw Exception("Couldn't find course with uuid ${dcGame.courseUuid}.")
            val hole =
                discscoresCourses.discscoresHoles.find { it.courseUuid == dcCourse.uuid && it.hole == dcGameHole.hole }
                    ?: throw Exception("Couldn't find hole for course with uuid ${dcCourse.uuid} and number ${dcGameHole.hole}.")

            dcGameHole.newHoleId = hole.newHoleId
        }

        // Go through each DiscsScoresGame and create a Round.
        discscoresGames.discscoresGames.forEach { discscoresGame ->
            val newGameIndex =
                convertDiscscoresTimeStampToOffsetDateTime(discscoresGame.createdAt.toLong())
            val round = Round(
                dateStarted = newGameIndex,
                courseId = discscoresGame.newCourseId
                    ?: throw Exception("Game with uuid ${discscoresGame.uuid} had null as new course id.")
            )
            rounds.add(round)
        }

        // TODO - Problem with scores: In Discscores, if game is ended before all scores are set, then the score objects are not created for the rest of the holes. In our case, we would need to go through each game, and make sure for all holes in the courses, there are Scores.
        // Possible solution - make sure that for each hole in the course, there is a Score. If not, for the rest, create empty scores.
        // Another solution - loop through Hole numbers from 1 to CourseHolesCount. Find holes with hole number, and for those that can't be found, create with no data.
        discscoresGames.discscoresScores.forEachIndexed { scoreId, dsScore ->
            val parentRoundIdString =
                discscoresGames.discscoresGames.find { dsGame -> dsGame.uuid == dsScore.gameUuid }?.createdAt
                    ?: throw Exception("Couldn't find game with uuid ${dsScore.gameUuid}.")
            val parentRoundId =
                convertDiscscoresTimeStampToOffsetDateTime(parentRoundIdString.toLong())

            val playerId = dsScore.newPlayerId
                ?: throw Exception("Player with uuid ${dsScore.gamePlayerUuid} wasn't assigned.")
            val gameHole =
                discscoresGames.discscoresGameHoles.find { it.uuid == dsScore.gameHoleUuid }
                    ?: throw Exception("GameHole with uuid ${dsScore.gameHoleUuid} couldn't be found.")
            val holeId = gameHole.newHoleId
                ?: throw Exception("GameHole with uuid ${gameHole.uuid} didn't have new hole assigned.")

            val score = Score(
                id = scoreId.toLong(),
                parentRoundId = parentRoundId,
                playerId = playerId,
                holeId = holeId,
                result = dsScore.score,
                isOutOfBounds = false, // OB is not tracked in DiscScores
                didNotFinish = false
            )
            scores.add(score)
        }
    }

    fun getPlayers(discscoresPlayers: DiscscoresPlayers): List<Player> {
        return discscoresPlayers.players.mapIndexed { index, player ->
            Player(
                id = index.toLong(),
                name = player.name
            )
        }
    }

/*    fun getPlayer(discscoresPlayer: DiscscoresPlayer): Player {
        // One way to get Long from UUID. However better maybe to use other methods to generate - for example, just use sequential longs.
        val uuid = UUID.fromString(discscoresPlayer.uuid)
        return Player(
            id = uuid.mostSignificantBits and Long.MAX_VALUE,
            name = discscoresPlayer.name
        )
    }*/

    companion object {
        fun convertDiscscoresTimeStampToOffsetDateTime(epochValue: Long): OffsetDateTime {
            val offset = OffsetDateTime.now().offset
            return OffsetDateTime.of(
                LocalDateTime.ofEpochSecond(epochValue / 1000, 0, offset),
                offset
            )
        }
    }
}