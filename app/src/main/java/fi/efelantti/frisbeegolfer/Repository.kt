package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Player

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class Repository(private val database: FrisbeegolferRoomDatabase) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    private val playerDao = database.playerDao()
    private val courseDao = database.courseDao()
    val allPlayers: LiveData<List<Player>> = playerDao.getPlayers()
    val allCourses: LiveData<List<CourseWithHoles>> = courseDao.getCoursesWithHoles()
    val allCoursesWithHoles: LiveData<List<CourseWithHoles>> = courseDao.getCoursesWithHoles()

    suspend fun insert(player: Player) {
        playerDao.insert(player)
    }

    suspend fun update(player: Player) {
        playerDao.update(player)
    }

    suspend fun update(course: CourseWithHoles)
    {
        courseDao.updateAll(course.holes)
        courseDao.update(course.course)
    }

    suspend fun insertCourseWithHoles(course: CourseWithHoles) {
        for (hole in course.holes) {
            hole.parentCourseId = course.course.courseId
        }
        courseDao.insertAll(course.holes)
        courseDao.insert(course.course)
    }
}