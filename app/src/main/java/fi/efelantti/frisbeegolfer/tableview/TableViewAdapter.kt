package fi.efelantti.frisbeegolfer.tableview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
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
        val cellContainer: LinearLayout = itemView.findViewById(R.id.cell_container)
        val cellTextView: TextView = itemView.findViewById(R.id.cell_data)

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
        if (cell.data.isNullOrBlank() || cell.data == "null") viewHolder.cellTextView.text = ""
        else viewHolder.cellTextView.text = cell.data.toString()



        viewHolder.cellContainer.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        viewHolder.cellTextView.requestLayout()
    }

    class MyColumnHeaderViewHolder(itemView: View) :
        AbstractViewHolder(itemView) {
        val columnHeaderContainer: LinearLayout =
            itemView.findViewById(R.id.column_header_container)
        val cellTextView: TextView = itemView.findViewById(R.id.column_header_textView)
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
        columnHeaderViewHolder.cellTextView.text = columnHeader.data.toString()



        columnHeaderViewHolder.columnHeaderContainer.layoutParams.width =
            LinearLayout.LayoutParams.WRAP_CONTENT
        columnHeaderViewHolder.cellTextView.requestLayout()
    }

    class MyRowHeaderViewHolder(itemView: View) :
        AbstractViewHolder(itemView) {
        val parentView: RelativeLayout = itemView.findViewById(R.id.row_root)
        val cellTextView: TextView = itemView.findViewById(R.id.row_header_textView)
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


        rowHeaderViewHolder.cellTextView.text = rowHeader.data.toString()
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