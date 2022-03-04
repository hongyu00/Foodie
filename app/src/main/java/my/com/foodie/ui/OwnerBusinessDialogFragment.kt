package my.com.foodie.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.data.AuthViewModel
import my.com.foodie.data.userToOwnerAddRestaurant
import my.com.foodie.databinding.FragmentOwnerBusinessDialogBinding

class OwnerBusinessDialogFragment : DialogFragment() {


    private lateinit var binding: FragmentOwnerBusinessDialogBinding
    private val nav by lazy { findNavController() }
    private val authVM: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOwnerBusinessDialogBinding.inflate(inflater, container, false)

        binding.lblYes.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("You will be logged out from your current account? Are you sure to logout and login to your business account?" )
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Yes") { dialog, whichButton -> redirect() }
                .setNegativeButton("No", null).show()
        }

        binding.lblNo.setOnClickListener {

            userToOwnerAddRestaurant = true
            nav.navigate(R.id.signUpOwnerAccFragment)
            dismiss()
        }

        return binding.root
    }

    private fun redirect() {
        authVM.logout(requireContext())
        userToOwnerAddRestaurant = true
        val intent = Intent(activity, loginActivity::class.java)
            .putExtra("role", "Business Owner")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity?.startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

}