package com.group19.stashup.login

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthRepository(private val activity: Activity) {
    private var auth: FirebaseAuth = Firebase.auth
    private var user: MutableLiveData<FirebaseUser> = MutableLiveData()
    private var isReset: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Checks if currently logged in.
     */
    init {
        if (auth.currentUser != null) {
            user.postValue(auth.currentUser)
        }
    }

    /**
     * Creates a new user with given [email] and [password].
     */
    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                user.postValue(auth.currentUser)
            } else {
                user.postValue(null)
                Toast.makeText(
                    activity, "${it.exception!!.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Login with given [email] and [password].
     */
    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                user.postValue(auth.currentUser)
            } else {
                user.postValue(null)
                Toast.makeText(
                    activity, "${it.exception!!.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Logout of current user.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Resets the password for [email].
     */
    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                isReset.postValue(true)
            } else {
                isReset.postValue(false)
                Toast.makeText(
                    activity, "${it.exception!!.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Returns a MutableLiveData of FirebaseUser
     * @return [MutableLiveData]
     */
    fun getUser(): MutableLiveData<FirebaseUser> {
        return user
    }

    /**
     * Returns a MutableLiveData of Boolean
     * @return [MutableLiveData]
     */
    fun passwordResetStatus(): MutableLiveData<Boolean> {
        return isReset
    }
}