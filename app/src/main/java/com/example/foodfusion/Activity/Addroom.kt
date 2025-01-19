package com.example.foodfusion.Activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityAddroomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class addroom : AppCompatActivity() {
    private lateinit var binding: ActivityAddroomBinding
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_addroom)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityAddroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, all_room::class.java))
            finish()
        }

        binding.roomimage.setOnClickListener {
            openImageSelector()
        }

        binding.addroom.setOnClickListener {
            saveRoomToDatabase()
        }

        // Fetch categories from database and set up spinner
           fetchCategoriesAndSetupSpinner()
        // set the feature spinner and status spinner
           val feature= arrayOf("Air Conditioner" ,"N/A Air Conditioner")
           val statuses = arrayOf("Available")

          setupSpinner(binding.status, statuses)
          setupSpinner(binding.feature,feature)
    }

    private fun fetchCategoriesAndSetupSpinner() {
        CoroutineScope(Dispatchers.Main).launch {
            val categories = withContext(Dispatchers.IO) {
                fetchCategoriesFromDatabase()
            }
            setupSpinner(binding.category, categories)
        }
    }

    private fun fetchCategoriesFromDatabase(): Array<String> {
        val categories = mutableListOf<String>()
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            connection = DBConnection.connect()
            val query = "SELECT category_name FROM Categories"
            preparedStatement = connection?.prepareStatement(query)
            resultSet = preparedStatement?.executeQuery()

            while (resultSet?.next() == true) {
                categories.add(resultSet.getString("category_name"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }

        return categories.toTypedArray()
    }

    private fun setupSpinner(spinner: Spinner, data: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun openImageSelector() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            binding.roomimage.setImageURI(selectedImageUri)
        }
    }



    private fun saveRoomToDatabase() {
        val roomNo = binding.roomno.text.toString().toIntOrNull()
        val roomCategory = binding.category.selectedItem.toString()
        val roomStatus = binding.status.selectedItem.toString()
        val floorNo = binding.floorno.text.toString().toIntOrNull()
        val roomPrice = binding.roomprice.text.toString().toIntOrNull()
        val roomImage = (binding.roomimage.drawable as? BitmapDrawable)?.bitmap
        val roomFeatures = binding.feature.selectedItem.toString()

        if (roomNo == null || floorNo == null || roomPrice == null || roomImage == null) {
            // Handle the error, show a message to the user
            Toast.makeText(this, "Fill all the Field", Toast.LENGTH_SHORT).show()
            return
        }

        val roomImageBytes = getImageBytes(roomImage)

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                saveRoom(roomNo, roomCategory, roomFeatures, roomStatus, floorNo, roomPrice, roomImageBytes)
            }
            if (result) {
                // Successfully saved the room, you can show a success message to the user
                Toast.makeText(this@addroom, "Room Added Successfully", Toast.LENGTH_SHORT).show()
                // Refresh the activity
                   refreshActivity()
            } else {
                // Failed to save the room, you can show an error message to the user
                Toast.makeText(this@addroom, "Failed to Add Room", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getImageBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun saveRoom(roomNo: Int, roomCategory: String, roomFeatures: String, roomStatus: String, floorNo: Int, roomPrice: Int, roomImageBytes: ByteArray): Boolean {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        return try {
            connection = DBConnection.connect()
            val query = "INSERT INTO Rooms (room_no, category, features, status, floor_no, room_price, room_image) VALUES (?, ?, ?, ?, ?, ?, ?)"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setInt(1, roomNo)
            preparedStatement?.setString(2, roomCategory)
            preparedStatement?.setString(3, roomFeatures)
            preparedStatement?.setString(4, roomStatus)
            preparedStatement?.setInt(5, floorNo)
            preparedStatement?.setInt(6, roomPrice)
            preparedStatement?.setBytes(7, roomImageBytes)
            (preparedStatement?.executeUpdate() ?: 0) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this@addroom, "Failed:${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        } finally {
            preparedStatement?.close()
            connection?.close()
        }
    }

    private fun refreshActivity() {
        finish()
        startActivity(intent)
    }
}
