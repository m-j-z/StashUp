package com.group19.stashup.ui.transactions.database

import android.content.Context
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime

class TransactionsViewModel(tUid: String = "") : ViewModel() {
    private var transactionsRepository = TransactionsRepository(tUid)
    var transactionList = transactionsRepository.transactionList
    var listUpdated = transactionsRepository.listUpdated

    var peopleList = transactionsRepository.peopleList
    var peopleUpdated = transactionsRepository.peopleUpdated

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
     * Deletes transaction with [tUid].
     */
    fun deleteEntry(tUid: String) {
        transactionsRepository.deleteEntry(tUid)
    }

    /**
     * Add [name] list with [tUid].
     */
    fun updatePeople(tUid: String, people: ArrayList<String>) {
        transactionsRepository.updatePeople(tUid, people)
    }

    /**
     * Get transaction with [tUid].
     */
    fun addTransactionByUid(tUid: String, context: Context) {
        transactionsRepository.addTransactionByUid(tUid, context)
    }

    /**
     *
     */
    fun updateEntry(tUid: String, transaction: Transaction) {
        transactionsRepository.updateEntry(tUid, transaction)
    }
}