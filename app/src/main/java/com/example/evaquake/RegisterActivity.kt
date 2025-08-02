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
import androidx.appcompat.app.AlertDialog
import android.widget.Spinner
import android.widget.AdapterView

class RegisterActivity : AppCompatActivity() {

    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth

    // UI elements, now matching the IDs in your XML layout
    private lateinit var backButton: TextView
    private lateinit var studentEmployeeNumberInput: EditText
    private lateinit var fullNameInput: EditText
    private lateinit var userTypeSpinner: Spinner // Corrected to Spinner
    private lateinit var gradeInput: EditText
    private lateinit var roomInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText // Added for the confirm password field
    private lateinit var termsCheckbox: CheckBox
    private lateinit var signUpButton: Button
    private lateinit var loginRedirectText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Wrap the entire onCreate logic in a try-catch to catch any unexpected runtime exceptions
        try {
            setContentView(R.layout.activity_register)

            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()

            // Initialize all UI elements from the layout.
            backButton = findViewById(R.id.back_button)
            studentEmployeeNumberInput = findViewById(R.id.student_employee_number)
            fullNameInput = findViewById(R.id.full_name)
            userTypeSpinner = findViewById(R.id.item1) // Initialized as a Spinner
            gradeInput = findViewById(R.id.grade)
            roomInput = findViewById(R.id.room)
            emailInput = findViewById(R.id.email)
            passwordInput = findViewById(R.id.password)
            confirmPasswordInput = findViewById(R.id.confirm_password)
            termsCheckbox = findViewById(R.id.terms_checkbox)
            signUpButton = findViewById(R.id.sign_up_button)
            loginRedirectText = findViewById(R.id.login_redirect_text)

            // Set click listener for the "Back" button
            backButton.setOnClickListener {
                finish()
            }

            // Set click listener for the "SIGN UP" button
            signUpButton.setOnClickListener {
                val studentEmployeeNumber = studentEmployeeNumberInput.text.toString()
                val fullName = fullNameInput.text.toString()
                val userType = userTypeSpinner.selectedItem.toString()
                val grade = gradeInput.text.toString()
                val room = roomInput.text.toString()
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                // Validate that no fields are empty
                if (email.isEmpty() || password.isEmpty() || studentEmployeeNumber.isEmpty() ||
                    fullName.isEmpty() || userTypeSpinner.selectedItemPosition == 0 ||
                    grade.isEmpty() || room.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check if passwords match
                if (password != confirmPassword) {
                    Toast.makeText(this, getString(R.string.toast_passwords_do_not_match), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check if terms and conditions are accepted
                if (!termsCheckbox.isChecked) {
                    Toast.makeText(this, "Please agree to the terms and conditions.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create a new user with Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, getString(R.string.toast_account_created), Toast.LENGTH_SHORT).show()
                            // Redirect to the LoginActivity after successful registration
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Log the error for better debugging
                            Log.e("RegisterActivity", "Authentication failed: ", task.exception)
                            Toast.makeText(this, "Authentication failed. " + task.exception?.message, Toast.LENGTH_LONG).show()
                        }
                    }
            }

            // Set click listener for the "Log In" redirect text
            loginRedirectText.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        } catch (e: Exception) {
            // This catch block will help us find the root cause if it's not a simple NullPointerException.
            // The Logcat will show the full stack trace.
            Log.e("RegisterActivity", "An unexpected error occurred during activity creation: ${e.message}", e)
            Toast.makeText(this, "An unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * This function is called when the "terms and conditions" TextView is clicked.
     * It displays a dialog with the full terms and conditions text.
     */
    fun showTermsAndConditionsDialog(view: View) {
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
