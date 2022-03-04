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
import my.com.foodie.data.Review
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentCurrentReservationBinding
import my.com.foodie.util.UserReservationAdapter

class CurrentReservationFragment : Fragment() {
    private lateinit var binding: FragmentCurrentReservationBinding
    private val nav by lazy { findNavController() }
    private val vmReservation: ReservationViewModel by activityViewModels()
    private var reservations: List<Reservation>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCurrentReservationBinding.inflate(inflater, container, false)

        val adapter = UserReservationAdapter(){
            holder, reservation ->
            holder.root.setOnClickListener {
                if (reservation.status == "Pending") {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Edit Reservation")
                        .setMessage("Do you want to edit this reservation?")
                        .setIcon(R.drawable.ic_warning)
                        .setPositiveButton("Yes") { dialog, whichButton ->
                            val args = bundleOf(
                                "status" to "Pending",
                                "id" to reservation.restaurantID,
                                "reservationID" to reservation.id
                            )
                            nav.navigate(R.id.reservationFragment, args)
                        }
                        .setNegativeButton("No", null).show()
                }
            }
        }

        binding.rvCurrentReservation.adapter = adapter
        binding.rvCurrentReservation.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmReservation.getAll().observe(viewLifecycleOwner) { reservation ->
            reservations = reservation.filter { r -> r.customerID == currentUser!!.id && (r.status == "Pending" || r.status == "Approved") && !r.hasEnded }
            if(reservations!!.isEmpty()){
                binding.txtCurrent.isVisible = true
            }

            adapter.submitList(reservations)

        }

        return binding.root
    }

}