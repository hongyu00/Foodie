package my.com.foodie.util

import android.graphics.Color
import android.util.Log
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
import org.w3c.dom.Text

class RestaurantForUserAdapter(
    val fn: (ViewHolder, Restaurant) -> Unit = { _, _ ->}
) : ListAdapter<Restaurant, RestaurantForUserAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val restaurantLogo : ImageView = view.findViewById(R.id.imgRestaurantLogo)
        val restaurantName : TextView = view.findViewById(R.id.lblRestaurantName)
        val restaurantCuisine : TextView = view.findViewById(R.id.lblRestaurantCuisine)
        val restaurantCity : TextView = view.findViewById(R.id.lblRestaurantCity)
        val restaurantReview : TextView = view.findViewById(R.id.lblRestaurantReview)
        val restaurantPriceRange : TextView = view.findViewById(R.id.lblRestaurantPrice)
        val restaurantDistance : TextView = view.findViewById(R.id.lblRestaurantDistance)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_restaurants_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurantItem = getItem(position)
        var rating = 0.00F
        if(restaurantItem.reviewCount != 0){
           rating = restaurantItem.totalRating/restaurantItem.reviewCount
        }

        if(restaurantItem.restaurantImg?.toBitmap() == null){
            holder.restaurantLogo.setImageResource(R.drawable.logo)
        }else{
            holder.restaurantLogo.setImageBitmap(restaurantItem.restaurantImg?.toBitmap())
        }
        //holder.restaurantLogo.setImageBitmap(restaurantItem.restaurantImg?.toBitmap())
        holder.restaurantName.text       = restaurantItem.name
        holder.restaurantCuisine.text = restaurantItem.cuisine
        holder.restaurantCity.text = restaurantItem.cities
        //holder.restaurantReview.text     = restaurantItem.avgReview.toString() + "⭐"
        holder.restaurantReview.text     = String.format("%.2f", rating) + "⭐"
        holder.restaurantPriceRange.text   = "RM" + restaurantItem.priceRange + "/person"
        holder.restaurantDistance.text   = String.format("%.2f", restaurantItem.distance) + " km"


        fn(holder, restaurantItem)
    }


}