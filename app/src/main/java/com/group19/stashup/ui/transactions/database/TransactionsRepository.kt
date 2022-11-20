package com.group19.stashup.ui.transactions.database

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class TransactionsRepository {
    private var hasAllEntries: MutableLiveData<Boolean> = MutableLiveData(false)
    private var transactionList: MutableLiveData<ArrayList<Transaction>> = MutableLiveData()

    private var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser = auth.currentUser!!

    private var database: DatabaseReference = Firebase.database.reference.child("transaction")

    /**
     * Adds an entry of [transaction] into realtime database.
     */
    fun addEntry(transaction: Transaction) {
        CoroutineScope(IO).launch {
            val key = database.push().key
            database.child(key.toString()).setValue(transaction)
            transaction.people.forEach {
                database.child(key.toString()).setValue(it)
            }
            updatePeople(key.toString(), user.displayName.toString())
        }
    }

    /**
     * Gets all entries of user uid.
     */
    fun getAllEntries() {
        CoroutineScope(IO).launch {
            val transactions: ArrayList<Transaction> = ArrayList()

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        val transaction = Transaction().apply {
                            transactionUid = it.key.toString()
                            transactionName = it.child("transactionName").value.toString()
                            cost = it.child("cost").value.toString().toDouble()
                            isShared = it.child("shared").value.toString().toBoolean()
                            ownerUid = it.child("ownerUid").value.toString()
                            payerUid = it.child("payerUid").value.toString()
                            city = it.child("city").value.toString()
                            country = it.child("country").value.toString()
                            dateEpoch = it.child("dateEpoch").value.toString().toLong()
                        }
                        val people: ArrayList<String> = ArrayList()
                        it.child("people").children.forEach { ds ->
                            people.add(ds.value.toString())
                        }
                        transaction.people = people
                        transactions.add(transaction)
                    }
                    hasAllEntries.postValue(true)
                    transactionList.postValue(transactions)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ValueEventListener", error.message)
                }
            }

            database.orderByChild("ownerUid").equalTo(user.uid)
                .addListenerForSingleValueEvent(valueEventListener)
        }
    }

    /**
     * Deletes transaction with [transactionUid].
     */
    fun deleteEntry(transactionUid: String) {
        CoroutineScope(IO).launch {
            database.child(transactionUid).removeValue()
        }
    }

    /**
     * Add [name] list with [transactionUid].
     */
    fun updatePeople(transactionUid: String, name: String) {
        CoroutineScope(IO).launch {
            val key = database.push().key
            database.child(transactionUid).child("people").child(key.toString()).setValue(name)

            val updated: ArrayList<String> = ArrayList()
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        return
                    }

                    val children = snapshot.children
                    children.forEach {
                        if (updated.contains(it.key.toString())) return@forEach

                        updated.add(it.key.toString())
                        val peopleKey =
                            database.child(it.key.toString()).child("people").push().key.toString()
                        database.child(it.key.toString()).child("people").child(peopleKey)
                            .setValue(name)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ValueEventListener", error.message)
                }

            }
            database.orderByChild("transactionUid").equalTo(transactionUid)
                .addListenerForSingleValueEvent(valueEventListener)
        }
    }

    fun addTransactionByUid(tUid: String, context: Context) {
        CoroutineScope(IO).launch {
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(
                            context, "Could not find transaction.", Toast.LENGTH_SHORT
                        ).show()
                        hasAllEntries.postValue(true)
                        return
                    }

                    val transaction = Transaction().apply {
                        transactionUid = snapshot.key.toString()
                        transactionName = snapshot.child("transactionName").value.toString()
                        cost = snapshot.child("cost").value.toString().toDouble()
                        isShared = snapshot.child("shared").value.toString().toBoolean()
                        ownerUid = user.uid
                        payerUid = snapshot.child("payerUid").value.toString()
                        city = snapshot.child("city").value.toString()
                        country = snapshot.child("country").value.toString()
                        dateEpoch = snapshot.child("dateEpoch").value.toString().toLong()
                    }
                    val people: ArrayList<String> = ArrayList()
                    snapshot.child("people").children.forEach { ds ->
                        people.add(ds.value.toString())
                    }
                    people.add(user.displayName.toString())
                    transaction.people = people
                    val key = database.push().key.toString()
                    database.child(key).setValue(transaction)
                    Toast.makeText(
                        context, "Successfully added transaction.", Toast.LENGTH_SHORT
                    ).show()
                    hasAllEntries.postValue(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ValueEventListener", error.message)
                }

            }
            database.child(tUid).addListenerForSingleValueEvent(valueEventListener)
        }
    }

    /**
     * Returns uid of user.
     */
    fun getUid(): String {
        return user.uid
    }

    /**
     * Returns the status of data retrieval.
     */
    fun dataStatus(): MutableLiveData<Boolean> {
        return hasAllEntries
    }

    /**
     * Returns the transaction list.
     */
    fun getTransactionList(): MutableLiveData<ArrayList<Transaction>> {
        return transactionList
    }

    /**
     * Returns the name of the user.
     */
    fun getName(): String {
        return user.displayName.toString()
    }
}