package my.com.foodie.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.com.foodie.R
import my.com.foodie.data.Restaurant

class PickRestaurantAdapter (
    val fn: (ViewHolder, Restaurant) -> Unit = { _, _ ->}
) : ListAdapter<Restaurant, PickRestaurantAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val restaurantName : TextView = view.findViewById(R.id.pickRestaurantName)
        val distance : TextView = view.findViewById(R.id.pickRestaurantDistance)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_pick_restaurant_to_spinning_wheel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurantItem = getItem(position)

        holder.restaurantName.text       = restaurantItem.name
        holder.distance.text   = String.format("%.2f", restaurantItem.distance) + " km"

        fn(holder, restaurantItem)
    }


}