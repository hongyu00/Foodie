package my.com.foodie.util

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.com.foodie.R
import my.com.foodie.data.Request
import my.com.foodie.data.Restaurant
import java.text.SimpleDateFormat

class RestaurantRequestAdapter (
    val fn: (ViewHolder, Request) -> Unit = { _, _ ->}
) : ListAdapter<Request, RestaurantRequestAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Request>() {
        override fun areItemsTheSame(oldItem: Request, newItem: Request)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Request, newItem: Request) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val requestID : TextView = view.findViewById(R.id.requestID)
        val requestRestaurantID : TextView = view.findViewById(R.id.requestRestaurantID)
        val requestDate : TextView = view.findViewById(R.id.requestDate)
        val requestType : TextView = view.findViewById(R.id.requestType)
        val requestStatus : TextView = view.findViewById(R.id.requestStatus)
        //val layout: ConstraintLayout = view.findViewById(R.id.layoutRequest)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_restaurant_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestItem = getItem(position)


        holder.requestID.text       = requestItem.id
        holder.requestRestaurantID.text       = requestItem.restaurantID
        holder.requestDate.text = SimpleDateFormat("dd-MM-yyyy HH:mm").format(requestItem.dateTime)
        holder.requestType.text = requestItem.requestType
        holder.requestStatus.text = requestItem.status

        when(requestItem.status){
            "Pending" -> holder.requestStatus.setTextColor(Color.parseColor("#F6BE00"))
            //holder.layout.setBackgroundColor(Color.parseColor("#F6BE00"))
            "Approved" -> holder.requestStatus.setTextColor(Color.parseColor("#23BF13"))
                //holder.layout.setBackgroundColor(Color.parseColor("#23BF13"))
            "Rejected" -> holder.requestStatus.setTextColor(Color.RED)
                //holder.layout.setBackgroundColor(Color.RED)

        }

        fn(holder, requestItem)
    }


}