package fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscscoresGames(
    @Json(name = "createdAt")
    val createdAt: String?,
    @Json(name = "gameHoles")
    val discscoresGameHoles: List<DiscscoresGameHole>,
    @Json(name = "gamePlayers")
    val discscoresGamePlayers: List<DiscscoresGamePlayer>,
    @Json(name = "games")
    val discscoresGames: List<DiscscoresGame>,
    @Json(name = "scores")
    val discscoresScores: List<DiscscoresScore>,
    @Json(name = "version")
    val version: Int?
)