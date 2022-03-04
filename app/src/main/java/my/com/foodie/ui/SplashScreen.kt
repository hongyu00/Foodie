package my.com.foodie.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    private val authVM: AuthViewModel by viewModels()
    var progressDialog: ProgressDialog? = null
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val vmRestaurant: RestaurantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        checkUserRememberMe()

        Handler().postDelayed({
            Log.d("This is onCreate function", "1")
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
            checkLocationPermission()
            Log.d("currentUSeR?", currentUser.toString())
            if(currentUser == null){
                Log.d("This is Handler function", "1")
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }, 1000)

    }

    private fun checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK") { _, _ ->
                        //Prompt the user once dialog has been shown
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,), 1)
                    }
                    .create()
                    .show()
            } else {
                //request the permission.
                Log.d("This is checkLocationPermission function", "1")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,), 1)
            }
        }else{
            Log.d("This is checkLocationPermission function", "1")
            getUserLastLocation {
                    result ->
                var addressList: List<Address>? = null
                val geoCoder = Geocoder(this)
                userLocation = result!!
                userLatitude = result.latitude
                userLongitude = result.longitude
                Log.d("location", userLocation.toString() + "  " + userLatitude + "  " +userLongitude)

                //get the city of the user location
                addressList = geoCoder.getFromLocation(userLatitude!!, userLongitude!!, 1)
                val address = addressList!![0]
                userCity = address.locality
                userAddress = address.getAddressLine(0)
                Log.d("so when do you run here", "now!!")
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (!grantResults.isNotEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission denied..", Toast.LENGTH_LONG).show()
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,), 1)
                }else{
                    //get user current location
                    getUserLastLocation {
                        result ->
                        var addressList: List<Address>? = null
                        val geoCoder = Geocoder(this)
                        userLocation = result!!
                        userLatitude = result.latitude
                        userLongitude = result.longitude
                        Log.d("location", userLocation.toString() + "  " + userLatitude + "  " +userLongitude)

                        //get the city of the user location
                        addressList = geoCoder.getFromLocation(userLatitude!!, userLongitude!!, 1)
                        val address = addressList!![0]
                        userCity = address.locality
                        userAddress = address.getAddressLine(0)
                        Log.d("so when do you run here", "now")
                    }
                }
                Log.d("This is onRequestPermissionsResult function", "1")
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return
            }
        }

    }

    private fun getUserLastLocation( callback: (Location?) -> Unit){
        var loc: Location? = null
        Log.d("This is getUserLastLocation function", "1")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //fusedLocationProvider?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            Log.d("This is success getUserLastLocation function", "2")
            val task = fusedLocationProvider?.lastLocation
            var addressList: List<Address>? = null
            val geoCoder = Geocoder(this)

            //lifecycleScope.launch {
                task?.addOnSuccessListener {
                        location ->
                    Log.d("This is fusedLocationProvider task", "1")

                    if(location != null){
                        loc = location
                        userLocation = location
                        userLatitude = location.latitude
                        userLongitude = location.longitude
                        Log.d("location", userLocation.toString() + "  " + userLatitude + "  " +userLongitude)

                        //get the city of the user location
                        addressList = geoCoder.getFromLocation(userLatitude!!, userLongitude!!, 1)
                        val address = addressList!![0]
                        userCity = address.locality
                        userAddress = address.getAddressLine(0)
                        Log.d("useraddress", userAddress.toString())
                        callback.invoke(loc)
                    }
                }
            //}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }



    private fun checkUserRememberMe() {
        lifecycleScope.launch { authVM.loginFromPreferences(this@SplashScreen) }

        authVM.getUserLiveData().observe(this) { user ->
            Log.d("do you run getUserLiveData", "yes")
            if (user == null) {
                Log.d("this is null", "yes")
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return@observe
            } else {
                currentUser = User(user.id,user.emailAddress,user.password,user.name,user.phoneNumber,user.role,user.birthDate,user.gender,user.userProfile)
                progressDialog = ProgressDialog(this@SplashScreen)
                progressDialog?.setTitle("Loading")
                progressDialog?.setMessage("Please wait a moment")
                progressDialog?.show()

                val role = user.role
                Log.d("role?", role)
                if (role == getString(R.string.user)) {
                    Log.d("This is checkUserRememberMe function into user", "1")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    return@observe
                } else if(role == getString(R.string.restaurant_owner)) {
                    val intent = Intent(this, MainBusinessActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    return@observe
                }else if(role == "Moderator"){
                    val intent = Intent(this, MainModeratorActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    return@observe
                }else{
                    Log.d("This is checkUserRememberMe function", "1")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

        }
    }
}
