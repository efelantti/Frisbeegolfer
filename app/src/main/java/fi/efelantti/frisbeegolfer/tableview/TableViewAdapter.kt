package fi.efelantti.frisbeegolfer.tableview

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amulyakhare.textdrawable.TextDrawable
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.tableview.model.Cell
import fi.efelantti.frisbeegolfer.tableview.model.ColumnHeader
import fi.efelantti.frisbeegolfer.tableview.model.RowHeader

class TableViewAdapter :
    AbstractTableAdapter<ColumnHeader, RowHeader, Cell>() {

    class MyCellViewHolder(itemView: View) :
        AbstractViewHolder(itemView) {
        var builder: TextDrawable.IBuilder = TextDrawable.builder()
            .beginConfig()
            .bold()
            .endConfig()
            .round()
        val obShape: FrameLayout = itemView.findViewById(R.id.ob_shape)
        val resultImageView: ImageView = itemView.findViewById(R.id.result)
        val plusMinusCumulativeTextView: TextView =
            itemView.findViewById(R.id.plus_minus_cumulative)
        val dnfTextView: TextView = itemView.findViewById(R.id.dnf)
    }

    override fun onCreateCellViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {
        val layout: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_view_cell_layout, parent, false)
        return MyCellViewHolder(layout)
    }

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder,
        cellItemModel: Cell?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        val cell =
            cellItemModel as Cell

        val viewHolder =
            holder as MyCellViewHolder
        if (cell.isOutOfBounds == true) {
            viewHolder.obShape.visibility = View.VISIBLE
        } else {
            viewHolder.obShape.visibility = View.GONE
        }
        if (cell.didNotFinish != null && cell.didNotFinish) {
            viewHolder.plusMinusCumulativeTextView.visibility = View.GONE
            viewHolder.resultImageView.visibility = View.GONE
            viewHolder.dnfTextView.visibility = View.VISIBLE
        } else if (cell.result.isNullOrBlank() || cell.result == "null") {
            viewHolder.dnfTextView.visibility = View.GONE
            viewHolder.plusMinusCumulativeTextView.text = ""
            viewHolder.resultImageView.visibility = View.GONE
        } else {
            viewHolder.dnfTextView.visibility = View.GONE
            viewHolder.resultImageView.visibility = View.VISIBLE
            viewHolder.plusMinusCumulativeTextView.visibility = View.VISIBLE
            val icon = viewHolder.builder.build(cell.result, cell.resultColor ?: Color.GRAY)
            viewHolder.resultImageView.setImageDrawable(icon)
            viewHolder.plusMinusCumulativeTextView.text = cell.plusMinusCumulative.toString()
        }

        //viewHolder.cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        //viewHolder.resultImageView.requestLayout()
        //viewHolder.plusMinusCumulativeTextView.requestLayout()
    }

    class MyColumnHeaderViewHolder(itemView: View) :
        AbstractViewHolder(itemView) {
        val columnHeaderContainer: LinearLayout =
            itemView.findViewById(R.id.column_header_container)
        val cellPlayerName: TextView = itemView.findViewById(R.id.column_header_player_name)
        val cellResult: TextView = itemView.findViewById(R.id.column_header_result)
        //val lineDivider: View = itemView.findViewById(R.id.vLine)

    }

    override fun onCreateColumnHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {

        val layout: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_view_column_header_layout, parent, false)

        return MyColumnHeaderViewHolder(layout)
    }


    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder,
        columnHeaderItemModel: ColumnHeader?,
        position: Int
    ) {
        val columnHeader = columnHeaderItemModel as ColumnHeader

        val columnHeaderViewHolder =
            holder as MyColumnHeaderViewHolder
        columnHeaderViewHolder.cellPlayerName.text = columnHeader.playerName.toString()
        columnHeaderViewHolder.cellResult.text = columnHeader.resultPlusMinus.toString()

        columnHeaderViewHolder.columnHeaderContainer.layoutParams.width =
            LinearLayout.LayoutParams.WRAP_CONTENT
        columnHeaderViewHolder.cellPlayerName.requestLayout()
    }

    class MyRowHeaderViewHolder(itemView: View) :
        AbstractViewHolder(itemView) {
        val cellTextView: TextView = itemView.findViewById(R.id.row_header_textView)
        val cellTextViewParCount: TextView =
            itemView.findViewById(R.id.row_header_textView_par_count)
        //val lineDivider: View = itemView.findViewById(R.id.vLine)
    }

    override fun onCreateRowHeaderViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {

        val layout: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_view_row_header_layout, parent, false)

        return MyRowHeaderViewHolder(layout)
    }

    override fun onBindRowHeaderViewHolder(
        holder: AbstractViewHolder,
        rowHeaderItemModel: RowHeader?,
        position: Int
    ) {
        val rowHeader = rowHeaderItemModel as RowHeader

        val rowHeaderViewHolder =
            holder as MyRowHeaderViewHolder

        rowHeaderViewHolder.cellTextView.text = rowHeader.holeNumber
        rowHeaderViewHolder.cellTextViewParCount.text = rowHeader.parCount
    }

    override fun onCreateCornerView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.table_view_corner_layout, parent, false)
    }

    override fun getColumnHeaderItemViewType(columnPosition: Int): Int {
        return 0
    }

    override fun getRowHeaderItemViewType(rowPosition: Int): Int {
        return 0
    }

    override fun getCellItemViewType(columnPosition: Int): Int {
        return 0
    }
}