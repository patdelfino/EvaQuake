package com.example.evaquake

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Declare FirebaseAuth
    private lateinit var db: FirebaseFirestore // Declare Firestore

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var loginRedirectText: TextView
    private lateinit var fullNameInput: EditText
    private lateinit var studentEmployeeNumberInput: EditText
    private lateinit var userTypeSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance() // Initialize Firestore

        // Initialize UI elements
        fullNameInput = findViewById(R.id.register_full_name)
        studentEmployeeNumberInput = findViewById(R.id.register_student_employee_number)
        userTypeSpinner = findViewById(R.id.register_user_type_spinner)
        emailInput = findViewById(R.id.register_email)
        passwordInput = findViewById(R.id.register_password)
        confirmPasswordInput = findViewById(R.id.register_confirm_password)
        registerButton = findViewById(R.id.register_button)
        loginRedirectText = findViewById(R.id.login_text)

        // --- Start of code to style the "Login" text ---
        val fullText = "Already have an account? Login"
        val loginWord = "Login"

        val startIndex = fullText.indexOf(loginWord)
        val endIndex = startIndex + loginWord.length

        if (startIndex != -1) {
            val spannableString = SpannableString(fullText)

            // Apply bold style
            spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Apply blue color using ContextCompat for compatibility
            spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue)), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Set the styled text to the TextView
            loginRedirectText.text = spannableString
        }
        // --- End of code to style the "Login" text ---

        registerButton.setOnClickListener {
            registerUser()
        }

        loginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        // Get values from all fields
        val fullName = fullNameInput.text.toString().trim()
        val studentEmployeeNumber = studentEmployeeNumberInput.text.toString().trim()
        val userType = userTypeSpinner.selectedItem.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        // Basic validation
        if (fullName.isEmpty() || studentEmployeeNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || userType == getString(R.string.select_user_type_default)) {
            Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        } else if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.toast_passwords_do_not_match), Toast.LENGTH_SHORT).show()
            return
        } else if (password.length < 6) { // Firebase requires at least 6 characters for password
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading indicator
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show()

        // Firebase registration logic
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User registered successfully in Firebase Auth
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        // Save additional user data to Firestore
                        val userData = hashMapOf(
                            "fullName" to fullName,
                            "studentEmployeeNumber" to studentEmployeeNumber,
                            "userType" to userType,
                            "email" to email,
                            "uid" to firebaseUser.uid // Store Firebase UID
                        )

                        db.collection("users").document(firebaseUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, getString(R.string.toast_account_created), Toast.LENGTH_SHORT).show()
                                // Redirect to LoginActivity after successful registration and data save
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                                // Optionally, delete the Firebase Auth user if Firestore save fails
                                firebaseUser.delete()
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
