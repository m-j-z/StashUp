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
    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>

    private lateinit var transaction: Transaction

    /**
     * On creation of fragment, check if it is launched to edit a transaction instead.
     */
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            transaction = requireArguments().getParcelable("transaction")!!
        }
    }

    /**
     * Create binding and run necessary methods to setup view.
     */
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
        // Populate spinner.
        spinnerAdapter = ArrayAdapter.createFromResource(requireActivity(), R.array.expense_categories, R.layout.spinner_layout)
        binding.spinner.adapter = spinnerAdapter

        // Set onClickListeners.
        binding.sharedCb.setOnCheckedChangeListener(this)
        binding.setLocationCv.setOnClickListener(this)
        binding.discardBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
    }

    /**
     * Load view models.
     */
    private fun loadViewModelData() {
        // Create MainViewModel.
        countryCityViewModel = ViewModelProvider(this)[CountryCityViewModel::class.java]
        countryCityViewModel.loadData(requireActivity())

        // Create TransactionViewModel.
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        // Load data from passed Transaction.
        if (this::transaction.isInitialized) {
            transactionsViewModel.transactionName = transaction.transactionName
            transactionsViewModel.cost = transaction.cost
            transactionsViewModel.category = transaction.category
            transactionsViewModel.city = transaction.city
            transactionsViewModel.country = transaction.country
            transactionsViewModel.isShared = transaction.isShared
            transactionsViewModel.creatorPaid = transaction.ownerUid == transaction.payerUid

            if (transaction.ownerUid != transaction.payerUid) {
                binding.nameEt.isEnabled = false
                binding.costEt.isEnabled = false
                binding.spinner.isEnabled = false
                binding.setLocationCv.isClickable = false
                binding.sharedCb.isClickable = false
                binding.payCb.isClickable = false

                binding.discardBtn.visibility = Button.GONE
                binding.saveBtn.visibility = Button.GONE
            }
        }

        // Load data.
        binding.nameEt.setText(transactionsViewModel.transactionName)
        binding.costEt.setText(transactionsViewModel.cost.toString())
        binding.spinner.setSelection(spinnerAdapter.getPosition(transactionsViewModel.category))
        val text = "${transactionsViewModel.city}, ${transactionsViewModel.country}"
        binding.setLocationTv.text = text
        binding.sharedCb.isChecked = transactionsViewModel.isShared
        binding.payCb.isChecked = transactionsViewModel.creatorPaid
    }

    /**
     * Remove binding onDestroyView.
     */
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
            // Create dialog that displays all countries and then cities.
            R.id.set_location_cv -> {
                requireView().findViewById<LinearLayout>(R.id.linear_layout).visibility =
                    LinearLayout.GONE
                requireView().findViewById<ProgressBar>(R.id.progress_bar).visibility =
                    ProgressBar.VISIBLE
                onSetLocationClicked()
            }

            // Discard entry and return to previous fragment.
            R.id.discard_btn -> onDiscardClicked()

            // Save transaction to database.
            R.id.save_btn -> {
                if (checkFields()) {
                    onSaveClicked()
                }
            }
        }
    }

    /**
     * Check validity of fields.
     */
    private fun checkFields(): Boolean {
        val transactionName = binding.nameEt.text.toString()
        val cost = binding.costEt.text.toString().toDouble()

        // Check if name is empty.
        if (transactionName.isEmpty()) {
            binding.nameEt.error = "Transaction name can not be empty."
            binding.nameEt.requestFocus()
        }

        // Check if total cost is <= 0.
        if (cost <= 0.0) {
            binding.costEt.error = "Enter the cost of the transaction."
            binding.costEt.requestFocus()
        }

        // Return true if conditions above are met.
        if (transactionName.isNotEmpty() && cost > 0.0) {
            return true
        }

        return false
    }

    /**
     * On set location clicked, create a dialog to show countries and then cities.
     */
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
                searchView.setQuery("", false)
                // remove data if pressed
                if (transactionsViewModel.country.isNotEmpty() && transactionsViewModel.city.isNotEmpty()) {
                    transactionsViewModel.country = ""
                    transactionsViewModel.city = ""
                }

                // Get item at position.
                val item = listView.adapter.getItem(position).toString()

                // If country is empty, add to country.
                if (transactionsViewModel.country.isEmpty()) {
                    transactionsViewModel.country = item
                    val cityList = countryCityViewModel.getCities(item)
                    listAdapter = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, cityList
                    )
                    currentList = cityList
                    listView.adapter = listAdapter
                } else { // else add to city.
                    transactionsViewModel.city = item
                    val text = "${transactionsViewModel.city}, ${transactionsViewModel.country}"
                    binding.setLocationTv.text = text
                    dialog.cancel()
                }
            }

            // Set view, create dialog, show dialog.
            builder.setView(view)
            dialog = builder.create()
            dialog.show()

            // Remove progress bar.
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
        // Determine if creator of transaction is payer.
        var payUid = ""
        if (binding.payCb.isChecked) {
            payUid = transactionsViewModel.uid
        }

        // Create new transaction as specified by data fields in the view.
        val transaction = Transaction().apply {
            transactionName = binding.nameEt.text.toString().trim()
            cost = binding.costEt.text.toString().trim().toDouble()
            category = binding.spinner.selectedItem.toString()
            isShared = binding.sharedCb.isChecked
            ownerUid = transactionsViewModel.uid
            payerUid = payUid
            city = transactionsViewModel.city
            country = transactionsViewModel.country
            dateEpoch = transactionsViewModel.dateEpoch
            people = arrayListOf()
        }

        // If fragment was launched to edit a transaction, update transaction in database.
        if (this::transaction.isInitialized) {
            transaction.transactionUid = this.transaction.transactionUid
            transaction.people = this.transaction.people
            transaction.parentUid = this.transaction.parentUid
            transaction.payerUid = this.transaction.payerUid
            transactionsViewModel.updateEntry(this.transaction.transactionUid, transaction)
            findNavController().popBackStack()
        } else { // Else add as new transaction.
            transactionsViewModel.addEntry(transaction)
        }

        // On finish, show status and return to previous fragment.
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
            binding.payCb.isChecked = false
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