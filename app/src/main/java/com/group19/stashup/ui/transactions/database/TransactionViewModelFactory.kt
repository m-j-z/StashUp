package com.group19.stashup.ui.transactions.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TransactionViewModelFactory(private val tUid: String) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            return TransactionsViewModel(tUid) as T
        }
        throw IllegalArgumentException("Error, TransactionViewModel.")
    }
}