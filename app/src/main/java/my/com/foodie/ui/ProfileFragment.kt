package my.com.foodie.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
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
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import my.com.foodie.R
import my.com.foodie.data.UserViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.data.image
import my.com.foodie.databinding.FragmentProfileBinding
import my.com.foodie.util.*

//ViewModel


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var changeFragmentAdapter: ProfileChangeFragmentAdapter

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
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.btnUserLogin.setOnClickListener {
            val intent = Intent(activity, loginActivity::class.java)
            activity?.startActivity(intent)
        }

        if(!verifyUser()){
            return binding.root
        }

        binding.userProfilePhoto.setImageBitmap(currentUser!!.userProfile?.toBitmap())

        tabLayout = binding.root.findViewById(R.id.tabLayout)
        viewPager = binding.root.findViewById(R.id.viewPager)




        binding.userProfilePhoto.setOnClickListener {
            image = currentUser!!.userProfile?.toBitmap()
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")
        }
        binding.imgPhoto.setOnClickListener { addPhoto() }

        changeFragmentAdapter = ProfileChangeFragmentAdapter(this)
        viewPager.adapter = changeFragmentAdapter

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            when(position){
                0 -> {
                    tab.text = "Profile"
                }
                1 -> {
                    tab.text = "My Reviews"
                }
            }
        }.attach()

        return binding.root
    }

    private fun verifyUser(): Boolean {
        if(currentUser == null){
            binding.imgPhoto.isVisible = false
            binding.userProfilePhoto.isVisible = false
            AlertDialog.Builder(requireContext())
                .setTitle("Attention")
                .setMessage("You have not login to your account! Please login first before proceed to the Profile Page.")
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, whichButton ->
                        binding.btnUserLogin.isVisible = true
                        val intent = Intent(activity, loginActivity::class.java)
                        activity?.startActivity(intent)
                    return@OnClickListener}).show()
            return false
        }
        binding.btnUserLogin.isVisible = false
        binding.imgPhoto.isVisible = true
        binding.userProfilePhoto.isVisible = true
        return true
    }

}