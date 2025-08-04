package com.example.evaquake

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.evaquake.databinding.ActivitySafetyGuidesBinding

class SafetyGuidesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySafetyGuidesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize View Binding
        binding = ActivitySafetyGuidesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the custom "Back" button from the layout
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set up click listeners for each collapsible card header
        binding.beforeHeader.setOnClickListener {
            toggleCardVisibility(binding.beforeTipsContainer, binding.beforeArrow)
        }

        binding.duringHeader.setOnClickListener {
            toggleCardVisibility(binding.duringTipsContainer, binding.duringArrow)
        }

        // Assuming there is an 'After an Earthquake' card in the layout
        binding.afterHeader.setOnClickListener {
            toggleCardVisibility(binding.afterTipsContainer, binding.afterArrow)
        }

        // Initially set the 'During' section to be visible and the arrow to point up
        binding.duringTipsContainer.visibility = View.VISIBLE
        binding.duringArrow.rotation = 180f
    }

    /**
     * Toggles the visibility of a given LinearLayout and animates the rotation of an ImageView.
     *
     * @param tipsContainer The LinearLayout containing the tips to be shown or hidden.
     * @param arrowIcon The ImageView that serves as the dropdown arrow.
     */
    private fun toggleCardVisibility(tipsContainer: View, arrowIcon: View) {
        if (tipsContainer.visibility == View.VISIBLE) {
            // Hide the tips container
            tipsContainer.visibility = View.GONE
            // Rotate the arrow back to its default position (pointing down)
            arrowIcon.animate().rotation(0f).setDuration(200).start()
        } else {
            // Show the tips container
            tipsContainer.visibility = View.VISIBLE
            // Rotate the arrow to point up
            arrowIcon.animate().rotation(180f).setDuration(200).start()
        }
    }
}
