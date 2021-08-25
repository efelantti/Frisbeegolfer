package fi.efelantti.frisbeegolfer

import android.app.Application

class FrisbeegolferApplication : Application() {

    val repository: Repository
        get() = ServiceLocator.provideRepository(this)

    val database: FrisbeegolferRoomDatabase
        get() = ServiceLocator.provideDatabase(this)
}
