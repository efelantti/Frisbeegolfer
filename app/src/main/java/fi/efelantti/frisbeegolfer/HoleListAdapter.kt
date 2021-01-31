package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.model.Hole

class HoleListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<HoleListAdapter.HoleViewHolder>() {

    private var holes: List<Hole> = emptyList<Hole>()
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val res: Resources = context.resources
    private val context = context

    inner class HoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val holeRow: CardView = itemView.findViewById(R.id.holeCard)
        val holeIndexTextView: TextView = itemView.findViewById(R.id.recyclerView_hole_item_hole_index)
        val decrementButton: Button = itemView.findViewById(R.id.decrement_par)
        val incrementButton: Button = itemView.findViewById(R.id.increment_par)
        val parCountView: TextView = itemView.findViewById(R.id.parCount)
        val holeLengthView: TextView = itemView.findViewById(R.id.edit_length)
        var par: Int = 3

        fun incrementPar()
        {
            if(par < 9)
            {
                par = par+1
            }
        }

        fun decrementPar()
        {
            if(par > 1)
            {
                par = par - 1
            }
        }

        fun updateViews() {
            parCountView.text = "" + par
            if(par == 9) incrementButton.setVisibility(View.INVISIBLE)
            else incrementButton.setVisibility(View.VISIBLE)
            if(par == 1 ) decrementButton.setVisibility(View.INVISIBLE)
            else decrementButton.setVisibility(View.VISIBLE)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoleViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_hole, parent, false)
        return HoleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HoleViewHolder, position: Int) {
        var hole: Hole = holes[position]
        holder.holeIndexTextView.text = (position+1).toString() //res.getString(R.string.courseName, current.course.name)
        holder.parCountView.text = hole.par.toString()
        holder.par = holder.parCountView.text.toString().toInt()
        holder.holeLengthView.text = hole.lengthMeters?.toString()

        if(holder.par == 9) holder.incrementButton.setVisibility(View.INVISIBLE)
        else holder.incrementButton.setVisibility(View.VISIBLE)
        if(holder.par == 1 ) holder.decrementButton.setVisibility(View.INVISIBLE)
        else holder.decrementButton.setVisibility(View.VISIBLE)

        holder.decrementButton.setOnClickListener(View.OnClickListener {
            holder.decrementPar()
            holder.updateViews()
        })
        holder.incrementButton.setOnClickListener(View.OnClickListener {
            holder.incrementPar()
            holder.updateViews()
        })
    }

    internal fun setHoles(holes: List<Hole>) {
        this.holes = holes
        notifyDataSetChanged()
    }

    override fun getItemCount() = holes.size
}