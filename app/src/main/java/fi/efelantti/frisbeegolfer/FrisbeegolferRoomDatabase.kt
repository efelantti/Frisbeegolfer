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

//TODO - Consider export schema
@Database(
    entities = [Player::class, Course::class, Hole::class, Round::class, Score::class],
    version = 17,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FrisbeegolferRoomDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun courseDao(): CourseDao
    abstract fun roundDao(): RoundDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: FrisbeegolferRoomDatabase? = null

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
                    "frisbeegolfer_database"
                ).addCallback(FrisbeegolferDatabaseCallback(scope))
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

    private class FrisbeegolferDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
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