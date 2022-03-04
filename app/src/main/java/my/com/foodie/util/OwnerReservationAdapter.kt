package my.com.foodie.util

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.com.foodie.R
import my.com.foodie.data.Reservation
import my.com.foodie.data.Review
import java.text.SimpleDateFormat

class OwnerReservationAdapter (
    val fn: (ViewHolder, Reservation) -> Unit = { _, _ ->}
) : ListAdapter<Reservation, OwnerReservationAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val reserverName : TextView = view.findViewById(R.id.reserverName)
        val reservationDate : TextView = view.findViewById(R.id.reservationDate2)
        val reservationTime : TextView = view.findViewById(R.id.reservationTime2)
        val reservationNoOfPeople : TextView = view.findViewById(R.id.reservationNoOfPeople2)
        val reservationStatus : TextView = view.findViewById(R.id.reservationStatus2)
        val lblRejectReason : TextView = view.findViewById(R.id.lblRejectReason2)
        val rejectReason : TextView = view.findViewById(R.id.rejectReason2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_owner_reservation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservationItem = getItem(position)

        holder.reserverName.text = reservationItem.user.name
        holder.reservationDate.text = reservationItem.date
        holder.reservationTime.text = reservationItem.time
        holder.reservationNoOfPeople.text = reservationItem.noOfPeople.toString()
        holder.reservationStatus.text = reservationItem.status
        if(reservationItem.rejectReason != ""){
            holder.lblRejectReason.isVisible = true
            holder.rejectReason.isVisible = true
            holder.rejectReason.text = reservationItem.rejectReason
            holder.rejectReason.setTextColor(Color.RED)
        }else{
            holder.lblRejectReason.isVisible = false
            holder.rejectReason.isVisible = false
        }

        when(reservationItem.status){
            "Pending" -> holder.reservationStatus.setTextColor(Color.parseColor("#F6BE00"))
            "Approved" -> holder.reservationStatus.setTextColor(Color.parseColor("#23BF13"))
            "Rejected" -> holder.reservationStatus.setTextColor(Color.RED)
        }

        fn(holder, reservationItem)
    }
}