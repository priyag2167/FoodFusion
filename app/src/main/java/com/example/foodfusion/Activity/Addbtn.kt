package com.example.foodfusion.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityAddbtnBinding

class addbtn : AppCompatActivity() {
    private lateinit var binding: ActivityAddbtnBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_addbtn)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding=ActivityAddbtnBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cancelbtn.setOnClickListener{
            startActivity(Intent(this,Home::class.java))
            finish()
        }
         binding.roomadd.setOnClickListener {
             startActivity(Intent(this,addroom::class.java))
             finish()
         }
        binding.categoryadd.setOnClickListener {
            startActivity(Intent(this,addcategory::class.java))
            finish()
        }
        binding.bookingadd.setOnClickListener {
            startActivity(Intent(this,addbooking::class.java))
            finish()
        }
    }
}