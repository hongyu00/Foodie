package my.com.foodie.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentRestaurantDetailsBinding
import my.com.foodie.util.ReviewAdapter
import my.com.foodie.util.errorDialog
import my.com.foodie.util.toBitmap

class RestaurantDetailsFragment : Fragment() {

    private lateinit var binding: FragmentRestaurantDetailsBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val id by lazy { requireArguments().getString("id") ?: "" }
    private val vmRequest: RequestViewModel by activityViewModels()
    private val vmReview: ReviewViewModel by activityViewModels()
    private val vmReservation: ReservationViewModel by activityViewModels()
    private val vmReport: ReportViewModel by activityViewModels()

    private var reviews = ArrayList<Review>()
    private var phone: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var r: Restaurant? = null
    private var report: Report? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRestaurantDetailsBinding.inflate(inflater, container, false)



        lifecycleScope.launch {
            vmRequest.getAll().observe(viewLifecycleOwner){
                request ->
            }
            vmReservation.getAll().observe(viewLifecycleOwner){}
            vmReport.getAll().observe(viewLifecycleOwner){}
        }

        load()
        loadReviews()
        binding.restaurantLogo.setOnClickListener {
            image = r!!.restaurantImg?.toBitmap()
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")
        }
        binding.restaurantSurrImg.setOnClickListener {
            image = r!!.restaurantSurrImg?.toBitmap()
            if(image == null){
                return@setOnClickListener
            }
            val dialog = ImageFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")
        }

