package fi.efelantti.frisbeegolfer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import fi.efelantti.frisbeegolfer.FrisbeegolferRoomDatabase
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.model.Course
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Hole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application){
    private val repository: Repository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allCourses: LiveData<List<CourseWithHoles>>

    init {
        val database = FrisbeegolferRoomDatabase.getDatabase(
            application,
            viewModelScope
        )
        repository = Repository(database)
        allCourses = repository.allCourses
    }

    fun delete(hole: Hole) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(hole)
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