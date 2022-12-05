package com.group19.stashup.ui.transactions

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView
import com.group19.stashup.MainViewModel
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentViewTransactionBinding
import com.group19.stashup.ui.transactions.database.PersonListViewAdapter
import com.group19.stashup.ui.transactions.database.Transaction
import com.group19.stashup.ui.transactions.database.TransactionViewModelFactory
import com.group19.stashup.ui.transactions.database.TransactionsViewModel
import java.util.Currency

class ViewTransactionFragment : Fragment(), View.OnClickListener {

    // Binding
    private var _binding: FragmentViewTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var transaction: Transaction
    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var currencyCode: String
    private lateinit var currencySymbol: String

    /**
     * Create binding and run necessary methods to setup view.
     */
    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Create binding.
        _binding = FragmentViewTransactionBinding.inflate(inflater, container, false)

        // Get passed transaction entity.
        transaction = requireArguments().getParcelable("transaction")!!

        // Add overflow menu.
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        mainViewModel.menuId.postValue(R.menu.view_transaction_menu)
        mainViewModel.transaction = transaction

        // Create ViewModel.
        val transactionsViewModelFactory = TransactionViewModelFactory(transaction.transactionUid)
        transactionsViewModel = ViewModelProvider(this, transactionsViewModelFactory)[TransactionsViewModel::class.java]

        setViewsInLayout()

        return binding.root
    }

    /**
     * Set data as described by [transaction].
     */
    private fun setViewsInLayout() {
        // Get currency code and symbol.
        val preference = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        currencyCode = preference.getString("currency", "CAD").toString()
        currencySymbol = Currency.getInstance(currencyCode).symbol

        // Set transaction name.
        binding.transactionNameTv.text = transaction.transactionName

        // Set total cost of the transaction.
        val cost = "$currencySymbol ${String.format("%.2f", transaction.cost)}"
        binding.totalCostTv.text = cost

        // Get number of people.
        val numOfPeople = transaction.people.size

        // Set pay/receive.
        var yourCost =
            "-$currencySymbol ${String.format("%.2f", transaction.cost / numOfPeople)}"
        if (transaction.ownerUid == transaction.payerUid && transaction.isShared) {
            binding.payReceiveTv.setTextColor(requireActivity().getColor(R.color.green))
            var receive = transaction.cost
            if (numOfPeople > 1) {
                receive -= transaction.cost / numOfPeople
            }
            yourCost = "+$currencySymbol ${
                String.format(
                    "%.2f",
                    receive
                )
            }"
        }
        binding.payReceiveTv.text = yourCost

        // Display button or list view depending on transaction type.
        if (!transaction.isShared) {
            binding.listViewCv.visibility = MaterialCardView.INVISIBLE
            binding.addPersonBtn.visibility = Button.INVISIBLE
        }

        if (transaction.ownerUid != transaction.payerUid) {
            binding.addPersonBtn.visibility = Button.INVISIBLE
        }

        // Set observer for list view of people stored in transaction.
        transactionsViewModel.peopleUpdated.observe(viewLifecycleOwner) {
            if (!it) return@observe

            val listAdapter = PersonListViewAdapter(transactionsViewModel.peopleList, transaction, requireActivity())
            binding.listView.adapter = listAdapter
        }

        // Set onClickListeners for buttons.
        binding.deleteBtn.setOnClickListener(this)
        binding.addPersonBtn.setOnClickListener(this)
    }

    /**
     * Remove binding onDestroyView
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Determines action onClick of [v].
     */
    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            // OnDeletePressed, delete entry from Firebase and return to previous fragment.
            R.id.delete_btn -> {
                transactionsViewModel.deleteEntry(transaction.transactionUid)
                findNavController().popBackStack()
            }
            // OnAddPersonPressed, add person to listview and store new array to Firebase.
            R.id.add_person_btn -> {
                onAddPersonClicked()
            }
        }
    }

    /**
     * On add person clicked, create dialog and then add to list view and to realtime database if
     * positive button pressed.
     */
    private fun onAddPersonClicked() {
        val editText = EditText(requireActivity())

        // Create dialog and display EditText.
        val builder = AlertDialog.Builder(requireActivity())
        builder.apply {
            setTitle("Enter the name:")
            setView(editText)
            setPositiveButton("OK") { _: DialogInterface, _: Int ->
                // Create deep copy of people list.
                val people: ArrayList<String> = ArrayList()
                transactionsViewModel.peopleList.forEach {
                    people.add(it)
                }
                people.add(editText.text.toString())

                // Add to database.
                transactionsViewModel.updatePeople(
                    transaction.transactionUid,
                    people
                )

                // Recalculate pay/receive cost.
                var yourCost =
                    "-$currencySymbol ${
                        String.format(
                            "%.2f",
                            transaction.cost / transaction.people.size
                        )
                    }"
                if (transaction.ownerUid == transaction.payerUid) {
                    binding.payReceiveTv.setTextColor(requireActivity().getColor(R.color.green))
                    yourCost = "+$currencySymbol ${
                        String.format(
                            "%.2f",
                            transaction.cost - (transaction.cost / transaction.people.size)
                        )
                    }"
                }
                binding.payReceiveTv.text = yourCost
            }
            setNegativeButton("CANCEL") { _: DialogInterface, _: Int -> }
        }
        builder.create().show()
    }
}