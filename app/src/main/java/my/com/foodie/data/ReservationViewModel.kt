package my.com.foodie.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.com.foodie.util.generateID
import java.text.SimpleDateFormat
import java.util.*

class ReservationViewModel: ViewModel() {
    private val reservations = MutableLiveData<List<Reservation>>()
    private var res = listOf<Reservation>()
    private var lastID = ""

    private var restID = ""
    private var status = ""
    private var field = ""
    private var reverse = false
    init {
        viewModelScope.launch {
            val users = USER.get().await().toObjects<User>()
            val restaurants = RESTAURANT.get().await().toObjects<Restaurant>()

            RESERVATION.addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                res = snap.toObjects()

                lastID = if(res.isEmpty()){
                    "RS0000000"
                }else{
                    res.last().id
                }

                for(re in res){
                    re.hasEnded = checkStatus(re)
                    val user = users.find { u -> u.id == re.customerID }
                    val restaurant = restaurants.find { r -> r.id == re.restaurantID }!!
                    if(user != null){
                        re.user = user
                    }
                    re.restaurant = restaurant
                }

                updateResult()
            }
        }
    }

    fun checkStatus(reservation: Reservation): Boolean {
        val reservationDate    = SimpleDateFormat("dd-MM-yyyy HH:mm").parse(reservation.date + " " + reservation.time)!!
        return !reservationDate.after(Date())
    }

    private fun updateResult() {
        var list = res

        //TODO change to datetime
        list = list.sortedByDescending { r -> SimpleDateFormat("dd-MM-yyyy HH:mm").parse(r.date + " " + r.time)!! }

        if(currentUser != null){
            if(currentUser!!.role != "User"){
                list = list.filter {
                        r -> r.restaurantID == restID
                        && (status == "All" || status == r.status)
                }
            }
        }



        reservations.value = list
    }

    fun filterByRestaurant(restaurantID: String) {
        restID = restaurantID
        updateResult()
    }

    fun filterByStatus(status: String) {
        this.status = status
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

    fun get(id: String): Reservation?{
        return reservations.value?.find  {rs -> rs.id == id}
    }
    fun set(rs: Reservation){
        RESERVATION.document(rs.id).set(rs)
    }

    fun getAll() = reservations

    suspend fun getReservationsFromUser(userID: String): List<Reservation>{

        val reservations = RESERVATION.whereEqualTo("customerID", userID).get().await().toObjects<Reservation>()

        return reservations
    }

    suspend fun getReservationFromUserAndRestaurant(userID: String, restaurantID: String): List<Reservation>?{
        val reservation = RESERVATION.whereEqualTo("customerID", userID).whereEqualTo("restaurantID", restaurantID).get().await().toObjects<Reservation>()
        return reservation
    }

    suspend fun getReservationFromUserAndRestaurantAndPendingStatus(userID: String, restaurantID: String): Reservation?{
        val reservation = RESERVATION.whereEqualTo("customerID", userID).whereEqualTo("restaurantID", restaurantID).whereEqualTo("status", "Pending").get().await().toObjects<Reservation>().firstOrNull()
        return reservation
    }

    suspend fun getReservationDetails(reservationID: String): Reservation?{
        val reservation = RESERVATION.document(reservationID).get().await().toObject<Reservation>()
        return reservation
    }

    fun updateStatus(id: String, status: String, rejectReason: String){
        RESERVATION.document(id).update("status", status, "rejectReason", rejectReason)
    }

    fun generateID(): String = generateID(lastID)

}