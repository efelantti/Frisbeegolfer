package fi.efelantti.frisbeegolfer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.efelantti.frisbeegolfer.databinding.TableListItemBinding

// https://github.com/monsterbrain/RecyclerviewTableViewAndroid
class TableViewAdapter(private val scoreList: List<String>) :
    RecyclerView.Adapter<TableViewAdapter.RowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = TableListItemBinding.inflate(LayoutInflater.from(parent.context))
        return RowViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return if (scoreList[position].toIntOrNull() == null) CellType.Header.id
        else CellType.Data.id
    }

    private fun setHeaderBg(view: View) {
        view.setBackgroundResource(R.drawable.table_header_cell_bg)
    }

    private fun setContentBg(view: View) {
        view.setBackgroundResource(R.drawable.table_content_cell_bg)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        val rowPos = holder.bindingAdapterPosition
        if (getItemViewType(position) == CellType.Header.id) {
            // Header Cells. Main Headings appear here

            setHeaderBg(holder.binding.cellData)

        } else {
            val modal = scoreList[rowPos - 1]

            setContentBg(holder.binding.cellData)
        }
        holder.binding.cellData.text = scoreList[position]
    }

    override fun getItemCount(): Int {
        return scoreList.size
    }

    inner class RowViewHolder(val binding: TableListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}

enum class CellType(val id: Int) {
    Header(id = 0),
    Data(id = 1)
}