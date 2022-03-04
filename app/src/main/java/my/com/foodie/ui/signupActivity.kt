package my.com.foodie.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import my.com.foodie.data.claimRestaurantID
import my.com.foodie.data.signUpRole
import my.com.foodie.databinding.ActivityLoginBinding
import my.com.foodie.databinding.ActivitySignupBinding

class signupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val role = intent.getStringExtra("role") ?: "User"
        signUpRole = role

        binding.lblLogin.setOnClickListener { finish() }

    }

}