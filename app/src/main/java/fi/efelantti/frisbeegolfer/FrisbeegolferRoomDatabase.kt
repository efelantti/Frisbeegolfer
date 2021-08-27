package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import unzip
import zip
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//TODO - Consider export schema
@Database(
    entities = [Player::class, Course::class, Hole::class, Round::class, Score::class],
    version = 17,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FrisbeegolferRoomDatabase : RoomDatabase() {

    private val numberOfThreads = 1
    val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)

    abstract fun playerDao(): PlayerDao
    abstract fun courseDao(): CourseDao
    abstract fun roundDao(): RoundDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: FrisbeegolferRoomDatabase? = null

        const val databaseName = "frisbeegolfer_database"

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): FrisbeegolferRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FrisbeegolferRoomDatabase::class.java,
                    databaseName
                )
                    .addCallback(FrisbeegolferDatabaseCallback(scope))
                    .fallbackToDestructiveMigrationFrom(15, 16).build()
                INSTANCE = instance
                return instance
            }
        }
    }

    override fun close() {
        super.close()
        INSTANCE = null
    }

    fun createDatabaseZip(context: Context): File {
        val currentDate = SimpleDateFormat("ddMMyy", Locale.getDefault())
        val dbPath = context.getDatabasePath(databaseName)
        val shmPath = context.getDatabasePath("$databaseName-shm")
        val walPath = context.getDatabasePath("$databaseName-wal")
        val dbFiles = listOf(dbPath, shmPath, walPath)

        val zipPathDir = File(context.filesDir, "exported_databases")
        if (!zipPathDir.exists()) {
            zipPathDir.mkdir()
        }
        // Remove obsolete backups.
        for (file: File in zipPathDir.listFiles()) {
            file.delete()
        }

        val zippedDatabase = File(
            zipPathDir,
            "${currentDate.format(Date())}_" + databaseName + ".zip"
        )
        zip(zippedDatabase, dbFiles)
        return zippedDatabase
    }

    fun importDatabaseZip(context: Context, zippedDatabase: File) {
        if (!zippedDatabase.exists()) throw FileNotFoundException("Database file does not exist.")
        if (zippedDatabase.extension != "zip") throw IllegalArgumentException("Database file is not a .zip file.")
        val tempDir = File(context.filesDir, "database_files_to_import")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        // Remove previous import files.
        for (file: File in tempDir.listFiles()) {
            file.delete()
        }
        unzip(zippedDatabase, tempDir)
        if (tempDir.listFiles().size != 3) throw IllegalArgumentException("Imported database zip did not contain 3 files.")
        val dbFile = tempDir.listFiles { file ->
            file.name == databaseName
        }
        val walFile = tempDir.listFiles { file ->
            file.name == "$databaseName-wal"
        }
        val shmFile = tempDir.listFiles { file ->
            file.name == "$databaseName-shm"
        }
        if (dbFile.size != 1) throw IllegalArgumentException("Imported database zip did not contain the database file.")
        if (walFile.size != 1) throw IllegalArgumentException("Imported database zip did not contain the database-wal file.")
        if (shmFile.size != 1) throw IllegalArgumentException("Imported database zip did not contain the database-shm file.")

        // TODO - Should db be closed at this point?
        // TODO - Take a snapshot of the database before overwriting.
        // TODO - Autorestore to previous version if something goes wrong in this process.
        val databaseFolder = context.getDatabasePath(databaseName).parentFile

        for (file: File in tempDir.listFiles()) {
            val toFile = File(tempDir, file.name)
            copyDataFromOneToAnother(file.canonicalPath, toFile.canonicalPath)
        }

        // TODO - Verify db integrity
    }

    private fun copyDataFromOneToAnother(fromPath: String, toPath: String) {
        val inStream = File(fromPath).inputStream()
        val outStream = FileOutputStream(toPath)

        inStream.use { input ->
            outStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    private class FrisbeegolferDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL("CREATE TRIGGER delete_childless_rounds AFTER DELETE ON Score WHEN (SELECT COUNT(*) FROM Score WHERE parentRoundId = OLD.parentRoundId) = 0 BEGIN DELETE FROM Round WHERE dateStarted = OLD.parentRoundId; END;")
            INSTANCE?.let {
                scope.launch {
                    //populateDatabase(database.roundDao())
                }
            }
        }

        /*suspend fun populateDatabase(roundDao: RoundDao) {

            var round = Round()
            roundDao.insert(round)

            round = Round(dateStarted = OffsetDateTime.now())
            roundDao.insert(round)

            round = Round(dateStarted = OffsetDateTime.of(2020, 6, 1, 13, 37, 0, 0, ZoneOffset.of("+02:00")))
            roundDao.insert(round)
            // Examples in case needed to add.
            //var player = Player(firstName = "Esa", lastName = "Esimerkki", email = "esa@esimerkki.com", nickName = "")
            //playerDao.insert(player)
            //player = Player(firstName = "Maisa", lastName = "Mallikappale", email = "maisa@mallikappale.com", nickName = "")
            //playerDao.insert(player)
        }*/
    }
}