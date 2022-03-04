package my.com.foodie.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }
    private val vmRestaurant: RestaurantViewModel by viewModels()
    private var fusedLocationProvider: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
        vmRestaurant.getAll()

        if(userLatitude == 0.0){
            geolocation = GeoLocation( 3.1392202495502546, 101.68669097303189)
        }else{
            geolocation = GeoLocation(userLatitude!!, userLongitude!!)
        }
        vmRestaurant.getAllByDistance(geolocation!!)

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val appBarConfig = AppBarConfiguration(setOf(
            R.id.homeFragment,
            R.id.decisionFragment,
            R.id.reservationFragment,
            R.id.profileFragment
        ))

        bottomNavView.setupWithNavController(nav)



        //business

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        val role = "User"
        if(role == "User"){
            inflater.inflate(R.menu.bottom_nav_menu, menu)
        }else{
            inflater.inflate(R.menu.bottom_nav_menu_business, menu)
        }


        return true
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
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,), 1)
            }
        }else{
            getUserLastLocation()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (!grantResults.isNotEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,), 1)
                }else{
                    getUserLastLocation()
                }

                return
            }
        }

    }
    private fun getUserLastLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val task = fusedLocationProvider?.lastLocation
            var addressList: List<Address>? = null
            val geoCoder = Geocoder(this)

            lifecycleScope.launch {
                task?.addOnSuccessListener {
                        location ->
                    if(location != null){
                        userLocation = location
                        userLatitude = location.latitude
                        userLongitude = location.longitude

                        addressList = geoCoder.getFromLocation(userLatitude!!, userLongitude!!, 1)
                        val address = addressList!![0]
                        userCity = address.locality
                        userAddress = address.getAddressLine(0)
                    }
                }
            }
        }
    }
}