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
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentHomeBinding
import com.group19.stashup.ui.transactions.database.Transaction
import com.group19.stashup.ui.transactions.database.TransactionRecyclerViewAdapter
import com.group19.stashup.ui.transactions.database.TransactionsViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var transactionsVM: TransactionsViewModel
    private lateinit var navController: NavController
    private lateinit var recycleAdapter: TransactionRecyclerViewAdapter
    private lateinit var currencySymbol: String

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
        navController = findNavController()

        val preference = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val currencyCode = preference.getString("currency", "CAD")
        currencySymbol = Currency.getInstance(currencyCode).symbol

        // Create layout for RecyclerView and listener for each item inside the RecyclerView
        binding.recyclerView.addItemDecoration(DividerItemDecoration(binding.recyclerView.context, LinearLayoutManager.VERTICAL))
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val listener = object : TransactionRecyclerViewAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val item = recycleAdapter.getItem(position)
                val bundle = bundleOf("transaction" to item)
                navController.navigate(R.id.action_nav_home_to_viewTransactionFragment, bundle)
            }
        }

        transactionsVM = ViewModelProvider(this)[TransactionsViewModel::class.java]
        transactionsVM.listUpdated.observe(viewLifecycleOwner) {
            if (!it) return@observe

            var sum = 0.0
            val transactionList: ArrayList<Transaction> = ArrayList()
            transactionsVM.transactionList.forEach { item ->
                transactionList.add(item)
                if (item.ownerUid == item.payerUid && item.isShared) {
                    sum += item.cost / item.people.size
                } else {
                    sum -= item.cost / item.people.size
                }
            }

            var balance = "-$currencySymbol ${String.format("%.2f", abs(sum))}"
            if (sum >= 0) {
                binding.balancetv.setTextColor(requireActivity().getColor(R.color.green))
                balance = "+$currencySymbol ${String.format("%.2f", abs(sum))}"
            }
            binding.balancetv.text = balance

            recycleAdapter = TransactionRecyclerViewAdapter(transactionList, requireActivity())
            recycleAdapter.setOnClickListener(listener)
            binding.recyclerView.adapter = recycleAdapter
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


