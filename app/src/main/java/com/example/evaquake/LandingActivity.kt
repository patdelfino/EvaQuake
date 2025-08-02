package com.example.evaquake

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LandingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var iHaveAccountButton: Button
    private lateinit var createAccountButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        auth = FirebaseAuth.getInstance()

        // Hide the action bar for a full-screen look
        supportActionBar?.hide()

        // Check if user is already logged in
        // If so, redirect to MainActivity immediately
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Initialize UI elements
        iHaveAccountButton = findViewById(R.id.button_i_have_account)
        createAccountButton = findViewById(R.id.button_create_account)

        // Set up click listeners
        iHaveAccountButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        createAccountButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
