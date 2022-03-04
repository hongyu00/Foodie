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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.launch
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentOwnerAddBusinessBinding
import java.util.*
import my.com.foodie.R
import my.com.foodie.util.*


class OwnerAddBusinessFragment : Fragment() {

    private lateinit var binding: FragmentOwnerAddBusinessBinding
    private val nav by lazy { findNavController() }
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val id by lazy{ arguments?.getString("id", "")}

    private val vmLocation: LocationViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmRequest: RequestViewModel by activityViewModels()
    private var imageFor: String = ""

    private var name = ""
    private var cuisine = ""
    private var priceRange = ""
    private var address = ""
    private var contactNo = ""
    private var operatingHour = ""
    private var description = ""
    private var optionalDescription = ""
    private lateinit var logo: Blob
    private lateinit var surroundingImg: Blob
    private lateinit var ssmCertificate: Blob

    private var oldRequest: Request? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnerAddBusinessBinding.inflate(inflater, container, false)

        if(currentUser!!.role == "Moderator"){
            binding.textView21.text = "Please fill in the restaurant details"
            binding.textView83.isVisible = false
            binding.textView85.isVisible = false
            binding.textView44.isVisible = false
            binding.ownerAddRestaurantSSM.isVisible = false
            binding.ownerAddRestaurantRequestDescription.isVisible = false

        }



        lifecycleScope.launch {
            if(id != null){
                val restaurant = vmRestaurant.getRestaurantFromID(id!!)!!
                oldRequest = vmRequest.getRestaurantRequestFromUser(currentUser!!.id)
                //initialize value
                val spn = binding.ownerAddRestaurantCuisine.adapter.count-1
                for (a in 0..spn) {
                    if(binding.ownerAddRestaurantCuisine.getItemAtPosition(a) == restaurant.cuisine){
                        binding.ownerAddRestaurantCuisine.setSelection(a)
                        break
                    }
                }
                binding.ownerAddRestaurantName.setText(restaurant.name)
                binding.ownerAddRestaurantPriceRange.setText(restaurant.priceRange.toString())
                binding.ownerAddRestaurantAddress.setText(restaurant.location)
                binding.ownerAddRestaurantContactNo.setText(restaurant.contactNo)
                binding.ownerAddRestaurantOperatingHour.setText(restaurant.operatingHour)
                binding.ownerAddRestaurantDescription.setText(restaurant.description)
                binding.ownerAddRestaurantLogo.setImageBitmap(restaurant.restaurantImg?.toBitmap())
                binding.ownerAddRestaurantSurroundingImage.setImageBitmap(restaurant.restaurantSurrImg?.toBitmap())

                vmLocation.insertLocation(restaurant.location,restaurant.cities, LatLng(restaurant.latitude, restaurant.longitude))

            }
            return@launch
        }

        vmLocation.getTempLocation()
        if(vmLocation.location.location != ""){
            binding.ownerAddRestaurantAddress.text = vmLocation.location.location
        }
        if(galleryPhoto1 != null || cameraPhoto1 != null){
            if(galleryPhoto1 != null){
                binding.ownerAddRestaurantLogo.setImageURI(galleryPhoto1)
            }
            else{
                binding.ownerAddRestaurantLogo.setImageBitmap(cameraPhoto1)
            }
        }
        if(galleryPhoto2 != null || cameraPhoto2 != null){
            if(galleryPhoto2 != null){
                binding.ownerAddRestaurantSurroundingImage.setImageURI(galleryPhoto2)
            }
            else{
                binding.ownerAddRestaurantSurroundingImage.setImageBitmap(cameraPhoto2)
            }
        }
        if(galleryPhoto3 != null || cameraPhoto3 != null){
            if(galleryPhoto3 != null){
                binding.ownerAddRestaurantSSM.setImageURI(galleryPhoto3)
            }
            else{
                binding.ownerAddRestaurantSSM.setImageBitmap(cameraPhoto3)
            }
        }

        lifecycleScope.launch {
            vmRequest.getAll().observe(viewLifecycleOwner) {}
            vmRestaurant.getAll().observe(viewLifecycleOwner) {}
            val request = vmRequest.getRestaurantRequestFromUser(currentUser!!.id)
            Log.d("Check request", request.toString())
            if(request != null && request.status == "Pending"){
                errorDialog("You have submitted a request earlier!\n\nRequest related to ${request.requestType}!\n\nPlease wait until the request to be reviewed only you can submit another request!")
                claimRestaurantID = ""
                userToOwnerAddRestaurant = false
                nav.navigateUp()
                return@launch
            }
            val restaurant = vmRestaurant.getRestaurantIDFromUser(currentUser!!.id)
            if(restaurant != null){
                errorDialog("You have an existing business! Please create a new account if you want to add a new business!")
                claimRestaurantID = ""
                userToOwnerAddRestaurant = false
                nav.navigateUp()
                return@launch
            }
        }

