package my.com.foodie.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.AuthViewModel
import my.com.foodie.databinding.FragmentModeratorHomeBinding
import my.com.foodie.databinding.FragmentOwnerBusinessDialogBinding

class ModeratorHomeFragment : Fragment() {

    private lateinit var binding: FragmentModeratorHomeBinding
    private val nav by lazy { findNavController() }
    private val authVM: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModeratorHomeBinding.inflate(inflater, container, false)

        //requireActivity().actionBar!!.show()

        binding.cardAddRestaurant.setOnClickListener{nav.navigate(R.id.ownerAddBusinessFragment)}
        binding.cardRestaurantMaintenance.setOnClickListener{nav.navigate(R.id.restaurantListingFragment)}
        binding.cardRestaurantRequest.setOnClickListener{nav.navigate(R.id.moderatorRestaurantRequestListingFragment)}
        binding.cardRestaurantReportListing.setOnClickListener{nav.navigate(R.id.moderatorRestaurantReportListingFragment)}
        binding.cardOnScreenReport.setOnClickListener{nav.navigate(R.id.onScreenReportFragment)}
        binding.cardLogout.setOnClickListener{
            authVM.logout(requireContext())
            val intent = Intent(requireContext(), loginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.cardUserListing.setOnClickListener{ nav.navigate(R.id.userListingFragment)}
        binding.cardProfile.setOnClickListener{ nav.navigate(R.id.moderatorProfileFragment) }



        return binding.root
    }


}