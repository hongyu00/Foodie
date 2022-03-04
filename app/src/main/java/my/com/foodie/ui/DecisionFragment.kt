package my.com.foodie.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentDecisionBinding
import my.com.foodie.util.errorDialog
import kotlin.random.Random

class DecisionFragment : Fragment() {

    private lateinit var binding: FragmentDecisionBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDecisionBinding.inflate(inflater, container, false)

        binding.btnRandom.setOnClickListener {
            askKM()
        }

        binding.btnSpinningWheel.setOnClickListener {
            nav.navigate(R.id.pickRestaurantToSpinningWheelFragment)
        }

        return binding.root
    }
    private fun askKM(){
        val items: Array<CharSequence> = arrayOf<CharSequence>("1️⃣0️⃣ 10km within you!", "2️⃣0️⃣ 20km within you!", "3️⃣0️⃣ 30km within you!")
        AlertDialog.Builder(context)
            .setTitle("Choose a random restaurant")
            //.setIcon(R.drawable.decision)
            .setSingleChoiceItems(items, 3) { d, n ->
                random(n)
                d?.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null).show()
    }
    private fun random(n: Int){

        val distance = when(n){
            0 -> 10
            1 -> 20
            2 -> 30
            else -> 0
        }
        vmRestaurant.getAll().observe(viewLifecycleOwner){

            listOfRestaurant = it.filter { r -> r.status == "Active" && r.distance <= distance }
            if(listOfRestaurant!!.isNotEmpty()){

            val randomIndex = Random.nextInt(listOfRestaurant!!.size)
            randomKM = n
            randomRestaurant = listOfRestaurant!![randomIndex]
            randomRestaurantID = randomRestaurant!!.id
            Log.d("how many", listOfRestaurant!!.size.toString())
            Log.d("random restaurant", randomRestaurant.toString())

                val dialog = RandomRestaurantResultDialogFragment()
                val fm =requireFragmentManager()
                dialog.show(fm, "ss")
            }else{

                errorDialog("There is no nearby restaurants within $distance km from you!")
            }


        }

    }
}