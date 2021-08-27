package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import unzip
import java.io.IOException
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class DatabaseTests {
    private lateinit var db: FrisbeegolferRoomDatabase
    private lateinit var instrumentationContext: Context

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, FrisbeegolferRoomDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).allowMainThreadQueries()
            .build()
    }

    @Before
    fun initiateContext() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        // Can't close the database - otherwise test crashes... See https://stackoverflow.com/questions/61044457/android-room-instrumented-tests-crashing-when-properly-closing-db-connection
        // db.close()
    }

    @Test
    @Throws(Exception::class)
    fun createdDatabaseZipExists() = runBlocking {
        val zippedFile = db.createDatabaseZip(instrumentationContext)
        assertThat(zippedFile.exists(), equalTo(true))
    }

    @Test
    @Throws(Exception::class)
    fun createdDatabaseZipContainsTheDatabaseFiles() = runBlocking {
        val dbName = FrisbeegolferRoomDatabase.databaseName
        val dbPath = instrumentationContext.getDatabasePath(FrisbeegolferRoomDatabase.databaseName)
        val shmPath =
            instrumentationContext.getDatabasePath("${FrisbeegolferRoomDatabase.databaseName}-shm")
        val walPath =
            instrumentationContext.getDatabasePath("${FrisbeegolferRoomDatabase.databaseName}-wal")
        val zippedFile = db.createDatabaseZip(instrumentationContext)
        val testDir = instrumentationContext.getDir("tmp", 0)
        unzip(zippedFile, testDir)
        val walFiles = testDir.listFiles { file ->
            file.length() == walPath.length() && file.name == "$dbName-wal"
        }
        val dbFiles = testDir.listFiles { file ->
            file.length() == dbPath.length() && file.name == dbName
        }
        val shFiles = testDir.listFiles { file ->
            file.length() == shmPath.length() && file.name == "$dbName-shm"
        }
        assertThat(testDir.listFiles().size, equalTo(3))
        assertThat(walFiles.size, equalTo(1))
        assertThat(dbFiles.size, equalTo(1))
        assertThat(shFiles.size, equalTo(1))
    }

    @Test
    @Throws(Exception::class)
    fun importDatabaseRuns() = runBlocking {
        val zippedFile = db.createDatabaseZip(instrumentationContext)
        db.importDatabaseZip(instrumentationContext, zippedFile)
    }
}
