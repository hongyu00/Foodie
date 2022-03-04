package my.com.foodie.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.com.foodie.util.generateID
import java.text.SimpleDateFormat

class ReviewViewModel: ViewModel() {
    private val reviews = MutableLiveData<List<Review>>()
    private var rev = listOf<Review>()
    private var lastID = ""

    private var uID = ""
    private var restID = ""
    private var field = ""
    private var reverse = false

    init{
        viewModelScope.launch {
            val users = USER.get().await().toObjects<User>()
            val restaurants = RESTAURANT.get().await().toObjects<Restaurant>()
            REVIEW.addSnapshotListener { snap, _ -> if(snap == null) return@addSnapshotListener
                rev = snap.toObjects()

                lastID = if(rev.isEmpty()){
                    "RV0000000"
                }else{
                    rev.last().id
                }

                for(review in rev){
                    val user = users.find { u -> u.id == review.customerID }
                    val restaurant = restaurants.find { r -> r.id == review.restaurantID }!!
                    if(user != null){
                        review.user = user
                    }
                    review.restaurant = restaurant
                }

                updateResult()
            }
        }
    }

    private fun updateResult() {
        var list = rev

        list = list.sortedByDescending { r -> r.dateTime }

        if(restID != ""){
            list = list.filter { r -> r.restaurantID == restID}
            restID = ""
        }
        if(uID != ""){
            list = list.filter { r -> r.customerID == uID}
            Log.d("how many", list.size.toString())
            uID = ""
        }

        reviews.value = list
    }

    fun sort(field: String): Boolean {
        if(this.field == field)
            reverse = !reverse
        else
            reverse = false

        this.field = field
        updateResult()

        return reverse
    }

    fun get(id: String): Review?{
        return reviews.value?.find { rv -> rv.id == id }
    }

    fun set(rv: Review){
        REVIEW.document(rv.id).set(rv)
    }
    fun filterByRestaurant(restaurantID: String) {
        restID = restaurantID
        updateResult()
    }
    fun filterByUser(userID: String) {
        uID = userID
        updateResult()
    }
    fun getAll() = reviews

    suspend fun getReviewFromUser(userID: String): Review?{
        return REVIEW.whereEqualTo("customerID", userID).get().await().toObjects<Review>().firstOrNull()
    }
    suspend fun getReviewFromRestaurant(restaurantID: String): Review?{
        return REVIEW.whereEqualTo("restaurantID", restaurantID).get().await().toObjects<Review>().firstOrNull()
    }

    suspend fun getReviewFromUserAndRestaurant(userID: String, restaurantID: String): Review?{
        return REVIEW.whereEqualTo("customerID", userID).whereEqualTo("restaurantID", restaurantID).get().await().toObjects<Review>().firstOrNull()
    }


    fun generateID(): String = generateID(lastID)

}