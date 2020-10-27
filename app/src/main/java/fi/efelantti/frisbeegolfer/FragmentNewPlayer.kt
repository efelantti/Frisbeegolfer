package fi.efelantti.frisbeegolfer

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment


class FragmentNewPlayer : DialogFragment() {

    private lateinit var firstNameView: EditText
    private lateinit var nickNameView: EditText
    private lateinit var lastNameView: EditText
    private lateinit var emailView: EditText
    private lateinit var playerData: Player

    interface FragmentNewPlayerListener {
        fun onPlayerAdded(
            player: Player,
            result: Int
        )

        fun onPlayerEdited(
            player: Player,
            result: Int
        )
    }
    companion object {
        fun newInstance(action: String, player: Player): FragmentNewPlayer {
            val frag = FragmentNewPlayer()
            val args = Bundle()
            args.putParcelable("playerData", player)
            args.putString("action", action)
            frag.setArguments(args)
            return frag
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_new_player, container)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.dialog_toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.inflateMenu(R.menu.appbar_dialog)

        val actionCategory = arguments!!.getString("action")?.let { NewPlayerAction.valueOf(it) }

        firstNameView = view.findViewById(R.id.edit_first_name)
        nickNameView = view.findViewById(R.id.edit_nickname)
        lastNameView = view.findViewById(R.id.edit_last_name)
        emailView = view.findViewById(R.id.edit_email)

        val oldPlayerData = arguments!!.getParcelable<Player>("playerData")

        if (actionCategory == NewPlayerAction.ADD)
        {
            toolbar.setTitle(getString(R.string.text_activity_new_player_title_add))
        }
        else if (actionCategory == NewPlayerAction.EDIT)
        {
            toolbar.setTitle(getString(R.string.text_activity_new_player_title_edit))
            firstNameView.setText(oldPlayerData?.firstName)
            nickNameView.setText(oldPlayerData?.nickName)
            lastNameView.setText(oldPlayerData?.lastName)
            emailView.setText(oldPlayerData?.email)
        }

        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {
                    val requiredFields: List<EditText> = listOf(firstNameView, lastNameView)
                    if (areValidFields(requiredFields) and isValidEmail(emailView)) {

                        val firstName = firstNameView.text.toString().trim()
                        val nickName = nickNameView.text.toString().trim()
                        val lastName = lastNameView.text.toString().trim()
                        val email = emailView.text.toString().trim()

                        playerData = Player(
                            firstName = firstName,
                            nickName = nickName,
                            lastName = lastName,
                            email = email
                        )

                        if (actionCategory == NewPlayerAction.EDIT) {
                            if(oldPlayerData == null) throw IllegalArgumentException("Cannot edit player data - it was null.")
                            else playerData.id = oldPlayerData.id // Take id from old player in order to update it to database.
                            if(Player.equals(playerData, oldPlayerData)){
                                Toast.makeText(context, getString(R.string.player_data_not_edited), Toast.LENGTH_LONG).show()
                            }
                            else{
                                AlertDialog.Builder(context)
                                    .setTitle("Overwrite")
                                    .setMessage("Are you sure you want to overwrite existing data? Please note that previous data can not be recovered.") // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(R.string.button_yes,
                                        DialogInterface.OnClickListener { dialog, which ->
                                            sendBackResult(Activity.RESULT_OK, actionCategory)
                                        }) // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(R.string.button_no, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show()
                            }
                        }
                        else {
                            sendBackResult(Activity.RESULT_OK, actionCategory)
                        }
                    }
                    else
                    {
                        Toast.makeText(context, getString(R.string.error_message_requiredFields), Toast.LENGTH_SHORT).show()
                    }
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        toolbar.setNavigationOnClickListener{
            AlertDialog.Builder(context)
                .setTitle("Cancel")
                .setMessage("Are you sure you want to cancel? Any unsaved data will be lost.") // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.button_yes,
                    DialogInterface.OnClickListener { dialog, which ->
                        playerData = Player()
                        sendBackResult(Activity.RESULT_CANCELED, actionCategory)
                    }) // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.button_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    // Call this method to send the data back to the parent fragment
    fun sendBackResult(result: Int, category: NewPlayerAction?) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        val listener: FragmentNewPlayerListener = activity as FragmentNewPlayerListener
        when(category)
        {
            NewPlayerAction.ADD -> listener.onPlayerAdded(playerData, result)
            NewPlayerAction.EDIT -> listener.onPlayerEdited(playerData, result)
        }
        dismiss()
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