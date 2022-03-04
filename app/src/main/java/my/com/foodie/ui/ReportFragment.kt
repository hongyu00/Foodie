package my.com.foodie.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentReportBinding
import my.com.foodie.util.errorDialog
import java.util.*

class ReportFragment : Fragment() {

    private lateinit var binding: FragmentReportBinding
    private val nav by lazy { findNavController() }
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val vmReport: ReportViewModel by activityViewModels()
    private val id by lazy{ arguments?.getString("id", "")}
    private var name = ""
    private var reason = ""
    private var description = ""
    private var reportCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        val r = vmRestaurant.get(id!!)
        name = r!!.name
        reportCount = r.reportCount
        binding.lblReportRestaurant.text = "Why do you want to report $name?"
        binding.imgReturn8.setOnClickListener {
            nav.navigateUp()
        }
        binding.btnSubmit.setOnClickListener { submit() }


        return binding.root
    }

    private fun submit() {
         reason = binding.spnReport.selectedItem.toString()
         description = binding.edtReportDescription.text.toString()

        if(description == ""){
            errorDialog("Please provide some description before submitting the report!")
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Report $name?")
            .setMessage("Are you sure you want to report this restaurant?")
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Yes") { dialog, whichButton -> confirm() }
            .setNegativeButton("No", null).show()
    }

    private fun confirm() {
        val report = Report(
             id = vmReport.generateID(),
            dateTime = Date(),
            reportType = reason,
            description = description,
            customerID = currentUser!!.id,
            restaurantID = id!!,
        )

        vmReport.set(report)
        vmRestaurant.updateReportCount(id!!, (reportCount+1))
        Toast.makeText(context, "Your report has been successfully submitted.", Toast.LENGTH_LONG).show()
        nav.navigateUp()
    }

}