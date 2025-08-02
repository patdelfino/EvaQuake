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
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Shared UI elements (these will be null when the other layout is active)
    private var loginStudentEmployeeNumberEditText: EditText? = null
    private var loginPasswordEditText: EditText? = null // FIXED: Removed the extra 'EditText:'
    private var loginButton: Button? = null
    private var registrationRedirectText: TextView? = null

    private var registerFullNameEditText: EditText? = null
    private var registerStudentEmployeeNumberEditText: EditText? = null
    private var registerEmailEditText: EditText? = null
    private var registerPasswordEditText: EditText? = null
    private var registerConfirmPasswordEditText: EditText? = null
    private var registerUserTypeSpinner: Spinner? = null
    private var registerButton: Button? = null
    private var loginRedirectTextView: TextView? = null
    private var registerGradeEditText: EditText? = null
    private var registerRoomEditText: EditText? = null


    // IMPORTANT: Replace with your backend server's URL.
    // For emulator, use 10.0.2.2. For physical device, use your machine's local IP.
    private val backendBaseUrl = "http://10.0.2.2:3000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        showLoginLayout()
    }

    private fun showLoginLayout() {
        setContentView(R.layout.activity_login)
        setupLoginLayout()
    }

    private fun showRegisterLayout() {
        setContentView(R.layout.activity_register)
        setupRegisterLayout()
    }

    private fun setupLoginLayout() {
        try {
            loginStudentEmployeeNumberEditText = findViewById(R.id.login_student_employee_number)
            loginPasswordEditText = findViewById(R.id.login_password)
            loginButton = findViewById(R.id.button_login)
            registrationRedirectText = findViewById(R.id.registration_redirect_text)

            loginButton?.setOnClickListener { performLogin() }
            registrationRedirectText?.setOnClickListener { showRegisterLayout() }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error setting up login layout. Check your activity_login.xml IDs.", e)
            Toast.makeText(this, "Login layout error. See logcat.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRegisterLayout() {
        try {
            registerFullNameEditText = findViewById(R.id.full_name)
            registerStudentEmployeeNumberEditText = findViewById(R.id.student_employee_number)
            registerEmailEditText = findViewById(R.id.email)
            registerPasswordEditText = findViewById(R.id.password)
            registerUserTypeSpinner = findViewById(R.id.item1)
            registerButton = findViewById(R.id.sign_up_button)
            loginRedirectTextView = findViewById(R.id.login_redirect_text)
            registerGradeEditText = findViewById(R.id.grade)
            registerRoomEditText = findViewById(R.id.room)

            // Setup spinner for user types
            ArrayAdapter.createFromResource(
                this,
                R.array.user_types_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                registerUserTypeSpinner?.adapter = adapter
            }

            registerButton?.setOnClickListener { performRegistration() }
            loginRedirectTextView?.setOnClickListener { showLoginLayout() }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error setting up register layout. Check your activity_register.xml IDs.", e)
            Toast.makeText(this, "Registration layout error. See logcat.", Toast.LENGTH_LONG).show()
        }
    }

    private fun performLogin() {
        val studentEmployeeNumber = loginStudentEmployeeNumberEditText?.text.toString().trim()
        val password = loginPasswordEditText?.text.toString().trim()

        if (studentEmployeeNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter student/employee number and password.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

        thread {
            try {
                val url = URL("$backendBaseUrl/login")
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
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val firebaseToken = jsonResponse.getString("token")

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
                                    Log.e("LoginActivity", "Firebase login failed", task.exception)
                                    Toast.makeText(this, "Firebase login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    runOnUiThread {
                        val errorJson = try { JSONObject(errorResponse) } catch (e: Exception) { null }
                        val errorMessage = errorJson?.optString("message", "Login failed.") ?: "Login failed."
                        Log.e("LoginActivity", "Backend login failed: $responseCode - $errorResponse")
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.e("LoginActivity", "Network error during login", e)
                    Toast.makeText(this, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun performRegistration() {
        val fullName = registerFullNameEditText?.text.toString().trim()
        val studentEmployeeNumber = registerStudentEmployeeNumberEditText?.text.toString().trim()
        val email = registerEmailEditText?.text.toString().trim()
        val password = registerPasswordEditText?.text.toString().trim()
        val confirmPassword = registerConfirmPasswordEditText?.text.toString().trim()
        val userType = registerUserTypeSpinner?.selectedItem?.toString() ?: ""
        val grade = registerGradeEditText?.text.toString().trim()
        val room = registerRoomEditText?.text.toString().trim()

        if (fullName.isEmpty() || studentEmployeeNumber.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty() || userType == getString(R.string.select_user_type_default)) {
            Toast.makeText(this, getString(R.string.toast_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.toast_passwords_do_not_match), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show()

        thread {
            try {
                val url = URL("$backendBaseUrl/register")
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
                    put("grade", grade)
                    put("room", room)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.toast_account_created), Toast.LENGTH_SHORT).show()
                        showLoginLayout()
                        Toast.makeText(this, "Please login with your new credentials.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    runOnUiThread {
                        val errorJson = try { JSONObject(errorResponse) } catch (e: Exception) { null }
                        val errorMessage = errorJson?.optString("message", "Registration failed.") ?: "Registration failed."
                        Log.e("LoginActivity", "Backend registration failed: $responseCode - $errorResponse")
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.e("LoginActivity", "Network error during registration", e)
                    Toast.makeText(this, "Network error during registration: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
