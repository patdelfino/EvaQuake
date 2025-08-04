package com.example.evaquake

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * An activity that acts as a container for the ARGuideFragment.
 * This is necessary because Fragments must be hosted by an Activity.
 */
class ArGuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the simple container layout for this activity.
        setContentView(R.layout.activity_container)

        // Only add the fragment if the activity is being created for the first time.
        // This prevents the fragment from being added multiple times on configuration changes.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_frame, ARGuideFragment())
                .commit()
        }
    }
}
