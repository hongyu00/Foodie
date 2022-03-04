package my.com.foodie.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentBusinessApplicationBinding

class BusinessApplicationFragment : Fragment() {

    private lateinit var binding: FragmentBusinessApplicationBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmRequest: RequestViewModel by activityViewModels()


    private var requestType = ""
    private var restaurantID = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBusinessApplicationBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            vmRestaurant.getAll().observe(viewLifecycleOwner){}
            vmRequest.getAll().observe(viewLifecycleOwner){}
            Log.d("where first", "1")

            val restaurant = vmRestaurant.getRestaurantIDFromUser(currentUser!!.id)
            Log.d("user", currentUser!!.id)


            //1. see whether this user got existing restaurant
            //2. if dont have, see whether the user has submitted any request or not (add business)
            //3. if dont have, means this user got no request and active restaurant, and ask him to add or claim new one.
            if(restaurant != null){
                //means the user has this restaurant as his/her business
                insertValueToTextField("You have an ONGOING business!", "Your business,\n\n ${restaurant.name.uppercase()}\n\n is doing great! Make sure you constantly provides accurate details for users to visit your restaurant!", "", "Go to Home Page", true, "#48CE3F")
                return@launch
            }

            val request = vmRequest.getRestaurantRequestFromUser(currentUser!!.id)

            if(request != null){
                 requestType = request.requestType
                val status = request.status
                val rejectReason = request.rejectReason

                val getRestaurant = vmRestaurant.getRestaurantFromID(request.restaurantID)
                restaurantID = getRestaurant!!.id
                if(status == "Pending"){
                    insertValueToTextField("You are PENDING for your business to be approved", "Your request for\n\n ${getRestaurant!!.name.uppercase()}\n\n is still in review. Kindly wait patiently as we will inform you about the updates through email or you can know your status here", "", "Back to Home Page", true, "#FFA500")
                }else if(status == "Rejected"){
                    if(requestType == "Claim Business"){
                        insertValueToTextField("Your request has been REJECTED", "Your submission of claiming\n\n ${getRestaurant!!.name.uppercase()}\n\n has been rejected due to following reason", rejectReason, "Back to Home Page", false, "#FF0000")
                    }else{
                        insertValueToTextField("Your request has been REJECTED", "Your submission of adding your business,\n\n ${getRestaurant!!.name.uppercase()}\n\n into our apps has been rejected due to following reason", rejectReason, "Resubmit Application", false, "#FF0000")
                    }
                }
                return@launch
            }else{
                //no request means new restaurant owner with no restaurant. Ask to add one or claim one
                insertValueToTextField("You do not have a business right now", "It seems like you do not have an active business right now!", "", "Add New Business Now", true, "#000000")
            }
        }

        binding.btnBA.setOnClickListener {
            when (binding.btnBA.text) {
                "Go to Home Page" -> {
                    nav.navigate(R.id.restaurantFragment)
                }
                "Back to Home Page" -> {
                    nav.navigateUp()
                }
                "Add New Business Now" -> {
                    nav.navigate(R.id.ownerAddBusinessFragment)
                }
                "Resubmit Application" -> {
                    nav.navigate(R.id.ownerAddBusinessFragment, bundleOf("id" to restaurantID))
                }
                else -> {
                    nav.navigateUp()
                }
            }
        }



        return binding.root
    }

    private fun insertValueToTextField(status: String, description: String, rejectReason: String, btnField: String, disable: Boolean, color: String) {
        binding.btnBA.isVisible = true
        binding.baTitle.text = status
        binding.baDescription.text = description
        binding.baReason.text = rejectReason
        binding.btnBA.text = btnField

        binding.baTitle.setTextColor(Color.parseColor(color))
        if(disable){
            binding.baReason.isVisible = false
            binding.lblBAReason.isVisible = false
        }else{
            binding.baReason.isVisible = true
            binding.lblBAReason.isVisible = true
        }
    }


}