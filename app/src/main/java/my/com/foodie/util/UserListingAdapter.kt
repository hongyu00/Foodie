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
import my.com.foodie.data.User
import java.text.SimpleDateFormat

class UserListingAdapter (
    val fn: (ViewHolder, User) -> Unit = { _, _ ->}
) : ListAdapter<User, UserListingAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User)    = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val userID : TextView = view.findViewById(R.id.userID)
        val userName : TextView = view.findViewById(R.id.userName)
        val userRole : TextView = view.findViewById(R.id.userRole)
        val userImg : ImageView = view.findViewById(R.id.userImg)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userItem = getItem(position)

        holder.userID.text = userItem.id
        holder.userName.text = userItem.name
        holder.userRole.text = userItem.role
        holder.userImg.setImageBitmap(userItem.userProfile?.toBitmap())

        fn(holder, userItem)
    }
}