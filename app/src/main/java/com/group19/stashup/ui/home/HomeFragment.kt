package com.group19.stashup.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.group19.stashup.databinding.FragmentHomeBinding
import com.group19.stashup.ui.transactions.database.TransactionListViewAdapter
import com.group19.stashup.ui.transactions.database.TransactionsRepository
import com.group19.stashup.ui.transactions.database.TransactionsViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var tabs:ArrayList<Fragment>

    private lateinit var lvadapter: TransactionListViewAdapter

    private lateinit var transactionsVM: TransactionsViewModel

    private lateinit var transactionsrepo: TransactionsRepository

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

            // If list is updated.
            // Remove progress bar.


            var transactionList = transactionsrepo.transactionList
            var listUpdated = transactionsrepo.listUpdated

            val listAdapter = TransactionListViewAdapter(transactionList, requireActivity())
            binding.lv.adapter = listAdapter

        }

        // Create ViewModel.
//      /  val transactionsViewModelFactory = TransactionViewModelFactory(transaction.transactionUid)
        //  transactionsVM = ViewModelProvider(this, transactionsViewModelFactory)[TransactionsViewModel::class.java]


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}