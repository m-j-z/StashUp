package com.group19.stashup.ui.transactions.database

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class TransactionsViewModel : ViewModel() {
    private var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser = auth.currentUser!!

    private var database: DatabaseReference = Firebase.database.reference.child("transaction")

    var uid = user.uid
    var transactionName: String = ""
    var cost: Double = 0.0
    var isShared: Boolean = false
    var creatorPaid: Boolean = false

    fun addEntry(transaction: Transaction) {
        CoroutineScope(IO).launch {
            val key = database.push().key
            database.child(key.toString()).setValue(transaction)
        }
    }
}