package com.example.evaquake

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue // Import FieldValue for arrayUnion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var daySpinner: Spinner
    private lateinit var subjectInput: EditText
    private lateinit var startTimeInput: EditText
    private lateinit var endTimeInput: EditText
    private lateinit var addScheduleButton: Button
    private lateinit var viewScheduleButton: Button // For future "View Full Schedule" functionality

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupDaySpinner()
        setupTimePickers()
        setupListeners()
    }

    private fun initializeViews() {
        daySpinner = findViewById(R.id.day_spinner)
        subjectInput = findViewById(R.id.subject_input)
        startTimeInput = findViewById(R.id.start_time_input)
        endTimeInput = findViewById(R.id.end_time_input)
        addScheduleButton = findViewById(R.id.add_schedule_button)
        viewScheduleButton = findViewById(R.id.view_schedule_button)
    }

    private fun setupDaySpinner() {
        val daysOfWeek = resources.getStringArray(R.array.days_of_week)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, daysOfWeek)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daySpinner.adapter = adapter
    }

    private fun setupTimePickers() {
        startTimeInput.setOnClickListener {
            showTimePickerDialog(startTimeInput)
        }
        endTimeInput.setOnClickListener {
            showTimePickerDialog(endTimeInput)
        }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            editText.setText(formattedTime)
        }, hour, minute, true) // true for 24-hour format
        timePickerDialog.show()
    }

    private fun setupListeners() {
        addScheduleButton.setOnClickListener {
            addScheduleEntry()
        }

        viewScheduleButton.setOnClickListener {
            // TODO: Implement viewing the full schedule (e.g., in a new dialog or activity)
            Toast.makeText(this, "View Full Schedule functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addScheduleEntry() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDay = daySpinner.selectedItem.toString()
        val subject = subjectInput.text.toString().trim()
        val startTime = startTimeInput.text.toString().trim()
        val endTime = endTimeInput.text.toString().trim()

        if (subject.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please fill all schedule fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Basic time format validation (HH:MM) - can be more robust
        if (!isValidTimeFormat(startTime) || !isValidTimeFormat(endTime)) {
            Toast.makeText(this, "Please use HH:MM format for times.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a map for the schedule entry
        val scheduleEntry = hashMapOf(
            "subject" to subject,
            "startTime" to startTime,
            "endTime" to endTime
        )

        // Save to Firestore: users/{userId}/schedule/{dayOfWeek} document, with an array of entries
        db.collection("users").document(userId)
            .collection("schedule").document(selectedDay)
            .update("entries", FieldValue.arrayUnion(scheduleEntry)) // Add to array if exists, create if not
            .addOnSuccessListener {
                Toast.makeText(this, "Schedule added for $selectedDay!", Toast.LENGTH_SHORT).show()
                // Clear input fields after successful addition
                subjectInput.text.clear()
                startTimeInput.text.clear()
                endTimeInput.text.clear()
            }
            .addOnFailureListener { e ->
                // If the document for the day doesn't exist, create it with the first entry
                if (e.message?.contains("NOT_FOUND") == true) {
                    db.collection("users").document(userId)
                        .collection("schedule").document(selectedDay)
                        .set(hashMapOf("entries" to listOf(scheduleEntry)))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Schedule created for $selectedDay!", Toast.LENGTH_SHORT).show()
                            subjectInput.text.clear()
                            startTimeInput.text.clear()
                            endTimeInput.text.clear()
                        }
                        .addOnFailureListener { e2 ->
                            Toast.makeText(this, "Error creating schedule: ${e2.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Error adding schedule: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun isValidTimeFormat(time: String): Boolean {
        return time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))
    }
}
