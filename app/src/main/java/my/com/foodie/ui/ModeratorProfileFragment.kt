package my.com.foodie.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.UserViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.data.image
import my.com.foodie.databinding.FragmentModeratorProfileBinding
import my.com.foodie.util.cropToBlob
import my.com.foodie.util.showPhotoSelection
import my.com.foodie.util.snackbar
import my.com.foodie.util.toBitmap

class ModeratorProfileFragment : Fragment() {

    private lateinit var binding: FragmentModeratorProfileBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModeratorProfileBinding.inflate(inflater, container, false)

        binding.moderatorName.text = currentUser!!.name
        binding.moderatorEmail.text = currentUser!!.emailAddress
        binding.moderatorPhone.text = currentUser!!.phoneNumber
        if(currentUser!!.birthDate == ""){
            binding.moderatorBirthDate.text = "N/A"
            binding.moderatorGender.text = "N/A"
        }else{
            binding.moderatorBirthDate.text = currentUser!!.birthDate
            binding.moderatorGender.text = currentUser!!.gender
        }
        binding.moderatorImg.setImageBitmap(currentUser!!.userProfile?.toBitmap())

        binding.btnChangePassword.setOnClickListener { nav.navigate(R.id.changePasswordFragment) }
        binding.btnEditProfile.setOnClickListener { nav.navigate(R.id.editProfileFragment) }
        binding.btnReturn7.setOnClickListener { nav.navigateUp() }

        binding.moderatorImg.setOnClickListener {
            image = currentUser!!.userProfile?.toBitmap()
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")
        }
        binding.editImage.setOnClickListener { addPhoto() }

        return binding.root
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_CANCELED) {
            val thumbnail = it.data!!.extras!!["data"] as Bitmap?
            binding.moderatorImg.setImageBitmap(thumbnail)
            vmUser.updateProfilePhoto(currentUser!!.id, binding.moderatorImg.cropToBlob(300,300))
            currentUser!!.userProfile = binding.moderatorImg.cropToBlob(300,300)
        }
    }
    private val photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data != null) {
            val photoURI: Uri? = it.data!!.data
            binding.moderatorImg.setImageURI(photoURI)
            vmUser.updateProfilePhoto(currentUser!!.id, binding.moderatorImg.cropToBlob(300,300))
            currentUser!!.userProfile = binding.moderatorImg.cropToBlob(300,300)

        }
    }
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) addPhoto() else snackbar("Please allow camera and file permission before proceed to change profile picture!")
    }

    private fun addPhoto() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        else {
            showPhotoSelection("Change Profile Photo",
                { cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE)) }, {
                    val photoIntent =  Intent(Intent.ACTION_GET_CONTENT)
                    photoIntent.type = "image/*"
                    photoLauncher.launch(photoIntent)})
        }
    }

}