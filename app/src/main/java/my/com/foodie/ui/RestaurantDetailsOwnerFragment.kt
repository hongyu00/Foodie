package my.com.foodie.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Blob
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentRestaurantDetailsOwnerBinding
import my.com.foodie.util.*
import java.util.*

class RestaurantDetailsOwnerFragment : Fragment() {

    private lateinit var binding: FragmentRestaurantDetailsOwnerBinding
    private val nav by lazy { findNavController() }
    private val id by lazy { requireArguments().getString("id") ?: "" }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmLocation: LocationViewModel by activityViewModels()
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var imageFor: String = ""
    private var restaurant: Restaurant? = null

    private var location: Location? = null

    private var name = ""
    private var cuisine = ""
    private var priceRange = ""
    private var address = ""
    private var contactNo = ""
    private var operatingHour = ""
    private var description = ""
    private lateinit var logo: Blob
    private lateinit var surroundingImg: Blob
    private var hasReservation: Boolean = false

    var latitude = 0.0
    var longitude = 0.0
    var loc = ""
    var city = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRestaurantDetailsOwnerBinding.inflate(inflater, container, false)

        binding.switchReservation.isVisible = currentUser!!.id != "Moderator"

        restaurant = vmRestaurant.getTempRestaurant()
        Log.d("restaurant is", restaurant.toString())
        initializeValue()
        location = vmLocation.getTempLocation()

        if(location?.location != ""){
            Log.d("location", location?.location ?: "nothing")
            Log.d("city", location?.city ?: "nothing")
            binding.ownerEditAddress.text = location!!.location
        }
        if(galleryPhoto1 != null || cameraPhoto1 != null){
            if(galleryPhoto1 != null){
                binding.ownerEditLogo.setImageURI(galleryPhoto1)
            }
            else{
                binding.ownerEditLogo.setImageBitmap(cameraPhoto1)
            }
        }
        if(galleryPhoto2 != null || cameraPhoto2 != null){
            if(galleryPhoto2 != null){
                binding.ownerEditSurrImg.setImageURI(galleryPhoto2)
            }
            else{
                binding.ownerEditSurrImg.setImageBitmap(cameraPhoto2)
            }
        }

