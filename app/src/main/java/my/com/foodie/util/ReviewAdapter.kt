package my.com.foodie.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import my.com.foodie.R
import my.com.foodie.data.Review
import java.text.SimpleDateFormat


class ReviewAdapter(
    val fn: (ViewHolder, Review) -> Unit = { _, _ ->}
) : ListAdapter<Review, ReviewAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val userRating : RatingBar = view.findViewById(R.id.restDetailsRatingBar)
        val userName : TextView = view.findViewById(R.id.txtReviewerName)
        val userDate : TextView = view.findViewById(R.id.txtReviewerDate)
        val userDescription : TextView = view.findViewById(R.id.txtReviewerDescription)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_restaurant_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewItem = getItem(position)
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        var name = reviewItem.user.name
        if(reviewItem.isAnonymous){
            name = name.replaceRange(1 until name.length,"*******")
        }
        holder.userRating.rating = reviewItem.star
        holder.userName.text = name
        holder.userDate.text = sdf.format(reviewItem.dateTime)
        holder.userDescription.text =  reviewItem.description

        fn(holder, reviewItem)
    }
}