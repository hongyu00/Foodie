package my.com.foodie.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import my.com.foodie.R
import my.com.foodie.data.ReportViewModel
import my.com.foodie.data.ReservationViewModel
import my.com.foodie.data.Restaurant
import my.com.foodie.data.RestaurantViewModel
import my.com.foodie.databinding.FragmentModeratorRestaurantReportListingBinding
import my.com.foodie.util.RestaurantReportListingAdapter


class ModeratorRestaurantReportListingFragment : Fragment() {


    private lateinit var binding: FragmentModeratorRestaurantReportListingBinding
    private val nav by lazy { findNavController() }
    private val vmReport: ReportViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private var restaurants: List<Restaurant>? = null
    private var restaurant: Restaurant? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModeratorRestaurantReportListingBinding.inflate(inflater, container, false)

        vmRestaurant.filterStatus("All")
        vmRestaurant.filterCuisine("Cuisine")

        binding.spnStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val status = binding.spnStatus.selectedItem.toString()
                vmRestaurant.filterStatus(status)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }

        binding.btnReportCount.setOnClickListener { sort("report") }

        val adapter = RestaurantReportListingAdapter(){
            holder, restaurant ->
            holder.root.setOnClickListener {
                nav.navigate(R.id.ownerReportListFragment, bundleOf("id" to restaurant.id))
            }
            holder.btnMore.setOnClickListener {
                this.restaurant = restaurant
                val items: Array<CharSequence> = arrayOf<CharSequence>("\uD83D\uDCDD Edit Restaurant", "\uD83C\uDD99 Change Status")
                AlertDialog.Builder(context)
                    .setTitle("Choose an option")
                    .setIcon(R.drawable.ic_select_photo)
                    .setSingleChoiceItems(items, 3) { d, n ->
                        //pickImage(n, cameraLauncher,  photoLauncher)
                        navigate(n, restaurant.id)
                        d?.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel), null).show()
            }
        }

        binding.rvRestaurantReport.adapter = adapter
        binding.rvRestaurantReport.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        vmRestaurant.getAll().observe(viewLifecycleOwner){
            restaurant ->
            //restaurants.clear()
            restaurants = restaurant.filter { r -> r.reportCount >= 1 }
            adapter.submitList(restaurants)
            binding.lblRestaurantCount.text = "${restaurants!!.size} record(s)"
        }

        binding.imgReturn26.setOnClickListener { nav.navigateUp() }
        return binding.root
    }

    private fun navigate(n: Int, id: String) {
        if( n == 0){
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

    private fun sort(field: String) {
        val reverse = vmRestaurant.sort(field)

        binding.btnReportCount.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)

        val res = if (reverse) R.drawable.ic_down else R.drawable.ic_up
        when(field){
            "report" -> binding.btnReportCount.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,res,0)
        }
    }

}