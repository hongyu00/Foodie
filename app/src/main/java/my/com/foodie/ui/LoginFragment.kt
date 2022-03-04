package my.com.foodie.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.AuthViewModel
import my.com.foodie.databinding.FragmentLoginBinding
import my.com.foodie.ui.*
import my.com.foodie.util.errorDialog
import my.com.foodie.util.hideKeyboard

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val nav by lazy { findNavController() }
    private val authVm: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.edtLoginEmail.requestFocus()

        binding.imgClose.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.lblSignUp.setOnClickListener { nav.navigate(R.id.signUpFragment) }
        binding.lblForgotPassword.setOnClickListener { nav.navigate(R.id.forgotPasswordFragment) }

        binding.btnLoginAcc.setOnClickListener { login() }

        return binding.root
    }


    private fun login(){
        hideKeyboard()

        val ctx = requireContext()
        val email = binding.edtLoginEmail.text.toString().trim()
        val password = binding.edtLoginPass.text.toString().trim()
        val remember = binding.chkRemember.isChecked

        if(email == "" || password == "")
        {
            errorDialog("Please filled in your login credentials!")
            return
        }

        lifecycleScope.launch {
            val success = authVm.login(ctx, email, password, remember)
            if(success){
                Toast.makeText(context, "Login Successfully", Toast.LENGTH_SHORT).show()
            }else{
                errorDialog("Invalid login credentials.")
            }
        }
    }

    private fun changeUI(role: String) {
        if(role == "Restaurant Owner"){
            binding.lblNoAccount.text = ""
            binding.lblSignUp.text = ""
        }
    }
}