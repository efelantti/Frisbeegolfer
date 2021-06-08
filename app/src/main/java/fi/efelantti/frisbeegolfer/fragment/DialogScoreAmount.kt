package fi.efelantti.frisbeegolfer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.DialogScoreAmountBinding

class DialogScoreAmount(private val onScoreAmountSelected: OnScoreAmountSelected) :
    DialogFragment() {

    private var _binding: DialogScoreAmountBinding? = null
    private val binding get() = _binding!!

    interface OnScoreAmountSelected {
        fun selectedScoreAmount(score: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogScoreAmountBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.dialog_score_amount_title)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                Toast.makeText(
                    requireContext(),
                    binding.scoreAmount.text.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .create()
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DialogScoreAmount"
    }
}
