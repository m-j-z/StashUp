package com.group19.stashup.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentHomeBinding
import com.group19.stashup.ui.transactions.database.Transaction
import com.group19.stashup.ui.transactions.database.TransactionListViewAdapter
import com.group19.stashup.ui.transactions.database.TransactionsViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var navc: NavController

    private lateinit var transactionsVM: TransactionsViewModel
    private var sum = 0.0

    var isLoaded: MutableLiveData<Boolean> = MutableLiveData(false)
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

        binding.balancetv.text = sum.toString()


        transactionsVM = ViewModelProvider(this)[TransactionsViewModel::class.java]
        transactionsVM.listUpdated.observe(viewLifecycleOwner) {
            if (!it) return@observe
            val transactionList: ArrayList<Transaction> = ArrayList()
            transactionsVM.transactionList.forEach { item ->
                transactionList.add(item)
                sum += item.cost / item.people.size
            }
            binding.balancetv.text = sum.toString()

            val listAdapter = TransactionListViewAdapter(transactionList, requireActivity())
            binding.lv.adapter = listAdapter


            // On click of list view item, start ViewTransactionFragment.
            binding.lv.setOnItemClickListener { _, _, position, _ ->
                val item = binding.lv.adapter.getItem(position)
                val bundle = bundleOf("transaction" to item)
                navc.navigate(R.id.action_nav_transactions_to_viewTransactionFragment, bundle)
            }
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


//    fun main(args: Transaction) {
//
//         val transactionsList: ArrayList<Transaction> = ArrayList()
//
//
//        for (i in transactionsList) {
//            sum += cost
//            println(i)
//
//        }
//        return textView : sum
//
//    }


