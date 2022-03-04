package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.databinding.FragmentModeratorEditRestaurantStatusBinding

class ModeratorEditRestaurantStatusFragment : Fragment() {

    private lateinit var binding: FragmentModeratorEditRestaurantStatusBinding
    private val nav by lazy { findNavController() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModeratorEditRestaurantStatusBinding.inflate(inflater, container, false)

        binding.imgReturn22.setOnClickListener { nav.navigateUp() }

        return binding.root
    }

}