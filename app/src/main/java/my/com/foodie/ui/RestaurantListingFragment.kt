package my.com.foodie.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SearchView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.Restaurant
import my.com.foodie.data.RestaurantViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentRestaurantListingBinding
import my.com.foodie.util.RestaurantListingModeratorAdapter

class RestaurantListingFragment : Fragment() {

    private lateinit var binding: FragmentRestaurantListingBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    private var restaurant: Restaurant? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRestaurantListingBinding.inflate(inflater, container, false)

        vmRestaurant.filterStatus("All")
        vmRestaurant.filterCuisine("Cuisine")

        binding.svRestaurants.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(value: String) = true
            override fun onQueryTextChange(value: String): Boolean {
                vmRestaurant.search(value)
                return true
            }
        })

        binding.spCuisines.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val cuisine = binding.spCuisines.selectedItem.toString()
                vmRestaurant.filterCuisine(cuisine)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }

        binding.spStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val status = binding.spStatus.selectedItem.toString()
                vmRestaurant.filterStatus(status)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }



        val adapter = RestaurantListingModeratorAdapter(){
            holder, restaurant ->
            holder.root.setOnClickListener {
                this.restaurant = restaurant
                val items: Array<CharSequence> = arrayOf<CharSequence>("\uD83D\uDC40 View Restaurant", "\uD83D\uDCDD Edit Restaurant", "\uD83C\uDD99 Change Status")
                AlertDialog.Builder(context)
                    .setTitle("Choose an option")
                    .setIcon(R.drawable.ic_select_photo)
                    .setSingleChoiceItems(items, 3) { d, n ->
                        navigate(n, restaurant.id)
                        d?.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel), null).show()

            }
        }
        binding.rvRestaurants.adapter = adapter
        binding.rvRestaurants.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmRestaurant.getAll().observe(viewLifecycleOwner) { restaurant ->
            adapter.submitList(restaurant)
            Log.d("size", restaurant.size.toString())
            binding.restaurantCount.text = "${restaurant.size} records"
        }

        binding.imgReturn18.setOnClickListener { nav.navigateUp() }

        return binding.root
    }

    private fun navigate(n: Int, id: String) {
        if(n == 0){
            nav.navigate(R.id.restaurantDetailsFragment, bundleOf("id" to id))
        }else if( n == 1){
            vmRestaurant.setTempRestaurant(restaurant!!)
            nav.navigate(R.id.restaurantDetailsOwnerFragment, bundleOf("id" to id))

        }else{
            AlertDialog.Builder(requireContext())
                .setTitle("You are about to change the status of this restaurant.")
                .setMessage("Are you sure you want to change the status of this restaurant?" )
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Yes") { dialog, whichButton ->
                    changeStatus()
                }
                .setNegativeButton("No", null).show()
        }
    }

    private fun changeStatus() {
        if(restaurant!!.status == "Active"){
            vmRestaurant.updateRestaurantStatus(restaurant!!.id, "Inactive")
        }else{
            vmRestaurant.updateRestaurantStatus(restaurant!!.id, "Active")
        }
        Toast.makeText(context, "Restaurant Status Changed.", Toast.LENGTH_LONG).show()

    }
}