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


class UserReservationAdapter (
    val fn: (ViewHolder, Reservation) -> Unit = { _, _ ->}
) : ListAdapter<Reservation, UserReservationAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val resName : TextView = view.findViewById(R.id.reservationRestaurantName)
        val resDate : TextView = view.findViewById(R.id.reservationDate)
        val resTime : TextView = view.findViewById(R.id.reservationTime)
        val resNoOfPeople : TextView = view.findViewById(R.id.reservationNoOfPeople)
        val resStatus: TextView = view.findViewById(R.id.reservationStatus)
        val lblRejectReason : TextView = view.findViewById(R.id.lblUserRejectReason)
        val rejectReason : TextView = view.findViewById(R.id.userRejectReason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_user_reservations, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservationItem = getItem(position)
        holder.resName.text = reservationItem.restaurant.name
        holder.resDate.text = reservationItem.date
        holder.resTime.text = reservationItem.time
        holder.resNoOfPeople.text = reservationItem.noOfPeople.toString() + " person(s)"
        holder.resStatus.text = reservationItem.status

        if(reservationItem.rejectReason != ""){
            holder.lblRejectReason.isVisible = true
            holder.rejectReason.isVisible = true
            holder.rejectReason.text = reservationItem.rejectReason
            holder.rejectReason.setTextColor(Color.RED)
        }

        when(reservationItem.status){
            "Pending" -> holder.resStatus.setTextColor(Color.parseColor("#F6BE00"))
            "Approved" -> holder.resStatus.setTextColor(Color.parseColor("#23BF13"))
            "Rejected" -> holder.resStatus.setTextColor(Color.RED)
        }

        fn(holder, reservationItem)
    }
}