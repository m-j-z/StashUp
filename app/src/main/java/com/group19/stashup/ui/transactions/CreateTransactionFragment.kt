package com.group19.stashup.ui.transactions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentCreateTransactionBinding
import com.group19.stashup.ui.expenditure.CountryCityViewModel
import com.group19.stashup.ui.transactions.database.Transaction
import com.group19.stashup.ui.transactions.database.TransactionsViewModel

class CreateTransactionFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, SearchView.OnQueryTextListener {

    // Bindings
    private var _binding: FragmentCreateTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var countryCityViewModel: CountryCityViewModel
    private lateinit var transactionsViewModel: TransactionsViewModel

    private lateinit var dialog: AlertDialog
    private lateinit var currentList: ArrayList<String>
    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTransactionBinding.inflate(inflater, container, false)

        initializeViewsInLayout()
        loadViewModelData()

        return binding.root
    }

    /**
     * Initialize views in layouts.
     */
    private fun initializeViewsInLayout() {
        binding.sharedCb.setOnCheckedChangeListener(this)
        binding.setLocationCv.setOnClickListener(this)
        binding.discardBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
    }

    /**
     * Load view models.
     */
    private fun loadViewModelData() {
        // Create MainViewModel
        countryCityViewModel = ViewModelProvider(this)[CountryCityViewModel::class.java]
        countryCityViewModel.loadData(requireActivity())

        // Create TransactionViewModel
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        // Load data
        binding.nameEt.setText(transactionsViewModel.transactionName)
        binding.costEt.setText(transactionsViewModel.cost.toString())
        val text = "${transactionsViewModel.city}, ${transactionsViewModel.country}"
        binding.setLocationTv.text = text
        binding.sharedCb.isChecked = transactionsViewModel.isShared
        binding.payCb.isChecked = transactionsViewModel.creatorPaid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * On pause, save the data.
     */
    override fun onPause() {
        super.onPause()
        transactionsViewModel.apply {
            transactionName = binding.nameEt.text.toString()
            cost = binding.costEt.text.toString().toDouble()
            isShared = binding.sharedCb.isChecked
            creatorPaid = binding.sharedCb.isChecked
        }
    }

    /**
     * Determines what action to take depending on view clicked.
     */
    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            R.id.set_location_cv -> {
                requireView().findViewById<LinearLayout>(R.id.linear_layout).visibility =
                    LinearLayout.GONE
                requireView().findViewById<ProgressBar>(R.id.progress_bar).visibility =
                    ProgressBar.VISIBLE
                onSetLocationClicked()
            }
            R.id.discard_btn -> onDiscardClicked()
            R.id.save_btn -> onSaveClicked()
        }
    }

    /**
     * On set location clicked, create a dialog to show countries and then cities.
     */
    @SuppressLint("InflateParams")
    private fun onSetLocationClicked() {
        // observe until data is loaded
        countryCityViewModel.isLoaded.observe(this) {
            if (!it) return@observe

            // create builder
            val builder = AlertDialog.Builder(requireActivity())

            // inflate view
            val view = layoutInflater.inflate(R.layout.searchable_list, null)

            // set query listener for search view
            val searchView: SearchView = view.findViewById(R.id.search_view)
            searchView.setOnQueryTextListener(this)

            // set data for list view
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
                if (transactionsViewModel.country.isNotEmpty() && transactionsViewModel.city.isNotEmpty()) {
                    transactionsViewModel.country = ""
                    transactionsViewModel.city = ""
                }

                // get item at position
                val item = listView.adapter.getItem(position).toString()

                // if country is empty, add to country
                if (transactionsViewModel.country.isEmpty()) {
                    transactionsViewModel.country = item
                    val cityList = countryCityViewModel.getCities(item)
                    listAdapter = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, cityList
                    )
                    currentList = cityList
                    listView.adapter = listAdapter
                } else { // else add to city
                    transactionsViewModel.city = item
                    val text = "${transactionsViewModel.city}, ${transactionsViewModel.country}"
                    binding.setLocationTv.text = text
                    dialog.cancel()
                }
            }

            // set view, create dialog, show dialog
            builder.setView(view)
            dialog = builder.create()
            dialog.show()

            // remove progress bar
            requireView().findViewById<ProgressBar>(R.id.progress_bar).visibility = ProgressBar.GONE
            requireView().findViewById<LinearLayout>(R.id.linear_layout).visibility =
                LinearLayout.VISIBLE
        }
    }

    /**
     * On discard clicked, return to previous screen.
     */
    private fun onDiscardClicked() {
        Toast.makeText(requireActivity(), "Transaction discarded.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    /**
     * On save button clicked, save to database.
     */
    private fun onSaveClicked() {
        var payUid = ""
        if (binding.payCb.isChecked) {
            payUid = transactionsViewModel.uid
        }

        val transaction = Transaction().apply {
            transactionName = binding.nameEt.text.toString().trim()
            cost = binding.costEt.text.toString().trim().toDouble()
            isShared = binding.sharedCb.isChecked
            ownerUid = transactionsViewModel.uid
            payerUid = payUid
            city = transactionsViewModel.city
            country = transactionsViewModel.country
            dateEpoch = transactionsViewModel.dateEpoch
            people = ArrayList()
        }

        transactionsViewModel.addEntry(transaction)

        // On finish
        Toast.makeText(requireActivity(), "Transaction saved.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    /**
     * On isShared checked, re-enable check box to determine who paid.
     */
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == null) return

        if (buttonView.id == R.id.shared_cb) {
            binding.payCb.isEnabled = isChecked
        }
    }

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
}