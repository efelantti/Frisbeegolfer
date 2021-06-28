package fi.efelantti.frisbeegolfer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import fi.efelantti.frisbeegolfer.R

class DialogConfirmDelete(
    private val onConfirmationListener: OnConfirmationSelected,
    private val objectToDelete: Any,
    private val objectToDeleteType: String
) :
    DialogFragment() {

    interface OnConfirmationSelected {
        fun returnUserConfirmation(objectToDelete: Any)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(getString(R.string.delete_confirmation, objectToDeleteType))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                onConfirmationListener.returnUserConfirmation(objectToDelete)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()

    companion object {
        const val TAG = "DialogConfirmDelete"
    }
}
