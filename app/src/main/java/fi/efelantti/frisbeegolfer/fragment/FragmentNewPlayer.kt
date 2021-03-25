package fi.efelantti.frisbeegolfer.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import fi.efelantti.frisbeegolfer.NewPlayerAction
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.R

class FragmentNewPlayer : DialogFragment() {

    private lateinit var nameView: EditText
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

        val actionCategory = requireArguments().getString("action")?.let { NewPlayerAction.valueOf(it) }

        nameView = view.findViewById(R.id.edit_name)
        emailView = view.findViewById(R.id.edit_email)

        val oldPlayerData = requireArguments().getParcelable<Player>("playerData")

        if (actionCategory == NewPlayerAction.ADD)
        {
            toolbar.setTitle(getString(R.string.text_activity_new_player_title_add))
        }
        else if (actionCategory == NewPlayerAction.EDIT)
        {
            toolbar.setTitle(getString(R.string.text_activity_new_player_title_edit))
            nameView.setText(oldPlayerData?.name)
            emailView.setText(oldPlayerData?.email)
        }

        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {
                    val requiredFields: List<EditText> = listOf(nameView)
                    if (areValidFields(requiredFields) and isValidEmail(emailView)) {

                        val name = nameView.text.toString().trim()
                        val email = emailView.text.toString().trim()

                        playerData = Player(
                            name = name,
                            email = email
                        )

                        if (actionCategory == NewPlayerAction.EDIT) {
                            if(oldPlayerData == null) throw IllegalArgumentException("Cannot edit player data - it was null.")
                            else playerData.id = oldPlayerData.id // Take id from old player in order to update it to database.
                            if(Player.equals(
                                    playerData,
                                    oldPlayerData
                                )
                            ){
                                Toast.makeText(context, getString(R.string.player_data_not_edited), Toast.LENGTH_LONG).show()
                            }
                            else{
                                AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.dialog_title_overwrite))
                                    .setMessage(getString(R.string.dialog_message_confirm_overwrite)) // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(
                                        R.string.button_yes,
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
                .setTitle(getString(R.string.dialog_title_cancel))
                .setMessage(getString(R.string.dialog_message_cancel)) // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(
                    R.string.button_yes,
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

    private fun isValidEmail(target: EditText): Boolean
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