package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.managediscscoresdata.DiscscoresDataHandler
import fi.efelantti.frisbeegolfer.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
        const val emergencyBackupFolderName = "emergency_backup"
        const val databaseFilesToImportFolderName = "database_files_to_import"
        const val discscoresFilesToImportFolderName = "discscores_files_to_import"
        const val exportedDatabasesFolderName = "exported_databases"

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

    /**
     * Creates a .zip package containing the database files: actual db, -shm file and -wal file.
     *
     * @return File object representing the .zip package containing database files.
     */
    fun createDatabaseZip(context: Context): File {
        val currentDate = SimpleDateFormat("ddMMyy", Locale.getDefault())
        val dbPath = context.getDatabasePath(databaseName)
        val shmPath = context.getDatabasePath("$databaseName-shm")
        val walPath = context.getDatabasePath("$databaseName-wal")
        val dbFiles = listOf(dbPath, shmPath, walPath)

        val zipPathDir = File(context.filesDir, exportedDatabasesFolderName)
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

    /**
     *  Function for importing database.
     *
     *  The database files a unzipped to filesDir/database_files_to_import/, and from there copied
     *  to dbFolder.
     *  @throws FileNotFoundException If [zippedDatabase] does not exist.
     *  @throws IllegalArgumentException If [zippedDatabase] is not a .zip file.
     *  @throws IllegalArgumentException If [zippedDatabase] does not contain the expected files (db, db-shm, db-wal).
     *  @param context Activity context.
     *  @param zippedDatabase Zip package containing the database files.
     */
    fun importDatabaseZip(context: Context, zippedDatabase: File) {
        createEmergencyBackup(context)
        if (!zippedDatabase.exists()) throw FileNotFoundException("Database file does not exist.")
        if (zippedDatabase.extension != "zip") throw IllegalArgumentException("Database file is not a .zip file.")
        val tempDir = File(context.filesDir, databaseFilesToImportFolderName)
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

        val databaseFolder = context.getDatabasePath(databaseName).parentFile

        for (file: File in tempDir.listFiles()) {
            val toFile = File(databaseFolder, file.name)
            copyDataFromOneToAnother(file.canonicalPath, toFile.canonicalPath)
        }
    }

    /**
     *  Function for importing discscores zip.
     *
     *  The database files are unzipped to filesDir/discscores_files_to_import/, then they are read (to discscoresdataformat objects) and stored to database.
     *  @throws FileNotFoundException If [zippedDiscscoresFile] does not exist.
     *  @throws IllegalArgumentException If [zippedDiscscoresFile] is not a .zip file.
     *  @throws IllegalArgumentException If [zippedDiscscoresFile] does not contain the expected files (players.json, courses.json, games.json).
     *  @param context Activity context.
     *  @param zippedDiscscoresFile Zip package containing the discscores files.
     */
    fun importDiscscoresZip(context: Context, zippedDiscscoresFile: File) {
        createEmergencyBackup(context)
        if (!zippedDiscscoresFile.exists()) throw FileNotFoundException("Discscores zip does not exist.")
        if (zippedDiscscoresFile.extension != "zip") throw IllegalArgumentException("Discscores file is not a .zip file.")
        val tempDir = File(context.filesDir, discscoresFilesToImportFolderName)
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        // Remove previous import files.
        for (file: File in tempDir.listFiles()) {
            file.delete()
        }
        unzip(zippedDiscscoresFile, tempDir)
        // Discscores zip is expected to have 5 files - players.json, courses.json, games.json, meta.json & images folder (however meta.json & images is not needed for importing).
        if (tempDir.listFiles().size != 5) throw IllegalArgumentException("Discscores zip did not contain 4 files.")
        val playersFile = tempDir.listFiles { file ->
            file.name == "players.json"
        }
        val coursesFile = tempDir.listFiles { file ->
            file.name == "courses.json"
        }
        val gamesFile = tempDir.listFiles { file ->
            file.name == "games.json"
        }
        if (playersFile.size != 1) throw IllegalArgumentException("Discscores zip did not contain players.json.")
        if (coursesFile.size != 1) throw IllegalArgumentException("Discscores zip did not contain courses.json.")
        if (gamesFile.size != 1) throw IllegalArgumentException("Discscores zip did not contain games.json.")

        val playersJsonText = playersFile.single().readText()
        val coursesJsonText = coursesFile.single().readText()
        val gamesJsonText = gamesFile.single().readText()

        val discscoresDataHandler =
            DiscscoresDataHandler(playersJsonText, coursesJsonText, gamesJsonText)

        // WIPE ALL DATA BEFORE THIS.

        GlobalScope.launch(Dispatchers.Main) {
            insertDiscscoresData(
                discscoresDataHandler.players,
                discscoresDataHandler.courses,
                discscoresDataHandler.holes,
                discscoresDataHandler.rounds,
                discscoresDataHandler.scores
            )
        }
    }

    private suspend fun insertDiscscoresData(
        players: List<Player>,
        courses: List<Course>,
        holes: List<Hole>,
        rounds: List<Round>,
        scores: List<Score>
    ) {
        withTransaction {
            // TODO - Debug!
            playerDao().insertAll(players)
            courseDao().insertAllCourses(courses)
            courseDao().insertAll(holes)
            roundDao().insertAll(rounds)
        }
    }

    /**
     * Function for creating an emergency backup of the existing database files - in case something
     * goes wrong with the import process, database can still be restored.
     */
    private fun createEmergencyBackup(context: Context) {
        val dbPath = context.getDatabasePath(databaseName)
        val shmPath = context.getDatabasePath("$databaseName-shm")
        val walPath = context.getDatabasePath("$databaseName-wal")
        val dbFiles = listOf(dbPath, shmPath, walPath)

        val emergencyBackupFolder = File(context.filesDir, emergencyBackupFolderName)
        if (!emergencyBackupFolder.exists()) {
            emergencyBackupFolder.mkdir()
        }

        for (file: File in dbFiles) {
            val toFile = File(emergencyBackupFolder, file.name)
            copyDataFromOneToAnother(file.canonicalPath, toFile.canonicalPath)
        }
    }

    /**
     * Function for restoring database from emergency copy.
     */
    fun restoreFromEmergencyBackup(context: Context) {
        val databaseFolder = context.getDatabasePath(databaseName).parentFile
        val emergencyBackupFolder = File(context.filesDir, emergencyBackupFolderName)
        for (file: File in emergencyBackupFolder.listFiles()) {
            val toFile = File(databaseFolder, file.name)
            copyDataFromOneToAnother(file.canonicalPath, toFile.canonicalPath)
        }

    }

    /**
     * Copies file from [fromPath] to [toPath].
     * @param fromPath Source file path.
     * @param toPath Destination file path.
     */
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