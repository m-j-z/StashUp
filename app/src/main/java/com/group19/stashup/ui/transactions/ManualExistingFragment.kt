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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualExistingBinding.inflate(inflater, container, false)

        // ViewModels
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        // Set onClickListeners
        binding.addTransactionBtn.setOnClickListener(this)

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            R.id.add_transaction_btn -> {
                if (checkFields()) {
                    binding.linearLayout.visibility = LinearLayout.GONE
                    binding.progressBar.visibility = ProgressBar.VISIBLE
                    transactionsViewModel.listUpdated.observe(viewLifecycleOwner) {
                        if (!it) return@observe

                        binding.progressBar.visibility = ProgressBar.GONE
                        binding.linearLayout.visibility = LinearLayout.VISIBLE
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    transactionsViewModel.addTransactionByUid(
                        binding.transactionIdEt.text.toString(),
                        requireActivity()
                    )
                }
            }
        }
    }

    private fun checkFields(): Boolean {
        val tUid = binding.transactionIdEt.text.toString()

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