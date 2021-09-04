package fi.efelantti.frisbeegolfer.managediscscoresdata.discscoresdataformat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscscoresCourses(
    @Json(name = "courses")
    val discscoresCourses: List<DiscscoresCourse>,
    @Json(name = "createdAt")
    val createdAt: String?,
    @Json(name = "holes")
    val discscoresHoles: List<DiscscoresHole>,
    @Json(name = "version")
    val version: Int?
)