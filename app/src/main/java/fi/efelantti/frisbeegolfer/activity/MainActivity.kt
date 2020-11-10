package fi.efelantti.frisbeegolfer.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import fi.efelantti.frisbeegolfer.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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