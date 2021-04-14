package fi.efelantti.frisbeegolfer.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.fragment.FragmentCourses
import fi.efelantti.frisbeegolfer.fragment.FragmentNavigationScreen
import fi.efelantti.frisbeegolfer.fragment.FragmentPlayers

class MainActivity : AppCompatActivity(),
    FragmentNavigationScreen.FragmentNavigationScreenListener {

    private val navigationScreenTag = "FragmentNavigationScreen"
    private val coursesTag = "FragmentCourses"
    private val playersTag = "FragmentPlayers"

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
        //supportActionBar?.title = getString(R.string.courses_activity_title)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, FragmentCourses(), coursesTag)
        }
    }

    override fun navigatePlayers() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, FragmentPlayers(), playersTag)
        }
    }
}