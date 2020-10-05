package fi.efelantti.frisbeegolfer

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class NewPlayerActivity : AppCompatActivity() {

    private lateinit var firstNameView: EditText
    private lateinit var nickNameView: EditText
    private lateinit var lastNameView: EditText
    private lateinit var emailView: EditText

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_player)
        firstNameView = findViewById(R.id.edit_first_name)
        nickNameView = findViewById(R.id.edit_nickname)
        lastNameView = findViewById(R.id.edit_last_name)
        emailView = findViewById(R.id.edit_email)

        //TODO - Add cancel button
        val button = findViewById<Button>(R.id.button_save)

        //TODO - Add validation for fields (do not allow to save duplicates). Only allow valid email addresses.
        button.setOnClickListener {
            val requiredFields: List<EditText> = listOf(firstNameView, lastNameView)
            val replyIntent = Intent()
            var allFieldsValid = true
            for (field: EditText in requiredFields)
            {
                if(TextUtils.isEmpty(field.text.trim()))
                {
                    field.setError(getString(R.string.invalid_field,field.hint))

                    allFieldsValid = false
                }
            }
            if (allFieldsValid) {
                val firstName = firstNameView.text.toString()
                val nickName= nickNameView.text.toString()
                val lastName= lastNameView.text.toString()
                val email = emailView.text.toString()
                replyIntent.putExtra("firstName", firstName)
                replyIntent.putExtra("nickName", nickName)
                replyIntent.putExtra("lastName", lastName)
                replyIntent.putExtra("email", email)

                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
            else
            {
                Toast.makeText(this, getString(R.string.error_message_requiredFields), Toast.LENGTH_SHORT).show()
            }
        }
    }
}