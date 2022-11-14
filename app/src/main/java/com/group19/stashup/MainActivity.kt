package com.group19.stashup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.group19.stashup.databinding.ActivityMainBinding
import com.group19.stashup.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var nameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        /**
         * Add id below
         * Add item to @menu/activity_main_drawer.xml.
         * Add fragment to @navigation/mobile_navigation.xml
         */
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_transactions
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        auth = Firebase.auth

        checkLoginStatus()

        val checkUser = auth.currentUser
        if (checkUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            user = checkUser
            initializeLayoutViews()
        }
    }

    /**
     * Determines if the user is to be signed out.
     */
    private fun checkLoginStatus() {
        // Check if was directed from LoginFragment.
        val extras = intent.extras
        if (extras != null) {
            mainViewModel.loggedIn = extras.getBoolean("logged_in")
        }

        // Check Remember Me checkbox status.
        val preferences = getSharedPreferences("login", Context.MODE_PRIVATE)
        val rememberMe = preferences.getInt("remember_me", 0)

        // Determine if sign out is needed.
        if (rememberMe == 0 && !mainViewModel.loggedIn) {
            auth.signOut()
        }
    }

    /**
     * Initializes all Views present in the layout.
     * Sets any necessary default text.
     */
    private fun initializeLayoutViews() {
        // Set navigation header text to display name.
        nameTextView = binding.navView.getHeaderView(0).findViewById(R.id.textView)
        val text = "Hello, ${user.displayName}"
        nameTextView.text = text
    }

    fun signOut(item: MenuItem) {
        if (item.itemId != R.id.nav_sign_out) return

        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}