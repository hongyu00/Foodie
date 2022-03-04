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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.AuthViewModel
import my.com.foodie.data.UserViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.data.image
import my.com.foodie.databinding.FragmentOwnerProfileBinding
import my.com.foodie.util.cropToBlob
import my.com.foodie.util.showPhotoSelection
import my.com.foodie.util.snackbar
import my.com.foodie.util.toBitmap

class OwnerProfileFragment : Fragment() {

    private lateinit var binding: FragmentOwnerProfileBinding
    private val nav by lazy { findNavController() }
    private val authVM: AuthViewModel by activityViewModels()
    private val vmUser: UserViewModel by activityViewModels()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_CANCELED) {
            val thumbnail = it.data!!.extras!!["data"] as Bitmap?
            binding.userProfilePhoto.setImageBitmap(thumbnail)
            vmUser.updateProfilePhoto(currentUser!!.id, binding.userProfilePhoto.cropToBlob(300,300))
            currentUser!!.userProfile = binding.userProfilePhoto.cropToBlob(300,300)
        }
    }
    private val photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data != null) {
            val photoURI: Uri? = it.data!!.data
            binding.userProfilePhoto.setImageURI(photoURI)
            vmUser.updateProfilePhoto(currentUser!!.id, binding.userProfilePhoto.cropToBlob(300,300))
            currentUser!!.userProfile = binding.userProfilePhoto.cropToBlob(300,300)

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnerProfileBinding.inflate(inflater, container, false)

        binding.userProfilePhoto.setImageBitmap(currentUser!!.userProfile?.toBitmap())

        binding.ownerEmail.text = currentUser!!.emailAddress
        binding.ownerName.text = currentUser!!.name
        binding.ownerPhone.text = currentUser!!.phoneNumber
        binding.ownerBirthDate.text = currentUser!!.birthDate

        binding.btnLogout2.setOnClickListener { logout() }
        binding.btnBusiness2.setOnClickListener { nav.navigate(R.id.businessApplicationFragment) }
        binding.btnChangePass2.setOnClickListener { nav.navigate(R.id.changePasswordFragment) }
        binding.btnEdit2.setOnClickListener { nav.navigate(R.id.editProfileFragment) }
        binding.imgPhoto.setOnClickListener { addPhoto() }
        binding.userProfilePhoto.setOnClickListener {
            image = currentUser!!.userProfile?.toBitmap()
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")
        }



        return binding.root
    }

    private fun logout(){
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout from your account?")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Logout") { _, _ ->
                authVM.logout(requireContext())
                val intent = Intent(requireContext(), loginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Stay", null).show()
    }

}