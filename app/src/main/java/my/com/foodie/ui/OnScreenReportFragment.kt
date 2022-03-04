package my.com.foodie.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import my.com.foodie.R
import my.com.foodie.data.Report
import my.com.foodie.data.Restaurant
import my.com.foodie.data.RestaurantViewModel
import my.com.foodie.databinding.FragmentOnScreenReportBinding
import my.com.foodie.util.MostReservationMadeRestaurantAdapter
import my.com.foodie.util.MostViewRestaurantAdapter
import my.com.foodie.util.TopRatingRestaurantAdapter


class OnScreenReportFragment : Fragment() {

    private lateinit var binding: FragmentOnScreenReportBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private var restaurants: List<Restaurant>? = null
    private var reportType = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnScreenReportBinding.inflate(inflater, container, false)

        vmRestaurant.getAll()
        vmRestaurant.filterStatus("All")
        vmRestaurant.filterCuisine("Cuisine")

        binding.imgReturn29.setOnClickListener { nav.navigateUp() }

        binding.spOSReport.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reportType = binding.spOSReport.selectedItem.toString()
                Log.d("what report", reportType)
                promptAdapter(reportType, 3)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }

        binding.btnTop3.setOnClickListener { promptAdapter(reportType, 3) }
        binding.btnTop5.setOnClickListener { promptAdapter(reportType, 5) }
        binding.btnTop10.setOnClickListener { promptAdapter(reportType, 10) }

        return binding.root
    }

    private fun promptAdapter(report: String, value: Int) {

        when(report){
            "Most Viewed Restaurant" -> {
                val adapter = MostViewRestaurantAdapter{
                    holder, restaurant ->
                    holder.root.setOnClickListener {
                        nav.navigate(R.id.restaurantDetailsFragment, bundleOf("id" to restaurant.id))
                    }
                }
                binding.rvOSReport.adapter = adapter
                binding.rvOSReport.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

                vmRestaurant.getAll().observe(viewLifecycleOwner) { restaurant ->
                    restaurants = restaurant.sortedByDescending { r -> r.viewCount }
                    adapter.submitList(restaurants!!.take(value))
                }

            }
            "Most Reservation Made Restaurant" -> {
                val adapter = MostReservationMadeRestaurantAdapter{
                        holder, restaurant ->
                    holder.root.setOnClickListener {
                        nav.navigate(R.id.restaurantDetailsFragment, bundleOf("id" to restaurant.id))
                    }
                }
                binding.rvOSReport.adapter = adapter
                binding.rvOSReport.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

                vmRestaurant.getAll().observe(viewLifecycleOwner) { restaurant ->
                    restaurants = restaurant.sortedByDescending { r -> r.reservationCount }

                    adapter.submitList(restaurants!!.take(value))
                }

            }

            "Top Rating Restaurant" -> {
                val adapter = TopRatingRestaurantAdapter{
                        holder, restaurant ->
                    holder.root.setOnClickListener {
                        nav.navigate(R.id.restaurantDetailsFragment, bundleOf("id" to restaurant.id))
                    }
                }
                binding.rvOSReport.adapter = adapter
                binding.rvOSReport.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

                vmRestaurant.getAll().observe(viewLifecycleOwner) { restaurant ->
                    restaurants = restaurant.sortedByDescending { r -> r.avgRating }

                    adapter.submitList(restaurants!!.take(value))
                }

            }
        }

    }


}