package fi.efelantti.frisbeegolfer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import fi.efelantti.frisbeegolfer.R

class DialogConfirmImport(
    private val onConfirmationListener: OnConfirmationSelected,
) :
    DialogFragment() {

    interface OnConfirmationSelected {
        fun returnUserConfirmation()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.import_confirmation_title))
            .setMessage(getString(R.string.import_confirmation_message))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                onConfirmationListener.returnUserConfirmation()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()

    companion object {
        const val TAG = "DialogConfirmImport"
    }
}
