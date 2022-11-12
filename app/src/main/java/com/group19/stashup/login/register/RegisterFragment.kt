package com.group19.stashup.login.register

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.group19.stashup.R
import com.group19.stashup.login.AuthViewModel
import com.group19.stashup.login.AuthViewModelFactory
import com.group19.stashup.login.login.LoginFragment
import com.group19.stashup.login.LoginViewModel

class RegisterFragment : Fragment(), View.OnClickListener {
    // View models
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var authViewModel: AuthViewModel

    // Layout views
    private lateinit var parentScrollView: ScrollView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        loginViewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

        val authViewModelFactory = AuthViewModelFactory(requireActivity())
        authViewModel =
            ViewModelProvider(requireActivity(), authViewModelFactory)[AuthViewModel::class.java]

        setupLayoutViews(view)

        return view
    }

    /**
     * Initializes all Views present in the layout.
     * Sets any necessary default text.
     */
    private fun setupLayoutViews(view: View?) {
        if (view == null) return

        // Setup all Views present in the layout.
        parentScrollView = view.findViewById(R.id.scroll_view)
        nameEditText = view.findViewById(R.id.name_et)
        emailEditText = view.findViewById(R.id.email_et)
        passwordEditText = view.findViewById(R.id.password_et)
        registerButton = view.findViewById(R.id.register_btn)

        progressBar = view.findViewById(R.id.progress_bar)

        // Set text for name and email only, if saved.
        nameEditText.setText(loginViewModel.name)
        emailEditText.setText(loginViewModel.email)

        // Set OnClickListeners
        registerButton.setOnClickListener(this)

        // Set observers
        authViewModel.getUser().observe(viewLifecycleOwner) {
            progressBar.visibility = ProgressBar.GONE
            parentScrollView.visibility = ScrollView.VISIBLE

            if (it == null) return@observe

            updateUser(it, requireActivity())
            returnToLogin(requireActivity())
        }
    }

    /**
     * Updates [user] with name provided by EditText.
     */
    private fun updateUser(user: FirebaseUser, activity: FragmentActivity) {
        val name = nameEditText.text.toString().trim()
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }

        user.updateProfile(profileUpdates).addOnCompleteListener { update ->
            // If update was successful, show status message and finish activity.
            if (update.isSuccessful) {
                Toast.makeText(
                    activity, "Successfully registered user!", Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = ProgressBar.GONE
                parentScrollView.visibility = ScrollView.VISIBLE
            } else {
                // If update was unsuccessful, show status message and finish activity.
                Toast.makeText(
                    activity,
                    "Registration successful but error updating profile.",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = ProgressBar.GONE
                parentScrollView.visibility = ScrollView.VISIBLE
            }
        }
    }

    /**
     * Replaces RegistrationFragment with LoginFragment
     */
    private fun returnToLogin(activity: FragmentActivity) {
        authViewModel.logout()
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, LoginFragment.newInstance()).commit()
    }

    /**
     * Determines actions to perform depending on the View clicked.
     */
    override fun onClick(v: View?) {
        if (v == null) {
            return
        }

        when (v.id) {
            R.id.register_btn -> {
                if (checkRegistrationFields()) {
                    registerUser()
                }
            }
        }
    }

    /**
     * Checks all registration fields for validity.
     * @return true if all fields satisfies conditions, false otherwise
     */
    private fun checkRegistrationFields(): Boolean {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Check password EditText.
        if (password.length < 8) {
            passwordEditText.error = "Your password must be at least 8 characters in length"
            passwordEditText.requestFocus()
        }

        // Check email EditText.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid Email was entered."
            emailEditText.requestFocus()
        }

        // Check name EditText.
        if (name.isEmpty()) {
            nameEditText.error = "Please enter a name."
            nameEditText.requestFocus()
        }

        // Return if all conditions satisfied.
        if (password.length >= 8 && Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() && name.isNotEmpty()
        ) {
            return true
        }

        // Return if none or some satisfies conditions.
        return false
    }

    /**
     * Register user with email and password provided by EditText.
     */
    private fun registerUser() {
        // Show progress bar.
        parentScrollView.visibility = ScrollView.GONE
        progressBar.visibility = ProgressBar.VISIBLE

        // Get fields.
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Create a user on firebase with given email and password.
        authViewModel.register(email, password)
    }

    override fun onPause() {
        super.onPause()
        loginViewModel.name = nameEditText.text.toString()
        loginViewModel.email = emailEditText.text.toString()
    }

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }
}