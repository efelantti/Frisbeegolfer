package fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscscoresScore(
    @Json(name = "createdAt")
    val createdAt: String?,
    @Json(name = "gameHoleUuid")
    val gameHoleUuid: String,
    @Json(name = "gamePlayerUuid")
    val gamePlayerUuid: String,
    @Json(name = "gameUuid")
    val gameUuid: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "score")
    val score: Int,
    @Json(name = "updatedAt")
    val updatedAt: String?,
    @Json(name = "uuid")
    val uuid: String
)