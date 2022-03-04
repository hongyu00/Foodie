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
import my.com.foodie.databinding.FragmentModeratorManageAddRestaurantFromOwnerRequestBinding
import my.com.foodie.util.SendEmail
import my.com.foodie.util.errorDialog
import my.com.foodie.util.toBitmap

class ModeratorManageAddRestaurantFromOwnerRequestFragment : Fragment() {


    private lateinit var binding: FragmentModeratorManageAddRestaurantFromOwnerRequestBinding
    private val nav by lazy { findNavController() }
    private val id by lazy { requireArguments().getString("id") ?: "" }
    private val vmRequest: RequestViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    private var restaurantID = ""
    private var ownerID = ""
    private var imgSSM: Bitmap? = null
    private var logo: Bitmap? = null
    private var surrImg: Bitmap? = null
    private var request: Request? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModeratorManageAddRestaurantFromOwnerRequestBinding.inflate(inflater, container, false)

        load()

        binding.imgRequestSSMCert.setOnClickListener {
            image = imgSSM
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")
        }

        binding.restaurantSurrImgRequest.setOnClickListener {
            image = surrImg
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")

        }

        binding.restaurantLogoRequest.setOnClickListener {
            image = logo
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")

        }

        binding.btnApproveAddRestaurant.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Approve Request")
                .setMessage("Are you sure you want to approve this request?" )
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Yes") { dialog, whichButton ->
                    vmRequest.updateRequestStatus(id, "Approved", "", currentUser!!.id)
                    vmRestaurant.updateRestaurantOwner(restaurantID, ownerID)
                    vmRestaurant.updateRestaurantStatus(restaurantID, "Active")
                    sendEmail("Approved")
                    Toast.makeText(context, "Request has been approved.", Toast.LENGTH_LONG).show()
                    nav.navigateUp()
                }
                .setNegativeButton("No", null).show()
        }

        binding.btnRejectAddRestaurant.setOnClickListener {
            if(binding.btnRejectAddRestaurant.text == "Reject"){
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

        binding.imgReturn24.setOnClickListener { nav.navigateUp() }

        return binding.root
    }

    private fun rejectRequest() {
        val rejectReason = binding.edtRejectReasonAddRestaurant.text.toString()
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
        binding.divider34.isVisible = true
        binding.edtRejectReasonAddRestaurant.isVisible = true
        binding.lblRejectReasonAddRestaurant.isVisible = true
        binding.btnRejectAddRestaurant.text = "Confirm Reject"
    }

    private fun load() {
        request = vmRequest.get(id)
        if(request != null){
            restaurantID = request!!.restaurantID
            ownerID = request!!.customerID
            imgSSM = request!!.image?.toBitmap()
            logo = request!!.restaurant.restaurantImg?.toBitmap()
            surrImg = request!!.restaurant.restaurantSurrImg?.toBitmap()


            binding.requesterImg2.setImageBitmap(request!!.user.userProfile?.toBitmap())
            binding.userNameRequest.text = request!!.user.name
            binding.requestTypeAddRestaurant.text = request!!.requestType

            binding.imgRequestSSMCert.setImageBitmap(request!!.image?.toBitmap())
            if(request!!.description == ""){
                binding.lblDescriptionRequest.isVisible = false
                binding.txtAddRestaurantDescription.isVisible = false
            }else{
                binding.lblDescriptionRequest.isVisible = true
                binding.txtAddRestaurantDescription.isVisible = true
                binding.txtAddRestaurantDescription.text = request!!.description

            }

            binding.restaurantLogoRequest.setImageBitmap(request!!.restaurant.restaurantImg?.toBitmap())
            binding.restaurantSurrImgRequest.setImageBitmap(request!!.restaurant.restaurantSurrImg?.toBitmap())
            binding.restaurantNameRequest.text = request!!.restaurant.name
            binding.cuisineRequest.text = request!!.restaurant.cuisine
            binding.addressRequest.text = request!!.restaurant.location
            binding.contactNumberRequest.text = request!!.restaurant.contactNo
            binding.operatingHourRequest.text = request!!.restaurant.operatingHour
            binding.priceRangeRequest.text = "RM" + request!!.restaurant.priceRange.toString()
            binding.addRestaurantDescriptionRequest.text = request!!.restaurant.description

            if(request!!.status != "Pending"){
                binding.btnRejectAddRestaurant.isVisible = false
                binding.btnApproveAddRestaurant.isVisible = false
            }

            if(request!!.status == "Rejected"){
                binding.divider34.isVisible = true
                binding.lblRejectReasonAddRestaurant.isVisible = true
                binding.lblRejectReasonForAddRestaurant.isVisible = true
                binding.lblRejectReasonForAddRestaurant.text = request!!.rejectReason
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
                    "Your request of adding your business ${request!!.restaurant.name} has been approved!"
                }else{
                    "Your request of adding your business ${request!!.restaurant.name} has been rejected!"
                }
                val content = if(str == "Approved"){
                    """
            <p>Hi, ${user.name}!</p>
            <p>Moderator has approved your request of adding your business <b>${request!!.restaurant.name}</b>!</p><br/>        
            <p>User can now view your restaurant in the apps! </p>
            <p>You can now login to your account to change your restaurant details and make your restaurant available for reservation as well!</p>
            <p>Do make sure that your restaurant details are always accurate to let customer leave a good review for you!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                }
                else{
                    """
            <p>Hi, ${user.name}!</p>
            <p>Moderator has rejected your request of adding <b>${request!!.restaurant.name}</b>!</p><br/>        
            <p>The reject reason of your request are as below:</p>
            <h3>Reject Reason: ${binding.edtRejectReasonAddRestaurant.text.toString()}</h3>
            <p>You can still resubmit your application of adding your restaurant. We will review your request once again.</p>
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