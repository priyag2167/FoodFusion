package com.example.foodfusion.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityAddcategoryBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement

class addcategory : AppCompatActivity() {
    private lateinit var binding: ActivityAddcategoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_addcategory)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityAddcategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* this button to Redirected to Add page */
        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, addbtn::class.java))
            finish()
        }
        /* this button is store the category in database */
        binding.addcategory.setOnClickListener {
            val categoryName = binding.categoryname.text.toString()
            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Fill the category name", Toast.LENGTH_SHORT).show()
            } else {
                AddCategory(categoryName)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun AddCategory(categoryName: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val connection = DBConnection.connect()
            if (connection != null) {
                try {
                    val query = "INSERT INTO Categories (category_name) VALUES (?)"
                    val statement: PreparedStatement = connection.prepareStatement(query)
                    statement.setString(1, categoryName)
                    statement.executeUpdate()
                    connection.close()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@addcategory, "Category added successfully", Toast.LENGTH_SHORT).show()
                        // Refresh the activity
                        refreshActivity()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@addcategory, "error:${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@addcategory, "Database connection not established", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun refreshActivity() {
        finish()
        startActivity(intent)
    }
}
