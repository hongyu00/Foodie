package my.com.foodie.ui

import android.Manifest
import android.app.Activity
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentOwnerClaimBusinessBinding
import my.com.foodie.util.*
import java.util.*


class OwnerClaimBusinessFragment : Fragment() {


    private lateinit var binding: FragmentOwnerClaimBusinessBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmRequest: RequestViewModel by activityViewModels()

    private var r = Restaurant()
    private var oldRequest: Request? = null
    private var hasRestaurant: Restaurant? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnerClaimBusinessBinding.inflate(inflater, container, false)

        lifecycleScope.launch {

            vmRequest.getAll().observe(viewLifecycleOwner) {}
            vmRestaurant.getAll().observe(viewLifecycleOwner) {}
            hasRestaurant = vmRestaurant.getRestaurantIDFromUser(currentUser!!.id)
            oldRequest = vmRequest.getRestaurantRequestFromUser(currentUser!!.id)
            Log.d("Check request", oldRequest.toString())
            if(oldRequest != null){
                errorDialog("You have submitted a request earlier!\n\nRequest related to ${oldRequest!!.requestType}!\n\nPlease wait until the request to be reviewed only you can submit another request!")
                claimRestaurantID = ""
                nav.navigateUp()
                return@launch
            }
            if(hasRestaurant != null){
                errorDialog("You have an ongoing business already! If you wish to claim the business, kindly create a new owner account to claim it. Thanks.")
                claimRestaurantID = ""
                nav.navigateUp()
                return@launch
            }
        }


        if(claimRestaurantID != ""){
            Log.d("restaurant id", claimRestaurantID.toString())
            r = vmRestaurant.get(claimRestaurantID!!) ?: Restaurant()
        }

        binding.imgReturn19.setOnClickListener {
            claimRestaurantID = ""
            nav.navigateUp()
        }
        binding.imgSSM.setOnClickListener { addPhoto() }
        binding.btnSubmitClaim.setOnClickListener { submit() }

        return binding.root
    }

    override fun onDestroyView() {
        claimRestaurantID = ""
        super.onDestroyView()
    }

    private fun submit() {
        if(binding.imgSSM.cropToBlob(300,300).toBytes().isEmpty()){
            errorDialog("Please submit your SSM certificate before submitting the request!")
            return
        }

        val request = Request(
            id = vmRequest.generateID(),
            dateTime = Date(),
            requestType = "Claim Business",
            description = binding.edtClaimDescription.text.toString(),
            image = binding.imgSSM.cropToBlob(300,300),
            status = "Pending",
            rejectReason = "",
            customerID = currentUser!!.id,
            restaurantID = claimRestaurantID!!,
            moderatorID = ""
        )
        if(oldRequest != null){
            REQUEST.document(oldRequest!!.id).delete()
        }

        vmRequest.set(request)
        reset()
        successDialog("Claim Business Submitted", "It will take 2-3days for the moderator to approve or reject the request. Once it is done, you will receive an email regarding your request. Thanks.")
        nav.navigateUp()

    }

    private fun reset() {
        claimRestaurantID = ""
        binding.imgSSM.setImageDrawable(null)
        binding.edtClaimDescription.setText("")
    }

    private fun addPhoto() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        else {
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
            binding.imgSSM.setImageBitmap(thumbnail)
        }
    }
    private val photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data != null) {
            val photoURI: Uri? = it.data!!.data
            binding.imgSSM.setImageURI(photoURI)

        }
    }
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) addPhoto() else snackbar("Please allow camera and file permission before proceed to change profile picture!")
    }
}