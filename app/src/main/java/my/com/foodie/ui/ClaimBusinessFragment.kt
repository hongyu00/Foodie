package my.com.foodie.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.AuthViewModel
import my.com.foodie.data.RestaurantViewModel
import my.com.foodie.data.claimRestaurantID
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentClaimBusinessBinding
import my.com.foodie.util.errorDialog

class ClaimBusinessFragment : Fragment() {

    private lateinit var binding: FragmentClaimBusinessBinding
    private val nav by lazy { findNavController() }
    private val authVM: AuthViewModel by activityViewModels()
    private val id by lazy{ arguments?.getString("id", "")}
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentClaimBusinessBinding.inflate(inflater, container, false)

        val r = vmRestaurant.get(id!!)
        binding.claimText.text = "Claim ${r!!.name}"
        binding.imgReturn5.setOnClickListener { nav.navigateUp() }
        binding.btnLogin.setOnClickListener{
            if(!binding.checkBox.isChecked){
                errorDialog("Please check the checkbox first before proceeding to the next step.")
                returnTransition
            }else{
                if(currentUser == null){
                    redirect()
                }else{
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Logout")
                        .setMessage("You will be logged out from your current account? Are you sure to logout and login to your business account?" )
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton("Yes") { dialog, whichButton -> redirect() }
                        .setNegativeButton("No", null).show()
                }

            }

        }

        binding.btnRegisterBusiness.setOnClickListener {
            if(!binding.checkBox.isChecked){
                errorDialog("Please check the checkbox first before proceeding to the next step.")
                returnTransition
            }else{
                claimRestaurantID = id
                nav.navigate(R.id.signUpOwnerAccFragment, bundleOf("restaurantID" to id))
            }
        }

        return binding.root
    }

    private fun redirect() {
        authVM.logout(requireContext())
        claimRestaurantID = id
        val intent = Intent(activity, loginActivity::class.java)
            .putExtra("restaurantID", id)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity?.startActivity(intent)    }
}