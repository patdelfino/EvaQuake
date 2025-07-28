package com.example.evaquake

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EmergencyDrillActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // You'll need to create a layout for this activity, e.g., activity_emergency_drill.xml
        setContentView(R.layout.activity_emergency_drill)

        // Example: Display some text
        val textView = findViewById<TextView>(R.id.emergency_drill_text)
        textView.text = "Emergency Drill in Progress!"
    }
}
    