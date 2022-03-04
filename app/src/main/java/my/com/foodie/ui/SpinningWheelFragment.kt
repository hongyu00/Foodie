package my.com.foodie.ui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bluehomestudio.luckywheel.WheelItem
import com.google.android.material.tabs.TabLayout
import my.com.foodie.R
import my.com.foodie.databinding.FragmentSpinningWheelBinding
import com.bluehomestudio.luckywheel.LuckyWheel
import com.bluehomestudio.luckywheel.OnLuckyWheelReachTheTarget
import android.widget.Toast
import androidx.core.os.bundleOf
import my.com.foodie.data.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class SpinningWheelFragment : Fragment() {


    private lateinit var binding: FragmentSpinningWheelBinding
    private val nav by lazy { findNavController() }
    private var lw: LuckyWheel? = null
    var rand = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSpinningWheelBinding.inflate(inflater, container, false)
        binding.imgReturn27.setOnClickListener {
            pickRestaurant.clear()
            nav.navigateUp()
        }
        val wheelItems: MutableList<WheelItem> = ArrayList()

        var count = 0
        for(a in pickRestaurant){
            wheelItems.add(WheelItem(Color.parseColor(listOfColor[count]), BitmapFactory.decodeResource(resources, R.drawable.icecream), a.name))
             count++
            if(listOfColor.size < count){
                count = 0
            }
        }


        lw = binding.luckyView
        lw!!.addWheelItems(wheelItems)

        lw!!.setLuckyWheelReachTheTarget (OnLuckyWheelReachTheTarget {

            randomRestaurant = pickRestaurant[rand]
            randomRestaurantID = randomRestaurant!!.id
            val dialog = PickRestaurantResultDialogFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")

        })

        binding.button4.setOnClickListener(View.OnClickListener {
            rand = (0 until wheelItems.size).random()
            lw!!.rotateWheelTo(rand+1)
            Log.d("random restaurant $rand", pickRestaurant[rand].toString())
        }
        )


        return binding.root
    }


}