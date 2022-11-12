package com.group19.stashup.ui.profile

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.group19.stashup.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        user = auth.currentUser!!

        initializeEditTexts()

        binding.changeProfileBtn.setOnClickListener(this)

        return binding.root
    }

    /**
     * Adds current display name and email to EditText of [user]
     */
    private fun initializeEditTexts() {
        binding.nameEt.setText(user.displayName)
        binding.emailEt.setText(user.email)
    }

    /**
     * Determines actions to perform depending on the View clicked.
     */
    override fun onClick(v: View?) {
        if (v == null) return

        when (v.id) {
            binding.changeProfileBtn.id -> {
                if (checkProfileFields()) {
                    authenticateUser()
                }
            }
        }
    }

    /**
     * Checks if all fields in the layout meets the condition.
     * @return true if all conditions are met, false otherwise.
     */
    private fun checkProfileFields(): Boolean {
        val currentPassword = binding.currentPasswordEt.text.toString()
        val name = binding.nameEt.text.toString()
        val email = binding.emailEt.text.toString()
        val password = binding.passwordEt.text.toString()

        // Checks password length.
        if (password.isNotEmpty() && password.length < 8) {
            binding.passwordEt.error =
                "Enter a password that is greater than or equal to 8 characters."
            binding.passwordEt.requestFocus()
        }

        // Checks email validity.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.error = "Enter a valid email."
            binding.emailEt.requestFocus()
        }

        // Checks name validity.
        if (name.isEmpty()) {
            binding.nameEt.error = "Enter a valid name."
            binding.nameEt.requestFocus()
        }

        // Checks current password
        if (currentPassword.isEmpty() || currentPassword.length < 8) {
            binding.currentPasswordEt.error =
                "Enter your current password to make any changes to your profile."
            binding.currentPasswordEt.requestFocus()
        }

        // If all fields are valid, return true.
        if ((password.isEmpty() || password.length >= 8) && Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() && name.isNotEmpty() && (currentPassword.isNotEmpty() && currentPassword.length >= 8)
        ) {
            return true
        }

        // Else return false.
        return false
    }

    /**
     * Re-authenticates user to preparation for profile update.
     */
    private fun authenticateUser() {
        val currentPassword = binding.currentPasswordEt.text.toString()
        var name = binding.nameEt.text.toString()
        var email = binding.emailEt.text.toString()
        val password = binding.passwordEt.text.toString()

        if (name.isEmpty()) {
            name = user.displayName!!
        }

        if (email.isEmpty()) {
            email = user.email!!
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        // Re-authenticate with given password and email.
        user.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                changeProfile(name, email, password)
            } else {
                Toast.makeText(requireActivity(), "${it.exception!!.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Changes profile data.
     */
    private fun changeProfile(name: String, email: String, password: String) {
        var failedUpdate = false

        // Change display name.
        val profileChangeRequest = UserProfileChangeRequest.Builder().apply {
            displayName = name
        }.build()
        user.updateProfile(profileChangeRequest).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i("ProfileFragment", "Profile change successful.")
            } else {
                Log.e("ProfileFragment", "${it.exception!!.message}")
                failedUpdate = true
            }
        }

        // Change email.
        user.updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i("ProfileFragment", "Email change successful.")
            } else {
                Log.e("ProfileFragment", "${it.exception!!.message}")
                failedUpdate = true
            }
        }

        // Change password.
        user.updatePassword(password).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i("ProfileFragment", "Password change successful.")
            } else {
                Log.e("ProfileFragment", "${it.exception!!.message}")
                failedUpdate = true
            }
        }

        // Clear EditText.
        binding.currentPasswordEt.setText("")
        binding.passwordEt.setText("")

        // Show status depending on state.
        if (failedUpdate) {
            Toast.makeText(requireActivity(), "Profile update failed.", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(requireActivity(), "Profile update successful.", Toast.LENGTH_SHORT).show()
    }
}