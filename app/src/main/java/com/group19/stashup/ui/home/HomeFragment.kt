package com.group19.stashup.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.android.gms.common.internal.Objects.ToStringHelper
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentHomeBinding
import com.group19.stashup.ui.transactions.database.*
import org.w3c.dom.Text


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var transactionListView: ListView
    private lateinit var navc: NavController
    private lateinit var textView: Text
    private lateinit var transactions : List<Transaction>


    private lateinit var repository: TransactionsRepository
    private lateinit var transactionsVM: TransactionsViewModel
    private lateinit var transactionsbalance: Transaction
    private lateinit var factory: TransactionViewModelFactory
    private lateinit var arrayList: ArrayList<com.google.firebase.database.Transaction>
    private lateinit var arrayAdapter: TransactionListViewAdapter
    //private var sum=800.0
    private var sum=0.0


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
              //  sum += (transactionsVM.cost / transactionsVM.peopleList.size)
                transactionList.add(item)

                  sum =  (transactionsbalance.cost/transactionsbalance.people.size)



                binding.balancetv.text = sum.toString()

//                val listAdapter = TransactionListViewAdapter(transactionList, requireActivity())
//                binding.balancetv.text = getText(sum)


// WALLET BALANCE
//                val people:ArrayList<Transaction> = ArrayList()
//                transactionsbalance.people.forEach{item->
//                   transactionList.sumOf { cost/people.size }
//                    sum= cost/ people.size }
//
//                }
//
//                sum = transactionList.sumOf { cost / people.size }
//
//
//                for (item in transactionList) {
//
//                    sum = cost / people.size
//                }
//                sum = 800 + pay_recieve
//
//
//
//
//                println(sum)


//            val listAdapterw = TransactionListViewAdapter(transactionsbalance, requireActivity())
//            binding.balancetv.findViewById<TextView>(R.id.balancetv)


                // On click of list view item, start ViewTransactionFragment.
//        binding.balancetv.setOnItemClickListener { _, _, position, _ ->
//            val item = binding.lv.adapter.getItem(position)
//            val bundle = bundleOf("transaction" to item)
                //  navc.navigate(R.id.action_nav_transactions_to_viewTransactionFragment, bundle)
// Create new adapter and set list view adapter to display data.


//                val Adapter = TransactionListViewAdapter(transactionList, requireActivity())
//                binding.balancetv.adapter = Adapter

                // Create new adapter and set list view adapter to display data.
                val listAdapter = TransactionListViewAdapter(transactionList, requireActivity())
                binding.lv.adapter = listAdapter



                // On click of list view item, start ViewTransactionFragment.
                binding.lv.setOnItemClickListener { _, _, position, _ ->
                    val item = binding.lv.adapter.getItem(position)
                    val bundle = bundleOf("transaction" to item)
                    //  navc.navigate(R.id.action_nav_transactions_to_viewTransactionFragment, bundle)
                }
            }
        }



        return binding.root
    }

    private fun updateDashboard(){
        val data = transactions[id]

        if (data.ownerUid == data.payerUid){


        val totalAmount = transactions.map { it.cost }.sum()
        val budgetAmount = transactions.filter { it.cost>0 }.map{it.cost}.sum()
        val expenseAmount = totalAmount - budgetAmount

       // cost = "+$currencySymbol ${String.format("%.2f", data.cost)}"

        var budget=binding.budget
        var expense=binding.expense


        budget.text = "$ %.2f".format(budgetAmount)
        expense.text = "$ %.2f".format(expenseAmount)

        }
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


