package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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

        fun getDatabase(context: Context): FrisbeegolferRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FrisbeegolferRoomDatabase::class.java,
                    "frisbeegolfer_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}