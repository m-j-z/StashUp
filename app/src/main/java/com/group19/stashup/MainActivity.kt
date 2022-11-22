package com.group19.stashup

import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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
                R.id.nav_home, R.id.nav_transactions, R.id.nav_expenditure
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        mainViewModel.menuId.observe(this) {
            if (it == null) return@observe

            binding.appBarMain.toolbar.inflateMenu(it)
        }

        binding.appBarMain.toolbar.setOnMenuItemClickListener {
            if (it == null) return@setOnMenuItemClickListener false

            when (it.itemId) {
                R.id.display_id -> {
                    createDisplayIdDialog()
                }
                R.id.generate_qr -> {
                    generateQrCode()
                }
                R.id.change_settings -> {}
            }

            return@setOnMenuItemClickListener true
        }

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

    private fun generateQrCode() {
        val qrWriter = QRCodeWriter()
        val bitMatrix = qrWriter.encode(mainViewModel.transaction.transactionUid, BarcodeFormat.QR_CODE, 512, 512)

        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, bitMatrix.width, 0, 0, bitMatrix.width, bitMatrix.height)

        val builder = AlertDialog.Builder(this)

        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)

        builder.apply {
            setTitle("QR Code")
            setView(imageView)
            setNeutralButton("OK") { _: DialogInterface, _: Int -> }
        }
        builder.create().show()
    }

    /**
     * Create dialog that displays the transaction ID.
     */
    private fun createDisplayIdDialog() {
        val builder = AlertDialog.Builder(this)
        // Create drawable.
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_copy_24)!!
        drawable.setBounds(0, 0, 64, 64)

        // Create EditText.
        val editText = EditText(this)
        editText.apply {
            setCompoundDrawables(null, null, drawable, null)
            background = null
            setText(mainViewModel.transaction.transactionUid)
            focusable = EditText.NOT_FOCUSABLE
            gravity = Gravity.CENTER_HORIZONTAL
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("uid", mainViewModel.transaction.transactionUid)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@MainActivity, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.apply {
            setTitle("Transaction ID")
            setView(editText)
            setNeutralButton("OK") { _: DialogInterface, _: Int -> }
        }
        builder.create().show()
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