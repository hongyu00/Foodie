package my.com.foodie.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import my.com.foodie.R
import my.com.foodie.data.claimRestaurantID
import my.com.foodie.data.userToOwnerAddRestaurant
import my.com.foodie.databinding.ActivityMainBusinessBinding

class MainBusinessActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBusinessBinding.inflate(layoutInflater) }
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host2)!!.findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView2)
        bottomNavView.setupWithNavController(nav)

        if(claimRestaurantID != ""){
            nav.navigate(R.id.ownerClaimBusinessFragment)
            return
        }

        if(userToOwnerAddRestaurant){
            nav.navigate(R.id.ownerAddBusinessFragment)
        }


    }


}