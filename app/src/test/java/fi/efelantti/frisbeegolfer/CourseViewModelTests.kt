package fi.efelantti.frisbeegolfer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import fi.efelantti.frisbeegolfer.viewmodel.CourseViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.InvalidObjectException

@ExperimentalCoroutinesApi
class CourseViewModelTests {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private lateinit var repository: IRepository
    private lateinit var courseViewModel: CourseViewModel
    private val course = Course(name = "Test course", city = "Test city")
    private val holes = listOf(Hole(parentCourseId = course.courseId, par = 3))
    private val courseWithHoles = CourseWithHoles(course, holes)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        repository = mockk()
        every { repository.allCourses } returns MutableLiveData<List<CourseWithHoles>>(emptyList())
        courseViewModel = CourseViewModel(testScope, repository)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    internal fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun updateCourse() = testDispatcher.runBlockingTest {
        coEvery { repository.update(courseWithHoles) } returns Unit
        courseViewModel.update(courseWithHoles)
        coVerify(exactly = 1) { repository.update(courseWithHoles) }
    }

    @Test
    fun deleteHole() = testDispatcher.runBlockingTest {
        val hole = Hole()
        coEvery { repository.delete(hole) } returns Unit
        courseViewModel.delete(hole)
        coVerify(exactly = 1) { repository.delete(hole) }
    }

    @Test
    fun getCourseWithHolesById() = testDispatcher.runBlockingTest {
        coEvery { repository.getCourseWithHolesById(123) } returns courseWithHoles
        val resultLiveData = courseViewModel.getCourseWithHolesById(123)
        coVerify(exactly = 1) { repository.getCourseWithHolesById(123) }
        val result = resultLiveData.getValueBlocking() ?: throw InvalidObjectException("Null returned as CourseWithHoles.")
        assertThat(CourseWithHoles.equals(result, courseWithHoles), equalTo(true))
    }
}
