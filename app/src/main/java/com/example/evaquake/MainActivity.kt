package com.example.evaquake

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Import Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
// Removed import for BottomNavigationView as it's no longer used
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), OptionsBottomSheetDialogFragment.OptionsListener { // Implement the interface

    // Removed bottomNavigation declaration
    private lateinit var emergencyButton: FloatingActionButton
    private lateinit var goButtonFab: FloatingActionButton // Declare the new FAB

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔐 Redirect to login if not authenticated
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // Initialize Toolbar and set it as the support action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Optionally set a title for the toolbar
        supportActionBar?.title = "Evaquake" // Or your app name

        initializeViews()
        // Removed setupBottomNavigation() call
        setupEmergencyButton()
        setupGoButton() // Setup the new Go button
        requestPermissions()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }

    private fun initializeViews() {
        // Removed bottomNavigation initialization
        emergencyButton = findViewById(R.id.emergency_button)
        goButtonFab = findViewById(R.id.go_button_fab) // Initialize the new FAB
    }

    // Removed setupBottomNavigation() function

    private fun setupEmergencyButton() {
        emergencyButton.setOnClickListener {
            Toast.makeText(this, "Mock Drill Started", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, EmergencyDrillActivity::class.java))
        }
    }

    // NEW: Setup for the central "Go" button
    private fun setupGoButton() {
        goButtonFab.setOnClickListener {
            val bottomSheet = OptionsBottomSheetDialogFragment()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    // Implementation of OptionsListener interface to handle selections from the bottom sheet
    override fun onOptionSelected(optionId: Int) {
        val fragment = when (optionId) {
            OptionsBottomSheetDialogFragment.OPTION_3D_MODEL -> Building3DFragment()
            OptionsBottomSheetDialogFragment.OPTION_AR_GUIDE -> ARGuideFragment()
            else -> null // Should not happen with defined options
        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, it)
                .addToBackStack(null) // Allows navigating back to previous fragment
                .commit()
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Location permission is required for emergency features", Toast.LENGTH_LONG).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Camera permission is required for AR features", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 🔓 Logout Option
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
