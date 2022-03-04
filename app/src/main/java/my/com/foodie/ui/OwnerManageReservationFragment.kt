package my.com.foodie.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentOwnerManageReservationBinding
import my.com.foodie.util.SendEmail
import my.com.foodie.util.errorDialog


class OwnerManageReservationFragment : Fragment() {


    private lateinit var binding: FragmentOwnerManageReservationBinding
    private val nav by lazy { findNavController() }
    private val id by lazy{ arguments?.getString("id", "")}
    private val vmReservation: ReservationViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    private var reservation: Reservation? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnerManageReservationBinding.inflate(inflater, container, false)

        reservation = vmReservation.get(id!!)

        if(reservation != null){
            binding.lblReserverName.text = reservation!!.user.name
            binding.lblViewReservationDate.text = reservation!!.date
            binding.lblViewReservationTime.text = reservation!!.time
            binding.lblViewNoOfPeople.text = reservation!!.noOfPeople.toString()
        }

        binding.imgReturn20.setOnClickListener { nav.navigateUp() }
        binding.btnApprove.setOnClickListener {
            AlertDialog.Builder(requireContext())
            .setTitle("Confirm Approve Reservation")
            .setMessage("Are you sure you want to approve this reservation?")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Yes") { dialog, whichButton ->
                confirm("Approved", "")
            }
            .setNegativeButton("No", null).show() }

        binding.btnReject.setOnClickListener {
            if(binding.btnReject.text == "Reject"){
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Reject Reservation")
                    .setMessage("Are you sure you want to reject this reservation?")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes") { dialog, whichButton ->
                        binding.divider17.isVisible = true
                        binding.textView91.isVisible = true
                        binding.rejectReasonLayout.isVisible = true
                        binding.btnReject.text = "Confirm Reject"
                    }
                    .setNegativeButton("No", null).show()
            }else{
                if(binding.rejectReason.text.toString() == ""){
                    errorDialog("Please state the reject reason of this reservation!")
                    return@setOnClickListener
                }
                AlertDialog.Builder(requireContext())
                    .setTitle("Last Confirmation to Reject Reservation")
                    .setMessage("Are you sure you want to reject this reservation?")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes") { dialog, whichButton ->
                        confirm("Rejected", binding.rejectReason.text.toString().trim())
                    }
                    .setNegativeButton("No", null).show()
            }

        }

        return binding.root
    }

    private fun confirm(result: String, rejectReason: String) {
        //TODO send email or notification to user
        if(result == "Approved"){
            vmReservation.updateStatus(id!!, result, rejectReason)
            sendEmail("Approved")
            Toast.makeText(requireContext(), "Reservation has been approved!", Toast.LENGTH_SHORT).show()
        }else{
            vmReservation.updateStatus(id!!, result, rejectReason)
            sendEmail("Rejected")
            Toast.makeText(requireContext(), "Reservation has been rejected!", Toast.LENGTH_SHORT).show()
        }
        nav.navigateUp()


    }

    private fun sendEmail(str: String){

        lifecycleScope.launch {
            RESTAURANT.document(reservation!!.restaurantID).get().addOnSuccessListener {
                    snap ->
                val restaurant = snap.toObject<Restaurant>()!!

                //send to user
                USER.document(reservation!!.customerID).get().addOnSuccessListener {
                        snap2 ->
                    val user = snap2.toObject<User>()!!
                    val subject = if(str == "Approved"){
                        "Your reservation with ${restaurant.name} has been approved!"
                    }else{
                        "Your reservation with ${restaurant.name} has been rejected!"
                    }
                    val content = if(str == "Approved"){
                        """
            <p>Hi, ${user.name}!</p>
            <p>Your reservation has been Approved!</p><br/>        
            <p>The reservation details are as below:</p>
            <h3>Reservation Date: ${reservation!!.date}</h3>
            <h3>Reservation Time: ${reservation!!.time}</h3>
            <h3>No of People: ${reservation!!.noOfPeople} pax</h3><br/>
            <p>This reservation has been approved by the restaurant owner and it cant be edited anymore.</p>
            <p>${restaurant.name} hope to see you on that day and make sure you are punctual to prevent any inconvenience happened! Thanks and enjoy your meal on that day!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                    }
                    else{
                        """
            <p>Hi, ${user.name}!</p>
            <p>Your reservation has been Rejected!</p><br/>
            <p>The reservation details are as below:</p>
            <h3>Reservation Date: ${reservation!!.date}</h3>
            <h3>Reservation Time: ${reservation!!.time}</h3>
            <h3>No of People: ${reservation!!.noOfPeople} pax</h3><br/>
            <p>The reason of your reservation has been rejected are as below:</p>
            <h3>Reject Reason: ${binding.rejectReason.text.toString().trim()}</h3>
            <p>Sorry for any inconvenience caused. You can still try to make a reservation for a different date. Hope you enjoy using this app!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()
                    }
                    SendEmail().to(user.emailAddress).subject(subject).content(content).isHtml().send()
                }
            }


        }

    }

}