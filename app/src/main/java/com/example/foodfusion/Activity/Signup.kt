package com.example.foodfusion.Activity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivitySignupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

class signup : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // Allow network on main thread (not recommended for production)
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//
//        // Database connection check
//        CoroutineScope(Dispatchers.Main).launch {
//            val connection = DBConnection.connect()
//            if (connection != null) {
//                Toast.makeText(this@signup, "Database connected Successfully", Toast.LENGTH_LONG)
//                    .show()
//            } else {
//                Toast.makeText(this@signup, "Database connection failed", Toast.LENGTH_SHORT).show()
//            }
//        }


        // binding for signup page for redirected to login page
        binding.login.setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }
        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }
        // click listener for signup button for admin details are save
        binding.signupbtn.setOnClickListener {
            val Name = binding.adminname.text.toString()
            val Email = binding.userid.text.toString()
            val Pswd = binding.password.text.toString()
            if (Name.isEmpty() || Email.isEmpty() || Pswd.isEmpty()) {
                Toast.makeText(this, "Please Fill all Fields", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    if (insertAdminDetails(Name, Email,Pswd)) {
                        Toast.makeText(this@signup, "Admin created successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@signup, login::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@signup, "Failed to create only one admin exist", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun insertAdminDetails(Name: String, Email: String, Pswd: String): Boolean {
        var isInserted = false
        CoroutineScope(Dispatchers.IO).launch {
            val connection = DBConnection.connect()
            if (connection != null) {
                try {
                    val query = "INSERT INTO HotelUser (Name,Email,Pswd) VALUES (?, ?, ?)"
                    val statement: PreparedStatement = connection.prepareStatement(query)
                    statement.setString(1, Name)
                    statement.setString(2, Email)
                    statement.setString(3, Pswd)
                    statement.executeUpdate()
                    statement.close()
                    connection.close()
                    isInserted = true
                } catch (e: SQLException) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@signup, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@signup, "Failed to establish a database connection", Toast.LENGTH_SHORT).show()
                }
            }
            withContext(Dispatchers.Main) {
                if (isInserted) {
                    Toast.makeText(this@signup, "Admin created successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@signup, login::class.java))
                    finish()
                } else {
                    Toast.makeText(this@signup, "Failed to create admin. Only one admin can exist.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return isInserted
    }
}