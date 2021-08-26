package fi.efelantti.frisbeegolfer.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.FrisbeegolferRoomDatabase
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.ActivityMainWithNavigationBinding
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainWithNavigationBinding
    private val roundViewModel: RoundViewModel by viewModels {
        RoundViewModelFactory((applicationContext as FrisbeegolferApplication).repository)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        return when (item.itemId) {
            R.id.action_export_data -> {
                exportDatabaseAsZip()
                true
            }
            R.id.action_import_data -> {
                importZippedDatabase()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainWithNavigationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragmentChooseRound,
                R.id.fragmentCourses,
                R.id.fragmentPlayers
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentChooseCourse -> {
                    binding.bottomNav.visibility = View.GONE
                }
                R.id.fragmentChoosePlayers -> {
                    binding.bottomNav.visibility = View.GONE
                }
                R.id.fragmentGame -> {
                    binding.bottomNav.visibility = View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_content)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun exportDatabaseAsZip() {
        roundViewModel.checkPoint()
        val zippedDatabase =
            (applicationContext as FrisbeegolferApplication).database.createDatabaseZip(this)
        val contentUri: Uri = getUriForFile(
            this,
            "fi.efelantti.frisbeegolfer.fileprovider",
            zippedDatabase
        )

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TITLE, R.string.export_database_share)
            type = "application/zip"
        }
        shareIntent.data = contentUri
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(
            Intent.createChooser(
                shareIntent,
                resources.getText(R.string.export_database_share)
            )
        )
    }

    private fun importZippedDatabase() {

    }

    fun importDatabaseFile(context: Context) {
        try {
            copyDataFromOneToAnother(
                getExternalFilesDir(null)?.path + "/Download/" + "backup_" + FrisbeegolferRoomDatabase.databaseName,
                context.getDatabasePath(
                    FrisbeegolferRoomDatabase.databaseName
                ).path
            )
            copyDataFromOneToAnother(
                getExternalFilesDir(null)?.path + "/Download/" + "backup_" + FrisbeegolferRoomDatabase.databaseName + "-shm",
                context.getDatabasePath(
                    FrisbeegolferRoomDatabase.databaseName + "-shm"
                ).path
            )
            copyDataFromOneToAnother(
                getExternalFilesDir(null)?.path + "/Download/" + "backup_" + FrisbeegolferRoomDatabase.databaseName + "-wal",
                context.getDatabasePath(
                    FrisbeegolferRoomDatabase.databaseName + "-wal"
                ).path
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun copyDataFromOneToAnother(fromPath: String, toPath: String) {
        val inStream = File(fromPath).inputStream()
        val outStream = FileOutputStream(toPath)

        inStream.use { input ->
            outStream.use { output ->
                input.copyTo(output)
            }
        }
    }
}