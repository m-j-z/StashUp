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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        navController = findNavController()

        // Add on click listeners
        binding.createFab.setOnClickListener(this)
        binding.createNewCv.setOnClickListener(this)
        binding.manualExistingCv.setOnClickListener(this)
        binding.qrExistingCv.setOnClickListener(this)

        // Create listview
        initializeListSearchView()

        return binding.root
    }

    /**
     * Get all entries of the current logged in user.
     * Add to list view.
     */
    private fun initializeListSearchView() {
        binding.listView.visibility = ListView.GONE
        binding.progressBar.visibility = ProgressBar.VISIBLE
        transactionsViewModel.listUpdated.observe(viewLifecycleOwner) {
            if (!it) return@observe

            binding.progressBar.visibility = ProgressBar.GONE
            binding.listView.visibility = ProgressBar.VISIBLE

            val transactionList: ArrayList<Transaction> = ArrayList()
            transactionsViewModel.transactionList.forEach { item ->
                transactionList.add(item)
            }

            val listAdapter = TransactionListViewAdapter(transactionList, requireActivity())
            binding.listView.adapter = listAdapter
        }

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
            R.id.create_fab -> onFabPressed()
            R.id.create_new_cv -> {
                navController.navigate(R.id.action_nav_transactions_to_createTransactionFragment)
                onFabPressed()
            }
            R.id.manual_existing_cv -> {
                navController.navigate(R.id.action_nav_transactions_to_manualExistingFragment)
                onFabPressed()
            }
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
            binding.createFab.startAnimation(rotateCloseAnimation)
            binding.createNewCv.visibility = MaterialCardView.GONE
            binding.createNewCv.startAnimation(toBottomAnimation)
            binding.manualExistingCv.visibility = MaterialCardView.GONE
            binding.manualExistingCv.startAnimation(toBottomAnimation)
            binding.qrExistingCv.visibility = MaterialCardView.GONE
            binding.qrExistingCv.startAnimation(toBottomAnimation)
        } else {
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