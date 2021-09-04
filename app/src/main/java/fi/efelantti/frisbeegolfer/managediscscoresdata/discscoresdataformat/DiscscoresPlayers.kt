package fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscscoresPlayers(
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "players")
    val players: List<DiscscoresPlayer>,
    @Json(name = "version")
    val version: Int
)