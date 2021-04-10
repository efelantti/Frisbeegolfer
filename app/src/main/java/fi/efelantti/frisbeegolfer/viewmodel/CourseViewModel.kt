package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.*
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: IRepository) : ViewModel() {

    val allCourses: LiveData<List<CourseWithHoles>> = repository.allCourses

    fun delete(hole: Hole) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(hole)
    }

    fun getCourseWithHolesById(id: Long): LiveData<CourseWithHoles> {
        val result = MutableLiveData<CourseWithHoles>()
        viewModelScope.launch(Dispatchers.IO) {
            val courseWithHoles = repository.getCourseWithHolesById(id)
            result.postValue(courseWithHoles)
        }
        return result
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(course: CourseWithHoles) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertCourseWithHoles(course)
    }

    /**
     * Launching a new coroutine to update the data in a non-blocking way
     */
    fun update(course: CourseWithHoles) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(course)
    }
}

@Suppress("UNCHECKED_CAST")
class CourseViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (CourseViewModel(repository) as T)
}