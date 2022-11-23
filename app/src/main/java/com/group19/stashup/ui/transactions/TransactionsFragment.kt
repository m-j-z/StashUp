package com.group19.stashup.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ListView
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentTransactionsBinding
import com.group19.stashup.ui.transactions.database.Transaction
import com.group19.stashup.ui.transactions.database.TransactionListViewAdapter
import com.group19.stashup.ui.transactions.database.TransactionsViewModel

class TransactionFragment : Fragment(), View.OnClickListener {
    // Initialize animations
    private val rotateOpenAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.rotate_open_animation
        )
    }
    private val rotateCloseAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.rotate_close_animation
        )
    }
    private val fromBottomAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.from_bottom_animation
        )
    }
    private val toBottomAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireActivity(),
            R.anim.to_bottom_animation
        )
    }

    private var fabExpanded: Boolean = false


    // Bindings and NavController initializer
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    private lateinit var transactionsViewModel: TransactionsViewModel

    /**
     * To switch to next fragment...
     * 1. val navController = findNavController()
     * 2. navController.navigate({action id})
     * Action id is found in mobile_navigation.xml
     */

    /**
     * Create TransactionViewModel on creation of fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create TransactionsViewModel.
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]
    }

    /**
     * Create binding and run necessary methods to setup view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Set binding.
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        // Set navController.
        navController = findNavController()

        // Add on click listeners.
        binding.createFab.setOnClickListener(this)
        binding.createNewCv.setOnClickListener(this)
        binding.manualExistingCv.setOnClickListener(this)
        binding.qrExistingCv.setOnClickListener(this)

        // Create listview.
        initializeListSearchView()

        return binding.root
    }

    /**
     * Get all entries of the current logged in user.
     * Add to list view.
     */
    private fun initializeListSearchView() {
        // Display progress bar.
        binding.listView.visibility = ListView.GONE
        binding.progressBar.visibility = ProgressBar.VISIBLE

        // Set observer for transaction list.
        transactionsViewModel.listUpdated.observe(viewLifecycleOwner) {
            if (!it) return@observe

            // If list is updated.
            // Remove progress bar.
            binding.progressBar.visibility = ProgressBar.GONE
            binding.listView.visibility = ProgressBar.VISIBLE

            // Add data to transaction list.
            val transactionList: ArrayList<Transaction> = ArrayList()
            transactionsViewModel.transactionList.forEach { item ->
                transactionList.add(item)
            }

            // Create new adapter and set list view adapter to display data.
            val listAdapter = TransactionListViewAdapter(transactionList, requireActivity())
            binding.listView.adapter = listAdapter
        }

        // On click of list view item, start ViewTransactionFragment.
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val item = binding.listView.adapter.getItem(position)
            val bundle = bundleOf("transaction" to item)
            navController.navigate(R.id.action_nav_transactions_to_viewTransactionFragment, bundle)
        }
    }

    /**
     * Remove binding onDestroyView.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Perform action on view clicked.
     * On FAB clicked, expand menu.
     * On create clicked, navigate to create fragment.
     * On manual existing clicked, navigate to manual existing fragment.
     * On qr existing clicked, navigate to qr existing fragment.
     */
    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            // Expand FAB menu on pressed.
            R.id.create_fab -> onFabPressed()

            // Go to CreateTransactionFragment on create new transaction clicked.
            R.id.create_new_cv -> {
                navController.navigate(R.id.action_nav_transactions_to_createTransactionFragment)
                onFabPressed()
            }

            // Go to ManualExistingFragment on get existing transaction clicked.
            R.id.manual_existing_cv -> {
                navController.navigate(R.id.action_nav_transactions_to_manualExistingFragment)
                onFabPressed()
            }

            // Go to QrExistingFragment on scan qr clicked.
            R.id.qr_existing_cv -> {
                navController.navigate(R.id.action_nav_transactions_to_qrExistingFragment)
                onFabPressed()
            }
        }
    }

    /**
     * On FAB pressed, animate and show additional options.
     */
    private fun onFabPressed() {
        if (fabExpanded) {
            // Contract FAB menu.
            binding.createFab.startAnimation(rotateCloseAnimation)
            binding.createNewCv.visibility = MaterialCardView.GONE
            binding.createNewCv.startAnimation(toBottomAnimation)
            binding.manualExistingCv.visibility = MaterialCardView.GONE
            binding.manualExistingCv.startAnimation(toBottomAnimation)
            binding.qrExistingCv.visibility = MaterialCardView.GONE
            binding.qrExistingCv.startAnimation(toBottomAnimation)
        } else {
            // Expand FAB menu.
            binding.createFab.startAnimation(rotateOpenAnimation)
            binding.createNewCv.visibility = MaterialCardView.VISIBLE
            binding.createNewCv.startAnimation(fromBottomAnimation)
            binding.manualExistingCv.visibility = MaterialCardView.VISIBLE
            binding.manualExistingCv.startAnimation(fromBottomAnimation)
            binding.qrExistingCv.visibility = MaterialCardView.VISIBLE
            binding.qrExistingCv.startAnimation(fromBottomAnimation)
        }
        fabExpanded = !fabExpanded
    }
}