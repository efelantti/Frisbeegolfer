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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.io.InvalidObjectException
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class PlayerDaoTests {
    private lateinit var playerDao: PlayerDao
    private lateinit var db: FrisbeegolferRoomDatabase
    private var playerId = -1L

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, FrisbeegolferRoomDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).allowMainThreadQueries()
            .build()
        playerDao = db.playerDao()

        val player = Player(name = "Tester", email = "email")
        playerId = playerDao.insertAndGetId(player)
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        // Can't close the database - otherwise test crashes... See https://stackoverflow.com/questions/61044457/android-room-instrumented-tests-crashing-when-properly-closing-db-connection
        // db.close()
    }

    @Test
    @Throws(Exception::class)
    fun readPlayers() = runBlocking {
        val allPlayers = playerDao.getPlayers()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        val result = allPlayers.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        assertThat(result.count(), equalTo(1))

        val resultPlayer = result[0]

        assertThat(resultPlayer.name, equalTo("Tester"))
        assertThat(resultPlayer.email, equalTo("email"))
    }

    @Test
    @Throws(Exception::class)
    fun updatePlayers() = runBlocking {

        var allPlayers = playerDao.getPlayers()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        var result = allPlayers.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        var resultPlayer = result[0]
        resultPlayer.name = "Some other name"
        playerDao.update(resultPlayer)

        allPlayers = playerDao.getPlayers()
        result = allPlayers.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(1))
        resultPlayer = result[0]
        assertThat(resultPlayer.name, equalTo("Some other name"))
        assertThat(resultPlayer.email, equalTo("email"))
    }

    @Test
    @Throws(Exception::class)
    fun deletePlayers() = runBlocking {
        var allPlayers = playerDao.getPlayers()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        var result = allPlayers.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        val resultPlayer = result[0]
        playerDao.delete(resultPlayer)

        allPlayers = playerDao.getPlayers()
        result = allPlayers.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        assertThat(result.count(), equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun playerExistsReturnsTrueWhenPlayerExists() = runBlocking {
        val exists = playerDao.playerExists(playerId)
        var result =
            exists.getValueBlocking() ?: throw InvalidObjectException("null returned as result")
        assertThat(result, equalTo(true))
    }

    @Test
    @Throws(Exception::class)
    fun playerExistsReturnsFalseWhenPlayerDoesNotExist() = runBlocking {
        val exists = playerDao.playerExists(-1)
        var result =
            exists.getValueBlocking() ?: throw InvalidObjectException("null returned as result")
        assertThat(result, equalTo(false))
    }
}
