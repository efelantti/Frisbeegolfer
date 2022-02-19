package fi.efelantti.frisbeegolfer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.DialogFragment
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.DialogScoreAmountBinding

class DialogScoreAmount(private val onScoreAmountSelectedListener: OnScoreAmountSelected) :
    DialogFragment() {

    private var _binding: DialogScoreAmountBinding? = null
    private val binding get() = _binding!!

    interface OnScoreAmountSelected {
        fun selectedScoreAmount(score: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogScoreAmountBinding.inflate(LayoutInflater.from(context))
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.dialog_score_amount_title)
            .setPositiveButton(getString(R.string.set_score), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }

    override fun onResume() {
        super.onResume()
        val alertDialog = dialog as AlertDialog?
        val okButton: Button = alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener {
            val maxNumberOfThrows = resources.getInteger(R.integer.max_amount_of_throws)
            try {
                val scoreAmount = parseScoreAmount(binding.scoreAmount.text.toString())
                if (scoreAmount in 1..maxNumberOfThrows) {
                    onScoreAmountSelectedListener.selectedScoreAmount(scoreAmount)
                    dismiss()
                } else {
                    throw IllegalArgumentException("Score amount should be between 1 and $maxNumberOfThrows.")
                }
            } catch (exception: Exception) {
                binding.scoreAmountLayout.error =
                    getString(R.string.dialog_score_amount_error, maxNumberOfThrows)
            }
        }
    }

    private fun parseScoreAmount(scoreAmountString: String): Int {
        if (scoreAmountString.isBlank()) throw IllegalArgumentException("Can't get integer from empty string.")
        return scoreAmountString.toInt()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DialogScoreAmount"
    }
}
