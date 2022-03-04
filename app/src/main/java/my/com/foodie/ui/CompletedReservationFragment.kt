package my.com.foodie.ui

import android.app.AlertDialog
import android.os.Bundle
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
import my.com.foodie.data.Reservation
import my.com.foodie.data.ReservationViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentCompletedReservationBinding
import my.com.foodie.util.UserReservationAdapter

class CompletedReservationFragment : Fragment() {


    private lateinit var binding: FragmentCompletedReservationBinding
    private val nav by lazy { findNavController() }
    private val vmReservation: ReservationViewModel by activityViewModels()
    private var reservations: List<Reservation>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCompletedReservationBinding.inflate(inflater, container, false)

        val adapter = UserReservationAdapter()

        binding.rvCompletedReservation.adapter = adapter
        binding.rvCompletedReservation.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmReservation.getAll().observe(viewLifecycleOwner) { reservation ->
            reservations = reservation.filter { r -> r.customerID == currentUser!!.id && (r.status == "Approved" || r.status =="Pending") && r.hasEnded }
            if(reservations!!.isEmpty()){
                binding.txtComplete.isVisible = true
            }
            adapter.submitList(reservations)
        }

        return binding.root
    }


}