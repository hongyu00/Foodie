package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import my.com.foodie.data.Reservation
import my.com.foodie.data.ReservationViewModel
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentRejectedReservationBinding
import my.com.foodie.util.UserReservationAdapter

class RejectedReservationFragment : Fragment() {

    private lateinit var binding: FragmentRejectedReservationBinding
    private val nav by lazy { findNavController() }
    private val vmReservation: ReservationViewModel by activityViewModels()
    private var reservations: List<Reservation>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRejectedReservationBinding.inflate(inflater, container, false)

        val adapter = UserReservationAdapter()

        binding.rvRejectedReservation.adapter = adapter
        binding.rvRejectedReservation.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmReservation.getAll().observe(viewLifecycleOwner) { reservation ->
            reservations = reservation.filter { r -> r.customerID == currentUser!!.id && r.status == "Rejected"}
            if(reservations!!.isEmpty()){
                binding.txtRejected.isVisible = true
            }

            adapter.submitList(reservations)
        }


        return binding.root
    }


}