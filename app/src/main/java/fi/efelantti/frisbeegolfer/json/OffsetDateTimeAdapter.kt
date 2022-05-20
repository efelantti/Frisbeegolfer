package fi.efelantti.frisbeegolfer.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

// Almost same as Room converter - see Converters.
class OffsetDateTimeAdapter {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @ToJson
    fun toJson(offsetDateTime: OffsetDateTime): String {
        return offsetDateTime.format(formatter)
    }

    @FromJson
    fun fromJson(offsetDateTime: String): OffsetDateTime {
        return formatter.parse(offsetDateTime, OffsetDateTime::from)
    }
}