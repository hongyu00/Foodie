package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import my.com.foodie.R
import my.com.foodie.data.UserViewModel
import my.com.foodie.databinding.FragmentUserListingBinding
import my.com.foodie.util.UserListingAdapter

class UserListingFragment : Fragment() {

    private lateinit var binding: FragmentUserListingBinding
    private val nav by lazy { findNavController() }
    private val vmUser: UserViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserListingBinding.inflate(inflater, container, false)


        binding.svName.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(value: String) = true
            override fun onQueryTextChange(value: String): Boolean {
                vmUser.search(value)
                return true
            }
        })
        binding.spUserRole.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val role = binding.spUserRole.selectedItem.toString()
                vmUser.filterRole(role)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }

        val adapter = UserListingAdapter(){
            holder, user ->
            holder.root.setOnClickListener {
                nav.navigate(R.id.viewUserDetailsFragment, bundleOf("id" to user.id))
            }
        }
        binding.rvUser.adapter = adapter
        binding.rvUser.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vmUser.getAll().observe(viewLifecycleOwner){
            user ->
            adapter.submitList(user)
            binding.lblUserCount.text = "${user.size} record(s)"
        }
        binding.btnReturn5.setOnClickListener { nav.navigateUp() }

        binding.btnRegisterModerator.setOnClickListener {
           nav.navigate(R.id.signUpModeratorFragment)
        }

        return binding.root
    }


}