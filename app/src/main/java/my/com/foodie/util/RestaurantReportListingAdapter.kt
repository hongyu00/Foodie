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

class RestaurantReportListingAdapter (
    val fn: (ViewHolder, Restaurant) -> Unit = { _, _ ->}
) : ListAdapter<Restaurant, RestaurantReportListingAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Restaurant>() {
        override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val imgLogo : ImageView = view.findViewById(R.id.restaurantLogoReport)
        val restaurantID : TextView = view.findViewById(R.id.restaurantIDReport)
        val restaurantName : TextView = view.findViewById(R.id.restaurantNameReport)
        val reportCount : TextView = view.findViewById(R.id.reportCount)
        val restaurantStatus: TextView = view.findViewById(R.id.restaurantStatusReport)
        val btnMore: ImageView = view.findViewById(R.id.btnMore)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_restaurant_report_listing, parent, false)
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
        holder.restaurantName.text = restaurantItem.name
        holder.reportCount.text = restaurantItem.reportCount.toString()
        holder.restaurantStatus.text = restaurantItem.status
        holder.btnMore

        fn(holder, restaurantItem)
    }


}