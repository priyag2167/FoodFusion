package com.example.foodfusion.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityStartpageBinding
import com.example.foodfusion.foodActivity.foodlogin

class startpage : AppCompatActivity() {
    private lateinit var binding: ActivityStartpageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_startpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Database connection check
//        val connection = DBConnection.connect()
//        if (connection != null) {
//            Toast.makeText(this, "Database connected Successfully", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(this, "Database connection failed", Toast.LENGTH_SHORT).show()
//        }
        binding=ActivityStartpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.hotelhaven.setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }
        binding.foodfusion.setOnClickListener {
            startActivity(Intent(this,foodlogin::class.java))
            finish()
        }
    }
}