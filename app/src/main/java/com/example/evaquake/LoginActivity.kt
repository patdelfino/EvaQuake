package com.example.evaquake

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread // Keep using kotlin.concurrent.thread for network operations

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginStudentEmployeeNumberEditText: EditText
    private lateinit var loginPasswordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerRedirectText: TextView

    // Registration UI elements
    private lateinit var registerFullNameEditText: EditText
    private lateinit var registerStudentEmployeeNumberEditText: EditText
    private lateinit var registerEmailEditText: EditText
    private lateinit var registerPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText
    private lateinit var registerUserTypeSpinner: Spinner
    private lateinit var registerButton: Button
    private lateinit var loginRedirectTextView: TextView

    // Added for schedule integration - these fields will be sent during registration
    private lateinit var registerGradeEditText: EditText
    private lateinit var registerRoomEditText: EditText


    // IMPORTANT: Replace with your backend server's URL.
    // For emulator, use 10.0.2.2. For physical device, use your machine's local IP.
    private val backendBaseUrl = "http://10.0.2.2:3000" // Example for emulator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Default to login layout
        setContentView(R.layout.activity_login)
        setupLoginLayout()
    }

    private fun setupLoginLayout() {
        loginStudentEmployeeNumberEditText = findViewById(R.id.login_student_employee_number)
        loginPasswordEditText = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.button_login) // Corrected ID based on activity_login.xml
        registerRedirectText = findViewById(R.id.registration_redirect_text) // Corrected ID

        loginButton.setOnClickListener {
            performLogin()
        }

        registerRedirectText.setOnClickListener {
            setContentView(R.layout.activity_register) // Switch to registration layout
            setupRegisterLayout()
        }
    }

    private fun setupRegisterLayout() {
        registerFullNameEditText = findViewById(R.id.register_full_name)
        registerStudentEmployeeNumberEditText = findViewById(R.id.register_student_employee_number)
        registerEmailEditText = findViewById(R.id.register_email)
        registerPasswordEditText = findViewById(R.id.register_password)
        registerConfirmPasswordEditText = findViewById(R.id.register_confirm_password)
        registerUserTypeSpinner = findViewById(R.id.register_user_type_spinner)
        registerButton = findViewById(R.id.register_button)
        loginRedirectTextView = findViewById(R.id.login_text) // Corrected ID based on activity_register.xml

        // Initialize new fields for registration (grade and room)
        registerGradeEditText = findViewById(R.id.register_grade) // Make sure this ID exists in activity_register.xml
        registerRoomEditText = findViewById(R.id.register_room)   // Make sure this ID exists in activity_register.xml


        // Setup spinner for user types
        ArrayAdapter.createFromResource(
            this,
            R.array.user_types_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            registerUserTypeSpinner.adapter = adapter
        }

        registerButton.setOnClickListener {
            performRegistration()
        }

        loginRedirectTextView.setOnClickListener {
            setContentView(R.layout.activity_login) // Switch back to login layout
            setupLoginLayout()
        }
    }

    private fun performLogin() {
        val studentEmployeeNumber = loginStudentEmployeeNumberEditText.text.toString().trim()
        val password = loginPasswordEditText.text.toString().trim()

        if (studentEmployeeNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter student/employee number and password.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show a loading indicator
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

        // Make a network request to your backend to get a custom Firebase token
        thread {
            try {
                val url = URL("$backendBaseUrl/login") // Use /login endpoint
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInputString = JSONObject().apply {
                    put("studentEmployeeNumber", studentEmployeeNumber)
                    put("password", password)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) { // Expect 200 OK for successful login
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val firebaseToken = jsonResponse.getString("token") // Backend returns "token"

                    // Sign in to Firebase with the custom token
                    auth.signInWithCustomToken(firebaseToken)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                runOnUiThread {
                                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this, "Firebase login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    Log.e("LoginActivity", "Firebase login failed", task.exception)
                                }
                            }
                        }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    runOnUiThread {
                        val errorJson = try { JSONObject(errorResponse) } catch (e: Exception) { null }
                        val errorMessage = errorJson?.optString("message", "Login failed.") ?: "Login failed."
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("LoginActivity", "Backend login failed: $responseCode - $errorResponse")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("LoginActivity", "Network error during login", e)
                }
            }
        }
    }

    private fun performRegistration() {
        val fullName = registerFullNameEditText.text.toString().trim()
        val studentEmployeeNumber = registerStudentEmployeeNumberEditText.text.toString().trim()
        val email = registerEmailEditText.text.toString().trim()
        val password = registerPasswordEditText.text.toString().trim()
        val confirmPassword = registerConfirmPasswordEditText.text.toString().trim()
        val userType = registerUserTypeSpinner.selectedItem.toString()
        val grade = registerGradeEditText.text.toString().trim() // Get grade
        val room = registerRoomEditText.text.toString().trim()   // Get room

        if (fullName.isEmpty() || studentEmployeeNumber.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty() || userType == getString(R.string.select_user_type_default)) {
            Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.toast_passwords_do_not_match), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) { // Firebase requires at least 6 characters for password
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading indicator
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show()

        // Make a network request to your backend for registration
        thread {
            try {
                val url = URL("$backendBaseUrl/register") // Use /register endpoint
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInputString = JSONObject().apply {
                    put("fullName", fullName)
                    put("studentEmployeeNumber", studentEmployeeNumber)
                    put("email", email)
                    put("password", password)
                    put("userType", userType)
                    put("grade", grade) // Include grade
                    put("room", room)   // Include room
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_CREATED) { // Expect 201 Created for successful registration
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val uid = jsonResponse.getString("uid") // Backend returns "uid"

                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.toast_account_created), Toast.LENGTH_SHORT).show()
                        // Redirect to login screen after successful registration
                        setContentView(R.layout.activity_login)
                        setupLoginLayout()
                        Toast.makeText(this, "Please login with your new credentials.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    runOnUiThread {
                        val errorJson = try { JSONObject(errorResponse) } catch (e: Exception) { null }
                        val errorMessage = errorJson?.optString("message", "Registration failed.") ?: "Registration failed."
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("LoginActivity", "Backend registration failed: $responseCode - $errorResponse")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Network error during registration: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("LoginActivity", "Network error during registration", e)
                }
            }
        }
    }
}