        binding.ownerEditLogo.setOnClickListener {
            image = binding.ownerEditLogo.cropToBlob(300,300).toBitmap()
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")
        }
        binding.ownerEditSurrImg.setOnClickListener {
            image = binding.ownerEditSurrImg.cropToBlob(300,300).toBitmap()
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")
        }
        binding.imgChangeLogo.setOnClickListener { addPhoto("logo") }
        binding.imgChangeSurrImg.setOnClickListener { addPhoto("surrImg") }
        binding.imgReturn14.setOnClickListener {
            reset()
            nav.navigateUp()
        }
        binding.btnEditLocation.setOnClickListener {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireContext() as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            }else{
                //store value
                val args = bundleOf("location" to restaurant!!.location)
                nav.navigate(R.id.mapsFragment, args)
            }
        }
        binding.btnDone.setOnClickListener { verifyDetails() }

        return binding.root
    }


    private fun verifyDetails() {
        name = binding.ownerEditName.text.toString()
        cuisine = binding.spinnerCuisine.selectedItem.toString()
        priceRange = binding.ownerEditPriceRange.text.toString()
        address = binding.ownerEditAddress.text.toString()
        contactNo = binding.ownerEditContactNo.text.toString()
        operatingHour = binding.ownerEditOperatingHour.text.toString()
        description = binding.ownerEditDescription.text.toString()
        logo = binding.ownerEditLogo.cropToBlob(300,300)
        surroundingImg = binding.ownerEditSurrImg.cropToBlob(300,300)
        hasReservation = binding.switchReservation.isChecked

        if(name == "" || priceRange == "" || address == "" || contactNo == "" || operatingHour == "" || description == ""){
            errorDialog("Please fill up all the required information!")
            return
        }
        if(cuisine == "Cuisine"){
            errorDialog("Please choose a cuisine for the restaurant!")
            return
        }
        if(logo.toBytes().isEmpty()){
            errorDialog("Please provide a logo of your restaurant!")
            return
        }
        if(surroundingImg.toBytes().isEmpty()){
            errorDialog("Please provide a surrounding image of your restaurant!")
            return
        }
        if(!(contactNo.length == 10 || contactNo.length == 11 || contactNo.isEmpty())){
            errorDialog("Contact number length is wrong!")
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Edit Restaurant")
            .setMessage("Please ensure that the information you provided is accurate. Are you sure you want to edit your restaurant?" )
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Yes") { dialog, whichButton -> submit() }
            .setNegativeButton("No", null).show()

    }

    private fun submit() {
        var newLocation = ""
        var newCities = ""
        var newLatitude = 0.0
        var newLongitude = 0.0

        if(vmLocation.getTempLocation().location != ""){
            newLocation = vmLocation.getTempLocation().location
            newCities = vmLocation.getTempLocation().city
            newLatitude = vmLocation.getTempLocation().latitude
            newLongitude = vmLocation.getTempLocation().longitude
        }else{
            newLocation = loc
            newCities = city
            newLatitude = latitude
            newLongitude = longitude
        }


        val restaurant = Restaurant(
            id = restaurant!!.id,
            name = name,
            location = newLocation,
            cities = newCities,
            latitude = newLatitude,
            longitude = newLongitude,
            cuisine = cuisine,
            restaurantImg = logo,
            restaurantSurrImg = surroundingImg,
            contactNo = contactNo,
            operatingHour = operatingHour,
            priceRange = priceRange.toInt(),
            description = description,
            gotReservation = hasReservation,
            reportCount = restaurant!!.reportCount,
            reviewCount = restaurant!!.reviewCount,
            totalRating = restaurant!!.totalRating,
            //avgReview = 0.0F,
            status = restaurant!!.status,
            dateCreated = restaurant!!.dateCreated,
            viewCount = 0,
            ownerID = restaurant!!.ownerID
        )
        vmRestaurant.set(restaurant)
        reset()
        Toast.makeText(context, "Restaurant Edited Successfully", Toast.LENGTH_LONG).show()
        nav.navigateUp()

    }

    private fun initializeValue() {
        val spn = binding.spinnerCuisine.adapter.count-1
        for (a in 0..spn) {
            if(binding.spinnerCuisine.getItemAtPosition(a) == restaurant!!.cuisine){
                binding.spinnerCuisine.setSelection(a)
                break
            }
        }

        binding.ownerEditLogo.setImageBitmap(restaurant!!.restaurantImg?.toBitmap())
        binding.ownerEditSurrImg.setImageBitmap(restaurant!!.restaurantSurrImg?.toBitmap())
        binding.ownerEditName.setText(restaurant!!.name)
        binding.ownerEditAddress.setText(restaurant!!.location)
        //vmLocation.insertLocation(restaurant!!.location, restaurant!!.cities, LatLng(restaurant!!.latitude, restaurant!!.longitude))
        binding.ownerEditContactNo.setText(restaurant?.contactNo ?: "")
        binding.ownerEditOperatingHour.setText(restaurant?.operatingHour ?: "")
        binding.ownerEditPriceRange.setText(restaurant!!.priceRange.toString())
        binding.ownerEditDescription.setText(restaurant?.description ?: "")
        binding.switchReservation.isChecked = restaurant!!.gotReservation

        latitude = restaurant!!.latitude
        longitude = restaurant!!.longitude
        loc = restaurant!!.location
        city = restaurant!!.cities

    }

    private fun addPhoto(field: String) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        else {
            imageFor = field
            showPhotoSelection("Change Picture",
                { cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE)) }, {
                    val photoIntent =  Intent(Intent.ACTION_GET_CONTENT)
                    photoIntent.type = "image/*"
                    photoLauncher.launch(photoIntent)})
        }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_CANCELED) {
            val thumbnail = it.data!!.extras!!["data"] as Bitmap?
            if(imageFor == "logo"){
                binding.ownerEditLogo.setImageBitmap(thumbnail)
                cameraPhoto1 = thumbnail
                galleryPhoto1 = null
            } else{
                binding.ownerEditSurrImg.setImageBitmap(thumbnail)
                cameraPhoto2 = thumbnail
                galleryPhoto2 = null
            }
        }
    }
    private val photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data != null) {
            val photoURI: Uri? = it.data!!.data
            if(imageFor == "logo"){
                binding.ownerEditLogo.setImageURI(photoURI)
                cameraPhoto1 = null
                galleryPhoto1 = photoURI
            } else{
                binding.ownerEditSurrImg.setImageURI(photoURI)
                cameraPhoto2 = null
                galleryPhoto2 = photoURI
            }

        }
    }
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (!isGranted) snackbar("Please allow camera and file permission before proceed to change profile picture!")
    }

    private fun reset(){
        cameraPhoto1 = null
        cameraPhoto2 = null
        galleryPhoto1 = null
        galleryPhoto2 = null
        vmLocation.insertLocation("", "", LatLng(0.0,0.0))
        vmRestaurant.setTempRestaurant(Restaurant())
    }
}