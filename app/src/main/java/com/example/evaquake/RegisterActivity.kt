package com.example.evaquake

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AlertDialog
import android.widget.Spinner
import android.widget.AdapterView
import android.text.method.LinkMovementMethod

class RegisterActivity : AppCompatActivity() {

    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth

    // Firestore database instance
    private lateinit var db: FirebaseFirestore

    // UI elements
    private lateinit var backButton: TextView
    private lateinit var studentEmployeeNumberInput: EditText
    private lateinit var fullNameInput: EditText
    private lateinit var userTypeSpinner: Spinner
    private lateinit var gradeInput: EditText
    private lateinit var roomInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var signUpButton: Button
    private lateinit var loginRedirectText: TextView
    private lateinit var termsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_register)

            // Initialize Firebase instances
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            // Initialize UI elements
            backButton = findViewById(R.id.back_button)
            studentEmployeeNumberInput = findViewById(R.id.student_employee_number)
            fullNameInput = findViewById(R.id.full_name)
            userTypeSpinner = findViewById(R.id.item1)
            gradeInput = findViewById(R.id.grade)
            roomInput = findViewById(R.id.room)
            emailInput = findViewById(R.id.email)
            passwordInput = findViewById(R.id.password)
            confirmPasswordInput = findViewById(R.id.confirm_password)
            termsCheckbox = findViewById(R.id.terms_checkbox)
            signUpButton = findViewById(R.id.sign_up_button)
            loginRedirectText = findViewById(R.id.login_redirect_text)
            termsText = findViewById(R.id.terms_text) // New: Find the terms TextView

            // Set up click listeners
            backButton.setOnClickListener {
                finish()
            }

            signUpButton.setOnClickListener {
                val studentEmployeeNumber = studentEmployeeNumberInput.text.toString()
                val fullName = fullNameInput.text.toString()
                val userType = userTypeSpinner.selectedItem.toString()
                val grade = gradeInput.text.toString()
                val room = roomInput.text.toString()
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                if (email.isEmpty() || password.isEmpty() || studentEmployeeNumber.isEmpty() ||
                    fullName.isEmpty() || userTypeSpinner.selectedItemPosition == 0 ||
                    grade.isEmpty() || room.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password != confirmPassword) {
                    Toast.makeText(this, getString(R.string.toast_passwords_do_not_match), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!termsCheckbox.isChecked) {
                    Toast.makeText(this, getString(R.string.toast_agree_terms_and_conditions), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            // Account created successfully. Now save additional user data to Firestore.
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // Create a user map to store in Firestore
                                val user = hashMapOf(
                                    "uid" to userId,
                                    "email" to email,
                                    "studentEmployeeNumber" to studentEmployeeNumber,
                                    "fullName" to fullName,
                                    "userType" to userType,
                                    "grade" to grade,
                                    "room" to room
                                )

                                // Save the user data to a Firestore collection named "users"
                                db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Log.d("RegisterActivity", "User data successfully written!")
                                        Toast.makeText(this, getString(R.string.toast_account_created), Toast.LENGTH_SHORT).show()
                                        // Navigate to the next activity after data is saved
                                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("RegisterActivity", "Error writing user data", e)
                                        Toast.makeText(this, "Failed to save user data. Please try again.", Toast.LENGTH_LONG).show()
                                        // You may want to delete the user from auth if saving data fails.
                                    }
                            }
                        } else {
                            Log.e("RegisterActivity", "Authentication failed: ", task.exception)
                            Toast.makeText(this, "Authentication failed. " + task.exception?.message, Toast.LENGTH_LONG).show()
                        }
                    }
            }

            loginRedirectText.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            // Set up a click listener for the terms and conditions text.
            // This is a more robust way than using android:onClick in XML.
            termsText.setOnClickListener {
                showTermsAndConditionsDialog()
            }

        } catch (e: Exception) {
            Log.e("RegisterActivity", "An unexpected error occurred during activity creation: ${e.message}", e)
            Toast.makeText(this, "An unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * This function displays a dialog with the full terms and conditions text.
     */
    private fun showTermsAndConditionsDialog() {
        // Create a custom view for the dialog to make the content scrollable
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_terms_and_conditions, null)
        val termsTextView: TextView = dialogView.findViewById(R.id.terms_dialog_text)
        termsTextView.text = resources.getString(R.string.terms_and_conditions_content)
        termsTextView.movementMethod = LinkMovementMethod.getInstance()

        AlertDialog.Builder(this)
            .setTitle(R.string.terms_and_conditions_title)
            .setView(dialogView)
            .setPositiveButton(R.string.ok_button) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}
