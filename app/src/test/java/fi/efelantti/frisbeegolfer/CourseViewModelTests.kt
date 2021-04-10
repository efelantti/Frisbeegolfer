package fi.efelantti.frisbeegolfer

class CourseViewModelTests {

    /* TODO - These don't work
    @Mock
    private lateinit var repository: IRepository
    private lateinit var courseViewModel: CourseViewModel
    private val course = Course(name = "Test course", city = "Test city")
    private val holes = listOf(Hole(parentCourseId = course.courseId, par = 3))
    private val courseWithHoles = CourseWithHoles(course, holes)

    @After
    internal fun tearDown() {
        clearAllMocks()
    }

    /*
        @Test
    fun getAllPlayersWhenEmpty() = runBlockingTest {
        `when`(fakePlayerDao.getPlayers())
            .thenReturn(MutableLiveData(emptyList()))
        repository = Repository(fakePlayerDao, fakeCourseDao, fakeRoundDao)
        val allPlayers = repository.allPlayers
        val result = allPlayers.getValueBlocking() ?: throw InvalidObjectException("null returned as players")
        assertThat(result.count(), equalTo(0))
    }
     */

    @Test
    fun updateCourse() {
        val repository: Repository = mockk()
        every { repository.allCourses } returns MutableLiveData<List<CourseWithHoles>>(emptyList())
        coEvery { repository.update(courseWithHoles) } returns Unit
        courseViewModel = CourseViewModel(repository)
        courseViewModel.update(courseWithHoles)
        coVerify(exactly = 1) { repository.update(courseWithHoles) }
    }

    @Test
    fun deleteHole() {
        val repository: Repository = mockk()
        val hole = Hole()
        every { repository.allCourses } returns MutableLiveData<List<CourseWithHoles>>(emptyList())
        coEvery { repository.delete(hole) } returns Unit
        courseViewModel = CourseViewModel(repository)
        courseViewModel.delete(hole)
        coVerify(exactly = 1) { repository.delete(hole) }
    }

    @Test
    fun getCourseWithHolesById() {
        val repository: Repository = mockk(relaxed = true)
        every { repository.allCourses } returns MutableLiveData<List<CourseWithHoles>>(emptyList())
        every { repository.getCourseWithHolesById(123) } returns courseWithHoles
        courseViewModel = CourseViewModel(repository)
        val resultLiveData = courseViewModel.getCourseWithHolesById(123)
        verify(exactly = 1) { repository.getCourseWithHolesById(123) }
        val result = resultLiveData.getValueBlocking() ?: throw InvalidObjectException("Null returned as CourseWithHoles.")
        assertThat(CourseWithHoles.equals(result, courseWithHoles), equalTo(true))
    }

     */
}
