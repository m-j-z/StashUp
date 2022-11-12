package com.group19.stashup.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.group19.stashup.R
import com.group19.stashup.login.forgotpassword.ForgotPasswordFragment
import com.group19.stashup.login.login.LoginFragment
import com.group19.stashup.login.register.RegisterFragment

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val extras = intent.extras
        var fragment = ""
        if (extras != null) {
            fragment = extras.getString("fragment").toString()
        }

        when (fragment) {
            "forgot_password" -> startFragment(ForgotPasswordFragment.newInstance())
            "register" -> startFragment(RegisterFragment.newInstance())
            else -> startFragment(LoginFragment.newInstance())
        }
    }

    /**
     * Starts [fragment] and puts it to back stack.
     */
    private fun startFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_view, fragment)
        transaction.commit()
    }
}