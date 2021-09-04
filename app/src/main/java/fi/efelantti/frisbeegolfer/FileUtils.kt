package fi.efelantti.frisbeegolfer

import android.content.Context
import java.io.InputStream

class FileUtils {
    companion object {
        fun getJsonFromAssets(context: Context, fileName: String): String {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            return String(buffer)

        }
    }
}