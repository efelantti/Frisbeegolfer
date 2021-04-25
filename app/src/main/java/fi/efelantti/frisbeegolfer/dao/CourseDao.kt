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

    @Insert
    suspend fun insertAllAndGetIds(holes: List<Hole>): List<Long>

    @Update
    suspend fun updateAll(holes: List<Hole>)

    @Insert
    suspend fun insert(course: Course): Long

    @Delete
    suspend fun delete(course: Course)

    @Update
    suspend fun update(course: Course)

    @Transaction
    @Query("SELECT * FROM Course")
    fun getCoursesWithHoles(): LiveData<List<CourseWithHoles>>

    @Transaction
    @Query("SELECT * FROM Course WHERE courseId =:id")
    fun getCourseWithHolesWithId(id: Long): LiveData<CourseWithHoles>

    @Transaction
    @Query("SELECT EXISTS(SELECT * FROM Course WHERE courseId = :id)")
    fun courseExists(id: Long): LiveData<Boolean>

    @Transaction
    @Query("SELECT * FROM Hole")
    fun getHoles(): LiveData<List<Hole>>

    @Delete
    suspend fun delete(hole: Hole)
}