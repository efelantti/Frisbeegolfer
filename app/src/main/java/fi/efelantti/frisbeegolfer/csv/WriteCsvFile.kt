package fi.efelantti.frisbeegolfer.csv

import java.io.FileWriter

public inline fun <reified T> writeCsvFile(data: Collection<T>, fileName: String) {
    FileWriter(fileName).use { writer ->
        csvMapper.writer(csvMapper.schemaFor(T::class.java).withHeader())
            .writeValues(writer)
            .writeAll(data)
            .close()
    }
}