package my.com.foodie.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.com.foodie.util.generateID

class RequestViewModel: ViewModel() {

    private val requests = MutableLiveData<List<Request>>()
    private var req = listOf<Request>()
    private var lastID = ""

    private var requestType = ""
    private var requestStatus = ""
    private var field = ""
    private var reverse = false

    init{
        viewModelScope.launch {
            val users = USER.get().await().toObjects<User>()
            val restaurants = RESTAURANT.get().await().toObjects<Restaurant>()

            REQUEST.addSnapshotListener { snap, _ -> if(snap == null) return@addSnapshotListener
                req = snap.toObjects()

                lastID = if(req.isEmpty()){
                    "RQ0000000"
                }else{
                    req.last().id
                }
                for(r in req){
                    val user = users.find { u -> u.id == r.customerID }
                    val restaurant = restaurants.find { res -> res.id == r.restaurantID }
                    if(restaurant != null){
                        r.restaurant = restaurant
                    }
                    if(user != null){
                        r.user = user
                    }
                }
                updateResult()
            }
        }
    }

    private fun updateResult() {
        var list = req

        list = list.filter{
            r -> (requestType == "All" || requestType == r.requestType)
                && (requestStatus == "All" || requestStatus == r.status)
        }

        list = when(field){
            "date" -> list.sortedBy { r -> r.dateTime }
            "restaurantID" -> list.sortedBy { r -> r.restaurantID }
            else -> list
        }

        if(reverse){
            list = list.reversed()
        }

        requests.value = list
    }

    fun get(id: String): Request?{
        return requests.value?.find { rq -> rq.id == id }
    }
    fun set(rq: Request){
        REQUEST.document(rq.id).set(rq)
    }

    fun filterRequestStatus(status: String) {
        requestStatus = status
        updateResult()
    }

    fun filterRequestType(requestType: String) {
        this.requestType = requestType
        updateResult()
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

    fun getAll() = requests


    suspend fun getRequestFromSameRestaurant(restaurantID: String): List<Request> {
        val requests = REQUEST.whereEqualTo("restaurantID", restaurantID).get().await().toObjects<Request>()
        Log.d("no?", requests.size.toString())
        return requests
    }

    suspend fun getRestaurantRequestFromUser(userID: String): Request?{
        val request = REQUEST.whereEqualTo("customerID", userID).get().await().toObjects<Request>().firstOrNull()
        return request
    }

    fun updateRequestStatus(id: String, status: String, rejectReason: String, moderatorID: String){
        REQUEST.document(id).update("status", status, "rejectReason", rejectReason, "moderatorID", moderatorID)
    }

    fun generateID(): String = generateID(lastID)
}