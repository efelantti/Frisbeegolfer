package fi.efelantti.frisbeegolfer

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.Hole
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
class CourseDaoTests {
    private lateinit var courseDao: CourseDao
    private lateinit var db: FrisbeegolferRoomDatabase
    private var courseId = -1L

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, FrisbeegolferRoomDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).allowMainThreadQueries()
            .build()
        courseDao = db.courseDao()

        val course = Course(name = "Test course", city = "Test city")
        courseId = courseDao.insert(course)

        val holes = listOf(
            Hole(parentCourseId = courseId, par = 3, holeNumber = 1, lengthMeters = 100),
            Hole(parentCourseId = courseId, par = 2, holeNumber = 2),
            Hole(parentCourseId = courseId, par = 4, holeNumber = 3)
        )

        courseDao.insertAll(holes)

        val holeFromOtherCourse = Hole(parentCourseId = courseId + 1, par = 3, holeNumber = 4)

        courseDao.insertAll(listOf(holeFromOtherCourse)) // To see that only holes with same course id are fetched.
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        // TODO - Can't close the database - otherwise test crashes... See https://stackoverflow.com/questions/61044457/android-room-instrumented-tests-crashing-when-properly-closing-db-connection
        // db.close()
    }

    @Test
    @Throws(Exception::class)
    fun readCourses() = runBlocking {
        val allCourses = courseDao.getCoursesWithHoles()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        val result = allCourses.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        assertThat(result.count(), equalTo(1))

        val resultCourse = result[0]

        assertThat(resultCourse.course.city, equalTo("Test city"))
        assertThat(resultCourse.course.name, equalTo("Test course"))
    }

    @Test
    @Throws(Exception::class)
    fun updateCourses() = runBlocking {
        var allCourses = courseDao.getCoursesWithHoles()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        var result = allCourses.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        var resultCourse = result[0]
        resultCourse.course.city = "Some other name"
        courseDao.update(resultCourse.course)

        allCourses = courseDao.getCoursesWithHoles()
        result = allCourses.getValueBlocking()
            ?: throw InvalidObjectException("null returned as courses")
        assertThat(result.count(), equalTo(1))
        resultCourse = result[0]
        assertThat(resultCourse.course.city, equalTo("Some other name"))
        assertThat(resultCourse.course.name, equalTo("Test course"))
    }

    @Test
    @Throws(Exception::class)
    fun deleteCourses() = runBlocking {
        var allCourses = courseDao.getCoursesWithHoles()

        // the .getValueBlocking cannot be run on the background thread - needs the InstantTaskExecutorRule
        var result = allCourses.getValueBlocking()
            ?: throw InvalidObjectException("null returned as players")

        val resultCourse = result[0]

        courseDao.delete(resultCourse.course)
        allCourses = courseDao.getCoursesWithHoles()
        result = allCourses.getValueBlocking()
            ?: throw InvalidObjectException("null returned as courses")
        assertThat(result.count(), equalTo(0))
    }

    @Test
    @Throws(Exception::class)
    fun readCoursesWithHoles() = runBlocking {
        val allCourses = courseDao.getCoursesWithHoles()
        val result = allCourses.getValueBlocking()
            ?: throw InvalidObjectException("null returned as courses")
        val resultCourse = result[0]

        assertThat(resultCourse.holes.count(), equalTo(3))

        resultCourse.holes.sortedBy { it.holeNumber }.forEach {hole ->
            assertThat(hole.parentCourseId, equalTo(courseId))
        }
    }
}
