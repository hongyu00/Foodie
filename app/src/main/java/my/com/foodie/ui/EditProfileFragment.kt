package my.com.foodie.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import my.com.foodie.databinding.FragmentDecisionBinding
import my.com.foodie.databinding.FragmentEditProfileBinding
import java.util.*
import com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker
import my.com.foodie.data.currentUser
import java.text.SimpleDateFormat
import android.R

import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import my.com.foodie.data.User
import my.com.foodie.data.UserViewModel
import my.com.foodie.data.returnFragment
import my.com.foodie.util.errorDialog


class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        initializeValue()
        binding.btnCfmEdit.setOnClickListener { verifyDetails() }
        binding.imgReturn13.setOnClickListener { nav.navigateUp() }
        return binding.root
    }

    private fun initializeValue() {
        binding.edtEditName.setText(currentUser!!.name)
        binding.edtEditPhone.setText(currentUser!!.phoneNumber)
        if(currentUser!!.birthDate != ""){
            val dp = binding.birthDate
            val year = currentUser!!.birthDate!!.takeLast(4).toInt()
            val month = currentUser!!.birthDate!!.substring(3,5).toInt().minus(1)
            val day = currentUser!!.birthDate!!.take(2).toInt()

            dp.init(year, month, day, null)
        }

        if(currentUser!!.gender != ""){
            if(currentUser!!.gender == "Male"){
                binding.Male.isChecked = true
            }else{
                binding.Female.isChecked = true
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun verifyDetails() {
        val name = binding.edtEditName.text.toString()
        val phone = binding.edtEditPhone.text.toString().trim()

        val day = binding.birthDate.dayOfMonth
        val month = binding.birthDate.month
        val year = binding.birthDate.year
        val calendar = Calendar.getInstance()
        calendar.set(year,month,day)
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val dateNow = sdf.format(Date())
        val formattedBirthDate: String = sdf.format(calendar.time)

        if(name == "" || phone == ""){
            errorDialog("Please fill up all the required details!")
            return
        }
        if(dateNow == formattedBirthDate){
            errorDialog("Please make sure you have selected your birth date!")
            return
        }
        if(binding.rgpGender.checkedRadioButtonId == -1){
            errorDialog("Please choose your gender!")
            return
        }

        val gender = resources.getResourceEntryName(binding.rgpGender.checkedRadioButtonId)
        Log.d("check", "$name + $phone + $dateNow + $formattedBirthDate + $gender")

        lifecycleScope.launch {

            vmUser.updateDetails(currentUser!!.id, name, phone, formattedBirthDate, gender)
            Toast.makeText(context, "Details updated successfully!", Toast.LENGTH_SHORT).show()
            currentUser!!.name = name
            currentUser!!.phoneNumber = phone
            currentUser!!.birthDate = formattedBirthDate
            currentUser!!.gender = gender

            nav.navigateUp()
        }

    }
}