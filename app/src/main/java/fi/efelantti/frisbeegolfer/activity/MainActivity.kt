package fi.efelantti.frisbeegolfer.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.model.Round
import fi.efelantti.frisbeegolfer.model.RoundWithScores
import fi.efelantti.frisbeegolfer.model.Score
import fi.efelantti.frisbeegolfer.model.ScoreWithPlayerAndHole
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import java.time.OffsetDateTime.now

class MainActivity : AppCompatActivity() {

    private val roundViewModel: RoundViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startNewRound(view: View) {
        var round = Round()
        var scores = emptyList<ScoreWithPlayerAndHole>()
        round.dateStarted = now()
        var roundWithScores: RoundWithScores = RoundWithScores(round, scores)
        roundViewModel.insert(roundWithScores)
    }

    fun navigateToPlayers(view: View) {
        val intent = Intent(this, ActivityPlayers::class.java)
        startActivity(intent)
    }

    fun navigateToCourses(view: View) {
        val intent = Intent(this, ActivityCourses::class.java)
        startActivity(intent)
    }

}