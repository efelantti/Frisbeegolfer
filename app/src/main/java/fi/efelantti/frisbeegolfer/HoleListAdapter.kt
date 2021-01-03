package fi.efelantti.frisbeegolfer

import android.content.Context
import android.content.res.Resources
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
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
        val parCount: TextView = itemView.findViewById(R.id.parCount)

        fun incrementPar(hole: Hole)
        {
            if(hole.par < 9)
            {
                hole.par = hole.par+1
            }
        }

        fun decrementPar(hole: Hole)
        {
            if(hole.par > 1)
            {
                hole.par = hole.par - 1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoleViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_hole, parent, false)
        return HoleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HoleViewHolder, position: Int) {
        var hole: Hole = holes[position]
        holder.holeIndexTextView.text = (position+1).toString() //res.getString(R.string.courseName, current.course.name)
        holder.parCount.text = hole.par.toString()
        
        if(hole.par == 9) holder.incrementButton.setVisibility(View.INVISIBLE)
        else holder.incrementButton.setVisibility(View.VISIBLE)
        if(hole.par == 1 ) holder.decrementButton.setVisibility(View.INVISIBLE)
        else holder.decrementButton.setVisibility(View.VISIBLE)

        holder.decrementButton.setOnClickListener(View.OnClickListener {
            holder.decrementPar(hole)
            notifyItemChanged(position)
        })
        holder.incrementButton.setOnClickListener(View.OnClickListener {
            holder.incrementPar(hole)
            notifyItemChanged(position)
        })
    }

    internal fun setHoles(holes: List<Hole>) {
        this.holes = holes
        notifyDataSetChanged()
    }

    override fun getItemCount() = holes.size
}