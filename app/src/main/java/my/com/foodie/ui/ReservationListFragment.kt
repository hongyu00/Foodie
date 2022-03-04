package my.com.foodie.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import my.com.foodie.R
import my.com.foodie.data.currentUser
import my.com.foodie.databinding.FragmentReservationListBinding

class ReservationListFragment : Fragment() {

    private lateinit var binding: FragmentReservationListBinding
    private val nav by lazy { findNavController() }

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var changeFragmentAdapter: ReservationChangeFragmentAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReservationListBinding.inflate(inflater, container, false)  //change

        if(!verifyUser()){
            return binding.root
        }

        tabLayout = binding.root.findViewById(R.id.tabLayout)
        viewPager = binding.root.findViewById(R.id.viewPager)

        changeFragmentAdapter = ReservationChangeFragmentAdapter(this)
        viewPager.adapter = changeFragmentAdapter

        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            when(position){
                0 -> {
                    tab.text = "Current"
                }
                1 -> {
                    tab.text = "Completed"
                }
                2 -> {
                    tab.text = "Rejected"
                }
            }
        }.attach()

        return binding.root
    }

    private fun verifyUser(): Boolean {
        if(currentUser == null){
            AlertDialog.Builder(requireContext())
                .setTitle("Attention")
                .setMessage("You have not login to your account! Please login first before proceed to the Reservation Page.")
                .setIcon(R.drawable.ic_error)
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, whichButton -> val intent = Intent(activity, loginActivity::class.java)
                        activity?.startActivity(intent)
                        return@OnClickListener}).show()
            return false
        }
        return true
    }
}