        binding.imgReturn3.setOnClickListener {
            nav.navigateUp()
        }
        binding.btnCall.setOnClickListener {
            if(phone != ""){
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                startActivity(intent)
            }
        }
        binding.btnLocation.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$latitude,$longitude"))
            startActivity(intent)
        }
        binding.btnClaim.setOnClickListener { nav.navigate(R.id.claimBusinessFragment, bundleOf("id" to id)) }
        binding.btnReservation.setOnClickListener {
            if(currentUser == null){
                AlertDialog.Builder(requireContext())
                    .setTitle("Invalid user")
                    .setMessage("You need to login to your account before making a reservation? Do you want to login to your account now?" )
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes") { dialog, whichButton ->
                        userToRestaurantDetailsFragment = true
                        userToRestaurantDetailsID = id
                        val intent = Intent(activity, loginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity?.startActivity(intent)
                    }
                    .setNegativeButton("No", null).show()
            }else{
                checkUserReservation()

            }
        }
        binding.btnReview.setOnClickListener{
            if(currentUser == null){
                AlertDialog.Builder(requireContext())
                    .setTitle("Invalid user")
                    .setMessage("You need to login to your account before reviewing this restaurant? Do you want to login to your account now?" )
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes") { dialog, whichButton ->
                        userToRestaurantDetailsFragment = true
                        userToRestaurantDetailsID = id
                        val intent = Intent(activity, loginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity?.startActivity(intent)
                    }
                    .setNegativeButton("No", null).show()
            }else{
                nav.navigate(R.id.reviewFragment, bundleOf("id" to id))
            }
        }
        binding.btnReport.setOnClickListener {
            if(currentUser == null){
                AlertDialog.Builder(requireContext())
                    .setTitle("Invalid user")
                    .setMessage("You need to login to your account before reporting this restaurant? Do you want to login to your account now?" )
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes") { dialog, whichButton ->
                        userToRestaurantDetailsFragment = true
                        userToRestaurantDetailsID = id
                        val intent = Intent(activity, loginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity?.startActivity(intent)
                    }
                    .setNegativeButton("No", null).show()
            }else{
                checkUserReport()
            }
        }


        return binding.root
    }

    private fun checkUserReport() {
        lifecycleScope.launch {
            vmReport.getAll().observe(viewLifecycleOwner) {}
            report = vmReport.getReportByUserAndRestaurant(currentUser!!.id, id)
            if(report != null){
                errorDialog("You have reported this restaurant before!")
                return@launch
            }
            nav.navigate(R.id.reportFragment, bundleOf("id" to id))
        }

    }

    private fun checkUserReservation() {
        lifecycleScope.launch {
            vmReservation.getAll()
            val reservation = vmReservation.getReservationFromUserAndRestaurant(currentUser!!.id,id)
            if(reservation != null){
                for(r in reservation){
                    Log.d("xx", r.toString())
                    r.hasEnded = vmReservation.checkStatus(r)
                    if(r.status == "Pending" && (r.hasEnded == false)){
                        AlertDialog.Builder(requireContext())
                            .setTitle("Reservation found!")
                            .setMessage("You have an ongoing reservation with this restaurant! Do you want to edit your reservation?" )
                            .setIcon(R.drawable.ic_warning)
                            .setPositiveButton("Yes") { dialog, whichButton ->
                                val args = bundleOf(
                                    "status" to "Pending",
                                    "id" to id,
                                    "reservationID" to r.id
                                )
                                nav.navigate(R.id.reservationFragment, args)
                            }
                            .setNegativeButton("No", null).show()
                        return@launch
                    }else if(r.status == "Approved" && (r.hasEnded == false)){
                        errorDialog("You have already successfully made a reservation with this restaurant!")
                        return@launch
                    }
                }
                val args = bundleOf(
                    "status" to "New",
                    "id" to id
                )
                nav.navigate(R.id.reservationFragment, args)

            }else{
                    val args = bundleOf(
                        "status" to "New",
                        "id" to id
                    )
                    nav.navigate(R.id.reservationFragment, args)
            }
        }
    }



    private fun loadReviews() {
        //vmReview.filterByRestaurant(id)
        val adapter = ReviewAdapter()
        binding.rvRestaurantReview.adapter = adapter
        binding.rvRestaurantReview.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmReview.getAll().observe(viewLifecycleOwner){
            review ->
            reviews = review.filter { r -> r.restaurantID == id } as ArrayList<Review>
            adapter.submitList(reviews)

        }
    }

    private fun load() {

         r = vmRestaurant.get(id)
        if(r == null){
            nav.navigateUp()
            Log.d("return null", "Null")
            return
        }
        binding.restaurantLogo.setImageBitmap(r!!.restaurantImg?.toBitmap())
        binding.restaurantSurrImg.setImageBitmap(r!!.restaurantSurrImg?.toBitmap())
        binding.restaurantName.text = r!!.name
        binding.restaurantCuisine.text = r!!.cuisine
        binding.restaurantLocation.text = r!!.location
        binding.restaurantDesc.text = r!!.description
        binding.restaurantPriceRange.text = "RM${r!!.priceRange}/per person"
        binding.restaurantContactNo.text = r!!.contactNo
        if(r!!.totalRating.equals(0.0F)){
            binding.restaurantRating.text = "-"
        }else{
            binding.restaurantRating.text = String.format("%.2f",(r!!.totalRating/r!!.reviewCount) ) + "⭐"
        }
        if(r!!.gotReservation){
            binding.restaurantReservation.text = "Yes"
            binding.restaurantReservation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick, 0,0,0)
            binding.btnReservation.isVisible = true
        }else{
            binding.restaurantReservation.text = "No"
            binding.restaurantReservation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cross, 0,0,0)
            binding.btnReservation.isVisible = false
        }

        phone = r!!.contactNo
        latitude = r!!.latitude
        longitude = r!!.longitude

        if(currentUser != null){
            if(currentUser!!.role == "Moderator"){
                binding.btnReservation.isVisible = false
                binding.btnClaim.isVisible = false
                binding.btnReport.isVisible = false
                binding.btnReview.isVisible = false
                return
            }else{
                binding.btnReservation.isVisible = true
                binding.btnClaim.isVisible = true
                binding.btnReport.isVisible = true
                binding.btnReview.isVisible = true
            }
        }
        //if this restaurant got owner ady, then claim button become invisible
        binding.btnClaim.isVisible = r!!.ownerID == ""
        //if this restaurant not allow for reservation, then reservation button become invisible
        binding.btnReservation.isVisible = r!!.gotReservation

    }
}