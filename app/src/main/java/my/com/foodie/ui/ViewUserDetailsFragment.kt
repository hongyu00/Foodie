package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.UserViewModel
import my.com.foodie.databinding.FragmentViewUserDetailsBinding
import my.com.foodie.util.toBitmap


class ViewUserDetailsFragment : Fragment() {

    private lateinit var binding: FragmentViewUserDetailsBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()
    private val id by lazy { requireArguments().getString("id") ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentViewUserDetailsBinding.inflate(inflater, container, false)

        load()

        binding.btnReturn6.setOnClickListener { nav.navigateUp() }
        return binding.root
    }

    private fun load() {
        val user = vmUser.get(id)
        if(user != null){
            binding.lblUserID.text = user.id
            binding.lblName.text = user.name
            binding.lblEmail.text = user.emailAddress
            binding.lblPhone.text = user.phoneNumber
            if(user.birthDate == ""){
                binding.lblDate.text = "N/A"
            }else{
                binding.lblDate.text = user.birthDate
            }
            if(user.gender == ""){
                binding.lblGender.text = "N/A"
            }else{
                binding.lblGender.text = user.gender
            }
            binding.lblRole.text = user.role
            binding.imgUser.setImageBitmap(user.userProfile?.toBitmap())
        }
    }


}