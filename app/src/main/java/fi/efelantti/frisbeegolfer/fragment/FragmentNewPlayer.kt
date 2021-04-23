package fi.efelantti.frisbeegolfer.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.content.ContextCompat.getColor
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import fi.efelantti.frisbeegolfer.FrisbeegolferApplication
import fi.efelantti.frisbeegolfer.NewPlayerAction
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.FragmentNewPlayerBinding
import fi.efelantti.frisbeegolfer.model.Player
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModel
import fi.efelantti.frisbeegolfer.viewmodel.PlayerViewModelFactory

class FragmentNewPlayer : DialogFragment() {

    private var _binding: FragmentNewPlayerBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentNewPlayerArgs by navArgs()
    private val playerViewModel: PlayerViewModel by activityViewModels {
        PlayerViewModelFactory((requireContext().applicationContext as FrisbeegolferApplication).repository)
    }
    private val _tag = "FragmentNewPlayer"
    private lateinit var nameView: EditText
    private lateinit var emailView: EditText
    private lateinit var playerData: Player

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlayerBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.dialogToolbar
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.inflateMenu(R.menu.appbar_dialog)

        val actionCategory = NewPlayerAction.valueOf(args.actionType)

        nameView = binding.editName
        emailView = binding.editEmail

        val oldPlayerId = args.playerId
        playerViewModel.getPlayerById(oldPlayerId).observe(viewLifecycleOwner) { player ->
            if (oldPlayerId == -1L) {
                toolbar.title = getString(R.string.text_activity_new_player_title_add)
            } else if (player != null) {
                toolbar.title = getString(R.string.text_activity_new_player_title_edit)
                nameView.setText(player.name)
                emailView.setText(player.email)
            }
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
                            if (oldPlayerData == null) throw IllegalArgumentException("Cannot edit player data - it was null.")
                            else playerData.id =
                                oldPlayerData.id // Take id from old player in order to update it to database.
                            if (Player.equals(
                                    playerData,
                                    oldPlayerData
                                )
                            ) {
                                Toast.makeText(
                                    context,
                                    getString(R.string.player_data_not_edited),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.dialog_title_overwrite))
                                    .setMessage(getString(R.string.dialog_message_confirm_overwrite)) // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(
                                        R.string.button_yes
                                    ) { dialog, which ->
                                        exitWithResult(actionCategory)
                                    } // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(R.string.button_no, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show()
                            }
                        } else {
                            exitWithResult(actionCategory)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.error_message_requiredFields),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        toolbar.setNavigationOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.dialog_title_cancel))
                .setMessage(getString(R.string.dialog_message_cancel)) // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(
                    R.string.button_yes
                ) { dialog, which ->
                    dismiss()
                } // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.button_no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    private fun checkIfPlayerAlreadyExists(player: Player, players: List<Player>?): Boolean {
        if (players == null) return false
        for (existingPlayer: Player in players) {
            if (Player.equals(
                    player,
                    existingPlayer
                )
            ) {
                Log.e(_tag, "Could not add player data to database - duplicate.")
                val toast = Toast.makeText(
                    requireContext(), HtmlCompat.fromHtml(
                        "<font color='" + getColor(
                            requireContext(),
                            R.color.colorErrorMessage
                        ) + "' ><b>" + getString(
                            R.string.error_duplicate_player
                        ) + "</b></font>", HtmlCompat.FROM_HTML_MODE_LEGACY
                    ), Toast.LENGTH_LONG
                )
                toast.show()
                return true
            }
        }
        return false
    }

    private fun exitWithResult(category: NewPlayerAction?) {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        when (category) {
            NewPlayerAction.ADD -> {
                val players = playerViewModel.allPlayers.value
                val duplicateFound = checkIfPlayerAlreadyExists(playerData, players)
                if (!duplicateFound) {
                    playerViewModel.insert(playerData)
                    dismiss()
                }
            }
            NewPlayerAction.EDIT -> {
                val players = playerViewModel.allPlayers.value
                val duplicateFound = checkIfPlayerAlreadyExists(playerData, players)
                if (!duplicateFound) {
                    playerViewModel.update(playerData)
                    dismiss()
                }
            }
        }
    }

    private fun isValidEmail(target: EditText): Boolean {
        var isValid = false
        if (target.text.isNullOrEmpty() || Patterns.EMAIL_ADDRESS.matcher(target.text)
                .matches()
        ) isValid = true
        return if (!isValid) {
            target.error = getString(R.string.invalid_field, target.hint)
            false
        } else true
    }

    private fun areValidFields(fields: List<EditText>): Boolean {
        var allFieldsValid = true
        for (field: EditText in fields) {
            if (TextUtils.isEmpty(field.text.trim())) {
                field.error = getString(R.string.invalid_field, field.hint)
                allFieldsValid = false
            }
        }
        return allFieldsValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}