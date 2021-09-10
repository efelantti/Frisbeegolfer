package fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscscoresGameHole(
    @Json(name = "createdAt")
    val createdAt: String?,
    @Json(name = "gameUuid")
    val gameUuid: String,
    @Json(name = "hole")
    val hole: Int,
    @Json(name = "id")
    val id: String,
    @Json(name = "par")
    val par: Int,
    @Json(name = "updatedAt")
    val updatedAt: String?,
    @Json(name = "uuid")
    val uuid: String,
    var newHoleId: Long?
)