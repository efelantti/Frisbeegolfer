package fi.efelantti.frisbeegolfer.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole

@Dao
interface CourseDao {

    @Insert
    suspend fun insertAll(holes: List<Hole>)

    @Update
    suspend fun updateAll(holes: List<Hole>)

    @Insert
    suspend fun insert(course: Course): Long

    //TODO - Figure out how to delete a course.
    //TODO - When deleting a course, also its holes should be deleted.
    //TODO - What happens to stats when a course is deleted?
    @Delete
    suspend fun delete(course: Course)

    @Update
    fun update(course: Course)

    @Transaction
    @Query("SELECT * FROM Course")
    fun getCoursesWithHoles(): LiveData<List<CourseWithHoles>>

    @Query("SELECT * FROM Course WHERE courseId =:id")
    fun getCourseWithHolesWithId(id: Long): CourseWithHoles

    @Transaction
    @Query("SELECT * FROM Hole")
    fun getHoles(): LiveData<List<Hole>>

    //TODO - What happens to stats when a hole is deleted?
    @Delete
    suspend fun delete(hole: Hole)
}