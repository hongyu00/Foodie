package my.com.foodie.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.com.foodie.R
import my.com.foodie.data.Restaurant

class MostReservationMadeRestaurantAdapter (
    val fn: (ViewHolder, Restaurant) -> Unit = { _, _ ->}
) : ListAdapter<Restaurant, MostReservationMadeRestaurantAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view

        val count : TextView = view.findViewById(R.id.count)
        val restaurant : TextView = view.findViewById(R.id.osrRestaurant)
        val label : TextView = view.findViewById(R.id.osrlbl)
        val field : TextView = view.findViewById(R.id.osrField)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_on_screen_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurantItem = getItem(position)

        holder.count.text     = (position+1).toString() + "."
        holder.restaurant.text     = restaurantItem.name
        holder.label.text     = "Reservation Count:"
        holder.field.text     = restaurantItem.reservationCount.toString() + " times"

        fn(holder, restaurantItem)
    }


}