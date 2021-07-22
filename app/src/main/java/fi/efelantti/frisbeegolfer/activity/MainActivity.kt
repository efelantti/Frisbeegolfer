package fi.efelantti.frisbeegolfer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import fi.efelantti.frisbeegolfer.FrisbeegolferRoomDatabase
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.ActivityMainWithNavigationBinding
import zip
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainWithNavigationBinding
    private lateinit var requestCreateDocumentLauncher: ActivityResultLauncher<Intent>

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        return when (item.itemId) {
            R.id.action_backup -> {
                startExportProcess()
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
            if (destination.id == R.id.fragmentChooseCourse) {
                binding.bottomNav.visibility = View.GONE
            } else if (destination.id == R.id.fragmentChoosePlayers) {
                binding.bottomNav.visibility = View.GONE
            } else if (destination.id == R.id.fragmentGame) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }

        requestCreateDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                onActivityResult(result)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_content)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun startExportProcess() {
        val currentDate = SimpleDateFormat("MMddyy", Locale.getDefault())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"
        intent.putExtra(
            Intent.EXTRA_TITLE,
            "backup_${currentDate.format(Date())}_" + FrisbeegolferRoomDatabase.databaseName + ".zip"
        )
        requestCreateDocumentLauncher.launch(intent)
    }

    private fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val userChosenUri: Uri? = intent.data
                if (userChosenUri != null) {
                    val dbPath = getDatabasePath(FrisbeegolferRoomDatabase.databaseName)
                    val shmPath = getDatabasePath(FrisbeegolferRoomDatabase.databaseName + "-shm")
                    val walPath = getDatabasePath(FrisbeegolferRoomDatabase.databaseName + "-wal")
                    val dbFiles = listOf(dbPath, shmPath, walPath)
                    zip(this, userChosenUri, dbFiles)
                }
            }
        }
    }

    private fun exportDatabaseFile(context: Context, toFile: String) {

        try {
            Log.i("Export location", toFile)
            val dbPath = context.getDatabasePath(FrisbeegolferRoomDatabase.databaseName).path
            val shmPath =
                context.getDatabasePath(FrisbeegolferRoomDatabase.databaseName + "-shm").path
            val walPath =
                context.getDatabasePath(FrisbeegolferRoomDatabase.databaseName + "-wal").path
            copyDataFromOneToAnother(
                dbPath,
                toFile
            )
            copyDataFromOneToAnother(
                shmPath,
                "$toFile-shm"
            )
            copyDataFromOneToAnother(
                walPath,
                "$toFile-wal"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
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