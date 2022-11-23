package com.group19.stashup.ui.transactions.database

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime

class TransactionsViewModel : ViewModel() {
    private var transactionsRepository = TransactionsRepository()

    var name = transactionsRepository.getName()
    var uid = transactionsRepository.getUid()
    var transactionName: String = ""
    var cost: Double = 0.0
    var isShared: Boolean = false
    var creatorPaid: Boolean = false
    var country: String = "Canada"
    var city: String = "Burnaby"
    var dateEpoch: Long = 0

    init {
        val now = LocalDateTime.now()
        dateEpoch = now.toEpochSecond(OffsetDateTime.now().offset)
    }

    /**
     * Adds an entry of [transaction] into realtime database.
     */
    fun addEntry(transaction: Transaction) {
        transactionsRepository.addEntry(transaction)
    }

    /**
     * Gets all entries of user uid.
     */
    fun getAllEntries() {
        transactionsRepository.getAllEntries()
    }

    /**
     * Deletes transaction with [transactionUid].
     */
    fun deleteEntry(transactionUid: String) {
        transactionsRepository.deleteEntry(transactionUid)
    }

    /**
     * Add [name] list with [transactionUid].
     */
    fun updatePeople(transactionUid: String, name: String) {
        transactionsRepository.updatePeople(transactionUid, name)
    }

    /**
     * Get transaction with [tUid].
     */
    fun addTransactionByUid(tUid: String, context: Context) {
        transactionsRepository.addTransactionByUid(tUid, context)
    }

    /**
     * Returns the status of data retrieval.
     */
    fun dataStatus(): MutableLiveData<Boolean> {
        return transactionsRepository.dataStatus()
    }

    /**
     * Returns the transaction list.
     */
    fun getTransactionList(): MutableLiveData<ArrayList<Transaction>> {
        return transactionsRepository.getTransactionList()
    }
}