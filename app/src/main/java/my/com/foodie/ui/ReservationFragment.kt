package my.com.foodie.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentReservationBinding
import my.com.foodie.util.SendEmail
import my.com.foodie.util.errorDialog
import my.com.foodie.util.snackbar
import my.com.foodie.util.successDialog
import java.text.SimpleDateFormat
import java.util.*


class ReservationFragment : Fragment() {
   private lateinit var binding: FragmentReservationBinding
    private val nav by lazy { findNavController() }
    private val status by lazy {requireArguments().getString("status", "New")}
    private val id by lazy {requireArguments().getString("id", "")}
    private val reservationID by lazy {requireArguments().getString("reservationID", "")}
    private val vmReservation: ReservationViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    private var reservationDate = ""
    private var reservationTime = ""
    private var noOfPeople = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReservationBinding.inflate(inflater, container, false)


        lifecycleScope.launch {
            if(status == "Pending"){
                val reservation = vmReservation.getReservationDetails(reservationID)!!
                reservationDate = reservation.date
                reservationTime = reservation.time
                noOfPeople = reservation.noOfPeople
                binding.cvReservation.date = SimpleDateFormat("dd-MM-yyyy").parse(reservation.date).time
                binding.lblReservationDate.text = reservation.date
                binding.timePicker.hour = reservation.time.take(2).toInt()
                binding.timePicker.minute = reservation.time.takeLast(2).toInt()
                binding.noOfPeople.setText(reservation.noOfPeople.toString())
            }
        }

        if(status == "Pending"){
            binding.lblBookTable.text = "Edit your reservation"
        }
        //calendar
        val calendar = Calendar.getInstance()
        val date = calendar.time.time
        binding.cvReservation.minDate = date

        binding.cvReservation.setOnDateChangeListener { calendarView, year, month, day ->
            var newMonth = month.toString()
            var newDay = day.toString()
            if((month+1) <= 9){
                newMonth = "0${month+1}"
            }else{
                newMonth = "${month+1}"
            }
            if(day <= 9){
                newDay = "0$day"
            }
            reservationDate = "$newDay-$newMonth-$year"
            binding.lblReservationDate.text = reservationDate
        }
        //time picker
        binding.timePicker.setOnTimeChangedListener { timePicker, hour, minute ->
            var hour = hour
            var am_pm = ""
            var newHour = hour.toString()
            var newMin = minute.toString()

            if(hour <= 9){
                newHour = "0$hour"
            }
            if(minute <= 9){
                newMin = "0$minute"
            }
            // AM_PM decider logic
            when {hour == 0 -> { hour += 12
                am_pm = "AM"
            }
                hour == 12 -> am_pm = "PM"
                hour > 12 -> { hour -= 12
                    am_pm = "PM"
                }
                else -> am_pm = "AM"
            }
            reservationTime = "$newHour:$newMin"
            binding.lblReservationTime.text = "$reservationTime  $am_pm"
        }
        binding.noOfPeople.doOnTextChanged { text, _, _, _ ->
            if(text != null || text != ""){
                binding.lblNoOfPeople.text = text.toString()
            }else{
                binding.lblNoOfPeople.text = "0"
            }

        }

        binding.imgReturn6.setOnClickListener {
            nav.navigateUp()
        }

        binding.btnConfirmReservation.setOnClickListener { verifyDetails() }

        return binding.root
    }

    private fun verifyDetails() {
        if(reservationDate == ""){
            errorDialog("Please choose your reservation date!")
            return
        }
        if(reservationTime == ""){
            errorDialog("Please choose your reservation time!")
            return
        }
        if(!binding.noOfPeople.text.isDigitsOnly() || binding.noOfPeople.text.toString() == ""){
            errorDialog("Please fill up the number of people!")
            return
        }
        noOfPeople = binding.noOfPeople.text.toString().toInt()
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Reservation")
            .setMessage("Make sure your reservation details are accurate before submitting. Confirm submit reservation?")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Yes") { dialog, whichButton ->
                submit()
            }
            .setNegativeButton("No", null).show()

    }

    private fun submit() {

        lifecycleScope.launch {
            if(status != "Pending"){
                val res = vmReservation.getReservationsFromUser(currentUser!!.id)
                for(r in res){
                    if(r.date == reservationDate){
                        if(r.time == reservationTime){
                            errorDialog("You have made a reservation that has the same date and time!")
                            return@launch
                        }
                    }
                }
            }

            var resID = ""

            if(status == "Pending"){
                resID = reservationID
            }else{
                resID = vmReservation.generateID()
            }
            val reservation = Reservation(
                id = resID,
                date = reservationDate,
                time = reservationTime,
                noOfPeople = noOfPeople,
                status = "Pending",
                rejectReason = "",
                customerID = currentUser!!.id,
                restaurantID = id
            )
            if(status == "Pending"){
                sendEmail("pending")
            }else{
                sendEmail("new")
            }
            vmReservation.set(reservation)
            nav.navigateUp()
            successDialog("Reservation has been made", "Please check your Reservation page or Email for the status of the reservation.")
        }

    }

    private fun sendEmail(str: String){

        lifecycleScope.launch {
            val restaurant = vmRestaurant.get(id)
            Log.d("restaurant?", restaurant.toString())
            if(restaurant != null){

                //send to user
                val subject = if(str == "new"){
                    "Your reservation has been made"
                }else{
                    "Your reservation has been updated"
                }
                val content = """
            <p>Hi, ${currentUser!!.name}!</p>
            <p>Thanks for making a reservation with ${restaurant.name}! </p><br/>
            <p>The reservation details are as below:</p>
            <h3>Reservation Date: $reservationDate</h3>
            <h3>Reservation Time: $reservationTime</h3>
            <h3>No of People: $noOfPeople pax</h3><br/>
            <p>Please wait for your reservation to be approved by the restaurant owner.</p>
            <p>You will be notify through email when the restaurant owner has approve/reject your reservation. Kindly be informed that you can also go to the Foodie app > Reservation page to keep track about your reservation status. Thanks!</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()

                SendEmail().to(currentUser!!.emailAddress).subject(subject).content(content).isHtml().send()


                //send to restaurant owner
                USER.document(restaurant.ownerID).get().addOnSuccessListener {
                        snap ->
                    val user = snap.toObject<User>()!!
                    Log.d("user?", user.toString())
                    val subject2 = if(str == "new"){
                        "You have a pending reservation!"
                    }else{
                        "${currentUser!!.name} has updated his reservation details!"
                    }
                    val content2 = """
            <p>Hi, ${user.name}!</p>
            <p>A reservation from <b>${currentUser!!.name}</b> is pending for your approval!</p><br/>
            <p>The reservation details are as below:</p>
            <h3>Reservation Date: $reservationDate</h3>
            <h3>Reservation Time: $reservationTime</h3>
            <h3>No of People: $noOfPeople pax</h3><br/>
            <p>Please proceed to the Foodie app to manage this reservation. </p>
            <p>Login to your account and from the Home page, press Reservation and press on the Reservation Details where status are in <b>Pending</b> to proceed to approve or reject the reservation. Thanks.</p>
            <br/>
            <p><b>Foodie</b></p>
        """.trimIndent()

                    SendEmail().to(user.emailAddress).subject(subject2).content(content2).isHtml().send()
                }

            }

        }

    }

}