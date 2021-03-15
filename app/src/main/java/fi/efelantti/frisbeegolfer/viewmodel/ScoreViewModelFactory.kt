package fi.efelantti.frisbeegolfer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.time.OffsetDateTime

class ScoreViewModelFactory(
    private val application: Application,
    private val roundId: OffsetDateTime
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        ScoreViewModel(application, roundId) as T
}