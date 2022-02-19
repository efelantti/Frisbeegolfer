package fi.efelantti.frisbeegolfer.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider.getUriForFile
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.ToastUtils
import fi.efelantti.frisbeegolfer.databinding.ActivityMainWithNavigationBinding
import fi.efelantti.frisbeegolfer.fragment.DialogConfirmImport
import fi.efelantti.frisbeegolfer.fragment.DialogConfirmImportFromDiscscores
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModelFactory
import java.io.*

// TODO - Change ProgressBars to SkeletonUI.
// TODO - Wrap adapter/fragment logic in base class.
// TODO - Restart application after importing Discscores data in order to refresh the app.
// TODO - Create tests where missing - especially UI tests.
// TODO - Add statistics (to relevant fragments + a separate fragment).
// TODO - "Paste this link on the website where your app is available for download or in the description section of the platform or marketplace you’re using." - <a href="https://www.flaticon.com/free-icons/disc-golf" title="disc golf icons">Disc golf icons created by Freepik - Flaticon</a>
class MainActivity : BaseActivity(), DialogConfirmImport.OnConfirmationSelected,
    DialogConfirmImportFromDiscscores.OnConfirmationSelectedImportDiscscores {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainWithNavigationBinding
    private val roundViewModel: RoundViewModel by viewModels {
        RoundViewModelFactory((applicationContext as FrisbeegolferApplication).repository)
    }
    private lateinit var getZipFileLauncher: ActivityResultLauncher<Intent>
    private lateinit var getDiscscoresFileLauncher: ActivityResultLauncher<Intent>
    private val downloadedDatabaseFilesToImportFolderName = "downloaded_database_files_to_import"
    private val downloadedDiscscoresFilesToImportFolderName =
        "downloaded_discscores_files_to_import"

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
                DialogConfirmImport(this).show(
                    supportFragmentManager, DialogConfirmImport.TAG
                )
                true
            }
            R.id.action_import_data_from_discscores -> {
                DialogConfirmImportFromDiscscores(this).show(
                    supportFragmentManager, DialogConfirmImportFromDiscscores.TAG
                )
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

        getZipFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data == null) Toast.makeText(
                        this,
                        R.string.no_import_file_received,
                        Toast.LENGTH_SHORT
                    ).show()
                    data?.data.also { uri ->
                        if (uri == null) Toast.makeText(
                            this,
                            R.string.no_import_file_received,
                            Toast.LENGTH_SHORT
                        ).show()
                        else {
                            val dbDownloadFile = getDbDownloadFile()
                            Log.i(
                                "IMPORT DB",
                                "Starting to download database to import from ${uri.path} to ${dbDownloadFile.path}."
                            )
                            downloadFile(uri, dbDownloadFile)
                            Log.i("IMPORT DB", "Database downloaded.")
                            importZippedDatabase(dbDownloadFile)
                        }
                    }
                }
            }

        getDiscscoresFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data == null) Toast.makeText(
                        this,
                        R.string.no_import_file_received,
                        Toast.LENGTH_SHORT
                    ).show()
                    data?.data.also { uri ->
                        if (uri == null) Toast.makeText(
                            this,
                            R.string.no_import_file_received,
                            Toast.LENGTH_SHORT
                        ).show()
                        else {
                            val discscoresFile = getDiscscoresDownloadFile()
                            Log.i(
                                "IMPORT Discscores",
                                "Starting to download discscores file to import from ${uri.path} to ${discscoresFile.path}."
                            )
                            downloadFile(uri, discscoresFile)
                            Log.i("IMPORT Discscores", "Discscores file downloaded.")
                            importZippedDiscscoresFile(discscoresFile)
                        }
                    }
                }
            }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
        navController = navHostFragment.navController
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

    /**
     * Function for getting the folder where the imported discscores files are saved before importing. Note
     * that this is File is reused for each imported discscores file.
     * @return A file object where the discscores file to import should be saved.
     */
    private fun getDiscscoresDownloadFile(): File {
        val downloadedDiscscoresDir = File(filesDir, downloadedDiscscoresFilesToImportFolderName)
        if (!downloadedDiscscoresDir.exists()) {
            downloadedDiscscoresDir.mkdir()
        }
        return File(downloadedDiscscoresDir, "discscores.zip")
    }

    /**
     * Function for getting the folder where the imported database is saved before importing. Note
     * that this is File is reused for each imported database.
     * @return A file object where the imported database should be saved.
     */
    private fun getDbDownloadFile(): File {
        val downloadedDbDir = File(filesDir, downloadedDatabaseFilesToImportFolderName)
        if (!downloadedDbDir.exists()) {
            downloadedDbDir.mkdir()
        }
        return File(downloadedDbDir, "database_to_import.zip")
    }

    /**
     * Downloads a file from fromUri to toFile.
     *
     * @param fromUri Uri from where the data should be saved
     * @param toFile File where the data should be saved to
     * @throws IOException In case can't read from [fromUri]
     */
    private fun downloadFile(fromUri: Uri, toFile: File) {
        val inStream: InputStream =
            contentResolver.openInputStream(fromUri)
                ?: throw IOException("Couldn't read from ${fromUri.path}.")
        val outStream: OutputStream = FileOutputStream(toFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inStream.read(buf).also {
                len = it
            } > 0) {
            outStream.write(buf, 0, len)
        }
        outStream.close()
        inStream.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_content)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /**
    Function for exporting the database as a .zip package, which the user can save wherever
    they choose with the ACTION_SEND intent.
     */
    private fun exportDatabaseAsZip() {
        try {
            val db = (applicationContext as FrisbeegolferApplication).database
            roundViewModel.checkPoint()
            db.close()
            val zippedDatabase =
                db.createDatabaseZip(this)
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
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                resources.getText(R.string.error_exporting_database),
                Toast.LENGTH_SHORT
            ).show()
            Log.e("EXPORT DB", "Error while exporting database: ${exception.message}")
        }
    }

    /**
    Start the importing process by asking user the database file they want to import.
     */
    private fun importZippedDiscscoresFile() {
        val openIntent: Intent = Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, resources.getText(R.string.import_discscores))
            type = "application/zip"
        }

        getDiscscoresFileLauncher.launch(
            Intent.createChooser(
                openIntent,
                resources.getText(R.string.import_discscores)
            )
        )
    }

    /**
    Start the importing process by asking user the database file they want to import.
     */
    private fun importZippedDatabase() {
        val openIntent: Intent = Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, resources.getText(R.string.import_database))
            type = "application/zip"
        }

        getZipFileLauncher.launch(
            Intent.createChooser(
                openIntent,
                resources.getText(R.string.import_database)
            )
        )
    }

    /**
    Function for restarting app.

    This is required after importing database, because the import is
    performed by copying another database FILE in place of current database file. Restarting app
    forces Room to reload the database from file.
     */
    private fun restartApp() {
        val refresh = Intent(applicationContext, MainActivity::class.java)
        refresh.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(refresh)
        Runtime.getRuntime().exit(0)
    }

    /**
     *Invoke import function and restart app.
     *@param zippedDatabase The database that should be imported. Expected a .zip file.
     */
    private fun importZippedDatabase(zippedDatabase: File) {
        Log.i("IMPORT DB", "Starting to import database.")
        val db = (applicationContext as FrisbeegolferApplication).database
        try {
            db.importDatabaseZip(
                this,
                zippedDatabase
            )
            Log.i("IMPORT DB", "Database imported -> restarting app.")
            restartApp()
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                resources.getText(R.string.error_importing_database),
                Toast.LENGTH_SHORT
            ).show()
            Log.e("IMPORT DB", "Error while importing database: ${exception.message}")
            db.restoreFromEmergencyBackup(this)
            restartApp()
        }
    }

    /**
     *Invoke import function and restart app.
     *@param zippedDiscscoresFile The Discscores file that should be imported. Expected a .zip file.
     */
    private fun importZippedDiscscoresFile(zippedDiscscoresFile: File) {
        Log.i("IMPORT Discscores", "Starting to import discscores file.")
        val db = (applicationContext as FrisbeegolferApplication).database
        try {
            db.importDiscscoresZip(
                this,
                zippedDiscscoresFile
            )
            Log.i("IMPORT Discscores", "Discscores data read -> starting to import.")

        } catch (exception: Exception) {
            Log.e("IMPORT Discscores", "Error while importing discscores: ${exception.message}")
            ToastUtils.showErrorToast(this, resources.getText(R.string.error_importing_discscores))
            db.restoreFromEmergencyBackup(this)
            restartApp()
        }
    }

    override fun returnUserConfirmationToImportDiscscores() {
        importZippedDiscscoresFile()
    }

    override fun returnUserConfirmation() {
        importZippedDatabase()
    }
}