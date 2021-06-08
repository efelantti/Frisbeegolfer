package fi.efelantti.frisbeegolfer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.databinding.TableListItemBinding

// https://github.com/monsterbrain/RecyclerviewTableViewAndroid
class TableViewAdapter(private val scoreList: List<Int>) :
    RecyclerView.Adapter<TableViewAdapter.RowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = TableListItemBinding.inflate(LayoutInflater.from(parent.context))
        return RowViewHolder(binding)
    }

    private fun setHeaderBg(view: View) {
        view.setBackgroundResource(R.drawable.table_header_cell_bg)
    }

    private fun setContentBg(view: View) {
        view.setBackgroundResource(R.drawable.table_content_cell_bg)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val rowPos = holder.bindingAdapterPosition
        if (rowPos == 0) {
            // Header Cells. Main Headings appear here

            setHeaderBg(holder.binding.txtRank)
            setHeaderBg(holder.binding.txtMovieName)
            setHeaderBg(holder.binding.txtYear)
            setHeaderBg(holder.binding.txtCost)

            holder.binding.txtRank.text = "Rank"
            holder.binding.txtMovieName.text = "Name"
            holder.binding.txtYear.text = "Year"
            holder.binding.txtCost.text = "Budget (in Millions)"

        } else {
            val modal = scoreList[rowPos - 1]


            setContentBg(holder.binding.txtRank)
            setContentBg(holder.binding.txtMovieName)
            setContentBg(holder.binding.txtYear)
            setContentBg(holder.binding.txtCost)

            holder.binding.txtRank.text = "Rank $rowPos"
            holder.binding.txtMovieName.text = "Movie name $rowPos"
            holder.binding.txtYear.text = "Year $rowPos"
            holder.binding.txtCost.text = "Cost $rowPos"

        }
    }

    override fun getItemCount(): Int {
        return scoreList.size + 1 // one more to add header row
    }

    inner class RowViewHolder(val binding: TableListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}