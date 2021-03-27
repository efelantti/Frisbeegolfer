package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.dao.PlayerDao
import fi.efelantti.frisbeegolfer.dao.RoundDao
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.Hole
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
class DatabaseTests {
    private lateinit var playerDao: PlayerDao
    private lateinit var courseDao: CourseDao
    private lateinit var roundDao: RoundDao
    private lateinit var db: FrisbeegolferRoomDatabase

    @get:Rule
    val  instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, FrisbeegolferRoomDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).allowMainThreadQueries().build()
        playerDao = db.playerDao()
        courseDao = db.courseDao()
        roundDao = db.roundDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        // TODO - Can't close the database - otherwise test crashes... See https://stackoverflow.com/questions/61044457/android-room-instrumented-tests-crashing-when-properly-closing-db-connection
        //db.close()
    }

    @Test
    @Throws(Exception::class)
    fun storeReadAndUpdateAndDeletePlayers() = runBlocking {
            val player = Player(name= "Tester", email="email")
            playerDao.insert(player)
            var allPlayers = playerDao.getPlayers()

            // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
            var result = allPlayers.getValueBlocking() ?: throw InvalidObjectException("null returned as players")

            assertThat(result.count(), equalTo(1))

            var resultPlayer = result[0]

            assertThat(resultPlayer.name, equalTo(player.name))
            assertThat(resultPlayer.email, equalTo(player.email))

            resultPlayer.name = "Some other name"
            playerDao.update(resultPlayer)

            allPlayers = playerDao.getPlayers()
            result = allPlayers.getValueBlocking() ?: throw InvalidObjectException("null returned as players")

            assertThat(result.count(), equalTo(1))

            resultPlayer = result[0]

            assertThat(resultPlayer.name, equalTo("Some other name"))
            assertThat(resultPlayer.email, equalTo(player.email))

            playerDao.delete(resultPlayer)

            allPlayers = playerDao.getPlayers()
            result = allPlayers.getValueBlocking() ?: throw InvalidObjectException("null returned as players")

            assertThat(result.count(), equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun storeReadAndUpdateAndDeleteCourses() = runBlocking {
            val course = Course(name= "Test course", city = "Test city")
            courseDao.insert(course)
            var allCourses = courseDao.getCoursesWithHoles()

            // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
            var result = allCourses.getValueBlocking() ?: throw InvalidObjectException("null returned as players")

            assertThat(result.count(), equalTo(1))

            var resultCourse = result[0]

            assertThat(resultCourse.course.city, equalTo(course.city))
            assertThat(resultCourse.course.name, equalTo(course.name))

            resultCourse.course.city = "Some other name"
            courseDao.update(resultCourse.course)
            allCourses = courseDao.getCoursesWithHoles()
            result = allCourses.getValueBlocking() ?: throw InvalidObjectException("null returned as courses")

            assertThat(result.count(), equalTo(1))

            resultCourse = result[0]

            assertThat(resultCourse.course.city, equalTo("Some other name"))
            assertThat(resultCourse.course.name, equalTo(course.name))

            courseDao.delete(resultCourse.course)

            allCourses = courseDao.getCoursesWithHoles()
            result = allCourses.getValueBlocking() ?: throw InvalidObjectException("null returned as courses")

            assertThat(result.count(), equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun readCoursesWithHoles() = runBlocking {
        val course = Course(name= "Test course", city = "Test city")
        val courseId = courseDao.insert(course)
        val holes = listOf(
            Hole(parentCourseId = courseId, par = 3, holeNumber = 1, lengthMeters = 100),
            Hole(parentCourseId = courseId, par = 2, holeNumber = 2),
            Hole(parentCourseId = courseId, par = 4, holeNumber = 3))

        courseDao.insertAll(holes)

        var holeFromOtherCourse = Hole(parentCourseId = courseId + 1, par = 3, holeNumber = 4)

        courseDao.insertAll(listOf(holeFromOtherCourse)) // To see that only holes with same course id are fetched.

        var allCourses = courseDao.getCoursesWithHoles()
        var result = allCourses.getValueBlocking() ?: throw InvalidObjectException("null returned as courses")
        var resultCourse = result[0]

        assertThat(resultCourse.holes.count(), equalTo(3))

        resultCourse.holes.sortedBy { it.holeNumber }.forEachIndexed { index, hole ->
            assertThat(hole.parentCourseId, equalTo(holes[index].parentCourseId))
        }
    }

}
