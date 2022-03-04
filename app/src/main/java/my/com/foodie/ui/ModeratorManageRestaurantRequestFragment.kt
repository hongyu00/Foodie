package my.com.foodie.ui

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentModeratorManageRestaurantRequestBinding
import my.com.foodie.util.SendEmail
import my.com.foodie.util.errorDialog
import my.com.foodie.util.toBitmap

class ModeratorManageRestaurantRequestFragment : Fragment() {


    private lateinit var binding: FragmentModeratorManageRestaurantRequestBinding
    private val nav by lazy { findNavController() }
    private val id by lazy{ arguments?.getString("id", "")}
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmRequest: RequestViewModel by activityViewModels()

    private var restaurantID = ""
    private var ownerID = ""
    private var imgSSM: Bitmap? = null
    private var request: Request? = null
    private var rejectRequests: ArrayList<Request> = ArrayList<Request>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModeratorManageRestaurantRequestBinding.inflate(inflater, container, false)

        load()
        binding.imgRequestSSM.setOnClickListener {
            image = imgSSM
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")

        }


        binding.btnApproveAddBusiness.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Approve Request")
                .setMessage("Are you sure you want to approve this request? Other submission related to this restaurant will be rejected at once. Confirm to approve?" )
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Yes") { dialog, whichButton ->
//                    vmRequest.updateRequestStatus(id!!, "Approved", "", currentUser!!.id)
                    lifecycleScope.launch {
                        vmRestaurant.updateRestaurantOwner(restaurantID, ownerID)
                        sendEmail("Approved")
                        Log.d("got run this lifecycle? what restaurant", restaurantID)
                        //val requests = vmRequest.getRequestFromSameRestaurant(restaurantID)
                        val requests = REQUEST.whereEqualTo("restaurantID", restaurantID).get().await().toObjects<Request>()
                        Log.d("request size", requests.size.toString())
                        if(requests.isNotEmpty()){
                            for(r in requests){
                                if(r.id != id){
                                    rejectRequests.add(r)
                                    vmRequest.updateRequestStatus(r.id, "Rejected", "Business has been claimed by others", currentUser!!.id)
                                }else{
                                    vmRequest.updateRequestStatus(id!!, "Approved", "", currentUser!!.id)
                                }
                            }
                            if(!rejectRequests.isNullOrEmpty()){
                                sendEmail("Rejected")
                            }
                        }
                        Toast.makeText(context, "Request has been approved.", Toast.LENGTH_LONG).show()
                        nav.navigateUp()
                    }

                }
                .setNegativeButton("No", null).show()

        }

        binding.btnRejectAddBusiness.setOnClickListener {

            if(binding.btnRejectAddBusiness.text == "Reject"){
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
        binding.btnReturn4.setOnClickListener { nav.navigateUp() }
        return binding.root
    }

    private fun rejectRequest() {
        val rejectReason = binding.edtRejectReason.text.toString()
        if(rejectReason == ""){
            errorDialog("Please enter a reject reason.")
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Reject Request")
            .setMessage("Are you sure you want to reject this request?" )
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Yes") { dialog, whichButton ->
                vmRequest.updateRequestStatus(id!!, "Rejected", rejectReason, currentUser!!.id)
                sendEmail("Rejected")
                Toast.makeText(context, "Request has been rejected.", Toast.LENGTH_LONG).show()
                nav.navigateUp()
            }
            .setNegativeButton("No", null).show()
    }

    private fun insertRejectReason() {
        binding.divider22.isVisible = true
        binding.edtRejectReason.isVisible = true
        binding.lblRejectReason.isVisible = true
        binding.btnRejectAddBusiness.text = "Confirm Reject"
    }

    private fun load() {
        request = vmRequest.get(id!!)
        if(request != null){
            restaurantID = request!!.restaurantID
            ownerID = request!!.customerID
            imgSSM = request!!.image?.toBitmap()

            binding.requesterName.text = request!!.user.name
            binding.requesterImg.setImageBitmap(request!!.user.userProfile?.toBitmap())
            binding.txtRequestType.text = request!!.requestType
            binding.requestRestaurantName.text = request!!.restaurant.name
            binding.imgRequestSSM.setImageBitmap(request!!.image?.toBitmap())
            binding.txtAddBusinessDescription.text = request!!.description

            if(request!!.status != "Pending"){
                binding.btnRejectAddBusiness.isVisible = false
                binding.btnApproveAddBusiness.isVisible = false
            }

            if(request!!.status == "Rejected"){
                binding.divider22.isVisible = true
                binding.lblRejectReason.isVisible = true
                binding.lblReject.isVisible = true
                binding.lblReject.text = request!!.rejectReason
            }
        }
    }

    private fun sendEmail(str: String){
        lifecycleScope.launch {
            if(!rejectRequests.isNullOrEmpty()){
                for (r in rejectRequests!!){
                    request = r
                    binding.edtRejectReason.setText("Business has been claimed by others")
                    USER.document(request!!.customerID).get().addOnSuccessListener {
                            snap ->
                        val user = snap.toObject<User>()!!
                        Log.d("user is", user.name + " " + user.emailAddress)
                        val subject = if(str == "Approved"){
                            "Your request of claiming ${request!!.restaurant.name} has been approved!"
                        }else{
                            "Your request of claiming ${request!!.restaurant.name} has been rejected!"
                        }
                        val content = if(str == "Approved"){
                            """
            <p>Hi, ${user.name}!</p>
            <p>Moderator has approved your request of claiming <b>${request!!.restaurant.name}</b>!</p><br/>        
            <p>You can now login to your account to change your restaurant details and make your restaurant available for reservation as well!</p>
            <p>Do make sure that your restaurant details are always accurate to let customer leave a good review for you!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                        }
                        else{
                            """
            <p>Hi, ${user.name}!</p>
            <p>Moderator has rejected your request of claiming <b>${request!!.restaurant.name}</b>!</p><br/> 
            <p>The reject reason of your request are as below:</p>
            <h3>Reject Reason: ${binding.edtRejectReason.text}</h3>
            <p>If the restaurant really are your business, you can choose to resubmit your details to us and we will review your request once again.</p>
            <p>Thanks.</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                        }
                        Log.d("yes", r.id + " has been rejected and email sent")
                        SendEmail().to(user.emailAddress).subject(subject).content(content).isHtml().send()
                    }
                }
                rejectRequests!!.clear()
                return@launch
            }
            //send to user
            USER.document(request!!.customerID).get().addOnSuccessListener {
                    snap ->
                val user = snap.toObject<User>()!!
                Log.d("user is", user.name + " " + user.emailAddress)
                val subject = if(str == "Approved"){
                    "Your request of claiming ${request!!.restaurant.name} has been approved!"
                }else{
                    "Your request of claiming ${request!!.restaurant.name} has been rejected!"
                }
                val content = if(str == "Approved"){
                    """
            <p>Hi, ${user.name}!</p>
            <p>Moderator has approved your request of claiming the restaurant!</p><br/>        
            <p>You can now login to your account to change your restaurant details and make your restaurant available for reservation as well!</p>
            <p>Do make sure that your restaurant details are always accurate to let customer leave a good review for you!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                }
                else{
                    """
            <p>Hi, ${user.name}!</p>
            <p>Moderator has rejected your request of claiming the restaurant!</p><br/> 
            <p>The reject reason of your request are as below:</p>
            <h3>Reject Reason: ${binding.edtRejectReason.text}</h3>
            <p>Sorry.</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                }
                SendEmail().to(user.emailAddress).subject(subject).content(content).isHtml().send()
            }
        }

    }


}