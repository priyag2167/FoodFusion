package com.example.foodfusion.Activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.DBConnection
import com.example.foodfusion.Models.RoomDetails
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityRoomdetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class Roomdetail : AppCompatActivity() {
    private lateinit var binding: ActivityRoomdetailBinding
    private var roomNo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_roomdetail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityRoomdetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, all_room::class.java))
            finish()
        }
       // edit button to edit the Room
        binding.editroom.setOnClickListener {
            updateRoomDetails()
        }
        // Get intent extras
         roomNo = intent.getIntExtra("roomNo", 0)

        // Button to room Delete
        binding.deleteroom.setOnClickListener {
            DeleteRoom(roomNo)
        }

         // Fetch categories from database and set up spinner
            fetchCategoriesAndSetupSpinner()
        // set the feature and status from database and set up spinner
           val feature= arrayOf("Air Conditioner" ,"N/A Air Conditioner")
           val statuses = arrayOf("Available","Maintenance","Unavailable")

        setupSpinner(binding.status, statuses)
        setupSpinner(binding.feature,feature)

           // Fetch room image from the database
              fetchRoomImageFromDatabase(roomNo)
          // Populate the Rooms Details
             fetchRoomDetailsFromDatabase(roomNo)

    }
    // This use to delete the room
    private fun DeleteRoom(roomNo: Int) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            connection = DBConnection.connect()
            val query = "DELETE FROM Rooms WHERE room_no = ?"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setInt(1, roomNo)
            Toast.makeText(this, "Rooms Deleted Successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error:${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            preparedStatement?.close()
            connection?.close()
        }
    }
    // This function use to set the values in Spinner .
    private fun setupSpinner(spinner: Spinner, data: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
    // This function use to fetch the Category in Database and set it.
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
   // This function is use to fetch teh Rooms Details in database
    private fun fetchRoomDetailsFromDatabase(roomNo: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val roomDetails = withContext(Dispatchers.IO) {
                getRoomDetailsFromDB(roomNo)
            }
            if (roomDetails != null) {
                binding.roomno.text = roomNo.toString()
                binding.status.setSelection(getSpinnerIndex(binding.status, roomDetails.status))
                binding.category.setSelection(getSpinnerIndex(binding.category, roomDetails.category))
                binding.feature.setSelection(getSpinnerIndex(binding.feature, roomDetails.features))
                binding.price.setText(roomDetails.price.toString())
                binding.floor.setText(roomDetails.floor.toString())
            } else {
                Toast.makeText(this@Roomdetail, "Failed to fetch room details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRoomDetailsFromDB(roomNo: Int): RoomDetails? {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DBConnection.connect()
            val query = "SELECT status, category, features, room_price, floor_no FROM Rooms WHERE room_no = ?"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setInt(1, roomNo)
            resultSet = preparedStatement?.executeQuery()
            if (resultSet?.next() == true) {
                RoomDetails(
                    status = resultSet.getString("status"),
                    category = resultSet.getString("category"),
                    features = resultSet.getString("features"),
                    price = resultSet.getInt("room_price"),
                    floor = resultSet.getInt("floor_no")
                )
            } else {
                null
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }
    }
    private fun fetchRoomImageFromDatabase(roomNo: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val roomImage = withContext(Dispatchers.IO) {
                getRoomImageFromDB(roomNo)
            }
            if (roomImage != null) {
                // Convert byte array to Bitmap and set it to the ImageView
                val bitmap = BitmapFactory.decodeByteArray(roomImage, 0, roomImage.size)
                binding.roomimage.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this@Roomdetail, "Failed to fetch room image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRoomImageFromDB(roomNo: Int): ByteArray? {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DBConnection.connect()
            val query = "SELECT room_image  FROM Rooms WHERE room_no = ?"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setInt(1, roomNo)
            resultSet = preparedStatement?.executeQuery()
            if (resultSet?.next() == true) {
                resultSet.getBytes("room_image")
            } else {
                null
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }
    }

    private fun updateRoomDetails() {
        val updatedStatus = binding.status.selectedItem.toString()
        val updatedCategory = binding.category.selectedItem.toString()
        val updatedFeatures = binding.feature.selectedItem.toString()
        val updatedPrice = binding.price.text.toString().toInt()
        val updatedFloor = binding.floor.text.toString().toInt()

        CoroutineScope(Dispatchers.IO).launch {
            updateRoomInDB(updatedStatus, updatedCategory, updatedFeatures, updatedPrice, updatedFloor)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Roomdetail, "Room details updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateRoomInDB(status: String, category: String, features: String, price: Int, floor: Int) {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        try {
            connection = DBConnection.connect()
            val query = "UPDATE Rooms SET status = ?, category = ?, features = ?, room_price = ?, floor_no = ? WHERE room_no = ?"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setString(1, status)
            preparedStatement?.setString(2, category)
            preparedStatement?.setString(3, features)
            preparedStatement?.setInt(4, price)
            preparedStatement?.setInt(5, floor)
            preparedStatement?.setInt(6, roomNo)
            preparedStatement?.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            preparedStatement?.close()
            connection?.close()
        }
    }
    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(value, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }
}


