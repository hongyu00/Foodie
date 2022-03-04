package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.databinding.FragmentAddBusinessBinding

class AddBusinessFragment : Fragment() {


    private lateinit var binding: FragmentAddBusinessBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddBusinessBinding.inflate(inflater, container, false)


        binding.btnReturn.setOnClickListener {
            nav.navigateUp()
        }

        binding.lblOwner.setOnClickListener {
            var dialog = OwnerBusinessDialogFragment()
            val fm =requireFragmentManager()
            dialog.show(fm, "ss")

        }

        binding.lblCustomer.setOnClickListener {
            nav.navigate(R.id.addBusinessCustomerFragment)
        }

        return binding.root
    }


}