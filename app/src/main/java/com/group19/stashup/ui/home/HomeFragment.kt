package com.group19.stashup.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentHomeBinding
import com.group19.stashup.ui.transactions.database.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var transactionListView: ListView

    //    private lateinit var database: InformationDatabase
//    private lateinit var databaseDao: InformationDatabaseDao
    private lateinit var repository: TransactionsRepository
    private lateinit var transactionsVM: TransactionsViewModel
    private lateinit var factory: TransactionViewModelFactory
    private lateinit var arrayList: ArrayList<com.google.firebase.database.Transaction>
    private lateinit var arrayAdapter: TransactionListViewAdapter


    var isLoaded: MutableLiveData<Boolean> = MutableLiveData(false)
    private var homeListv: ArrayList<TransactionsViewModel> = ArrayList()
    var homeList: ArrayList<String> = ArrayList()


    /**
     * To switch to next fragment...
     * 1. val navController = findNavController()
     * 2. navController.navigate({action id})
     * Action id is found in mobile_navigation.xml
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        transactionsVM = ViewModelProvider(this)[TransactionsViewModel::class.java]

        transactionsVM.listUpdated.observe(viewLifecycleOwner) {
            if (!it) return@observe

            val transactionList: ArrayList<Transaction> = ArrayList()
            transactionsVM.transactionList.forEach { item ->
                transactionList.add(item)
            }

            // Create new adapter and set list view adapter to display data.
            val listAdapter = TransactionListViewAdapter(transactionList, requireActivity())
            binding.lv.adapter = listAdapter
        }

        // On click of list view item, start ViewTransactionFragment.
        binding.lv.setOnItemClickListener { _, _, position, _ ->
            val item = binding.lv.adapter.getItem(position)
            val bundle = bundleOf("transaction" to item)
            //  navController.navigate(R.id.action_nav_transactions_to_viewTransactionFragment, bundle)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}