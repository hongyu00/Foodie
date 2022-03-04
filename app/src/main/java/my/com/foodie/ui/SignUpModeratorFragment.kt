package my.com.foodie.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.User
import my.com.foodie.data.UserViewModel
import my.com.foodie.databinding.FragmentSignUpModeratorBinding
import my.com.foodie.util.errorDialog
import my.com.foodie.util.toBlob


class SignUpModeratorFragment : Fragment() {

    private lateinit var binding: FragmentSignUpModeratorBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpModeratorBinding.inflate(inflater, container, false)

        binding.btnCreateAccModerator.setOnClickListener { createModeratorAccount() }



        binding.imgReturn30.setOnClickListener { nav.navigateUp() }
        return binding.root
    }

    private fun createModeratorAccount() {
        val name = binding.edtCreateAccNameModerator.text.toString().trim()
        val email = binding.edtCreateAccEmailModerator.text.toString().trim()
        val phone = binding.edtCreateAccPhoneModerator.text.toString().trim()
        val regexPassword = Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[=+@$#!%*?&,._-])[A-Za-z\d=+@$#!%*?&,._-]{8,}$""")
        val password = binding.edtCreateAccPassModerator.text.toString().trim()
        val cfmPassword = binding.edtCreateAccCfmPassModerator.text.toString().trim()
        val emailFormat = isValidEmail(email)

        if(name == "" || email == "" || phone == "" || password == "" || cfmPassword == ""){
            errorDialog("Please fill up all the required details!")
            return
        }
        if(!emailFormat){
            errorDialog("Email format is wrong!")
            return
        }
        if(!(phone.length == 10 || phone.length == 11)){
            errorDialog("Phone number length is wrong!")
            return
        }
        if(!password.matches(regexPassword)){
            errorDialog("Password must be: \n\n-Minimum eight characters\n" +
                    "-At least one uppercase letter\n" +
                    "-One lowercase letter\n" +
                    "-One number\n" +
                    "-One special character!")
            return
        }
        if(cfmPassword != password){
            errorDialog("Password and Confirm Password is not matched!")
            return
        }

        lifecycleScope.launch {
            if(vmUser.getUserByEmail(email) != null){
                errorDialog("Email exists! Please use a new email!")
                return@launch
            }

            if(vmUser.getUserByPhone(phone) != null){
                errorDialog("Phone number exists! Please use a new phone number!")
                return@launch
            }
            //provide hash for password
            val passHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
            val user = User(
                id = vmUser.generateID(),
                emailAddress = email,
                password = passHash,
                name = name,
                phoneNumber = phone,
                role = "Moderator",
                birthDate = "",
                gender = "",
                userProfile = ContextCompat.getDrawable(requireContext(), R.drawable.ic_profile_pic)!!.toBitmap().toBlob()
            )

            vmUser.set(user)
            reset()
            Toast.makeText(context, "Moderator Account Registered Successfully.", Toast.LENGTH_LONG).show()
            nav.navigateUp()
        }
    }

    private fun reset() {
        binding.edtCreateAccNameModerator.text.clear()
        binding.edtCreateAccEmailModerator.text.clear()
        binding.edtCreateAccPhoneModerator.text.clear()
        binding.edtCreateAccPassModerator.text.clear()
        binding.edtCreateAccCfmPassModerator.text.clear()
    }

    private fun isValidEmail(email: CharSequence?): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}