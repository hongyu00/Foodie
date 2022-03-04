package my.com.foodie.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.com.foodie.util.generateID

class RestaurantViewModel: ViewModel() {

    private val restaurant = MutableLiveData<List<Restaurant>>()
    private var rest = listOf<Restaurant>()
    private var lastID = ""
    private var tempRestaurant = Restaurant()

    private var report = 0

    private var name = ""
    private var status = ""
    private var cuisine = ""
    private var field = ""
    private var reverse = false
    
    init {
        viewModelScope.launch {
            val reservations = RESERVATION.get().await().toObjects<Reservation>()
            RESTAURANT.addSnapshotListener { snap, _ -> if(snap == null) return@addSnapshotListener
                rest = snap.toObjects()

                lastID = if(rest.isEmpty()){
                    "R0000000"
                }else{
                    rest.last().id
                }

                for(r in rest){
                    if(r.reviewCount != 0){
                        r.avgRating = r.totalRating/r.reviewCount
                    }else{
                        r.avgRating = 0.0F
                    }
                }

                if(userLocation != null){
                    for(r in rest){
                        val restaurantLocation = GeoLocation(r.latitude, r.longitude)
                        val distanceMetre = GeoFireUtils.getDistanceBetween(restaurantLocation, GeoLocation(userLatitude!!, userLongitude!!))
                        r.distance = distanceMetre / 1000.0
                    }
                }
                for(r in rest){
                    val count = reservations.filter { re -> re.restaurantID == r.id }
                    r.reservationCount = count.size
                }

                updateResult()
            }
        }
    }

    private fun updateResult() {
        var list = rest
            list = list.filter {
                    r -> r.name.contains(name, true)
                    &&(cuisine == "Cuisine" || cuisine == r.cuisine)
                    && (status == "All" || status == r.status)
            }


//        if(report != 0){
//            list = list.filter { r ->(r.reportCount >= report) }
//            report = 0
//        }

        list = when(field){
            "rating" -> list.sortedBy { r -> r.avgRating }
            "price" -> list.sortedBy { r -> r.priceRange }
            "distance" -> list.sortedBy { r -> r.distance }
            "report" -> list.sortedBy { r -> r.reportCount }
            else -> list
        }

        if(reverse){
            list = list.reversed()
        }
        restaurant.value = list
    }

    fun search(name: String) {
        this.name = name
        updateResult()
    }

    fun filterByReportCount(report: Int) {
        this.report = report
        updateResult()
    }

    fun filterCuisine(cuisine: String) {
        this.cuisine = cuisine
        updateResult()
    }

    fun filterStatus(status: String) {
        this.status = status
        updateResult()
    }
    fun sortDistanceOnce(){
        field = "distance"
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
    fun get(id: String): Restaurant?{
        return restaurant.value?.find { r -> r.id == id }
    }
    fun set(r: Restaurant){
        RESTAURANT.document(r.id).set(r)
    }

    fun getAll() = restaurant

    fun getAllByDistance(userlocation: GeoLocation){
        for(r in rest){
            val restaurantLocation = GeoLocation(r.latitude, r.longitude)
            val distanceMetre = GeoFireUtils.getDistanceBetween(restaurantLocation, userlocation)
            r.distance = distanceMetre / 1000.0
        }

        updateResult()
    }

    suspend fun getRestaurantIDFromUser(userID: String): Restaurant?{
        val restaurant = RESTAURANT.whereEqualTo("ownerID", userID).get().await().toObjects<Restaurant>().firstOrNull()
        return restaurant
    }

    suspend fun getRestaurantFromID(id: String): Restaurant?{
        val restaurant = RESTAURANT.document(id).get().await().toObject<Restaurant>()
        return restaurant
    }


    fun updateReportCount(id: String, count: Int){
        RESTAURANT.document(id).update("reportCount",count)
    }


    fun updateRestaurantExistingRating(id: String,oldRating: Float, newRating: Float){
        val restaurant = get(id)
        RESTAURANT.document(id).update("totalRating", (restaurant!!.totalRating-oldRating+newRating))
    }
    fun updateRestaurantTotalRating(id: String, rating: Float){
        val restaurant = get(id)
        RESTAURANT.document(id).update("totalRating", (restaurant!!.totalRating+rating))
    }
    fun updateRestaurantReviewCount(id: String){
        val restaurant = get(id)
        RESTAURANT.document(id).update("reviewCount", (restaurant!!.reviewCount+1))
    }
    fun updateRestaurantStatus(id: String, status: String){
        RESTAURANT.document(id).update("status", status)
    }
    fun updateRestaurantOwner(id: String, ownerID: String){
        RESTAURANT.document(id).update("ownerID", ownerID)
    }
    fun updateRestaurantViewCount(id: String, viewCount: Int){
        RESTAURANT.document(id).update("viewCount", viewCount+1)
    }
    fun generateID(): String = generateID(lastID)

    fun setTempRestaurant(restaurant: Restaurant){
        this.tempRestaurant = restaurant
    }

    fun getTempRestaurant(): Restaurant{
        return  tempRestaurant
    }


}