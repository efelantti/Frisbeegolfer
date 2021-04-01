package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.dao.CourseDao
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole

class FakeCourseDao: CourseDao {

    private var courses: MutableLiveData<List<CourseWithHoles>> = MutableLiveData<List<CourseWithHoles>>()

    override suspend fun insertAll(holes: List<Hole>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertAllAndGetIds(holes: List<Hole>): List<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun updateAll(holes: List<Hole>) {
        TODO("Not yet implemented")
    }

    override suspend fun insert(course: Course): Long {
        TODO("Not yet implemented")
    }

    override suspend fun delete(course: Course) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(hole: Hole) {
        TODO("Not yet implemented")
    }

    override fun update(course: Course) {
        TODO("Not yet implemented")
    }

    override fun getCoursesWithHoles(): LiveData<List<CourseWithHoles>> {
        return courses
    }

    override fun getCourseWithHolesWithId(id: Long): CourseWithHoles {
        TODO("Not yet implemented")
    }

    override fun getHoles(): LiveData<List<Hole>> {
        TODO("Not yet implemented")
    }
}