package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import my.com.foodie.databinding.FragmentSignUpBinding
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.User
import my.com.foodie.data.UserViewModel
import my.com.foodie.util.*


class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.imgReturn.setOnClickListener { nav.navigateUp() }
        binding.lblLogin.setOnClickListener { nav.navigateUp() }
        binding.lblSignUpOwner.setOnClickListener { nav.navigate(R.id.signUpOwnerAccFragment) }
        binding.btnCreateAcc.setOnClickListener { createAccount() }
        return binding.root
    }

    private fun createAccount() {
        val name = binding.edtCreateAccName.text.toString().trim()
        val email = binding.edtCreateAccEmail.text.toString().trim()
        val phone = binding.edtCreateAccPhone.text.toString().trim()
        val regexPassword = Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[=+@$#!%*?&,._-])[A-Za-z\d=+@$#!%*?&,._-]{8,}$""")
        val password = binding.edtCreateAccPass.text.toString().trim()
        val cfmPassword = binding.edtCreateAccCfmPass.text.toString().trim()
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
                role = "User",
                //status = "Active",
                //otp = 0,
                birthDate = "",
                gender = "",
                userProfile = ContextCompat.getDrawable(requireContext(), R.drawable.ic_profile_pic)!!.toBitmap().toBlob()
            )

            vmUser.set(user)
            sendEmail()
            reset()
            nav.navigateUp()
            Toast.makeText(context, "Account Registered Successfully. Please login to your account now.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun reset() {
        binding.edtCreateAccName.text.clear()
        binding.edtCreateAccEmail.text.clear()
        binding.edtCreateAccPhone.text.clear()
        binding.edtCreateAccPass.text.clear()
        binding.edtCreateAccCfmPass.text.clear()
    }

    private fun isValidEmail(email: CharSequence?): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun sendEmail(){
        val email = binding.edtCreateAccEmail.text.toString().trim()
        val name = binding.edtCreateAccName.text.toString().trim()

        //validate whether the email exist in our db
        lifecycleScope.launch {

            val subject = "Foodie Account Creation Successfully"
            val content = """
            <p>Hi ${name}!</p>
            <p>Thanks for creating an account in Foodie. There are a lot of feature for you explore and utilize! Hope you enjoy the app!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()


            SendEmail().to(email).subject(subject).content(content).isHtml().send(){}
        }

    }
}