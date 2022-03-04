package my.com.foodie.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import my.com.foodie.R
import my.com.foodie.databinding.ActivityMainBinding
import my.com.foodie.databinding.ActivityMainModeratorBinding

class MainModeratorActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainModeratorBinding.inflate(layoutInflater) }
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host3)!!.findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}