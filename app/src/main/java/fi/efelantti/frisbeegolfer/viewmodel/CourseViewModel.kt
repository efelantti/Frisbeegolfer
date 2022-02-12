package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.*
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.LiveDataState
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.getViewModelScope
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CourseViewModel(
    coroutineScopeProvider: CoroutineScope? = null,
    private val repository: IRepository
) : ViewModel() {

    private val coroutineScope = getViewModelScope(coroutineScopeProvider)

    private var _state: MutableLiveData<LiveDataState> =
        MutableLiveData<LiveDataState>(LiveDataState.LOADING)
    var state: LiveData<LiveDataState> = _state

    fun allCourses(): LiveData<List<CourseWithHoles>> {
        _state.value = LiveDataState.LOADING
        return Transformations.map(repository.allCourses) {
            _state.value = LiveDataState.SUCCESS
            return@map it
        }
    }

    fun delete(hole: Hole) = coroutineScope.launch {
        repository.delete(hole)
    }

    fun getCourseWithHolesById(id: Long): LiveData<CourseWithHoles> {
        return repository.getCourseWithHolesById(id)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(course: CourseWithHoles) = coroutineScope.launch {
        repository.insertCourseWithHoles(course)
    }

    /**
     * Launching a new coroutine to update the data in a non-blocking way
     */
    fun update(course: CourseWithHoles) = coroutineScope.launch {
        repository.update(course)
    }

    fun courseExists(name: String, city: String): LiveData<Boolean> {
        return repository.courseExists(name, city)
    }

    fun delete(course: CourseWithHoles) = coroutineScope.launch {
        repository.delete(course)
    }
}

@Suppress("UNCHECKED_CAST")
class CourseViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (CourseViewModel(null, repository) as T)
}