package fi.efelantti.frisbeegolfer.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.FragmentNavigationScreen

class MainActivity : AppCompatActivity(),
    FragmentNavigationScreen.FragmentNavigationScreenListener {

    private val navigationScreenTag = "FragmentNavigationScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_refactored)
        if (savedInstanceState == null) {
            displayNavigationScreenFragment()
        }
    }

    private fun displayNavigationScreenFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(
                R.id.fragment_container_view,
                FragmentNavigationScreen(),
                navigationScreenTag
            )
        }
    }

    override fun navigateToNewRound() {
        startActivity(Intent(this, ActivityRound::class.java))
    }

    override fun navigateToContinueRound() {
        startActivity(Intent(this, ActivityContinueRound::class.java))
    }

    override fun navigateToCourses() {
        startActivity(Intent(this, ActivityCourses::class.java))
    }

    override fun navigatePlayers() {
        startActivity(Intent(this, ActivityPlayers::class.java))
    }
}