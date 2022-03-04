package my.com.foodie.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentRandomRestaurantResultDialogBinding
import my.com.foodie.util.toBitmap
import kotlin.random.Random


class RandomRestaurantResultDialogFragment : DialogFragment() {


    private lateinit var binding: FragmentRandomRestaurantResultDialogBinding
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val nav by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRandomRestaurantResultDialogBinding.inflate(inflater, container, false)

        if(randomRestaurant != null){
            binding.logo.setImageBitmap(randomRestaurant!!.restaurantImg?.toBitmap())
            binding.restaurant.text = randomRestaurant!!.name
            binding.distance.text = "This restaurant is ${String.format("%.2f", randomRestaurant!!.distance)}km from your current location!"
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
        binding.btnRollAgain.setOnClickListener { random(randomKM) }
        binding.btnViewRestaurant.setOnClickListener {
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
    private fun random(n: Int){
        vmRestaurant.getAll().observe(viewLifecycleOwner){
            if(n == 0){
                listOfRestaurant = it.filter { r -> r.status == "Active" && r.distance <= 10 }
            }else if(n == 1){
                listOfRestaurant = it.filter { r -> r.status == "Active" && r.distance <= 20 }
            }else{
                listOfRestaurant = it.filter { r -> r.status == "Active" && r.distance <= 30 }
            }
            val randomIndex = Random.nextInt(listOfRestaurant!!.size)
            randomRestaurant = listOfRestaurant!![randomIndex]
            randomRestaurantID = randomRestaurant!!.id
            Log.d("how many", listOfRestaurant!!.size.toString())
            Log.d("random restaurant", randomRestaurant.toString())

            if(randomRestaurant != null){
                binding.logo.setImageBitmap(randomRestaurant!!.restaurantImg?.toBitmap())
                binding.restaurant.text = randomRestaurant!!.name
                binding.distance.text = "This restaurant is ${String.format("%.2f", randomRestaurant!!.distance)}km from your current location!"
            }

        }
    }
}