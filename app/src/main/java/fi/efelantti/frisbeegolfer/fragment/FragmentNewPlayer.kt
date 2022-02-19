package fi.efelantti.frisbeegolfer.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import fi.efelantti.frisbeegolfer.*
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
    private lateinit var nameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private var isFinalized = false

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_dialog, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val saveMenuItem = menu.findItem(R.id.action_save)
        saveMenuItem.isEnabled = isFinalized
        super.onPrepareOptionsMenu(menu)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.dialogToolbar.toolbar
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.inflateMenu(R.menu.appbar_dialog)

        val actionCategory = NewPlayerAction.valueOf(args.actionType)

        nameLayout = binding.editNameLayout
        nameEditText = binding.editName
        emailLayout = binding.editEmailLayout
        emailEditText = binding.editEmail

        val oldPlayerId = args.playerId
        lateinit var onSaveButtonClick: Toolbar.OnMenuItemClickListener

        if (actionCategory == NewPlayerAction.ADD) {
            isFinalized = true
            requireActivity().invalidateOptionsMenu()
            toolbar.title = getString(R.string.text_activity_new_player_title_add)
            onSaveButtonClick = Toolbar.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_save -> {
                        createNewPlayer()
                        return@OnMenuItemClickListener true
                    }
                }
                false
            }
            toolbar.setOnMenuItemClickListener(onSaveButtonClick)
        } else if (actionCategory == NewPlayerAction.EDIT) {
            requireActivity().invalidateOptionsMenu()
            toolbar.title = getString(R.string.text_activity_new_player_title_edit)

            playerViewModel.state.observe(viewLifecycleOwner) { state ->
                when (state) {
                    LiveDataState.LOADING -> binding.progressBar.visibility = View.VISIBLE
                    LiveDataState.SUCCESS -> binding.progressBar.visibility = View.GONE
                    null -> binding.progressBar.visibility = View.GONE
                }
            }
            playerViewModel.getPlayerById(oldPlayerId).observe(viewLifecycleOwner) { player ->
                if (player != null && playerViewModel.state.value == LiveDataState.SUCCESS) {
                    isFinalized = true
                    nameEditText.setText(player.name)
                    emailEditText.setText(player.email)
                    onSaveButtonClick = Toolbar.OnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_save -> {
                                editPlayer(player)
                                return@OnMenuItemClickListener true
                            }
                        }
                        false
                    }
                    toolbar.setOnMenuItemClickListener(onSaveButtonClick)
                }
            }
        }

        toolbar.setNavigationOnClickListener {
            showCancelDialog()
        }
    }

    /*
    Passed to onClickHandler for save menu item, when creating new player.
    */
    private fun createNewPlayer() {
        val playerFields: Pair<String, String> = getDataFromFields(nameEditText, emailEditText)
        val name = playerFields.first
        val email = playerFields.second
        val isValidName = validateName(name)
        val isValidEmail = validateEmail(email)
        if (!isValidName) {
            setFieldError(nameLayout, nameEditText)
        } else {
            nameLayout.error = null
        }
        if (!isValidEmail) {
            setFieldError(emailLayout, emailEditText)
        } else {
            emailLayout.error = null
        }
        if (isValidName && isValidEmail) {
            playerViewModel.playerExists(name).observeOnce(viewLifecycleOwner) {
                it.let { playerFound ->
                    if (!playerFound) {
                        val playerData = Player(
                            name = name,
                            email = email
                        )
                        playerViewModel.insert(playerData)
                        dismiss()
                    } else {
                        showPlayerAlreadyExistsError()
                    }
                }
            }
        }
    }

    private fun editPlayer(oldPlayerData: Player) {
        val playerFields: Pair<String, String> = getDataFromFields(nameEditText, emailEditText)
        val name = playerFields.first
        val email = playerFields.second
        val isValidName = validateName(name)
        val isValidEmail = validateEmail(email)
        if (!isValidName) {
            setFieldError(nameLayout, nameEditText)
        }
        if (!isValidEmail) {
            setFieldError(emailLayout, emailEditText)
        }
        if (isValidName && isValidEmail) {
            val playerData = Player(
                id = oldPlayerData.id,
                name = name,
                email = email
            )
            if (playerData == oldPlayerData) {
                showDataNotEditedError()
            } else {
                showConfirmationForEditPlayerDialog(playerData, oldPlayerData)
            }
        }
    }


    /*
Gets the text from the EditTexts, trims them and packs them to a Pair.
 */
    private fun getDataFromFields(nameView: EditText, emailView: EditText): Pair<String, String> {
        return Pair(nameView.text.toString().trim(), emailView.text.toString().trim())
    }

    /*
    Checks that name is not empty or whitespace.
     */
    private fun validateName(name: String): Boolean {
        return !TextUtils.isEmpty(name.trim())
    }

    /*
    Sets error message to name field.
     */
    private fun setFieldError(layout: TextInputLayout, field: TextInputEditText) {
        layout.error = getString(R.string.invalid_field, field.hint)
    }

    /*
    Checks if email is a valid email address (or empty).
     */
    private fun validateEmail(email: String): Boolean {
        val trimmedEmail = email.trim()
        return trimmedEmail.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(trimmedEmail)
            .matches()
    }

    /*
    Shows a dialog where the user can close this fragment.
     */
    private fun showCancelDialog() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.dialog_title_cancel))
            .setMessage(getString(R.string.dialog_message_cancel)) // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(
                R.string.button_yes
            ) { _, _ ->
                dismiss()
            } // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton(R.string.button_no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    /*
    Toast that is shown if there is a player with the same name in the database.
     */
    private fun showPlayerAlreadyExistsError() {
        Log.e(_tag, "Could not add player data to database - duplicate.")
        ToastUtils.showErrorToast(requireContext(), getString(R.string.error_duplicate_player))
    }

    /*
    Toast that is shown, if user tries to save, but no changes were made to player data.
     */
    private fun showDataNotEditedError() {
        Toast.makeText(
            context,
            getString(R.string.player_data_not_edited),
            Toast.LENGTH_LONG
        ).show()
    }

    /*
    Dialog that asks user for confirming to overwrite previous player data. If user confirms,
    the player is updated, in case there is no other player already with the same name.
     */
    private fun showConfirmationForEditPlayerDialog(playerData: Player, oldPlayerData: Player) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.dialog_title_overwrite))
            .setMessage(getString(R.string.dialog_message_confirm_overwrite))
            .setPositiveButton(
                R.string.button_yes
            ) { _, _ ->
                // If the name has not changed, there's no need to check for duplicates.
                if (playerData.name == oldPlayerData.name) {
                    playerViewModel.update(playerData)
                    dismiss()
                }
                // But if the name has changed, then verify that there is not already a player with the same name.
                else {
                    playerViewModel.playerExists(playerData.name!!)
                        .observeOnce(viewLifecycleOwner) {
                            it.let { playerFound ->
                                if (!playerFound) {
                                    playerViewModel.update(playerData)
                                    dismiss()
                                } else {
                                    showPlayerAlreadyExistsError()
                                }
                            }
                        }
                }
            }
            .setNegativeButton(R.string.button_no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}