package my.com.foodie.util

import android.content.Context
import android.graphics.BitmapFactory
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
import java.security.AccessController.getContext

class RestaurantListingModeratorAdapter (
    val fn: (ViewHolder, Restaurant) -> Unit = { _, _ ->}
) : ListAdapter<Restaurant, RestaurantListingModeratorAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val imgLogo : ImageView = view.findViewById(R.id.imgLogo)
        val restaurantID : TextView = view.findViewById(R.id.restaurantID)
        val itemRestaurantName : TextView = view.findViewById(R.id.itemRestaurantName)
        val itemRestaurantCuisine : TextView = view.findViewById(R.id.itemRestaurantCuisine)
        val restaurantStatus: TextView = view.findViewById(R.id.itemRestaurantStatus)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_restaurant_listing_moderator, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurantItem = getItem(position)

        if(restaurantItem.restaurantImg?.toBitmap() == null){
            holder.imgLogo.setImageResource(R.drawable.logo)
        }else{
            holder.imgLogo.setImageBitmap(restaurantItem.restaurantImg?.toBitmap())
        }
        holder.restaurantID.text       = restaurantItem.id
        holder.itemRestaurantName.text = restaurantItem.name
        holder.itemRestaurantCuisine.text = restaurantItem.cuisine
        holder.restaurantStatus.text = "(${restaurantItem.status})"


        fn(holder, restaurantItem)
    }


}