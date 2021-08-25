package fi.efelantti.frisbeegolfer

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

object ServiceLocator {

    private var database: FrisbeegolferRoomDatabase? = null

    @Volatile
    var repository: Repository? = null

    fun provideRepository(context: Context): Repository {
        synchronized(this) {
            return repository ?: createRepository(context)
        }
    }

    fun provideDatabase(context: Context): FrisbeegolferRoomDatabase {
        return FrisbeegolferRoomDatabase.getDatabase(
            context,
            CoroutineScope(EmptyCoroutineContext)
        )
    }

    private fun createRepository(context: Context): Repository {
        database = FrisbeegolferRoomDatabase.getDatabase(
            context,
            CoroutineScope(EmptyCoroutineContext)
        )
        val newRepo =
            Repository(
                database!!,
                database!!.playerDao(),
                database!!.courseDao(),
                database!!.roundDao()
            )
        repository = newRepo
        return newRepo
    }
}
