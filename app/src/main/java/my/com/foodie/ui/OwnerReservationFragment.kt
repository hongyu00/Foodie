package my.com.foodie.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import my.com.foodie.R
import my.com.foodie.data.ReservationViewModel
import my.com.foodie.data.RestaurantViewModel
import my.com.foodie.databinding.FragmentOwnerReservationBinding
import my.com.foodie.util.OwnerReservationAdapter

class OwnerReservationFragment : Fragment() {

    private lateinit var binding: FragmentOwnerReservationBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmReservation: ReservationViewModel by activityViewModels()
    private val id by lazy { requireArguments().getString("id") ?: "" }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnerReservationBinding.inflate(inflater, container, false)

        vmReservation.filterByRestaurant(id)
        filterStatus("Pending")

        binding.imgReturn16.setOnClickListener { nav.navigateUp() }
        binding.btnAllReservation.setOnClickListener { filterStatus("All") }
        binding.btnPendingReservation.setOnClickListener { filterStatus("Pending") }
        binding.btnApprovedReservation.setOnClickListener { filterStatus("Approved") }
        binding.btnRejectedReservation.setOnClickListener { filterStatus("Rejected") }

        val adapter = OwnerReservationAdapter(){
            holder, reservation ->
            holder.root.setOnClickListener {
                if(reservation.status == "Pending"){
                    nav.navigate(R.id.ownerManageReservationFragment, bundleOf("id" to reservation.id))
                }
            }
        }

        binding.rvOwnerReservation.adapter = adapter
        binding.rvOwnerReservation.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmReservation.getAll().observe(viewLifecycleOwner) { reservation ->

            adapter.submitList(reservation)
        }


        return binding.root
    }

    private fun filterStatus(status: String){
        when(status){
            "All" -> {
                resetBtn()
                binding.btnAllReservation.setBackgroundResource(R.drawable.button_address_background)
                binding.btnAllReservation.setTextColor(Color.WHITE)
                vmReservation.filterByStatus(status)
            }
            "Pending" -> {
                resetBtn()
                binding.btnPendingReservation.setBackgroundResource(R.drawable.button_address_background)
                binding.btnPendingReservation.setTextColor(Color.WHITE)
                vmReservation.filterByStatus(status)
            }
            "Approved" -> {
                resetBtn()
                binding.btnApprovedReservation.setBackgroundResource(R.drawable.button_address_background)
                binding.btnApprovedReservation.setTextColor(Color.WHITE)
                vmReservation.filterByStatus(status)
            }
            "Rejected" -> {
                resetBtn()
                binding.btnRejectedReservation.setBackgroundResource(R.drawable.button_address_background)
                binding.btnRejectedReservation.setTextColor(Color.WHITE)
                vmReservation.filterByStatus(status)
            }
        }
    }

    private fun resetBtn() {
        binding.btnAllReservation.setBackgroundResource(R.drawable.button_black_padding)
        binding.btnApprovedReservation.setBackgroundResource(R.drawable.button_black_padding)
        binding.btnPendingReservation.setBackgroundResource(R.drawable.button_black_padding)
        binding.btnRejectedReservation.setBackgroundResource(R.drawable.button_black_padding)

        binding.btnAllReservation.setTextColor(Color.BLACK)
        binding.btnApprovedReservation.setTextColor(Color.BLACK)
        binding.btnPendingReservation.setTextColor(Color.BLACK)
        binding.btnRejectedReservation.setTextColor(Color.BLACK)
    }

}