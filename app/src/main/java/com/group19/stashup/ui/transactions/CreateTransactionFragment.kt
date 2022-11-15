package com.group19.stashup.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.group19.stashup.R
import com.group19.stashup.databinding.FragmentCreateTransactionBinding
import com.group19.stashup.ui.transactions.database.Transaction
import com.group19.stashup.ui.transactions.database.TransactionsViewModel

class CreateTransactionFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {

    // Bindings
    private var _binding: FragmentCreateTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var paidCheckBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTransactionBinding.inflate(inflater, container, false)

        initializeViewsInLayout()
        loadTransactionViewModelData()

        return binding.root
    }

    private fun initializeViewsInLayout() {
        paidCheckBox = binding.payCb
        binding.sharedCb.setOnCheckedChangeListener(this)
        binding.discardBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
    }

    private fun loadTransactionViewModelData() {
        // Create TransactionViewModel
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        // Load data
        binding.nameEt.setText(transactionsViewModel.transactionName)
        binding.costEt.setText(transactionsViewModel.cost.toString())
        binding.sharedCb.isChecked = transactionsViewModel.isShared
        binding.payCb.isChecked = transactionsViewModel.creatorPaid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        transactionsViewModel.apply {
            transactionName = binding.nameEt.text.toString()
            cost = binding.costEt.text.toString().toDouble()
            isShared = binding.sharedCb.isChecked
            creatorPaid = binding.sharedCb.isChecked
        }
    }

    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            R.id.discard_btn -> onDiscardClicked()
            R.id.save_btn -> onSaveClicked()
        }
    }

    private fun onDiscardClicked() {
        Toast.makeText(requireActivity(), "Transaction discarded.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun onSaveClicked() {
        var payUid = ""
        if (binding.payCb.isChecked) {
            payUid = transactionsViewModel.uid
        }

        val transaction = Transaction().apply {
            transactionName = binding.nameEt.text.toString().trim()
            cost = binding.costEt.text.toString().trim().toDouble()
            isShared = binding.sharedCb.isChecked
            ownerUid = transactionsViewModel.uid
            payerUid = payUid
        }

        transactionsViewModel.addEntry(transaction)

        // On finish
        Toast.makeText(requireActivity(), "Transaction saved.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == null) return

        if (buttonView.id == R.id.shared_cb) {
            binding.payCb.isEnabled = isChecked
        }
    }
}