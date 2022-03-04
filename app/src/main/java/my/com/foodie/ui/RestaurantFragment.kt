package my.com.foodie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.RestaurantViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentRestaurantBinding
import my.com.foodie.util.errorDialog

class RestaurantFragment : Fragment() {

    private lateinit var binding: FragmentRestaurantBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRestaurantBinding.inflate(inflater, container, false)



      binding.cardRestaurant.setOnClickListener {
          checkUserRestaurant("Restaurant")
      }
      binding.cardReport.setOnClickListener {
          checkUserRestaurant("Report")
      }
      binding.cardReservation.setOnClickListener {
          checkUserRestaurant("Reservation")
      }
      binding.cardReview.setOnClickListener {
          checkUserRestaurant("Review")
      }


        return binding.root
    }
    private fun checkUserRestaurant(function: String){
        lifecycleScope.launch {
            vmRestaurant.getAll().observe(viewLifecycleOwner) {}
            val restaurant = vmRestaurant.getRestaurantIDFromUser(currentUser!!.id)
            if(restaurant == null){
                errorDialog("You got no active restaurant in your account. Please proceed to your \n\nProfile > Business Application\n\n to check your status or add new business.")
            }else{
                when(function){
                    "Restaurant" -> {
                        vmRestaurant.setTempRestaurant(restaurant)
                        nav.navigate(R.id.restaurantDetailsOwnerFragment, bundleOf("id" to restaurant.id))
                    }
                    "Report" -> {
                        nav.navigate(R.id.ownerReportListFragment, bundleOf("id" to restaurant.id))
                    }
                    "Reservation" -> {
                        nav.navigate(R.id.ownerReservationFragment, bundleOf("id" to restaurant.id))
                    }
                    "Review" -> {
                        nav.navigate(R.id.ownerReviewFragment, bundleOf("id" to restaurant.id))
                    }
                }
            }
            return@launch
        }
    }

}