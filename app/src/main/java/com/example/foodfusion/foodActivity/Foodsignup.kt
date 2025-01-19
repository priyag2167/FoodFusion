package com.example.foodfusion.foodActivity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityFoodsignupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.SQLException

class foodsignup : AppCompatActivity() {
    private lateinit var binding: ActivityFoodsignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_foodsignup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityFoodsignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, foodlogin::class.java))
            finish()
        }
        binding.signupbtn.setOnClickListener {
            val name = binding.username.text.toString()
            val email = binding.useremail.text.toString()
            val password = binding.password.text.toString()
            val phone = binding.phone.text.toString()
            val address = binding.address.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val result = InsertLoginDetails(name, email, password,phone,address)
                    if (result) {
                        Toast.makeText(this@foodsignup, "User created successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@foodsignup, foodlogin::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@foodsignup, "Failed to create user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun InsertLoginDetails(name: String, email: String, password: String , phone: String, address: String): Boolean {
        return withContext(Dispatchers.IO) {
            val connect = DBConnection.connect()
            if (connect != null) {
                try {
                    val query = "INSERT INTO Foodlogin (Name, Email, Pswd , phone , address) VALUES (?, ?, ?, ?, ?)"
                    val statement: PreparedStatement = connect.prepareStatement(query)
                    statement.setString(1, name)
                    statement.setString(2, email)
                    statement.setString(3, password)
                    statement.setString(4,phone)
                    statement.setString(5,address)
                    statement.executeUpdate()
                    statement.close()
                    connect.close()
                    return@withContext true
                } catch (e: SQLException) {
                    e.printStackTrace()
                    runOnUiThread { Toast.makeText(this@foodsignup, "Database error: ${e.message}", Toast.LENGTH_LONG).show() }
                }
            }
            return@withContext false
        }
    }
}
