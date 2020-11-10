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

    //TODO - Figure out how to insert holes to a course
    @Insert
    suspend fun insert(course: Course)

    //TODO - When deleting a course, also its holes should be deleted.
    //TODO - What happens to stats when a course is deleted?
    @Delete
    suspend fun delete(course: Course)

    //TODO - How to update course with its holes?
    @Update
    fun update(course: Course)

    @Transaction
    @Query("SELECT * FROM Course")
    fun getCoursesWithHoles(): LiveData<List<CourseWithHoles>>
}