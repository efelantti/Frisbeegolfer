package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//TODO - Add to entities rest of entities when they are done
//TODO - Consider export schema
@Database(entities = arrayOf(Player::class), version = 1, exportSchema = false)
public abstract class FrisbeegolferRoomDatabase : RoomDatabase() {

    //TODO - Add rest of DAO's when they are done
    abstract fun playerDao(): PlayerDao

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
                ).addCallback(FrisbeegolferDatabaseCallback(scope)).build()
                INSTANCE = instance
                return instance
            }
        }
    }

    private class FrisbeegolferDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.playerDao())
                }
            }
        }

        suspend fun populateDatabase(playerDao: PlayerDao) {

            // TODO - Remove the examples.
            //var player = Player(firstName = "Esa", lastName = "Esimerkki", email = "esa@esimerkki.com", nickName = "")
            //playerDao.insert(player)
            //player = Player(firstName = "Maisa", lastName = "Mallikappale", email = "maisa@mallikappale.com", nickName = "")
            //playerDao.insert(player)
        }
    }
}