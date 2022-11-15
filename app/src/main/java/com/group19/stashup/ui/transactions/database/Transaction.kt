package com.group19.stashup.ui.transactions.database

data class Transaction(
    var transactionName: String = "",
    var cost: Double = 0.0,
    var isShared: Boolean = false,
    var ownerUid: String = "",
    var payerUid: String = ""
)