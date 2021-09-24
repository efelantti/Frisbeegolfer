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
    private lateinit var discscoresPlayers: DiscscoresPlayers
    private lateinit var discscoresCourses: DiscscoresCourses
    private lateinit var discscoresGames: DiscscoresGames

    val players = mutableListOf<Player>()
    val courses = mutableListOf<Course>()
    val holes = mutableListOf<Hole>()
    val rounds = mutableListOf<Round>()
    val scores = mutableListOf<Score>()

    init {
        readJsonData(playersJson, coursesJson, gamesJson)
        processPlayerData()
        assignNewIdToHoles()
        processCourses()
        assignNewHoleIdsToGameHoles()
        createRounds()
        createScores()
        addMissingScores()
    }

    /**
     * Problem with scores: In Discscores, if game is ended before all scores are set, then the score objects are not created for the rest of the holes.
     * In our case, we need to go through each game, and make sure for all holes in the courses, there are Scores
     * The rounds are looped through, to add the scores for missing holes.
     * Rounds with no scores are deleted.
     */
    private fun addMissingScores() {
        val emptyRounds = mutableListOf<Round>()

        var nextScoreId = (scores.count() + 1).toLong()
        rounds.forEach { round ->
            val roundCourse = courses.find { it.courseId == round.courseId }
                ?: throw Exception("Couldn't find course with id ${round.courseId}.")
            val courseHoles = holes.filter { it.parentCourseId == roundCourse.courseId }
            val roundScores = scores.filter { it.parentRoundId == round.dateStarted }

            if (roundScores.count() == 0) {
                emptyRounds.add(round)
                return@forEach
            }

            val roundPlayerIds = roundScores.distinctBy { it.playerId }.map { it.playerId }

            for (holeId in courseHoles.map { it.holeId }) {
                if (roundScores.find { it.holeId == holeId } == null) {
                    for (playerId in roundPlayerIds) {
                        val scoreToAdd = Score(
                            id = nextScoreId,
                            parentRoundId = round.dateStarted,
                            playerId = playerId,
                            holeId = holeId,
                            result = null
                        )
                        scores.add(scoreToAdd)
                        nextScoreId += 1
                    }
                }
            }
        }
        rounds.removeAll(emptyRounds)
    }

    private fun createScores() {
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
                // Can't add ID 0 to Room.
                id = (scoreId + 1).toLong(),
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

    /**
     * Go through each DiscsScoresGame and create a Round.
     */
    private fun createRounds() {
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
    }

    /**
     * Go through each GameHole and assign the new holeId to each of the GameHoles as well.
     * In order to do it, we need to use the course uuid and hole number in order to find the corresponding hole.
     */
    private fun assignNewHoleIdsToGameHoles() {
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
    }

    /**
     * Create courses according to Course class.
     * Create new course id from the position in the json file, and add it to each hole for
     * that course. And to each game for that course as well.
     *
     * Also create a Hole object for each DiscscoresHole.
     */
    private fun processCourses() {
        discscoresCourses.discscoresCourses.forEachIndexed { courseIndex, discscoresCourse ->
            // Can't add ID 0 to Room
            val newCourseId = (courseIndex + 1).toLong()
            discscoresCourse.newCourseId = newCourseId
            val course = Course(
                courseId = newCourseId,
                name = discscoresCourse.name
            )
            courses.add(course)

            discscoresCourses.discscoresHoles.filter { it.courseUuid == discscoresCourse.uuid }
                .forEachIndexed { _, discscoresHole ->
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
    }

    /**
     * Goes through each hole and assign a new position based Id.
     */
    private fun assignNewIdToHoles() {
        discscoresCourses.discscoresHoles.forEachIndexed { holeIndex, dsHole ->
            // Can't add ID 0 to Room
            val newHoleId = (holeIndex + 1).toLong()
            dsHole.newHoleId = newHoleId
        }
    }

    /**
     * Create players according to Player class. Create new player id according to position in json file. Adds the new id value to DiscscoresGamePlayers &
     * DiscscoresScores.
     */
    private fun processPlayerData() {
        discscoresPlayers.players.forEachIndexed { index, discscoresPlayer ->
            // Can't add ID 0 to Room
            val newPlayerId = (index + 1).toLong()
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
    }

    /**
     * Read Discscores Json data and initialize the discscores data objects.
     * @param playersJson Text from players.json file.
     * @param coursesJson Text from courses.json file.
     * @param gamesJson Text from games.json file.
     */
    private fun readJsonData(playersJson: String, coursesJson: String, gamesJson: String) {
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
    }

    companion object {
        /**
         * Function for converting time in Discscores json files to OffsetDateTime.
         */
        fun convertDiscscoresTimeStampToOffsetDateTime(epochValue: Long): OffsetDateTime {
            val offset = OffsetDateTime.now().offset
            return OffsetDateTime.of(
                LocalDateTime.ofEpochSecond(epochValue / 1000, 0, offset),
                offset
            )
        }
    }
}