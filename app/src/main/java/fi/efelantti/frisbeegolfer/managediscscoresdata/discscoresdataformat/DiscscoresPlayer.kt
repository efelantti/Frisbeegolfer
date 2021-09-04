package fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscscoresPlayer(
    @Json(name = "createdAt")
    val createdAt: String?,
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "owner")
    val owner: Int?,
    @Json(name = "profileImageFilename")
    val profileImageFilename: String?,
    @Json(name = "updatedAt")
    val updatedAt: String?,
    @Json(name = "uuid")
    val uuid: String
)