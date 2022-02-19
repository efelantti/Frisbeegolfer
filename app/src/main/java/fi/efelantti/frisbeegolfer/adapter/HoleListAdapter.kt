package fi.efelantti.frisbeegolfer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.databinding.RecyclerviewHoleBinding
import fi.efelantti.frisbeegolfer.model.Hole

class HoleListAdapter internal constructor(
    val context: Context
) :
    RecyclerView.Adapter<HoleListAdapter.HoleViewHolder>() {

    private var holes: List<Hole> = emptyList()

    inner class HoleViewHolder(val binding: RecyclerviewHoleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val holeIndexTextView: TextView = binding.recyclerViewHoleItemHoleIndex
        val decrementButton: Button = binding.decrementPar
        val incrementButton: Button = binding.incrementPar
        val parCountView: TextView = binding.parCount
        val holeLengthView: TextView = binding.editLength
        var par: Int = 3

        fun incrementPar() {
            if (par < 9) {
                par += 1
            }
        }

        fun decrementPar()
        {
            if(par > 1)
            {
                par -= 1
            }
        }

        fun updateViews() {
            parCountView.text = par.toString()
            incrementButton.isEnabled = par != 9
            decrementButton.isEnabled = par != 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoleViewHolder {
        val binding =
            RecyclerviewHoleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HoleViewHolder, position: Int) {
        val hole: Hole = holes[position]
        holder.holeIndexTextView.text = context.getString(R.string.hole_index, position + 1)
        holder.parCountView.text = hole.par.toString()
        holder.par = holder.parCountView.text.toString().toInt()
        holder.holeLengthView.text = hole.lengthMeters?.toString()

        if (holder.par == 9) holder.incrementButton.visibility = View.INVISIBLE
        else holder.incrementButton.visibility = View.VISIBLE
        if (holder.par == 1) holder.decrementButton.visibility = View.INVISIBLE
        else holder.decrementButton.visibility = View.VISIBLE

        holder.decrementButton.setOnClickListener {
            holder.decrementPar()
            holder.updateViews()
        }
        holder.incrementButton.setOnClickListener {
            holder.incrementPar()
            holder.updateViews()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun setHoles(holes: List<Hole>) {
        this.holes = holes.sortedBy{it.holeNumber}
        notifyDataSetChanged()
    }

    override fun getItemCount() = holes.size
}