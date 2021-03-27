package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.model.Player
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InvalidObjectException
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class DatabaseTests {
    private lateinit var playerDao: PlayerDao
    private lateinit var db: FrisbeegolferRoomDatabase

    @get:Rule
    val  instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, FrisbeegolferRoomDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).build()
        playerDao = db.playerDao()
    }

    @Test
    @Throws(Exception::class)
    fun storeAndReadPlayers() {
        val player = Player(name= "Tester", email="email")

        runBlocking {
            playerDao.insert(player)
        }

        val allGames = playerDao.getPlayers()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        val result = allGames.getValueBlocking() ?: throw InvalidObjectException("null returned as players")

        assertThat(result.count(), equalTo(1))
        assertThat(result[0].name, equalTo(player.name))
        assertThat(result[0].email, equalTo(player.email))
    }

    /*
    @Test
    fun storeAndReadGameLinkedWithRound() {
        val game = Game(...)

        val rounds = listOf(
            Round(...),
        Round(...),
        Round(...)
        )

        runBlocking {
            // This is where the execution freezes when InstantTaskExecutorRule is used
            playerDao.insertGameAndRounds(game, rounds)
        }

        // retrieve the data, assert on it, etc
    }
     */
}
