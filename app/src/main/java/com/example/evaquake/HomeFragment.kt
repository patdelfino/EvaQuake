package com.example.evaquake

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var welcomeTextView: TextView
    private lateinit var gradeRoomTextView: TextView
    private lateinit var currentSubjectTextView: TextView
    private lateinit var cardManageSchedule: CardView // New CardView for managing schedule

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Handler for updating the current subject periodically
    private val handler = Handler(Looper.getMainLooper())
    private val updateSubjectRunnable = object : Runnable {
        override fun run() {
            displayCurrentSubject()
            handler.postDelayed(this, 60 * 1000) // Update every minute
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        welcomeTextView = view.findViewById(R.id.welcome_text)
        gradeRoomTextView = view.findViewById(R.id.grade_room_text)
        currentSubjectTextView = view.findViewById(R.id.current_subject_text_view)
        cardManageSchedule = view.findViewById(R.id.card_manage_schedule) // Initialize new CardView

        fetchAndDisplayUserName()
        gradeRoomTextView.text = "Grade 8 - Room 305"

        // Initialize existing CardViews
        val cardSafetyGuides = view.findViewById<CardView>(R.id.card_safety_guides)
        val cardScanQr = view.findViewById<CardView>(R.id.card_scan_qr)
        val cardEmergency = view.findViewById<CardView>(R.id.card_emergency)
        val cardAboutEarthquakes = view.findViewById<CardView>(R.id.card_about_earthquakes)

        // Removed OnClickListener for cardSafetyGuides as SafetyTipsFragment is deleted.
        // If you re-implement Safety Guides, you'll need to add this listener back.
        // cardSafetyGuides.setOnClickListener {
        //     Toast.makeText(context, "Navigating to Safety Guides", Toast.LENGTH_SHORT).show()
        //     (activity as? MainActivity)?.supportFragmentManager?.beginTransaction()
        //         ?.replace(R.id.fragment_container, SafetyTipsFragment())
        //         ?.addToBackStack(null)
        //         ?.commit()
        // }

        cardScanQr.setOnClickListener {
            Toast.makeText(context, "Opening QR Scanner", Toast.LENGTH_SHORT).show()
            // Start QRScannerActivity
            startActivity(Intent(requireContext(), QRScannerActivity::class.java))
        }

        cardEmergency.setOnClickListener {
            Toast.makeText(context, "Emergency Button Pressed!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), EmergencyDrillActivity::class.java))
        }

        cardAboutEarthquakes.setOnClickListener {
            Toast.makeText(context, "Navigating to About Earthquakes", Toast.LENGTH_SHORT).show()
            // Start AboutEarthquakesActivity
            startActivity(Intent(requireContext(), AboutEarthquakesActivity::class.java))
        }

        // Set OnClickListener for the new "Manage Schedule" CardView
        cardManageSchedule.setOnClickListener {
            startActivity(Intent(requireContext(), ScheduleActivity::class.java))
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Start updating the current subject when the fragment becomes visible
        handler.post(updateSubjectRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Stop updating the current subject when the fragment is no longer visible
        handler.removeCallbacks(updateSubjectRunnable)
    }

    /**
     * Fetches the current user's full name from Firestore and updates the welcome TextView.
     */
    private fun fetchAndDisplayUserName() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName")
                        if (fullName != null) {
                            welcomeTextView.text = "Welcome, $fullName!"
                        } else {
                            welcomeTextView.text = "Welcome!" // Fallback if name is missing
                        }
                    } else {
                        welcomeTextView.text = "Welcome!" // Fallback if document doesn't exist
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load user data: ${e.message}", Toast.LENGTH_LONG).show()
                    welcomeTextView.text = "Welcome!" // Fallback on error
                }
        } else {
            welcomeTextView.text = "Welcome!" // Fallback if no user is logged in (shouldn't happen if MainActivity redirects)
        }
    }

    /**
     * Displays a dialog for the user to input their schedule for the day.
     * For simplicity, this example will just ask for a single "current subject".
     * In a full implementation, this would be a more complex UI for daily schedules.
     */
    private fun showScheduleInputDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter Current Subject")

        val input = EditText(requireContext())
        input.hint = "e.g., Math, Science, English"
        builder.setView(input)

        builder.setPositiveButton("Set Subject") { dialog, _ ->
            val subject = input.text.toString().trim()
            if (subject.isNotEmpty()) {
                saveCurrentSubject(subject)
            } else {
                Toast.makeText(context, "Subject cannot be empty.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    /**
     * Saves the current subject to Firestore for the logged-in user.
     * In a more complex app, you'd save a full daily schedule.
     */
    private fun saveCurrentSubject(subject: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = db.collection("users").document(userId)
            userRef.update("currentSubject", subject) // Update existing user document
                .addOnSuccessListener {
                    Toast.makeText(context, "Subject saved!", Toast.LENGTH_SHORT).show()
                    displayCurrentSubject() // Update immediately after saving
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to save subject: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(context, "No user logged in to save subject.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Determines the current subject based on the stored weekly schedule and updates the TextView.
     */
    @Suppress("UNCHECKED_CAST") // Suppress the warning for the unchecked cast
    private fun displayCurrentSubject() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            currentSubjectTextView.text = "Current Subject: Not Set (Login to view)"
            return
        }

        val calendar = Calendar.getInstance()
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.US).format(calendar.time) // e.g., "Monday"
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        db.collection("users").document(userId)
            .collection("schedule").document(dayOfWeek)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val entries = document.get("entries") as? List<Map<String, String>>
                    if (entries != null) {
                        var foundSubject = "No subject currently"
                        for (entry in entries) {
                            val subject = entry["subject"]
                            val startTimeStr = entry["startTime"] // HH:MM
                            val endTimeStr = entry["endTime"]     // HH:MM

                            if (subject != null && startTimeStr != null && endTimeStr != null) {
                                try {
                                    val startHour = startTimeStr.substringBefore(":").toInt()
                                    val startMinute = startTimeStr.substringAfter(":").toInt()
                                    val endHour = endTimeStr.substringBefore(":").toInt()
                                    val endMinute = endTimeStr.substringAfter(":").toInt()

                                    // Check if current time is within the subject's time range
                                    val currentTimeInMinutes = currentHour * 60 + currentMinute
                                    val startTimeInMinutes = startHour * 60 + startMinute
                                    val endTimeInMinutes = endHour * 60 + endMinute

                                    if (currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes < endTimeInMinutes) {
                                        foundSubject = "$subject ($startTimeStr - $endTimeStr)"
                                        break // Found the current subject, no need to check further
                                    }
                                } catch (e: Exception) {
                                    // Log or handle parsing error
                                    e.printStackTrace()
                                }
                            }
                        }
                        currentSubjectTextView.text = "Current Subject: $foundSubject"
                    } else {
                        currentSubjectTextView.text = "Current Subject: No schedule for $dayOfWeek"
                    }
                } else {
                    currentSubjectTextView.text = "Current Subject: No schedule for $dayOfWeek"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching schedule: ${e.message}", Toast.LENGTH_SHORT).show()
                currentSubjectTextView.text = "Current Subject: Error"
            }
    }
}
