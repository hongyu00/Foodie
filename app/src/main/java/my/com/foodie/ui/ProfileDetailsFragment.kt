package my.com.foodie.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.AuthViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentProfileDetailsBinding

class ProfileDetailsFragment : Fragment() {


    private lateinit var binding: FragmentProfileDetailsBinding
    private val nav by lazy { findNavController() }
    private val authVM: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileDetailsBinding.inflate(inflater, container, false)

        binding.lblUserEmail.text = currentUser!!.emailAddress
        binding.lblUserName.text = currentUser!!.name
        binding.lblUserPhone.text = currentUser!!.phoneNumber
        binding.lblUserBirthday.text = currentUser!!.birthDate


        binding.btnLogout.setOnClickListener { logout() }
        binding.btnBusiness.setOnClickListener { nav.navigate(R.id.addBusinessFragment) }
        binding.btnChangePass.setOnClickListener { nav.navigate(R.id.changePasswordFragment) }
        binding.btnEdit.setOnClickListener { nav.navigate(R.id.editProfileFragment) }

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