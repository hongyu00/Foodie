package my.com.foodie.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentMyReviewBinding
import my.com.foodie.util.MyReviewAdapter
import java.text.SimpleDateFormat

class MyReviewFragment : Fragment() {

    private lateinit var binding: FragmentMyReviewBinding
    private val nav by lazy { findNavController() }
    private val vmReview: ReviewViewModel by activityViewModels()
    private var reviews = ArrayList<Review>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyReviewBinding.inflate(inflater, container, false)


        val adapter = MyReviewAdapter(){
            holder, review ->
            holder.edit.setOnClickListener {
                nav.navigate(R.id.reviewFragment, bundleOf("id" to review.restaurantID))
            }
        }

        binding.rvMyReviews.adapter = adapter
        binding.rvMyReviews.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))


        vmReview.getAll().observe(viewLifecycleOwner){
                review ->
            reviews = review.filter { r -> r.customerID == currentUser!!.id } as ArrayList<Review>
            adapter.submitList(reviews)
            binding.reviewCount.text = reviews.size.toString()
            Log.d("how many2", reviews.size.toString())

        }
        return binding.root
    }


}