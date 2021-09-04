package fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscscoresGame(
    @Json(name = "courseUuid")
    val courseUuid: String,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "endedAt")
    val endedAt: String?,
    @Json(name = "id")
    val id: String,
    @Json(name = "startedAt")
    val startedAt: String,
    @Json(name = "updatedAt")
    val updatedAt: String?,
    @Json(name = "uuid")
    val uuid: String
)