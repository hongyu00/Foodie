package my.com.foodie.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.bluehomestudio.luckywheel.WheelItem
import my.com.foodie.R
import my.com.foodie.data.Restaurant
import my.com.foodie.data.RestaurantViewModel
import my.com.foodie.data.pickRestaurant
import my.com.foodie.databinding.FragmentPickRestaurantToSpinningWheelBinding
import my.com.foodie.util.PickRestaurantAdapter
import my.com.foodie.util.errorDialog


class PickRestaurantToSpinningWheelFragment : Fragment() {


    private lateinit var binding: FragmentPickRestaurantToSpinningWheelBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPickRestaurantToSpinningWheelBinding.inflate(inflater, container, false)
        pickRestaurant.clear()
        binding.imgReturn28.setOnClickListener {
            pickRestaurant.clear()
            nav.navigateUp()
        }
        binding.imgNext.setOnClickListener {
            if(pickRestaurant.size < 2){
                errorDialog("Please select at least 2 restaurants to spin it!")
                return@setOnClickListener
            }
            nav.navigate(R.id.spinningWheelFragment)
        }
        binding.svRestaurant2.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(value: String) = true
            override fun onQueryTextChange(value: String): Boolean {
                vmRestaurant.search(value)
                return true
            }
        })
        binding.btnSortDistance.setOnClickListener { sort("distance") }
        vmRestaurant.filterStatus("Active")
        vmRestaurant.sort("")
        vmRestaurant.filterCuisine("Cuisine")

        vmRestaurant.sortDistanceOnce()

        val adapter = PickRestaurantAdapter(){
            holder, restaurant ->
            holder.root.setOnClickListener {
                if(holder.root.background == null){
                    holder.root.setBackgroundColor(Color.parseColor("#98FB98"))
                    pickRestaurant.add(restaurant)
                }else{
                    holder.root.background = null
                    pickRestaurant.remove(restaurant)
                }
            }
        }

        binding.rvPickRestaurant.adapter = adapter
        binding.rvPickRestaurant.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        vmRestaurant.getAll().observe(viewLifecycleOwner) { restaurants ->
            adapter.submitList(restaurants)
        }


        return binding.root
    }
    private fun sort(field: String) {
        val reverse = vmRestaurant.sort(field)

        val res = if (reverse) R.drawable.ic_down else R.drawable.ic_up
        when(field){
            "distance" -> binding.btnSortDistance.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,res,0)
        }
    }
}