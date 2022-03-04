package my.com.foodie.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import my.com.foodie.R
import my.com.foodie.data.*
import my.com.foodie.databinding.FragmentOwnerReportListBinding
import my.com.foodie.util.OwnerReportAdapter

class OwnerReportListFragment : Fragment() {


    private lateinit var binding: FragmentOwnerReportListBinding
    private val nav by lazy { findNavController() }
    private val vmReport: ReportViewModel by activityViewModels()
    private val vmRestaurant: RestaurantViewModel by activityViewModels()
    private val id by lazy{ arguments?.getString("id", "")}
    private var reports: List<Report>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnerReportListBinding.inflate(inflater, container, false)

        if(currentUser!!.role == "Moderator"){
            binding.textView94.text = "Restaurant Reports"
        }

        vmReport.filterByRestaurant(id!!)

        val adapter = OwnerReportAdapter(){
            holder, report ->
            holder.btnDeleteReport.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Delete Report")
                    .setMessage("You are about to delete this report. Are you sure you want to delete?" )
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("Yes") { dialog, whichButton ->
                        vmReport.delete(report.id)
                        vmRestaurant.updateReportCount(report.restaurantID,(reports!!.size-1))
                        Toast.makeText(context, "Report Deleted Successfully", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null).show()
            }
        }
        binding.rvOwnerReport.adapter = adapter
        binding.rvOwnerReport.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmReport.getAll().observe(viewLifecycleOwner){
            report ->
            reports = report.filter { r -> r.restaurantID == id }
            adapter.submitList(report)
            if(currentUser!!.role == "Moderator"){
                binding.lblReportCount.text = "This restaurant have received a total of ${reports!!.size} reports"
            }else{
                binding.lblReportCount.text = "You have received a total of ${reports!!.size} reports so far"

            }
        }


        binding.imgReturn21.setOnClickListener{ nav.navigateUp()}

        return binding.root
    }




}