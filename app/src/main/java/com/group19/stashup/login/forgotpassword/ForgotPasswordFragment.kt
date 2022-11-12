package com.group19.stashup.login.forgotpassword

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.group19.stashup.R
import com.group19.stashup.login.AuthViewModel
import com.group19.stashup.login.AuthViewModelFactory
import com.group19.stashup.login.LoginViewModel
import com.group19.stashup.login.login.LoginFragment

class ForgotPasswordFragment : Fragment(), View.OnClickListener {
    // View models
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var authViewModel: AuthViewModel

    // Layout views
    private lateinit var parentScrollView: ScrollView
    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button

    private lateinit var progressBar: ProgressBar

    /**
     * Inflates view.
     * Creates LoginViewModel.
     * Creates AuthViewModel.
     * @return [View]
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val authViewModelFactory = AuthViewModelFactory(requireActivity())
        authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]

        setupLayoutViews(view)

        return view
    }

    /**
     * Initializes all Views present in the layout.
     * Sets any necessary default text.
     * Set OnClickListeners.
     */
    private fun setupLayoutViews(view: View?) {
        if (view == null) return

        // Setup all Views present in the layout.
        parentScrollView = view.findViewById(R.id.scroll_view)
        emailEditText = view.findViewById(R.id.email_et)
        resetPasswordButton = view.findViewById(R.id.reset_password_btn)
        progressBar = view.findViewById(R.id.progress_bar)

        // Set text for email only, if saved.
        emailEditText.setText(loginViewModel.email)

        // Set OnClickListeners
        resetPasswordButton.setOnClickListener(this)

        // Set observers.
        authViewModel.passwordResetStatus().observe(viewLifecycleOwner) {
            progressBar.visibility = ProgressBar.GONE
            parentScrollView.visibility = ScrollView.VISIBLE

            if (it == false || it == null) return@observe

            Toast.makeText(requireActivity(), "Reset password email sent!", Toast.LENGTH_SHORT).show()
            returnToLogin(requireActivity())
        }
    }

    private fun returnToLogin(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, LoginFragment.newInstance()).commit()
    }

    /**
     * Determines actions to perform depending on the view clicked.
     */
    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            R.id.reset_password_btn -> {
                if (checkResetFields()) {
                    resetPassword()
                }
            }
        }
    }

    /**
     * Checks all reset fields for validity.
     * @return true if all fields satisfies conditions, false otherwise.
     */
    private fun checkResetFields(): Boolean {
        val email = emailEditText.text.toString()

        // Check email EditText.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid Email was entered."
            emailEditText.requestFocus()
            return false
        }
        return true
    }

    /**
     * Resets the emails password given by EditText.
     */
    private fun resetPassword() {
        // Show progress bar.
        parentScrollView.visibility = ScrollView.GONE
        progressBar.visibility = ProgressBar.VISIBLE

        // Get fields.
        val email = emailEditText.text.toString()

        // Reset password of email.
        authViewModel.resetPassword(email)
    }

    override fun onPause() {
        super.onPause()
        loginViewModel.email = emailEditText.text.toString()
    }

    companion object {
        fun newInstance() = ForgotPasswordFragment()
    }
}