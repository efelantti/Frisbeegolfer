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
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import fi.efelantti.frisbeegolfer.*
import fi.efelantti.frisbeegolfer.databinding.ActivityMainWithNavigationBinding
import fi.efelantti.frisbeegolfer.fragment.DialogConfirmImportFromDiscscores
import java.io.*

// TODO - Toasts to import/export success and failures.
// TODO - See how Backup is implemented and do similar for Discscores import...
// TODO - Change primary key in Round to long instead of start date.
// TODO - Change ProgressBars to SkeletonUI.
// TODO - Wrap adapter/fragment logic in base class.
// TODO - Restart application after importing Discscores data in order to refresh the app.
// TODO - Create tests where missing - especially UI tests.
// TODO - Add statistics (to relevant fragments + a separate fragment).
// TODO - "Paste this link on the website where your app is available for download or in the description section of the platform or marketplace you’re using." - <a href="https://www.flaticon.com/free-icons/disc-golf" title="disc golf icons">Disc golf icons created by Freepik - Flaticon</a>
class MainActivity : BaseActivity(),
    DialogConfirmImportFromDiscscores.OnConfirmationSelectedImportDiscscores {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainWithNavigationBinding
    private lateinit var getDiscscoresFileLauncher: ActivityResultLauncher<Intent>
    private val downloadedDiscscoresFilesToImportFolderName =
        "downloaded_discscores_files_to_import"
    private lateinit var backup: RoomBackup

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        return when (item.itemId) {
            R.id.action_export_data -> {
                backup
                    .database(ServiceLocator.provideDatabase(this))
                    .enableLogDebug(true)
                    .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                    .apply {
                        onCompleteListener { success, message, exitCode ->
                            Log.d(
                                "RoomBackup",
                                "success: $success, message: $message, exitCode: $exitCode"
                            )
                            if (success) restartApp(
                                Intent(
                                    this@MainActivity,
                                    MainActivity::class.java
                                )
                            )
                        }
                    }
                    .backup()

                // JSON export
                /*val repo = (applicationContext as FrisbeegolferApplication).repository
                repo.allData.observeOnce { triple ->

                    val players = triple.first
                    val courses = triple.second
                    val rounds = triple.third

                    val moshi: Moshi = Moshi.Builder().build()
                    val type = Types.newParameterizedType(List::class.java, Player::class.java)
                    val jsonAdapter = moshi.adapter<List<Player>>(type)

                    val playersJson = jsonAdapter.toJson(players)

                    Log.i("All data", "Players: " + players.count().toString())
                    Log.i("All data", "Courses: " + courses.count().toString())
                    Log.i("All data", "Rounds: " + rounds.count().toString())
                }
                */
                true
            }
            R.id.action_import_data -> {
                backup
                    .database(ServiceLocator.provideDatabase(this))
                    .customRestoreDialogTitle(getString(R.string.import_confirmation_title))
                    .enableLogDebug(true)
                    .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
                    .apply {
                        onCompleteListener { success, message, exitCode ->
                            Log.d(
                                "RoomRestore",
                                "success: $success, message: $message, exitCode: $exitCode"
                            )
                            if (success) restartApp(
                                Intent(
                                    this@MainActivity,
                                    MainActivity::class.java
                                )
                            )
                        }
                    }
                    .restore()
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

        backup = RoomBackup(this)

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
                            val fileType = uri.getMimeType(this)
                            Log.i("IMPORT Discscores", "File type: ${fileType}.")
                            if (fileType != "application/zip" && fileType != "application/x-zip-compressed") {
                                ToastUtils.showErrorToast(
                                    this,
                                    resources.getText(R.string.error_wrong_file_type)
                                )
                            } else {
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
        val fileToReturn = File(downloadedDiscscoresDir, "discscores.zip")
        if (fileToReturn.exists()) fileToReturn.delete()
        return fileToReturn
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

    /** TODO - Verify after getting the file that it is a zip.
    Start the importing process by asking user the database file they want to import.
     */
    private fun importZippedDiscscoresFile() {
        val openIntent: Intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, resources.getText(R.string.import_discscores))
            type = "*/*"
        }

        getDiscscoresFileLauncher.launch(
            Intent.createChooser(
                openIntent,
                resources.getText(R.string.import_discscores)
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
            restartApp()
        }
    }

    override fun returnUserConfirmationToImportDiscscores() {
        importZippedDiscscoresFile()
    }
}