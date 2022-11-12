package com.group19.stashup.login.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.group19.stashup.MainActivity
import com.group19.stashup.R
import com.group19.stashup.login.AuthViewModel
import com.group19.stashup.login.AuthViewModelFactory
import com.group19.stashup.login.LoginActivity
import com.group19.stashup.login.LoginViewModel

class LoginFragment : Fragment(), View.OnClickListener {
    // View models
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var authViewModel: AuthViewModel

    // Layout Views
    private lateinit var parentScrollView: ScrollView
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var rememberMeCheckBox: CheckBox
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var registerTextView: TextView

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
        val view = inflater.inflate(R.layout.fragment_login, container, false)

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
     * Set Observers.
     */
    private fun setupLayoutViews(view: View?) {
        if (view == null) return

        // Setup all Views present in the layout.
        parentScrollView = view.findViewById(R.id.scroll_view)
        emailEditText = view.findViewById(R.id.email_et)
        passwordEditText = view.findViewById(R.id.password_et)
        rememberMeCheckBox = view.findViewById(R.id.remember_me_cb)
        loginButton = view.findViewById(R.id.login_btn)
        forgotPasswordTextView = view.findViewById(R.id.forgot_password_tv)
        registerTextView = view.findViewById(R.id.register_tv)

        progressBar = view.findViewById(R.id.progress_bar)

        // Set text for email only, if saved.
        emailEditText.setText(loginViewModel.email)

        // Set status for rememberMeCheckBox
        val preferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        val rememberMe = preferences.getInt("remember_me", 0)
        if (rememberMe != 0) {
            rememberMeCheckBox.isChecked = true
        }

        // Set OnClickListeners
        loginButton.setOnClickListener(this)
        forgotPasswordTextView.setOnClickListener(this)
        registerTextView.setOnClickListener(this)

        // Set observers
        authViewModel.getUser().observe(viewLifecycleOwner) {
            progressBar.visibility = ProgressBar.GONE
            parentScrollView.visibility = ScrollView.VISIBLE

            if (it == null) return@observe

            Toast.makeText(requireActivity(), "Login successful!", Toast.LENGTH_SHORT).show()
            backToMainActivity()
        }
    }

    /**
     * Returns to MainActivity.
     */
    private fun backToMainActivity() {
        // Read remember me check box.
        val editor = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE).edit()
        if (rememberMeCheckBox.isChecked) {
            editor.putInt("remember_me", 1)
        } else {
            editor.putInt("remember_me", 0)
        }
        editor.apply()

        // Start MainActivity
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.putExtra("logged_in", true)
        startActivity(intent)
        requireActivity().finish()
    }

    /**
     * Determines actions to perform depending on the View clicked.
     */
    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            // On login button clicked.
            R.id.login_btn -> {
                progressBar.visibility = ProgressBar.GONE
                parentScrollView.visibility = ScrollView.VISIBLE
                if (checkLoginFields()) {
                    loginUser()
                }
            }

            // On forgot password clicked.
            R.id.forgot_password_tv -> {
                startFragment("forgot_password")
            }

            // On register clicked.
            R.id.register_tv -> {
                startFragment("register")
            }
        }
    }

    /**
     * Checks all login fields for validity.
     * @return true if all fields satisfies conditions, false otherwise.
     */
    private fun checkLoginFields(): Boolean {
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

        // Return if both satisfies conditions.
        if (password.length >= 8 && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true
        }

        // Return if one or neither satisfies conditions.
        return false
    }

    /**
     * Login user with email and password provided by EditText.
     */
    private fun loginUser() {
        // Show progress bar.
        parentScrollView.visibility = ScrollView.GONE
        progressBar.visibility = ProgressBar.VISIBLE

        // Get fields.
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Sign in with given Email and password.
        authViewModel.login(email, password)
    }

    /**
     * Starts [LoginActivity] and pass argument [fragmentName] as extra.
     */
    private fun startFragment(fragmentName: String) {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.putExtra("fragment", fragmentName)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        loginViewModel.email = emailEditText.text.toString()
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}