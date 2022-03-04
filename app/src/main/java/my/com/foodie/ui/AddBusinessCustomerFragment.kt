package my.com.foodie.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentAddBusinessBinding
import my.com.foodie.databinding.FragmentAddBusinessCustomerBinding
import my.com.foodie.util.errorDialog
import my.com.foodie.util.successDialog
import my.com.foodie.util.toBlob
import java.util.*

class AddBusinessCustomerFragment : Fragment() {

    private lateinit var binding: FragmentAddBusinessCustomerBinding
    private val nav by lazy { findNavController() }
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val vmLocation: LocationViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmRequest: RequestViewModel by activityViewModels()


    private var name = ""
    private var priceRange = ""
    private var address = ""
    private var contactNo = ""
    private var operatingHour = ""
    private var cuisine = ""

    override fun onDestroyView() {
        reset()
        super.onDestroyView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddBusinessCustomerBinding.inflate(inflater, container, false)

        vmLocation.getTempLocation()
        if(vmLocation.location.location != ""){
            binding.lblAddress.text = vmLocation.location.location
        }
        binding.btnSubmitAddRestaurant.setOnClickListener { verifyDetails() }
        binding.btnReturn.setOnClickListener {
            reset()
            nav.navigateUp()
        }
        binding.btnAddress.setOnClickListener {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireContext() as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            }else{
                val args = bundleOf("location" to "")
                nav.navigate(R.id.mapsFragment, args)
            }
        }
        lifecycleScope.launch {
            vmRequest.getAll().observe(viewLifecycleOwner) {}
        }

        return binding.root
    }

    private fun verifyDetails() {
         name = binding.newRestaurantName.text.toString().trim()
         priceRange = binding.newRestaurantPriceRange.text.toString().trim()
         address = binding.lblAddress.text.toString().trim()
         contactNo = binding.newRetaurantContact.text.toString().trim()
         operatingHour = binding.newRestaurantOperatingHour.text.toString().trim()
         cuisine = binding.spnCuisine.selectedItem.toString()

        if(name == "" || priceRange == "" || address == "" ){
            errorDialog("Please fill up all the required information!")
            return
        }

        if(cuisine == "Cuisine"){
            errorDialog("Please choose a cuisine for the restaurant!")
            return
        }

        if(priceRange.contains(".")){
            errorDialog("Enter a complete value without the cents behind!\nE.g. 10, 15, 20")
            return
        }

        if(!(contactNo.length == 10 || contactNo.length == 11 || contactNo.isEmpty())){
            errorDialog("Contact number length is wrong!")
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Add New Restaurant")
            .setMessage("Please ensure that the information you provided is accurate. Are you sure you want to submit?" )
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Yes") { dialog, whichButton -> submit() }
            .setNegativeButton("No", null).show()

    }

    private fun submit() {
            val restaurantID = vmRestaurant.generateID()
            val restaurant = Restaurant(
                id = restaurantID,
                name = name,
                location = address,
                cities = vmLocation.location.city,
                latitude = vmLocation.location.latitude,
                longitude = vmLocation.location.longitude,
                cuisine = cuisine,
                restaurantImg = ContextCompat.getDrawable(requireContext(), R.drawable.logo)!!.toBitmap().toBlob(),
                restaurantSurrImg = null,
                contactNo = contactNo,
                operatingHour = operatingHour,
                priceRange = priceRange.toInt(),
                description = "",
                gotReservation = false,
                reportCount = 0,
                reviewCount = 0,
                totalRating = 0.0F,
                status = "Inactive",
                dateCreated = Date(),
                viewCount = 0,
                ownerID = ""
            )

            val request = Request(
                id = vmRequest.generateID(),
                dateTime = Date(),
                requestType = "Add Restaurant(User)",
                description = "",
                image = null,
                status = "Pending",
                rejectReason = "",
                customerID = currentUser!!.id,
                restaurantID = restaurantID,
                moderatorID = ""
            )

            vmRequest.set(request)
            vmRestaurant.set(restaurant)
            reset()
            nav.navigateUp()
            successDialog("Add Restaurant Request Submitted", "It will take 1-3days for the moderator to approve or reject the request. Once it is done, you will receive an email regarding your request. Thanks.")


    }

    private fun reset() {
        binding.newRestaurantName.text.clear()
        binding.newRestaurantPriceRange.text.clear()
        binding.lblAddress.text = ""
        binding.newRetaurantContact.text.clear()
        binding.newRestaurantOperatingHour.text.clear()
        binding.spnCuisine.setSelection(0)
        vmLocation.insertLocation("", "", LatLng(0.0,0.0))
    }


}