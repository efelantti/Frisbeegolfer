package fi.efelantti.frisbeegolfer

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

// TODO - Transparent background?
class NewPlayerActivity : AppCompatActivity() {

    private val frisbeegolferViewModel: PlayerViewModel by viewModels()

    private lateinit var firstNameView: EditText
    private lateinit var nickNameView: EditText
    private lateinit var lastNameView: EditText
    private lateinit var emailView: EditText

    // TODO - Add a revert button for the fields
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_player)
        val intentCategory = intent.getStringExtra("action")?.let { NewPlayerAction.valueOf(it) }

        firstNameView = findViewById(R.id.edit_first_name)
        nickNameView = findViewById(R.id.edit_nickname)
        lastNameView = findViewById(R.id.edit_last_name)
        emailView = findViewById(R.id.edit_email)

        val oldPlayerData = intent.getParcelableExtra<Player>("playerData")

        if (intentCategory == NewPlayerAction.ADD)
        {
            getSupportActionBar()?.setTitle(getString(R.string.text_activity_new_player_title_add))
        }
        else if (intentCategory == NewPlayerAction.EDIT)
        {
            getSupportActionBar()?.setTitle(getString(R.string.text_activity_new_player_title_edit))
            firstNameView.setText(oldPlayerData?.firstName)
            nickNameView.setText(oldPlayerData?.nickName)
            lastNameView.setText(oldPlayerData?.lastName)
            emailView.setText(oldPlayerData?.email)
        }

        val replyIntent = Intent()
        val saveButton = findViewById<Button>(R.id.activity_new_player_button_save)
        val cancelButton = findViewById<Button>(R.id.activity_new_player_button_cancel)

        saveButton.setOnClickListener {
            val requiredFields: List<EditText> = listOf(firstNameView, lastNameView)
            val replyIntent = Intent()
            if (areValidFields(requiredFields) and isValidEmail(emailView)) {

                val firstName = firstNameView.text.toString().trim()
                val nickName = nickNameView.text.toString().trim()
                val lastName = lastNameView.text.toString().trim()
                val email = emailView.text.toString().trim()

                val newPlayerData = Player(
                    firstName = firstName,
                    nickName = nickName,
                    lastName = lastName,
                    email = email
                )

                if (intentCategory == NewPlayerAction.EDIT) {
                    if(oldPlayerData == null) throw IllegalArgumentException("Cannot edit player data - it was null.")
                    else newPlayerData.id = oldPlayerData.id // Take id from old player in order to update it to database.
                    if(Player.equals(newPlayerData, oldPlayerData)){
                        Toast.makeText(this, getString(R.string.player_data_not_edited), Toast.LENGTH_LONG).show()
                    }
                    else{
                        AlertDialog.Builder(this)
                            .setTitle("Overwrite")
                            .setMessage("Are you sure you want to overwrite existing data? Please note that previous data can not be recovered.") // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(R.string.button_yes,
                                DialogInterface.OnClickListener { dialog, which ->
                                    replyIntent.putExtra("playerData", newPlayerData)
                                    setResult(Activity.RESULT_OK, replyIntent)
                                    finish()
                                }) // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(R.string.button_no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    }
                }
                else {
                    replyIntent.putExtra("playerData", newPlayerData)
                    setResult(Activity.RESULT_OK, replyIntent)
                    finish()
                }
            }
            else
            {
                Toast.makeText(this, getString(R.string.error_message_requiredFields), Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cancel")
                .setMessage("Are you sure you want to cancel? Any unsaved data will be lost.") // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.button_yes,
                    DialogInterface.OnClickListener { dialog, which ->
                        setResult(Activity.RESULT_CANCELED, replyIntent)
                        finish()
                    }) // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.button_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    fun isValidEmail(target: EditText): Boolean
    {
        var isValid = false
        if (target.text.isNullOrEmpty() || Patterns.EMAIL_ADDRESS.matcher(target.text).matches()) isValid = true
        if (!isValid)
        {
            target.setError(getString(R.string.invalid_field,target.hint))
            return false
        }
        else return true
    }

    private fun areValidFields(fields: List<EditText>): Boolean
    {
        var allFieldsValid: Boolean = true
        for (field: EditText in fields)
        {
            if(TextUtils.isEmpty(field.text.trim()))
            {
                field.setError(getString(R.string.invalid_field,field.hint))
                allFieldsValid = false
            }
        }
        return allFieldsValid
    }
}