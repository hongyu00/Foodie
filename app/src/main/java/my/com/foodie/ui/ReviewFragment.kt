package my.com.foodie.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*

import my.com.foodie.databinding.FragmentReviewBinding
import my.com.foodie.util.errorDialog
import java.util.*

class ReviewFragment : Fragment() {
    private lateinit var binding: FragmentReviewBinding
    private val nav by lazy { findNavController() }
    private val id by lazy { requireArguments().getString("id") ?: "" }
    private val vmReview: ReviewViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private var review: Review? = null
    private var restName = ""
    private var description = ""
    private var star = 0.0F
    private var isAnonymous = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReviewBinding.inflate(inflater, container, false)

        val rest = vmRestaurant.get(id)
        restName = rest!!.name

        binding.lblReview.text = "How many stars would you like to rate $restName?"
        reset()
        lifecycleScope.launch {
            review = vmReview.getReviewFromUserAndRestaurant(currentUser!!.id, id)
            Log.d("check user id", currentUser!!.id)
            Log.d("review", review.toString())
            if(review != null){
                binding.edtReviewDescription.setText(review!!.description)
                binding.ratingBar.rating = review!!.star
                binding.switchAnonymous.isChecked = review!!.isAnonymous
            }
        }





        binding.imgReturn4.setOnClickListener {
            nav.navigateUp()
        }
        binding.btnSubmitReview.setOnClickListener { verify() }

        return binding.root
    }

    private fun verify() {
        star = binding.ratingBar.rating
        description = binding.edtReviewDescription.text.toString().trim()
        isAnonymous = binding.switchAnonymous.isChecked
        if(star == 0.0F){
            errorDialog("Please give $restName a rating before submitting!")
            return
        }
        if(description == ""){
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Submit Review Without Description")
                .setMessage("Are you sure you want to submit this review without leaving a feedback?" )
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Yes") { dialog, whichButton ->
                    submit()
                }
                .setNegativeButton("No", null).show()
        }else{
            submit()
        }

    }

    private fun reset() {
        binding.edtReviewDescription.setText("")
        binding.ratingBar.rating = 0.0F
        binding.switchAnonymous.isChecked = false
    }

    private fun submit(){
        var reviewID = ""

        if(review != null){
            reviewID = review!!.id
            vmRestaurant.updateRestaurantExistingRating(id, review!!.star, star)
        }else{
            reviewID = vmReview.generateID()
            vmRestaurant.updateRestaurantReviewCount(id)
            vmRestaurant.updateRestaurantTotalRating(id, star)
        }

        val review = Review(
            id = reviewID,
            dateTime = Date(),
            star = star,
            description = description,
            isAnonymous = isAnonymous,
            customerID = currentUser!!.id,
            restaurantID = id
        )

        vmReview.set(review)
        Toast.makeText(context, "You have made a review successfully.", Toast.LENGTH_SHORT).show()
        returnFragment = true
        nav.navigateUp()
    }

}