package com.group19.stashup.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentManualExistingBinding
import com.group19.stashup.ui.transactions.database.TransactionsViewModel

class ManualExistingFragment : Fragment(), View.OnClickListener {

    // Bindings
    private var _binding: FragmentManualExistingBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private lateinit var transactionsViewModel: TransactionsViewModel

    /**
     * Create binding and run necessary methods to initialize views.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create binding.
        _binding = FragmentManualExistingBinding.inflate(inflater, container, false)

        // Create ViewModel.
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        // Set onClickListeners.
        binding.addTransactionBtn.setOnClickListener(this)

        return binding.root
    }

    /**
     * Remove binding onDestroyView
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Determine action onClick of [v].
     */
    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            // Add transaction to database onAddTransactionClicked.
            R.id.add_transaction_btn -> {
                if (checkFields()) {
                    // Show progress bar.
                    binding.linearLayout.visibility = LinearLayout.GONE
                    binding.progressBar.visibility = ProgressBar.VISIBLE

                    // Observe status of transaction list.
                    transactionsViewModel.listUpdated.observe(viewLifecycleOwner) {
                        if (!it) return@observe

                        // Remove progress bar.
                        binding.progressBar.visibility = ProgressBar.GONE
                        binding.linearLayout.visibility = LinearLayout.VISIBLE

                        // Return to previous fragment.
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }

                    // Call addTransactionByUid to find and add new transaction with given Uid as
                    // parent.
                    transactionsViewModel.addTransactionByUid(
                        binding.transactionIdEt.text.toString(),
                        requireActivity()
                    )
                }
            }
        }
    }

    /**
     * Check validity of fields.
     */
    private fun checkFields(): Boolean {
        val tUid = binding.transactionIdEt.text.toString()

        // Check if Uid fields is empty.
        if (tUid.isEmpty()) {
            binding.transactionIdEt.error = "Transaction ID can not be empty."
            binding.transactionIdEt.requestFocus()
        }

        if (tUid.isNotEmpty()) {
            return true
        }

        return false
    }
}