        binding.btnReturn2.setOnClickListener {
            userToOwnerAddRestaurant = false
            nav.navigateUp()
        }
        binding.btnOwnerAddRestaurantSelectLocation.setOnClickListener {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireContext() as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            }else{
                val args = bundleOf("location" to "")
                nav.navigate(R.id.mapsFragment, args)
            }
        }
        binding.ownerAddRestaurantLogo.setOnClickListener { addPhoto("logo") }
        binding.ownerAddRestaurantSurroundingImage.setOnClickListener { addPhoto("surrImg") }
        binding.ownerAddRestaurantSSM.setOnClickListener { addPhoto("SSM") }
        binding.btnOwnerAddRestaurantSubmit.setOnClickListener { verifyDetails() }

        return binding.root
    }

    override fun onDestroyView() {
        userToOwnerAddRestaurant = false
        super.onDestroyView()
    }

    private fun verifyDetails() {
         name = binding.ownerAddRestaurantName.text.toString()
         cuisine = binding.ownerAddRestaurantCuisine.selectedItem.toString()
         priceRange = binding.ownerAddRestaurantPriceRange.text.toString()
         address = binding.ownerAddRestaurantAddress.text.toString()
         contactNo = binding.ownerAddRestaurantContactNo.text.toString()
         operatingHour = binding.ownerAddRestaurantOperatingHour.text.toString()
         description = binding.ownerAddRestaurantDescription.text.toString()
         logo = binding.ownerAddRestaurantLogo.cropToBlob(300,300)
         surroundingImg = binding.ownerAddRestaurantSurroundingImage.cropToBlob(300,300)
         ssmCertificate = binding.ownerAddRestaurantSSM.cropToBlob(300,300)
         optionalDescription = binding.ownerAddRestaurantRequestDescription.text.toString()
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

        if(currentUser!!.role == "Restaurant Owner"){
            if(ssmCertificate.toBytes().isEmpty()){
                errorDialog("Please provide your SSM Certificate to prove that you are the owner!")
                return
            }
        }


        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Submit Restaurant")
            .setMessage("Please ensure that the information you provided is accurate. Are you sure you want to submit?" )
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Yes") { dialog, whichButton -> submit() }
            .setNegativeButton("No", null).show()

    }

    private fun submit() {
        if(currentUser!!.role == "Restaurant Owner"){

        val restaurantID = vmRestaurant.generateID()
        val restaurant = Restaurant(
            id = restaurantID,
            name = name,
            location = vmLocation.location.location,
            cities = vmLocation.location.city,
            latitude = vmLocation.location.latitude,
            longitude = vmLocation.location.longitude,
            cuisine = cuisine,
            restaurantImg = logo,
            restaurantSurrImg = surroundingImg,
            contactNo = contactNo,
            operatingHour = operatingHour,
            priceRange = priceRange.toInt(),
            description = description,
            gotReservation = false,
            reportCount = 0,
            reviewCount = 0,
            totalRating = 0.0F,
            //avgReview = 0.0F,
            status = "Inactive",
            dateCreated = Date(),
            viewCount = 0,
            ownerID = ""
        )

        val request = Request(
            id = vmRequest.generateID(),
            dateTime = Date(),
            requestType = "Add Restaurant(Owner)",
            description = optionalDescription,
            image = ssmCertificate,
            status = "Pending",
            rejectReason = "",
            customerID = currentUser!!.id,
            restaurantID = restaurantID,
            moderatorID = ""
        )

        if(oldRequest != null){
            REQUEST.document(oldRequest!!.id).delete()
        }
        vmRequest.set(request)
        vmRestaurant.set(restaurant)
        reset()
            nav.navigateUp()
            successDialog("Add Restaurant Request Submitted","It will take 1-3days for the moderator to approve or reject the request. Once it is done, you will receive an email regarding your request. Thanks." )
        //Toast.makeText(context, "Add Restaurant Request Submitted. It will take 1-3days for the moderator to approve or reject the request. Once it is done, you will receive an email regarding your request. Thanks.", Toast.LENGTH_LONG).show()

        }else{
            val restaurant = Restaurant(
                id = vmRestaurant.generateID(),
                name = name,
                location = vmLocation.location.location,
                cities = vmLocation.location.city,
                latitude = vmLocation.location.latitude,
                longitude = vmLocation.location.longitude,
                cuisine = cuisine,
                restaurantImg = logo,
                restaurantSurrImg = surroundingImg,
                contactNo = contactNo,
                operatingHour = operatingHour,
                priceRange = priceRange.toInt(),
                description = description,
                gotReservation = false,
                reportCount = 0,
                reviewCount = 0,
                totalRating = 0.0F,
                //avgReview = 0.0F,
                status = "Active",
                dateCreated = Date(),
                viewCount = 0,
                ownerID = ""
            )
            vmRestaurant.set(restaurant)
            reset()
            Toast.makeText(context, "Add Restaurant Successfully.", Toast.LENGTH_LONG).show()
            nav.navigateUp()
        }
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
                binding.ownerAddRestaurantLogo.setImageBitmap(thumbnail)
                cameraPhoto1 = thumbnail
                galleryPhoto1 = null
            }else if(imageFor == "SSM"){
                binding.ownerAddRestaurantSSM.setImageBitmap(thumbnail)
                cameraPhoto3 = thumbnail
                galleryPhoto3 = null
            }
            else{
                binding.ownerAddRestaurantSurroundingImage.setImageBitmap(thumbnail)
                cameraPhoto2 = thumbnail
                galleryPhoto2 = null
            }
        }
    }
    private val photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data != null) {
            val photoURI: Uri? = it.data!!.data
            if(imageFor == "logo"){
                binding.ownerAddRestaurantLogo.setImageURI(photoURI)
                cameraPhoto1 = null
                galleryPhoto1 = photoURI
            }else if(imageFor == "SSM"){
                binding.ownerAddRestaurantSSM.setImageURI(photoURI)
                cameraPhoto3 = null
                galleryPhoto3 = photoURI
            }
            else{
                binding.ownerAddRestaurantSurroundingImage.setImageURI(photoURI)
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
        galleryPhoto3 = null
        galleryPhoto3 = null
        vmLocation.insertLocation("", "", LatLng(0.0,0.0))
        userToOwnerAddRestaurant = false

    }
}