package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import at.favre.lib.crypto.bcrypt.BCrypt
import my.com.foodie.R
import my.com.foodie.data.UserViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentChangePasswordBinding
import my.com.foodie.databinding.FragmentProfileBinding
import my.com.foodie.util.errorDialog

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        binding.imgReturn12.setOnClickListener { nav.navigateUp() }
        binding.btnUpdatePass.setOnClickListener { validatePassword() }
        return binding.root
    }

    private fun validatePassword() {
        val userPass = currentUser!!.password
        val currentPass = binding.edtCurrentPass.text.toString().trim()
        val hashResult = BCrypt.verifyer().verify(currentPass.toCharArray(), userPass)

        val newPass = binding.edtNewPass.text.toString().trim()
        val cfmNewPass = binding.edtCfmNewPass.text.toString().trim()
        val regexPassword = Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[=+@$#!%*?&,._-])[A-Za-z\d=+@$#!%*?&,._-]{8,}$""")

        if(userPass == "" || currentPass == "" || cfmNewPass == ""){
            errorDialog("Please fill up all the required details!")
            return
        }

        if(!hashResult.verified){
            errorDialog("Current Password mismatched. Please try again!")
            return
        }
        if(currentPass == newPass){
            errorDialog("Please set a new password that is different with the current password!")
            return
        }
        if(!newPass.matches(regexPassword)){
            errorDialog("New Password must be \n\n-Minimum eight characters \n" +
                    "-At least one uppercase letter \n" +
                    "-One lowercase letter \n" +
                    "-One number\n" +
                    "-One special character!")
            return
        }
        if(cfmNewPass != newPass){
            errorDialog("New Password and Confirm New Password is not matched!")
            return
        }

        vmUser.updatePassword(currentUser!!.id, newPass)
        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
        nav.navigateUp()

    }


}