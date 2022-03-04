package my.com.foodie.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
//import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import my.com.foodie.R
import my.com.foodie.databinding.FragmentSearchResultBinding


class SearchResultFragment : Fragment() {


    private lateinit var binding: FragmentSearchResultBinding
    private val nav by lazy { findNavController() }
    lateinit var searchView: SearchView
    lateinit var listView: ListView
    lateinit var list: ArrayList<String>
    lateinit var adapter: ArrayAdapter<*>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)

        searchView = binding.root.findViewById(R.id.searchView)
       // listView = binding.root.findViewById(R.id.listView)

        list = ArrayList()
        list.add("Restaurant Ali")
        list.add("Restaurant Abu")
        list.add("Mcdonalds")
        list.add("Kentucky Fried Chicken (KFC)")
        list.add("Burger King")
        list.add("Al Fariz")
        list.add("Taiwan Tea House")
        list.add("Kedai Ah Meng")
        list.add("Restaurant Lau Heong")

        adapter = ArrayAdapter<String>(requireActivity(),android.R.layout.simple_list_item_1, list)
        //adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list)
        listView.adapter = adapter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (list.contains(query)) {
                    adapter.filter.filter(query)
                } else {
                    //Toast.makeText(this@MainActivity, "No Match found", Toast.LENGTH_LONG).show()
                }
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        binding.imgReturn9.setOnClickListener {
            nav.navigateUp()
        }


        return binding.root
    }
}