package com.group19.stashup.login

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(activity: Activity) : ViewModel() {
    private val authRepository = AuthRepository(activity)

    /**
     * Creates a new user with given [email] and [password].
     */
    fun register(email: String, password: String) {
        authRepository.register(email, password)
    }

    /**
     * Login with given [email] and [password].
     */
    fun login(email: String, password: String) {
        authRepository.login(email, password)
    }

    /**
     * Logout of current user.
     */
    fun logout() {
        authRepository.logout()
    }

    /**
     * Resets the password for [email].
     */
    fun resetPassword(email: String) {
        authRepository.resetPassword(email)
    }

    /**
     * Returns a MutableLiveData of FirebaseUser
     * @return [MutableLiveData]
     */
    fun getUser(): MutableLiveData<FirebaseUser> {
        return authRepository.getUser()
    }

    /**
     * Returns a MutableLiveData of Boolean
     * @return [MutableLiveData]
     */
    fun passwordResetStatus(): MutableLiveData<Boolean> {
        return authRepository.passwordResetStatus()
    }
}