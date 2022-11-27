package com.group19.stashup.ui.transactions.database

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class TransactionsRepository(tUid: String) {
    var transactionList: ArrayList<Transaction> = ArrayList()
    var listUpdated: MutableLiveData<Boolean> = MutableLiveData(false)

    var peopleList: ArrayList<String> = ArrayList()
    var peopleUpdated: MutableLiveData<Boolean> = MutableLiveData(false)

    private var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser = auth.currentUser!!

    private var database: DatabaseReference = Firebase.database.reference.child("transaction")

    /**
     * Add listener for list of people.
     * Add listener for list of transactions.
     */
    init {
        setListenerForPeople(tUid)
        addChildListener()
    }

    /**
     * Create a listener for a particular transaction for live updates of list of persons.
     */
    private fun setListenerForPeople(tUid: String) {
        if (tUid == "") return

        CoroutineScope(IO).launch {
            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (!snapshot.exists()) return

                    peopleList.add(snapshot.value.toString())
                    peopleUpdated.postValue(true)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    return
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    return
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    return
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChildEventListener", error.message)
                }

            }
            database.child(tUid).child("people").addChildEventListener(childEventListener)
        }
    }

    /**
     * Adds a child listener that will update transaction list as needed.
     */
    private fun addChildListener() {
        CoroutineScope(IO).launch {
            val childListener = object : ChildEventListener {
                // Filters out newly added children that do not have the same UID as the currently
                // signed in user.
                // Adds transaction to transactionList if it does.
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.child("ownerUid").value.toString() != user.uid) return

                    // Create new transaction.
                    val transaction = Transaction().apply {
                        transactionUid = snapshot.child("transactionUid").value.toString()
                        transactionName = snapshot.child("transactionName").value.toString()
                        cost = snapshot.child("cost").value.toString().toDouble()
                        isShared = snapshot.child("shared").value.toString().toBoolean()
                        ownerUid = snapshot.child("ownerUid").value.toString()
                        payerUid = snapshot.child("payerUid").value.toString()
                        city = snapshot.child("city").value.toString()
                        country = snapshot.child("country").value.toString()
                        dateEpoch = snapshot.child("dateEpoch").value.toString().toLong()
                        parentUid = snapshot.child("parentUid").value.toString()
                    }

                    // Add people to list.
                    val people: ArrayList<String> = ArrayList()
                    snapshot.child("people").children.forEach { ds ->
                        people.add(ds.value.toString())
                    }
                    transaction.people = people

                    // Add to list.
                    addToTransactionList(transaction)
                    listUpdated.postValue(true)
                }

                // Filters out changed children that do not have the same UID as the currently
                // signed in user.
                // Update transaction in transactionList if it does.
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.child("ownerUid").value.toString() != user.uid) return

                    // Create transaction.
                    val transaction = Transaction().apply {
                        transactionUid = snapshot.child("transactionUid").value.toString()
                        transactionName = snapshot.child("transactionName").value.toString()
                        cost = snapshot.child("cost").value.toString().toDouble()
                        isShared = snapshot.child("shared").value.toString().toBoolean()
                        ownerUid = snapshot.child("ownerUid").value.toString()
                        payerUid = snapshot.child("payerUid").value.toString()
                        city = snapshot.child("city").value.toString()
                        country = snapshot.child("country").value.toString()
                        dateEpoch = snapshot.child("dateEpoch").value.toString().toLong()
                        parentUid = snapshot.child("parentUid").value.toString()
                    }

                    // Add people to list.
                    val people: ArrayList<String> = ArrayList()
                    snapshot.child("people").children.forEach { ds ->
                        people.add(ds.value.toString())
                    }
                    transaction.people = people

                    // Update list.
                    updateTransactionList(transaction)
                    listUpdated.postValue(true)
                }

                // Remove transaction with tUid if shared with user or belongs to user.
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val tUid = snapshot.child("transactionUid").value.toString()
                    removeFromTransactionList(tUid)
                    listUpdated.postValue(true)
                }

                // Should never be moved.
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    return
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChildEventListener", error.message)
                }
            }

            // To remove progress bar if there is no data to display on list view.
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listUpdated.postValue(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ValueEventListener", error.message)
                }

            }
            database.addChildEventListener(childListener)
            database.addListenerForSingleValueEvent(valueEventListener)
        }
    }

    // Adds transaction into transactionList.
    private fun addToTransactionList(transaction: Transaction) {
        CoroutineScope(IO).launch {
            transactionList.add(transaction)
        }
    }

    // Removes transaction from transactionList.
    // Checks if transaction exists inside transactionList.
    // Remove if it does.
    private fun removeFromTransactionList(tUid: String) {
        CoroutineScope(IO).launch {
            var removeIndex: Int = -1
            for (i in transactionList.indices) {
                if (transactionList[i].transactionUid == tUid) {
                    removeIndex = i
                    break
                }
            }

            if (removeIndex != -1) {
                transactionList.removeAt(removeIndex)
            }
        }
    }

    // Updates transactionList.
    // Checks if transaction exists inside transactionList.
    // Set if it does, add otherwise.
    private fun updateTransactionList(transaction: Transaction) {
        CoroutineScope(IO).launch {
            var updateIndex: Int = -1
            for (i in transactionList.indices) {
                if (transactionList[i].transactionUid == transaction.transactionUid) {
                    updateIndex = i
                    break
                }
            }

            if (updateIndex != -1) {
                transactionList[updateIndex] = transaction
            } else {
                addToTransactionList(transaction)
            }
        }
    }

    /**
     * Adds an entry of [transaction] into realtime database.
     */
    fun addEntry(transaction: Transaction) {
        CoroutineScope(IO).launch {
            // Get UID of new transaction.
            val key = database.push().key.toString()

            // Set UID of transaction.
            transaction.transactionUid = key
            transaction.people.add(user.displayName.toString())

            // Add transaction to database.
            database.child(key).setValue(transaction)
        }
    }

    /**
     * Deletes transaction with [transactionUid].
     */
    fun deleteEntry(transactionUid: String) {
        CoroutineScope(IO).launch {
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return

                    snapshot.children.forEach {
                        if (it.child("parentUid").value.toString() != transactionUid) return@forEach
                        it.ref.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ValueEventListener", error.message)
                }
            }
            database.orderByChild("parentUid").equalTo(transactionUid).addListenerForSingleValueEvent(valueEventListener)
            database.child(transactionUid).removeValue()
        }
    }

    /**
     * Set all transactions with [transactionUid] with list [people].
     */
    fun updatePeople(transactionUid: String, people: ArrayList<String>) {
        CoroutineScope(IO).launch {
            database.child(transactionUid).child("people").setValue(people)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        database.child(it.key.toString()).child("people").setValue(people)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ValueEventListener", error.message)
                }
            }

            database.orderByChild("parentUid").equalTo(transactionUid).addListenerForSingleValueEvent(valueEventListener)
        }
    }

    /**
     * Adds transaction to transactionList with given [tUid].
     */
    fun addTransactionByUid(tUid: String, context: Context) {
        CoroutineScope(IO).launch {
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check validity.
                    if (!snapshot.exists()) {
                        Toast.makeText(
                            context, "Could not find transaction.", Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    // Create transaction.
                    val transaction = Transaction().apply {
                        transactionName = snapshot.child("transactionName").value.toString()
                        cost = snapshot.child("cost").value.toString().toDouble()
                        isShared = snapshot.child("shared").value.toString().toBoolean()
                        ownerUid = user.uid
                        payerUid = snapshot.child("payerUid").value.toString()
                        city = snapshot.child("city").value.toString()
                        country = snapshot.child("country").value.toString()
                        dateEpoch = snapshot.child("dateEpoch").value.toString().toLong()
                        parentUid = snapshot.key.toString()
                    }

                    // Add people to list.
                    val people: ArrayList<String> = ArrayList()
                    snapshot.child("people").children.forEach { ds ->
                        people.add(ds.value.toString())
                    }
                    people.add(user.displayName.toString())
                    transaction.people = people
                    updatePeople(transaction.parentUid, people)

                    // Add to database.
                    val key = database.push().key.toString()
                    transaction.transactionUid = key
                    database.child(key).setValue(transaction)
                    Toast.makeText(
                        context, "Successfully added transaction.", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ValueEventListener", error.message)
                }

            }
            database.child(tUid).addListenerForSingleValueEvent(valueEventListener)
        }
    }

    /**
     * Update entry at [tUid] with [transaction].
     */
    fun updateEntry(tUid: String, transaction: Transaction) {
        CoroutineScope(IO).launch {
            database.child(tUid).setValue(transaction)
        }
    }

    /**
     * Returns uid of user.
     */
    fun getUid(): String {
        return user.uid
    }

    /**
     * Returns the name of the user.
     */
    fun getName(): String {
        return user.displayName.toString()
    }
}