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
import com.group19.stashup.ui.transactions.database.TransactionsViewModel

class ViewTransactionFragment : Fragment(), View.OnClickListener {

    // Binding
    private var _binding: FragmentViewTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var transaction: Transaction
    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var currencyCode: String
    private lateinit var people: ArrayList<String>

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewTransactionBinding.inflate(inflater, container, false)
        transaction = requireArguments().getParcelable("transaction")!!

        // Add overflow menu.
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        mainViewModel.menuId.postValue(R.menu.view_transaction_menu)
        mainViewModel.transaction = transaction

        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        setViewsInLayout()

        return binding.root
    }

    /**
     * Set data as described by [transaction].
     */
    private fun setViewsInLayout() {
        val preference = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        currencyCode = preference.getString("currency", "CAD").toString()

        binding.deleteBtn.setOnClickListener(this)
        binding.addPersonBtn.setOnClickListener(this)

        binding.transactionNameTv.text = transaction.transactionName
        val cost = "$ ${String.format("%.2f", transaction.cost)} $currencyCode"

        var numOfPeople = transaction.people.size
        if (numOfPeople == 0) {
            numOfPeople = 1
        }
        var yourCost =
            "-$ ${String.format("%.2f", transaction.cost / numOfPeople)} $currencyCode"
        if (transaction.ownerUid == transaction.payerUid) {
            binding.payReceiveTv.setTextColor(requireActivity().getColor(R.color.green))
            yourCost = "+$ ${
                String.format(
                    "%.2f",
                    transaction.cost - (transaction.cost / numOfPeople)
                )
            } $currencyCode"
        }

        binding.payReceiveTv.text = yourCost
        binding.totalCostTv.text = cost
        if (!transaction.isShared) {
            binding.listViewCv.visibility = MaterialCardView.INVISIBLE
            binding.addPersonBtn.visibility = Button.INVISIBLE
        }

        if (transaction.ownerUid != transaction.payerUid) {
            binding.addPersonBtn.visibility = Button.INVISIBLE
        }

        people = transaction.people
        val listAdapter = PersonListViewAdapter(people, transaction, requireActivity())
        binding.listView.adapter = listAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            R.id.delete_btn -> {
                transactionsViewModel.deleteEntry(transaction.transactionUid)
                findNavController().popBackStack()
            }
            R.id.add_person_btn -> {
                onAddPersonClicked()
            }
        }
    }

    /**
     * On add person clicked, add to list view and to realtime database.
     */
    private fun onAddPersonClicked() {
        val builder = AlertDialog.Builder(requireActivity())

        val editText = EditText(requireActivity())
        builder.apply {
            setTitle("Enter the name:")
            setView(editText)
            setPositiveButton("OK") { _: DialogInterface, _: Int ->
                people.add(editText.text.toString())
                val listAdapter = PersonListViewAdapter(people, transaction, requireActivity())
                binding.listView.adapter = listAdapter
                transactionsViewModel.updatePeople(
                    transaction.transactionUid,
                    editText.text.toString()
                )

                var yourCost =
                    "-$ ${
                        String.format(
                            "%.2f",
                            transaction.cost / transaction.people.size
                        )
                    } $currencyCode"
                if (transaction.ownerUid == transaction.payerUid) {
                    binding.payReceiveTv.setTextColor(requireActivity().getColor(R.color.green))
                    yourCost = "+$ ${
                        String.format(
                            "%.2f",
                            transaction.cost - (transaction.cost / transaction.people.size)
                        )
                    } $currencyCode"
                }
                binding.payReceiveTv.text = yourCost
            }
            setNegativeButton("CANCEL") { _: DialogInterface, _: Int -> }
        }
        builder.create().show()
    }
}