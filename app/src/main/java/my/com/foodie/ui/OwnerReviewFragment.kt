package my.com.foodie.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.RestaurantViewModel
import my.com.foodie.data.Review
import my.com.foodie.data.ReviewViewModel
import my.com.foodie.databinding.FragmentOwnerReservationBinding
import my.com.foodie.databinding.FragmentOwnerReviewBinding
import my.com.foodie.util.ReviewAdapter


class OwnerReviewFragment : Fragment() {


    private lateinit var binding: FragmentOwnerReviewBinding
    private val nav by lazy { findNavController() }
    private val vmReview: ReviewViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val id by lazy { requireArguments().getString("id") ?: "" }
    private var reviews = ArrayList<Review>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnerReviewBinding.inflate(inflater, container, false)

            lifecycleScope.launch {
                val restaurant = vmRestaurant.getRestaurantFromID(id)

                val avgRating = if(restaurant!!.reviewCount == 0){
                    0.0F
                }else{
                    restaurant.totalRating/restaurant.reviewCount
                }

                binding.ownerReviewTitle.text = "Reviews for ${restaurant!!.name}"
                binding.ownerReviewAvgRating.text = "You have an average of ${String.format("%.2f", avgRating)} ⭐ rating!"
                binding.ownerReviewCount.text = "A total of ${restaurant.reviewCount} customers had reviewed your restaurant!"
            }

        binding.imgReturn15.setOnClickListener { nav.navigateUp() }

        val adapter = ReviewAdapter()
        binding.rvOwnerReview.adapter = adapter
        binding.rvOwnerReview.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmReview.getAll().observe(viewLifecycleOwner){
                review ->
            reviews = review.filter { r -> r.restaurantID == id } as ArrayList<Review>
            adapter.submitList(reviews)
        }

        return binding.root
    }


}