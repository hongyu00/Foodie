package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.randomRestaurant
import my.com.foodie.data.randomRestaurantID
import my.com.foodie.databinding.FragmentPickRestaurantResultDialogBinding
import my.com.foodie.util.toBitmap


class PickRestaurantResultDialogFragment : DialogFragment() {


    private lateinit var binding: FragmentPickRestaurantResultDialogBinding
    private val nav by lazy { findNavController() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPickRestaurantResultDialogBinding.inflate(inflater, container, false)

        if(randomRestaurant != null){
            binding.pickRestaurantLogo.setImageBitmap(randomRestaurant!!.restaurantImg?.toBitmap())
            binding.pickRestaurantName.text = randomRestaurant!!.name
            binding.pickRestaurantDistance.text = "This restaurant is ${String.format("%.2f", randomRestaurant!!.distance)}km from your current location!"
        }
        binding.btnClose2.setOnClickListener {
            dismiss()
        }
        binding.btnViewPickRestaurant.setOnClickListener {
            nav.navigate(R.id.restaurantDetailsFragment, bundleOf("id" to randomRestaurantID))
            dismiss()
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}