package com.example.foodfusion.foodActivity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.Activity.startpage
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityFoodloginBinding
import com.example.foodfusion.utils.SharedPreferencesHelper
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class foodlogin : AppCompatActivity() {
    private lateinit var binding: ActivityFoodloginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFoodloginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if user is already logged in
        val userRole = SharedPreferencesHelper.getUserRole(this)
        if (userRole != null) {
            // User is already logged in, navigate to foodhome
            startActivity(Intent(this, foodhome::class.java))
            finish()
            return
        }

        // Redirect to the FoodSignup page
        binding.signup.setOnClickListener {
            startActivity(Intent(this, foodsignup::class.java))
            finish()
        }
        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, startpage::class.java))
            finish()
        }
        binding.loginbtn.setOnClickListener {
            val email = binding.useremail.text.toString()
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
            if (result.first) {
                SharedPreferencesHelper.saveUser(this@foodlogin, email, result.second!!)
                Toast.makeText(this@foodlogin, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@foodlogin, foodhome::class.java))
                finish()
            } else {
                Toast.makeText(this@foodlogin, "Login failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateUser(email: String, password: String): Pair<Boolean, String?> {
        val connection = DBConnection.connect()
        if (connection != null) {
            try {
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

                query = "SELECT * FROM Foodlogin WHERE Email = ? AND Pswd = ? COLLATE Latin1_General_CS_AS"
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
