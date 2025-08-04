package com.example.evaquake

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.evaquake.databinding.ActivityEmergencyHotlinesBinding

/**
 * An activity that displays a list of emergency hotlines.
 * Users can click on a number to initiate a call using the phone's dialer.
 */
class EmergencyHotlinesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyHotlinesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = ActivityEmergencyHotlinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar as the action bar and enable the back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Emergency Hotlines"

        // Set click listeners for the emergency hotline numbers
        setupHotlineClickListeners()
    }

    /**
     * Sets up click listeners for each hotline TextView.
     * When clicked, an Intent is created to open the phone dialer with the number.
     */
    private fun setupHotlineClickListeners() {
        // Police hotline
        binding.textPoliceNumber.setOnClickListener {
            dialPhoneNumber(binding.textPoliceNumber.text.toString())
        }

        // Fire department hotline
        binding.textFireNumber.setOnClickListener {
            dialPhoneNumber(binding.textFireNumber.text.toString())
        }

        // Medical services hotline
        binding.textMedicalNumber.setOnClickListener {
            dialPhoneNumber(binding.textMedicalNumber.text.toString())
        }
    }

    /**
     * Creates and starts an Intent to open the phone dialer with a given number.
     * @param phoneNumber The phone number to dial.
     */
    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No app found to handle dialing.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Handles the back button in the toolbar.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
