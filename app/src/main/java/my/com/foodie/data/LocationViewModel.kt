package my.com.foodie.data

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationViewModel: ViewModel() {
    var location = Location()

    fun insertLocation(address: String, city: String, latlng: LatLng){
        location = Location(address, city, latlng.latitude, latlng.longitude)
    }

    fun getTempLocation(): Location{
        return location
    }

}