package com.example.foodfusion.Activity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityLoginBinding
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if user is already logged in
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            // Redirect to homepage if user is already logged in
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signup.setOnClickListener {
            startActivity(Intent(this, signup::class.java))
            finish()
        }

        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, startpage::class.java))
            finish()
        }

        binding.loginbtn.setOnClickListener {
            val email = binding.userid.text.toString()
            val password = binding.password.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ValidateUserTask(email, password).execute()
        }
    }

    private inner class ValidateUserTask(private val email: String, private val password: String) : AsyncTask<Void, Void, Pair<Boolean, String?>>() {
        override fun doInBackground(vararg params: Void?): Pair<Boolean, String?> {
            return validateUser(email, password)
        }

        override fun onPostExecute(result: Pair<Boolean, String?>) {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            if (result.first) {
                // Save login state and role
                with(sharedPref.edit()) {
                    putBoolean("isLoggedIn", true)
                    putString("userEmail", email)
                    putString("userPassword", password)
                    putString("userRole", result.second)
                    apply()
                }
                Toast.makeText(this@login, "Login successful", Toast.LENGTH_SHORT).show()
                if (result.second == "admin") {
                    startActivity(Intent(this@login, Home::class.java))
                } else {
                    startActivity(Intent(this@login, Home::class.java))
                }
                finish()
            } else {
                Toast.makeText(this@login, "Login failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateUser(email: String, password: String): Pair<Boolean, String?> {
        val connection = DBConnection.connect()
        if (connection != null) {
            try {
                // Check Admin table first
                var query = "SELECT * FROM Admin WHERE AdminEmail = ? AND AdminPswd = ? COLLATE Latin1_General_CS_AS"
                var statement: PreparedStatement = connection.prepareStatement(query)
                statement.setString(1, email)
                statement.setString(2, password)
                var resultSet: ResultSet = statement.executeQuery()

                if (resultSet.next()) {
                    resultSet.close()
                    statement.close()
                    connection.close()
                    return Pair(true, "admin")
                }

                // Check HotelUser table
                query = "SELECT * FROM HotelUser WHERE Email = ? AND Pswd = ? COLLATE Latin1_General_CS_AS"
                statement = connection.prepareStatement(query)
                statement.setString(1, email)
                statement.setString(2, password)
                resultSet = statement.executeQuery()

                val userExists = resultSet.next()
                resultSet.close()
                statement.close()
                connection.close()

                return Pair(userExists, if (userExists) "user" else null)
            } catch (e: SQLException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "Failed to establish a database connection", Toast.LENGTH_SHORT).show()
            }
        }
        return Pair(false, null)
    }
}
