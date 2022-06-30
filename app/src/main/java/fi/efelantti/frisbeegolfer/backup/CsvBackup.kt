package fi.efelantti.frisbeegolfer.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.RoomDatabase
import fi.efelantti.frisbeegolfer.csv.writeCsvFile
import fi.efelantti.frisbeegolfer.model.*
import fi.efelantti.frisbeegolfer.zip
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CsvBackup(
    var context: Context
) {

    companion object {
        private var TAG = "debug_CsvBackup"
        private lateinit var INTERNAL_BACKUP_PATH: File
        private lateinit var BACKUP_FILE: File
        private lateinit var DATABASE_FILE: File

        private var currentProcess: Int? = null
        private const val PROCESS_BACKUP = 1
        private const val PROCESS_RESTORE = 2
        private var backupFilename: String? = null
    }

    private lateinit var dbName: String

    private var roomDatabase: RoomDatabase? = null
    private var enableLogDebug: Boolean = false
    private var restartIntent: Intent? = null
    private var onCompleteListener: OnCompleteListener? = null
    private var customRestoreDialogTitle: String = "Choose file to restore"
    private var customBackupFileName: String? = null

    /**
     * Set RoomDatabase instance
     *
     * @param roomDatabase RoomDatabase
     */
    fun database(roomDatabase: RoomDatabase): CsvBackup {
        this.roomDatabase = roomDatabase
        return this
    }

    /**
     * Set LogDebug enabled / disabled
     *
     * @param enableLogDebug Boolean
     */
    fun enableLogDebug(enableLogDebug: Boolean): CsvBackup {
        this.enableLogDebug = enableLogDebug
        return this
    }

    /**
     * Set Intent in which to boot after App restart
     *
     * @param restartIntent Intent
     */
    fun restartApp(restartIntent: Intent): CsvBackup {
        this.restartIntent = restartIntent
        restartApp(restartIntent)
        return this
    }

    /**
     * Set onCompleteListener, to run code when tasks completed
     *
     * @param onCompleteListener OnCompleteListener
     */
    fun onCompleteListener(onCompleteListener: OnCompleteListener): CsvBackup {
        this.onCompleteListener = onCompleteListener
        return this
    }

    /**
     * Set onCompleteListener, to run code when tasks completed
     *
     * @param listener (success: Boolean, message: String) -> Unit
     */
    fun onCompleteListener(listener: (success: Boolean, message: String, exitCode: Int) -> Unit): CsvBackup {
        this.onCompleteListener = object : OnCompleteListener {
            override fun onComplete(success: Boolean, message: String, exitCode: Int) {
                listener(success, message, exitCode)
            }
        }
        return this
    }

    /**
     * Set custom log tag, for detailed debugging
     *
     * @param customLogTag String
     */
    fun customLogTag(customLogTag: String): CsvBackup {
        TAG = customLogTag
        return this
    }

    /**
     * Set custom Restore Dialog Title, default = "Choose file to restore"
     *
     * @param customRestoreDialogTitle String
     */
    fun customRestoreDialogTitle(customRestoreDialogTitle: String): CsvBackup {
        this.customRestoreDialogTitle = customRestoreDialogTitle
        return this
    }

    /**
     * Set custom Backup File Name, default = "$dbName-$currentTime.sqlite3"
     *
     * @param customBackupFileName String
     */
    fun customBackupFileName(customBackupFileName: String): CsvBackup {
        this.customBackupFileName = customBackupFileName
        return this
    }

    /**
     * Init vars, and return true if no error occurred
     */
    private fun initRoomBackup(): Boolean {
        if (roomDatabase == null) {
            if (enableLogDebug) Log.d(TAG, "roomDatabase is missing")
            onCompleteListener?.onComplete(
                false,
                "roomDatabase is missing",
                OnCompleteListener.EXIT_CODE_ERROR_ROOM_DATABASE_MISSING
            )
            //       throw IllegalArgumentException("roomDatabase is not initialized")
            return false
        }

        dbName = roomDatabase!!.openHelper.databaseName!!
        INTERNAL_BACKUP_PATH = File("${context.filesDir}/databasebackup/")
        BACKUP_FILE = File("$INTERNAL_BACKUP_PATH/${dbName}_backup_${getTime()}_.zip")
        DATABASE_FILE = File(context.getDatabasePath(dbName).toURI())

        //Create temp backup directory if does not exist
        try {
            INTERNAL_BACKUP_PATH.mkdirs()
        } catch (e: FileAlreadyExistsException) {
        } catch (e: IOException) {
        }

        if (enableLogDebug) {
            Log.d(TAG, "DatabaseName: $dbName")
            Log.d(TAG, "Database Location: $DATABASE_FILE")
            Log.d(TAG, "INTERNAL_BACKUP_PATH: $INTERNAL_BACKUP_PATH")
        }
        return true
    }

    /**
     * restart App with custom Intent
     */
    private fun restartApp() {
        restartIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(restartIntent)
        if (context is Activity) {
            (context as Activity).finish()
        }
        Runtime.getRuntime().exit(0)
    }

    /**
     * @return current time formatted as String
     */
    private fun getTime(): String {

        val currentTime = Calendar.getInstance().time

        val sdf = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault())

        return sdf.format(currentTime)

    }

    /**
     * Start Backup process, and set onComplete Listener to success, if no error occurred, else onComplete Listener success is false
     * and error message is passed
     *
     * if custom storage ist selected, the [openBackupfileCreator] will be launched
     */
    fun backup(
        players: List<Player>, courses: List<Course>,
        holes: List<Hole>, rounds: List<Round>, scores: List<Score>
    ) {
        if (enableLogDebug) Log.d(TAG, "Starting Backup ...")
        val success = initRoomBackup()
        if (!success) return

        //Needed for storage permissions request
        currentProcess = PROCESS_BACKUP

        //Create name for backup file, if no custom name is set: Database name + currentTime + .sqlite3
        val filename =
            if (customBackupFileName == null) "$dbName-${getTime()}.zip" else customBackupFileName as String
        //Add .aes extension to filename, if file is encrypted

        Log.d(TAG, "backupFilename: $filename")

        val backupFile = File("$INTERNAL_BACKUP_PATH/$filename")
        doBackup(backupFile, players, courses, holes, rounds, scores)
    }

    /**
     * This method will do the backup action
     *
     * @param destination File
     */
    private fun doBackup(
        destination: File, players: List<Player>, courses: List<Course>,
        holes: List<Hole>, rounds: List<Round>, scores: List<Score>
    ) {

        // First create the .csv's.
        val playersPath = File("$INTERNAL_BACKUP_PATH/players.csv")
        val coursesPath = File("$INTERNAL_BACKUP_PATH/courses.csv")
        val holesPath = File("$INTERNAL_BACKUP_PATH/holes.csv")
        val roundsPath = File("$INTERNAL_BACKUP_PATH/rounds.csv")
        val scoresPath = File("$INTERNAL_BACKUP_PATH/holes.csv")

        writeCsvFile(players, playersPath.path)
        writeCsvFile(courses, coursesPath.path)
        writeCsvFile(holes, holesPath.path)
        writeCsvFile(rounds, roundsPath.path)
        writeCsvFile(scores, scoresPath.path)

        // Then create the .zip.
        val filesToZip = listOf<File>(playersPath, coursesPath, holesPath, roundsPath, scoresPath)
        zip(destination, filesToZip)

        if (enableLogDebug) Log.d(TAG, "Backup done and saved to $destination")
        onCompleteListener?.onComplete(true, "success", OnCompleteListener.EXIT_CODE_SUCCESS)
    }

    /*

    /**
     * Start Restore process, and set onComplete Listener to success, if no error occurred, else onComplete Listener success is false and error message is passed
     *
     * if internal or external storage is selected, this function shows a list of all available backup files in a MaterialAlertDialog and
     * calls [restoreSelectedInternalExternalFile] to restore selected file
     *
     * if custom storage ist selected, the [openBackupfileChooser] will be launched
     */
    fun restore() {
        if (enableLogDebug) Log.d(TAG, "Starting Restore ...")
        val success = initRoomBackup()
        if (!success) return

        //Needed for storage permissions request
        currentProcess = PROCESS_RESTORE

        //Path of Backup Directory
        val backupDirectory: File

        Log.d(TAG, "backupLocationCustomFile!!.exists()? : ${backupLocationCustomFile!!.exists()}")
        doRestore(backupLocationCustomFile!!)

        //All Files in an Array of type File
        val arrayOfFiles = backupDirectory.listFiles()

        //If array is null or empty show "error" and return
        if (arrayOfFiles.isNullOrEmpty()) {
            if (enableLogDebug) Log.d(TAG, "No backups available to restore")
            onCompleteListener?.onComplete(
                false,
                "No backups available",
                OnCompleteListener.EXIT_CODE_ERROR_RESTORE_NO_BACKUPS_AVAILABLE
            )
            Toast.makeText(context, "No backups available to restore", Toast.LENGTH_SHORT).show()
            return
        }

        //Sort Array: lastModified
        Arrays.sort(arrayOfFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR)

        //New empty MutableList of String
        val mutableListOfFilesAsString = mutableListOf<String>()

        //Add each filename to mutablelistOfFilesAsString
        runBlocking {
            for (i in arrayOfFiles.indices) {
                mutableListOfFilesAsString.add(arrayOfFiles[i].name)
            }
        }

        //Convert MutableList to Array
        val filesStringArray = mutableListOfFilesAsString.toTypedArray()

        //Show MaterialAlertDialog, with all available files, and on click Listener
        MaterialAlertDialogBuilder(context)
            .setTitle(customRestoreDialogTitle)
            .setItems(filesStringArray) { _, which ->
                restoreSelectedInternalExternalFile(filesStringArray[which])
            }
            .setOnCancelListener {
                if (enableLogDebug) Log.d(TAG, "Restore dialog canceled")
                onCompleteListener?.onComplete(
                    false,
                    "Restore dialog canceled",
                    OnCompleteListener.EXIT_CODE_ERROR_BY_USER_CANCELED
                )
            }
            .show()
    }

    /**
     * This method will do the restore action
     *
     * @param source File
     */
    private fun doRestore(source: File) {
        val fileExtension = source.extension
            if (fileExtension == "aes") {
                if (enableLogDebug) Log.d(
                    TAG,
                    "Cannot restore database, it is encrypted. Maybe you forgot to add the property .fileIsEncrypted(true)"
                )
                onCompleteListener?.onComplete(
                    false,
                    "cannot restore database, see Log for more details (if enabled)",
                    OnCompleteListener.EXIT_CODE_ERROR_RESTORE_BACKUP_IS_ENCRYPTED
                )
                return
            }
        //Copy back database and replace current database
        copy(source, DATABASE_FILE)

        if (enableLogDebug) Log.d(TAG, "Restore done, decrypted($backupIsEncrypted) and restored from $source")
        onCompleteListener?.onComplete(true, "success", OnCompleteListener.EXIT_CODE_SUCCESS)
    }

    /**
     * This method will do the restore action
     *
     * @param source InputStream
     */
    private fun doRestore(source: InputStream) {
            //Copy back database and replace current database
            source.use { input ->
                DATABASE_FILE.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        if (enableLogDebug) Log.d(TAG, "Restore done, decrypted($backupIsEncrypted) and restored from $source")
        onCompleteListener?.onComplete(true, "success", OnCompleteListener.EXIT_CODE_SUCCESS)
    }

    /*
    /**
     * Opens the [ActivityResultContracts.RequestMultiplePermissions] and prompts the user to grant storage permissions
     *
     * If granted backup or restore process starts
     */
    private val permissionRequestLauncher = (context as ComponentActivity).registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            if (!it.value) {
                onCompleteListener?.onComplete(
                    false,
                    "storage permissions are required, please allow!",
                    OnCompleteListener.EXIT_CODE_ERROR_STORAGE_PERMISSONS_NOT_GRANTED
                )
                return@registerForActivityResult
            }
        }
        when (currentProcess) {
            PROCESS_BACKUP -> {
                openBackupfileCreator.launch(backupFilename)

            }
            PROCESS_RESTORE -> {
                openBackupfileChooser.launch(arrayOf("*/*"))
            }
        }
    }

    /**
     * Opens the [ActivityResultContracts.OpenDocument] and prompts the user to open a document for restoring a backup file
     */
    private val openBackupfileChooser = (context as ComponentActivity).registerForActivityResult(ActivityResultContracts.OpenDocument()) { result ->
        if (result != null) {
            val inputstream = context.contentResolver.openInputStream(result)!!
            doRestore(inputstream)
            return@registerForActivityResult
        }
        onCompleteListener?.onComplete(
            false,
            "failure",
            OnCompleteListener.EXIT_CODE_ERROR_BACKUP_FILE_CHOOSER
        )


    }

    /**
     * Opens the [ActivityResultContracts.CreateDocument] and prompts the user to select a path for creating the new backup file
     */
    private val openBackupfileCreator = (context as ComponentActivity).registerForActivityResult(ActivityResultContracts.CreateDocument()) { result ->
        if (result != null) {
            val out = context.contentResolver.openOutputStream(result)!!
            doBackup(out)
            return@registerForActivityResult
        }
        onCompleteListener?.onComplete(
            false,
            "failure",
            OnCompleteListener.EXIT_CODE_ERROR_BACKUP_FILE_CREATOR
        )
    }
*/
}