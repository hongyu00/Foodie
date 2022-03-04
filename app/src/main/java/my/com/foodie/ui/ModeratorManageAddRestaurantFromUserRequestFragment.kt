package my.com.foodie.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentModeratorManageAddRestaurantFromUserRequestBinding
import my.com.foodie.util.SendEmail
import my.com.foodie.util.errorDialog
import my.com.foodie.util.toBitmap

class ModeratorManageAddRestaurantFromUserRequestFragment : Fragment() {


    private lateinit var binding: FragmentModeratorManageAddRestaurantFromUserRequestBinding
    private val nav by lazy { findNavController() }
    private val id by lazy { requireArguments().getString("id") ?: "" }
    private val vmRequest: RequestViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    private var restaurantID = ""
    private var ownerID = ""
    private var logo: Bitmap? = null
    private var surrImg: Bitmap? = null
    private var request: Request? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModeratorManageAddRestaurantFromUserRequestBinding.inflate(inflater, container, false)
        load()

        binding.restaurantSurrImgRequest2.setOnClickListener {
            image = surrImg
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")

        }

        binding.restaurantLogoRequest2.setOnClickListener {
            image = logo
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")

        }

        binding.btnApproveAddRestaurant2.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Approve Request")
                .setMessage("Are you sure you want to approve this request?" )
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Yes") { dialog, whichButton ->
                    vmRequest.updateRequestStatus(id, "Approved", "", currentUser!!.id)
                    vmRestaurant.updateRestaurantStatus(restaurantID, "Active")
                    sendEmail("Approved")
                    Toast.makeText(context, "Request has been approved.", Toast.LENGTH_LONG).show()
                    nav.navigateUp()
                }
                .setNegativeButton("No", null).show()
        }

        binding.btnRejectAddRestaurant2.setOnClickListener {
            if(binding.btnRejectAddRestaurant2.text == "Reject"){
                AlertDialog.Builder(requireContext())
                    .setTitle("Reject Request")
                    .setMessage("Are you sure you want to reject this request?" )
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes") { dialog, whichButton ->
                        insertRejectReason()
                    }
                    .setNegativeButton("No", null).show()
            }else{
                rejectRequest()
            }
        }


        binding.imgReturn25.setOnClickListener { nav.navigateUp() }
        return binding.root
    }

    private fun rejectRequest() {
        val rejectReason = binding.edtRejectReasonAddRestaurant2.text.toString()
        if(rejectReason == ""){
            errorDialog("Please enter a reject reason.")
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Reject Request")
            .setMessage("Are you sure you want to reject this request?" )
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Yes") { dialog, whichButton ->
                vmRequest.updateRequestStatus(id, "Rejected", rejectReason, currentUser!!.id)
                sendEmail("Rejected")
                Toast.makeText(context, "Request has been rejected.", Toast.LENGTH_LONG).show()
                nav.navigateUp()
            }
            .setNegativeButton("No", null).show()
    }

    private fun insertRejectReason() {
        binding.divider43.isVisible = true
        binding.edtRejectReasonAddRestaurant2.isVisible = true
        binding.lblRejectReasonAddRestaurant2.isVisible = true
        binding.btnRejectAddRestaurant2.text = "Confirm Reject"
    }

    private fun load() {
        request = vmRequest.get(id)
        if(request != null){
            restaurantID = request!!.restaurantID
            ownerID = request!!.customerID
            logo = request!!.restaurant.restaurantImg?.toBitmap()
            surrImg = request!!.restaurant.restaurantSurrImg?.toBitmap()

            binding.requesterImg3.setImageBitmap(request!!.user.userProfile?.toBitmap())
            binding.userNameRequest2.text = request!!.user.name
            binding.requestTypeAddRestaurant2.text = request!!.requestType

            binding.restaurantLogoRequest2.setImageBitmap(request!!.restaurant.restaurantImg?.toBitmap())
            binding.restaurantSurrImgRequest2.setImageBitmap(request!!.restaurant.restaurantSurrImg?.toBitmap())
            binding.restaurantNameRequest2.text = request!!.restaurant.name
            binding.cuisineRequest2.text = request!!.restaurant.cuisine
            binding.addressRequest2.text = request!!.restaurant.location
            binding.contactNumberRequest2.text = request!!.restaurant.contactNo
            binding.operatingHourRequest2.text = request!!.restaurant.operatingHour
            binding.priceRangeRequest2.text = "RM" + request!!.restaurant.priceRange.toString()
            binding.addRestaurantDescriptionRequest2.text = request!!.restaurant.description

            if(request!!.status != "Pending"){
                binding.btnRejectAddRestaurant2.isVisible = false
                binding.btnApproveAddRestaurant2.isVisible = false
            }

            if(request!!.status == "Rejected"){
                binding.divider43.isVisible = true
                binding.lblRejectReasonAddRestaurant2.isVisible = true
                binding.lblRejectReasonForAddRestaurant2.isVisible = true
                binding.lblRejectReasonForAddRestaurant2.text = request!!.rejectReason
            }
        }
    }

    private fun sendEmail(str: String){
        lifecycleScope.launch {
            //send to user
            USER.document(request!!.customerID).get().addOnSuccessListener {
                    snap ->
                val user = snap.toObject<User>()!!
                val subject = if(str == "Approved"){
                    "Your request of adding ${request!!.restaurant.name} has been approved!"
                }else{
                    "Your request of adding ${request!!.restaurant.name} has been rejected!"
                }
                val content = if(str == "Approved"){
                    """
            <p>Hi, ${user.name}!</p>
            <p>Moderator has approved your request of adding <b>${request!!.restaurant.name}</b>!</p><br/>        
            <p>You can now view the restaurant details at the apps! </p>
            <p>Thanks for contributing by suggesting this restaurant to us!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                }
                else{
                    """
            <p>Hi, ${user.name}!</p>
            <p>Moderator has rejected your request of adding <b>${request!!.restaurant.name}</b>!</p><br/>        
            <p>The reject reason of your request are as below:</p>
            <h3>Reject Reason: ${binding.edtRejectReasonAddRestaurant2.text.toString()}</h3>
            <p>You can still request to add restaurant even this request has been rejected. We will review your request once again.</p>
            <p>Thanks.</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                }
                SendEmail().to(user.emailAddress).subject(subject).content(content).isHtml().send()
            }
        }

    }
}

