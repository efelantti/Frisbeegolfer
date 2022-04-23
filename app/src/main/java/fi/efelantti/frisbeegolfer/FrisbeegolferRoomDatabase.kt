package fi.efelantti.frisbeegolfer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.managediscscoresdata.DiscscoresDataHandler
import fi.efelantti.frisbeegolfer.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(
    entities = [Player::class, Course::class, Hole::class, Round::class, Score::class],
    version = 18,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FrisbeegolferRoomDatabase : RoomDatabase() {

    private val numberOfThreads = 1
    val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)

    abstract fun playerDao(): PlayerDao
    abstract fun courseDao(): CourseDao
    abstract fun roundDao(): RoundDao

    companion object {
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table with "ON UPDATE CASCADE" for parentRoundId foreign key action
                database.execSQL(
                    """
             CREATE TABLE `Score_new`
             (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
             `parentRoundId` TEXT NOT NULL,
             `playerId` INTEGER NOT NULL,
             `holeId` INTEGER NOT NULL,
             `result` INTEGER,
             `isOutOfBounds` INTEGER NOT NULL,
             `didNotFinish` INTEGER NOT NULL,
             FOREIGN KEY(`parentRoundId`) REFERENCES `Round`(`dateStarted`) ON UPDATE CASCADE ON DELETE CASCADE ,
             FOREIGN KEY(`playerId`) REFERENCES `Player`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE ,
             FOREIGN KEY(`holeId`) REFERENCES `Hole`(`holeId`) ON UPDATE NO ACTION ON DELETE CASCADE )"""
                        .trimIndent()
                )
                // Copy the rows from existing table to new table
                database.execSQL(
                    """
            INSERT INTO Score_new (id, parentRoundId, playerId, holeId, result, isOutOfBounds, didNotFinish)
            SELECT id, parentRoundId, playerId, holeId, result, isOutOfBounds, didNotFinish FROM Score"""
                        .trimIndent()
                )

                // Remove the old table
                database.execSQL("DROP TABLE Score")
                // Change the new table name to the correct one
                database.execSQL("ALTER TABLE Score_new RENAME TO Score")

                // Add indices
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_Score_parentRoundId` ON `Score` (`parentRoundId`)
                """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_Score_playerId` ON `Score` (`playerId`)
                """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_Score_holeId` ON `Score` (`holeId`)
                """.trimIndent()
                )
            }
        }


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
                    .fallbackToDestructiveMigrationFrom(15, 16)
                    .addMigrations(MIGRATION_17_18)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    override fun close() {
        super.close()
        INSTANCE = null
    }

    // TODO - Memory leaks? Move this code to somewhere else, like Activity?
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
        if (!zippedDiscscoresFile.exists()) throw FileNotFoundException("Discscores zip does not exist.")
        if (zippedDiscscoresFile.extension != "zip") throw IllegalArgumentException("Discscores file is not a .zip file.")
        val tempDir = File(context.filesDir, discscoresFilesToImportFolderName)
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        // Remove previous import files.
        val tempDirFiles = tempDir.listFiles()
            ?: throw IllegalArgumentException("Temp dir files was null.")
        for (file: File in tempDirFiles) {
            file.delete()
        }
        unzip(zippedDiscscoresFile, tempDir)
        // Discscores zip is expected to have 5 files - players.json, courses.json, games.json, meta.json & images folder (however meta.json & images is not needed for importing).
        if (tempDirFiles.size != 5) throw IllegalArgumentException("Discscores zip did not contain 4 files.")
        val playersFile = tempDir.listFiles { file ->
            file.name == "players.json"
        }
        val coursesFile = tempDir.listFiles { file ->
            file.name == "courses.json"
        }
        val gamesFile = tempDir.listFiles { file ->
            file.name == "games.json"
        }
        if (playersFile == null || playersFile.size != 1) throw IllegalArgumentException("Discscores zip did not contain players.json.")
        if (coursesFile == null || coursesFile.size != 1) throw IllegalArgumentException("Discscores zip did not contain courses.json.")
        if (gamesFile == null || gamesFile.size != 1) throw IllegalArgumentException("Discscores zip did not contain games.json.")

        val playersJsonText = playersFile.single().readText()
        val coursesJsonText = coursesFile.single().readText()
        val gamesJsonText = gamesFile.single().readText()

        val discscoresDataHandler =
            DiscscoresDataHandler(playersJsonText, coursesJsonText, gamesJsonText)

        // TODO - Check if this works. Was GlobalScope.launch(Dispatchers.IO) before.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                insertDiscscoresData(
                    context,
                    discscoresDataHandler.players,
                    discscoresDataHandler.courses,
                    discscoresDataHandler.holes,
                    discscoresDataHandler.rounds,
                    discscoresDataHandler.scores
                )
            } catch (ex: Exception) {
                Handler(Looper.getMainLooper()).post {
                    ToastUtils.showErrorToast(
                        context,
                        context.resources.getText(R.string.error_importing_discscores)
                    )
                }
                Log.e("IMPORT Discscores", ex.stackTraceToString())
            }
        }
    }

    /**
     * Clears existing data from database, and insert new data into database. If any exception occurs, no data is added, nor removed.
     * Toasts are displayed in order to show progress.
     *
     */
    private suspend fun insertDiscscoresData(
        context: Context,
        players: List<Player>,
        courses: List<Course>,
        holes: List<Hole>,
        rounds: List<Round>,
        scores: List<Score>
    ) {
        withTransaction {
            clearAllTables()
            // reset all auto-incrementalValues
            val query = SimpleSQLiteQuery("DELETE FROM sqlite_sequence")
            query(query)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    context.resources.getText(R.string.adding_discscores_players),
                    Toast.LENGTH_SHORT
                ).show()
            }
            playerDao().insertAll(players)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    context.resources.getText(R.string.adding_discscores_courses),
                    Toast.LENGTH_SHORT
                ).show()
            }
            courseDao().insertAllCourses(courses)
            courseDao().insertAll(holes)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    context.resources.getText(R.string.adding_discscores_rounds),
                    Toast.LENGTH_SHORT
                ).show()
            }
            roundDao().insertAll(rounds)
            roundDao().insertScores(scores)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    context.resources.getText(R.string.discscores_imported),
                    Toast.LENGTH_SHORT
                ).show()
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