package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import my.com.foodie.databinding.FragmentReservation2Binding

class Reservation2Fragment : Fragment() {

    private lateinit var binding: FragmentReservation2Binding
    private val nav by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReservation2Binding.inflate(inflater, container, false)


        binding.imgReturn7.setOnClickListener {
            nav.navigateUp()
        }

        return binding.root
    }

}