package my.com.foodie.util

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.com.foodie.R
import my.com.foodie.data.Report
import my.com.foodie.data.Reservation
import my.com.foodie.data.currentUser
import java.text.SimpleDateFormat

class OwnerReportAdapter (
    val fn: (ViewHolder, Report) -> Unit = { _, _ ->}
) : ListAdapter<Report, OwnerReportAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Report, newItem: Report) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val ownerReportID : TextView = view.findViewById(R.id.ownerReportID)
        val ownerReportDate : TextView = view.findViewById(R.id.ownerReportDate)
        val ownerReportType : TextView = view.findViewById(R.id.ownerReportType)
        val ownerReportDescription : TextView = view.findViewById(R.id.ownerReportDescription)
        val btnDeleteReport: ImageView = view.findViewById(R.id.btnDeleteReport)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_report_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reportItem = getItem(position)

        holder.ownerReportID.text = reportItem.id
        holder.ownerReportDate.text = SimpleDateFormat("dd-MM-yyyy").format(reportItem.dateTime)
        holder.ownerReportType.text = reportItem.reportType
        holder.ownerReportDescription.text = reportItem.description
        holder.btnDeleteReport.isVisible = currentUser!!.role == "Moderator"

        fn(holder, reportItem)
    }
}