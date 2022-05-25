package fi.efelantti.frisbeegolfer.csv

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import java.io.FileReader

inline fun <reified T> readCsvFile(fileName: String): List<T> {
    FileReader(fileName).use { reader ->
        return csvMapper
            .readerFor(T::class.java)
            .with(CsvSchema.emptySchema().withHeader())
            .readValues<T>(reader)
            .readAll()
            .toList()
    }
}