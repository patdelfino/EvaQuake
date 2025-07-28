package com.example.evaquake

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AboutEarthquakesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_earthquakes)

        supportActionBar?.apply {
            title = "Learn About Earthquakes"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    // Enable back button in the action bar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
