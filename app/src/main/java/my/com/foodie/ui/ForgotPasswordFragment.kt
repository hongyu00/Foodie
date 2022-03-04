package my.com.foodie.ui

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import my.com.foodie.data.UserViewModel
import my.com.foodie.databinding.FragmentForgotPasswordBinding
import my.com.foodie.util.SendEmail
import my.com.foodie.util.errorDialog
import my.com.foodie.util.snackbar
import java.lang.StringBuilder
import java.util.*


class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()

    //for generate password
    //Removed i, l, o , I, L, O, 0 , 1. Cause all this number and capital very similar. User may type wrong
    private val r = Random()
    private val DIGITS = "23456789"
    private val LOWCASE_CHARACTERS = "abcdefghjkmnpqrstuvwxyz"
    private val UPCASE_CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZ"
    private val SYMBOLS = "=+@\$#!%*?&,._-"
    private val ALL = DIGITS + LOWCASE_CHARACTERS + UPCASE_CHARACTERS + SYMBOLS
    private val uppercaseArray = UPCASE_CHARACTERS.toCharArray()
    private val lowercaseArray = LOWCASE_CHARACTERS.toCharArray()
    private val digitsArray = DIGITS.toCharArray()
    private val symbolsArray = SYMBOLS.toCharArray()
    private val allArray = ALL.toCharArray()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)

        binding.btnReturn.setOnClickListener { nav.navigateUp() }
        binding.btnResetPass.setOnClickListener { sendEmail() }
        return binding.root
    }



    private fun generatePassword(): String {
        val sb = StringBuilder()

        // get one uppercase letter
        sb.append(uppercaseArray[r.nextInt(uppercaseArray.size)])

        // get one lowercase letter
        sb.append(lowercaseArray[r.nextInt(lowercaseArray.size)])

        // get one digit
        sb.append(digitsArray[r.nextInt(digitsArray.size)])

        // get one symbol
        sb.append(symbolsArray[r.nextInt(symbolsArray.size)])

        // fill in remaining 4 characters with random letters
        for (i in 0 until 4) {
            sb.append(allArray[r.nextInt(allArray.size)])
        }

        return sb.toString()
    }

    private fun sendEmail(){
        val email = binding.edtForgotPassEmail.text.toString().trim()
        if(!isEmail(email)){
            errorDialog("Email format is invalid!")
            binding.edtForgotPassEmail.requestFocus()
            return
        }
        //validate whether the email exist in our db
        lifecycleScope.launch {
            if(vmUser.getUserByEmail(email) == null){
                errorDialog("Email has not been registered! Ensure the email you typed has been registered with us.")
                return@launch
            }
            val newPassword = generatePassword()
            val subject = "Forgot Password Reset"
            val content = """
            <p>Your new password is:</p>
            <h1 style="color: #FF0036">$newPassword</h1>
            <p>Please change your password after logging in to your account. Otherwise you have to use this password everytime you login!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()

            //update password in db
            val user = vmUser.getUserByEmail(email)
            vmUser.updatePassword(user!!.id, newPassword)

            SendEmail().to(email).subject(subject).content(content).isHtml().send(){
                snackbar("Email Sent! Login to your account now!")
                nav.navigateUp()
                binding.btnResetPass.isEnabled = true

            }
            snackbar("Sending....")
            binding.btnResetPass.isEnabled = false
        }

    }

    private fun isEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

}