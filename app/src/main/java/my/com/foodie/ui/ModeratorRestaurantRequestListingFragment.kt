package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import my.com.foodie.R
import my.com.foodie.data.RequestViewModel
import my.com.foodie.data.ReviewViewModel
import my.com.foodie.databinding.FragmentModeratorRestaurantRequestListingBinding
import my.com.foodie.util.RestaurantRequestAdapter

class ModeratorRestaurantRequestListingFragment : Fragment() {


    private lateinit var binding: FragmentModeratorRestaurantRequestListingBinding
    private val nav by lazy { findNavController() }
    private val vmRequest: RequestViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModeratorRestaurantRequestListingBinding.inflate(inflater, container, false)

        binding.spRequestStatus.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val status = binding.spRequestStatus.selectedItem.toString()
                vmRequest.filterRequestStatus(status)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }
        binding.spRequestType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val type = binding.spRequestType.selectedItem.toString()
                vmRequest.filterRequestType(type)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }
        binding.btnRequestDate.setOnClickListener { sort("date") }
        binding.btnRestaurantID.setOnClickListener { sort("restaurantID") }
        binding.imgReturn23.setOnClickListener { nav.navigateUp() }
        val adapter = RestaurantRequestAdapter(){
            holder, request ->
            holder.root.setOnClickListener {
                when(request.requestType){
                    "Add Restaurant(User)" ->  nav.navigate(R.id.moderatorManageAddRestaurantFromUserRequestFragment, bundleOf("id" to request.id))
                    "Add Restaurant(Owner)" ->  nav.navigate(R.id.moderatorManageAddRestaurantFromOwnerRequestFragment, bundleOf("id" to request.id))
                    "Claim Business" -> nav.navigate(R.id.moderatorManageRestaurantRequestFragment, bundleOf("id" to request.id))
                }
            }
        }

        binding.rvRequest.adapter = adapter
        binding.rvRequest.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmRequest.getAll().observe(viewLifecycleOwner){
            request ->
            adapter.submitList(request)
            binding.lblRecords.text = "${request.size} record(s)"
        }
        return binding.root
    }

    private fun sort(field: String) {
        val reverse = vmRequest.sort(field)

        binding.btnRequestDate.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
        binding.btnRestaurantID.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)

        val res = if (reverse) R.drawable.ic_down else R.drawable.ic_up
        when(field){
            "date" -> binding.btnRequestDate.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,res,0)
            "restaurantID" -> binding.btnRestaurantID.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,res,0)
        }
    }
}