package my.com.foodie.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentHomeBinding

import my.com.foodie.util.RestaurantForUserAdapter
import my.com.foodie.util.errorDialog


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmReview: ReviewViewModel by activityViewModels()
    private var fusedLocationProvider: FusedLocationProviderClient? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        vmRestaurant.getAll()

        if(!isSearchRestaurant){
            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())
            getUserLastLocation {
                vmRestaurant.filterStatus("Active")
                vmRestaurant.getAllByDistance(geolocation!!)
                vmRestaurant.sortDistanceOnce()
            }
        }

        binding.btnRating.setOnClickListener { sort("rating") }
        binding.btnPrice.setOnClickListener { sort("price") }
        binding.btnDistance.setOnClickListener { sort("distance") }
        binding.imgMyLocation.setOnClickListener {
            isSearchRestaurant = false
            nav.navigate(R.id.homeFragment)
        }
        binding.btnSearchLocation.setOnClickListener {
            val text = binding.location.text.toString()
            var addressList: List<Address>? = null
            val geoCoder = Geocoder(context)
            if(text == ""){
                errorDialog("Please type an address before pressing search button.")
                return@setOnClickListener
            }
            addressList = geoCoder.getFromLocationName(text, 1)
            if(addressList.isEmpty()){
                errorDialog("Cant find address. Please retype.")
            }else{
                val address = addressList!![0]
                userLatitude = address.latitude
                userLongitude = address.longitude

                if(address.locality != null){
                    userCity = address.locality
                }else{
                    userCity = text
                }
                userAddress = address.getAddressLine(0)

            }
            isSearchRestaurant = true
            nav.navigate(R.id.homeFragment)

        }
        binding.restoreBtn.setOnClickListener {
            RESTORE_DATA(requireContext())
            Snackbar.make(requireView(), "Data restored successfully", Snackbar.LENGTH_SHORT).show()
        }
        binding.svRestaurant.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(value: String) = true
            override fun onQueryTextChange(value: String): Boolean {
                vmRestaurant.search(value)
                return true
            }

        })

        binding.spCuisine.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val cuisine = binding.spCuisine.selectedItem.toString()
                vmRestaurant.filterCuisine(cuisine)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }
        binding.location.setText(userAddress)

        if(userLatitude == 0.0){
            geolocation = GeoLocation( 3.1392202495502546, 101.68669097303189)
        }else{
            geolocation = GeoLocation(userLatitude!!, userLongitude!!)
        }


        vmRestaurant.filterStatus("Active")
        vmRestaurant.getAllByDistance(geolocation!!)
        vmRestaurant.sortDistanceOnce()


        val adapter = RestaurantForUserAdapter(){ holder, restaurant ->

            holder.root.setOnClickListener {
                vmRestaurant.updateRestaurantViewCount(restaurant.id, restaurant.viewCount)
                nav.navigate(R.id.restaurantDetailsFragment, bundleOf("id" to restaurant.id))
            }
        }
        binding.rvRestaurant.adapter = adapter

        vmRestaurant.getAll().observe(viewLifecycleOwner) { restaurants ->
            adapter.submitList(restaurants)
        }
        vmReview.getAll()

        return binding.root
    }

    private fun sort(field: String) {
        val reverse = vmRestaurant.sort(field)

        binding.btnRating.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
        binding.btnPrice.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
        binding.btnDistance.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)

        val res = if (reverse) R.drawable.ic_down else R.drawable.ic_up
        when(field){
            "rating" -> binding.btnRating.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,res,0)
            "price" -> binding.btnPrice.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,res,0)
            "distance" -> binding.btnDistance.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,res,0)
        }
    }
    private fun getUserLastLocation( callback: (Location?) -> Unit){
        var loc: Location? = null
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val task = fusedLocationProvider?.lastLocation
            var addressList: List<Address>? = null
            val geoCoder = Geocoder(context)
            lifecycleScope.launch {
                task?.addOnSuccessListener { location ->
                    if(location != null){
                    userLocation = location
                    userLatitude = location.latitude
                    userLongitude = location.longitude
                    //get the city of the user location
                    addressList = geoCoder.getFromLocation(userLatitude!!, userLongitude!!, 1)
                    val address = addressList!![0]
                    val city = address.locality
                    userAddress = address.getAddressLine(0)
                    binding.location.setText(userAddress)

                        if(userLatitude == 0.0){
                            geolocation = GeoLocation( 3.1392202495502546, 101.68669097303189)
                        }else{
                            geolocation = GeoLocation(userLatitude!!, userLongitude!!)
                        }
                        callback.invoke(loc)
                }
                }

            }
        }
    }

}