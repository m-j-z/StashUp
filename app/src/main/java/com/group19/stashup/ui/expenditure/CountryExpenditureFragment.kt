package com.group19.stashup.ui.expenditure

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentCountryExpenditureBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.String.format
import java.lang.reflect.Type
import java.math.RoundingMode
import java.text.DecimalFormat

class CountryExpenditureFragment : Fragment(), SearchView.OnQueryTextListener,
    View.OnClickListener {

    private var _binding: FragmentCountryExpenditureBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var countryCityViewModel: CountryCityViewModel
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var currentList: ArrayList<String>
    var country: String = ""
    var city: String = ""

    private lateinit var dialog: AlertDialog
    private var reference: DatabaseReference = FirebaseDatabase.getInstance()
        .reference.child("transaction")

    private var cityTransList: ArrayList<Double> = ArrayList()
    private var countryTransList: ArrayList<Double> = ArrayList()

    /**
     * To switch to next fragment...
     * 1. val navController = findNavController()
     * 2. navController.navigate({action id})
     * Action id is found in mobile_navigation.xml
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryExpenditureBinding.inflate(inflater, container, false)

        countryCityViewModel = ViewModelProvider(this)[CountryCityViewModel::class.java]
        countryCityViewModel.loadData(requireActivity())

        binding.selectLocationCv.setOnClickListener(this)


        //onSetLocationClicked()
        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun onSetLocationClicked() {
        // Observe until data is loaded.
        countryCityViewModel.isLoaded.observe(this) {
            if (!it) return@observe

            // Create builder.
            val builder = AlertDialog.Builder(requireActivity())

            // Inflate view.
            val view = layoutInflater.inflate(R.layout.searchable_list, null)

            // Set query listener for search view.
            val searchView: SearchView = view.findViewById(R.id.search_view)
            searchView.setOnQueryTextListener(this)

            // Set data for list view.
            val listView: ListView = view.findViewById(R.id.list_view)
            val countryList = countryCityViewModel.countryList
            listAdapter = ArrayAdapter(
                requireActivity(), android.R.layout.simple_list_item_1, countryList
            )
            currentList = countryList
            listView.adapter = listAdapter

            // set on item click listener for listview
            listView.setOnItemClickListener { _, _, position, _ ->
                // remove data if pressed
                if (country.isNotEmpty() && city.isNotEmpty()) {
                    country = ""
                    city = ""
                }

                // Get item at position.
                val item = listView.adapter.getItem(position).toString()

                // If country is empty, add to country.
                if (country.isEmpty()) {
                    country = item
                    val cityList = countryCityViewModel.getCities(item)
                    listAdapter = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, cityList
                    )
                    currentList = cityList
                    listView.adapter = listAdapter

                } else { // else add to city.
                    city = item
                    val text = "${city}, ${country}"
                    binding.selectLocationTv.text = text
                    //Update city and country's transaction list
                    getSpending(city, country)
                    dialog.cancel()
                }


            }

            // Set view, create dialog, show dialog.
            builder.setView(view)
            dialog = builder.create()
            dialog.show()

        }
    }


    /**
     * Search_view Listener's members
     * */
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (currentList.contains(query)) {
            listAdapter.filter.filter(query)
        }
        return false
    }
    override fun onQueryTextChange(newText: String?): Boolean {
        listAdapter.filter.filter(newText)
        return false
    }


    /**
    * Update avg spending boxes after selecting city/country
    * */
    override fun onClick(v: View?) {

        if(v == null) return

        if(binding.cityAvgTv.text.isNotEmpty())
            binding.cityAvgTv.text = "0.0"
            binding.countryAvgTv.text = "0.0"
        onSetLocationClicked()
    }



    private fun getSpending(city: String, country: String){

        CoroutineScope(Dispatchers.IO).launch {
            val valueEventListener= object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    cityTransList.clear()
                    countryTransList.clear()
                    for (snapshot in dataSnapshot.children){

                        if(snapshot.child("city").value.toString() == city)
                            cityTransList.add(snapshot.child("cost").value.toString().toDouble())


                        if(snapshot.child("country").value.toString() == country)
                            countryTransList.add(snapshot.child("cost").value.toString().toDouble())


                    }
                    //Update Textview if list is not empty
                    if(cityTransList.size != 0){
                        val df = DecimalFormat("#.##")
                        df.roundingMode = RoundingMode.UP
                        val cityAvg = df.format(cityTransList.average())
                        val countryAvg = df.format(countryTransList.average())
                        binding.cityAvgTv.text = cityAvg
                        binding.countryAvgTv.text = countryAvg
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
            reference.addValueEventListener(valueEventListener)
        }

    }



}