package fi.efelantti.frisbeegolfer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fi.efelantti.frisbeegolfer.IRepository
import fi.efelantti.frisbeegolfer.Repository
import fi.efelantti.frisbeegolfer.getViewModelScope
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithCourseAndScores
import fi.efelantti.frisbeegolfer.model.Score
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class RoundViewModel(
    coroutineScopeProvider: CoroutineScope? = null,
    private val repository: IRepository
) : ViewModel() {

    private val coroutineScope = getViewModelScope(coroutineScopeProvider)
    val allRounds: LiveData<List<RoundWithCourseAndScores>> = repository.allRounds

    fun getRoundWithRoundId(roundId: OffsetDateTime): LiveData<RoundWithCourseAndScores> {
        return repository.getRoundWithRoundId(roundId)
    }

    fun delete(round: RoundWithCourseAndScores) = coroutineScope.launch {
        repository.delete(round)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * Round should be inserted without the scores.
     */
    fun insert(round: Round) = coroutineScope.launch {
        repository.insert(round)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     * Round should be inserted without the scores.
     */
    fun insert(score: Score) = coroutineScope.launch {
        repository.insert(score)
    }

    fun update(score: Score) = coroutineScope.launch {
        repository.update(score)
    }

    /**
     * Adds an entry to the database for the round. Creates all the necessary scores, that are
     * then later to be edited when playing the round.
     */
    fun addRoundToDatabase(
        selectedCourse: CourseWithHoles,
        selectedPlayerIds: List<Long>,
        roundId: OffsetDateTime
    ) = coroutineScope.launch {
        val round = Round(dateStarted = roundId, courseId = selectedCourse.course.courseId)
        repository.insert(round)
        for (hole in selectedCourse.holes) {
            for (playerId in selectedPlayerIds) {
                val score = Score(
                    parentRoundId = roundId,
                    holeId = hole.holeId,
                    playerId = playerId,
                    result = null
                )
                repository.insert(score)
            }
        }
    }

    fun checkPoint() {
        repository.checkpoint()
    }
}


@Suppress("UNCHECKED_CAST")
class RoundViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (RoundViewModel(null, repository) as T)
}