package fi.efelantti.frisbeegolfer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class FrisbeegolferApplication : Application() {

    val repository: Repository
        get() = ServiceLocator.provideRepository(this)

    val database: FrisbeegolferRoomDatabase
        get() = ServiceLocator.provideDatabase(this)

    override fun onCreate() {
        super.onCreate()
        val theme = ThemeProvider(this).getThemeFromPreferences()
        AppCompatDelegate.setDefaultNightMode(theme)
    }
}
