package fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscscoresGamePlayer(
    @Json(name = "createdAt")
    val createdAt: String?,
    @Json(name = "gameUuid")
    val gameUuid: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "playerUuid")
    val playerUuid: String,
    @Json(name = "uuid")
    val uuid: String
)