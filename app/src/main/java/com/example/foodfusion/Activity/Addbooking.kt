package com.example.foodfusion.Activity

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodfusion.DBConnection
import com.example.foodfusion.R
import com.example.foodfusion.databinding.ActivityAddbookingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.Calendar
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import java.text.SimpleDateFormat

class addbooking : AppCompatActivity() {
    private lateinit var binding: ActivityAddbookingBinding
    private lateinit var dateEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_addbooking)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityAddbookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dateEditText = binding.date
        binding.date.setOnClickListener {
            showDatePickerDialog()
        }
        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, all_booking::class.java))
            finish()
        }
        // this button is used to store the data in database
        binding.checkin.setOnClickListener {
            saveBookingToDatabase()
        }

        // Fetch categories from database and set up spinner
        fetchCategoriesAndSetupSpinner()

        val feature = arrayOf("Air Conditioner", "N/A Air Conditioner")
        val bookingStatus = arrayOf("Paid")
        setupSpinner(binding.roomfeature, feature)
        setupSpinner(binding.bookingstatus, bookingStatus)

        binding.category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchAndSetupRoomNumbers()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.roomfeature.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchAndSetupRoomNumbers()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.roomno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchAndSetPrice()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun saveBookingToDatabase() {
        val name = binding.name.text?.toString()
        val email = binding.email.text?.toString()
        val phone = binding.phone.text?.toString()
        val category = binding.category.selectedItem?.toString()
        val roomNo = binding.roomno.selectedItem?.toString()?.toIntOrNull()
        val numberOfDay = binding.noofdays.text?.toString()?.toIntOrNull()
        val date = binding.date.text?.toString()
        val roomPrice = binding.price.text?.toString()?.toIntOrNull()

        // Debug log to print the values
        println("Name: $name, Email: $email, Phone: $phone, Category: $category, RoomNo: $roomNo, NumberOfDay: $numberOfDay, Date: $date, RoomPrice: $roomPrice")

        // Check for null values and show error message if any field is null
        if (name.isNullOrEmpty() || email.isNullOrEmpty() || phone.isNullOrEmpty() || category.isNullOrEmpty() || roomNo == null || numberOfDay == null || date.isNullOrEmpty() || roomPrice == null) {
            // Handle the error, show a message to the user
            Toast.makeText(this, "Fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                saveBooking(name, email, phone, category, roomNo, numberOfDay, date, roomPrice)
            }
            if (result) {
                // Successfully saved the room, you can show a success message to the user
                   Toast.makeText(this@addbooking, "Booked Successfully", Toast.LENGTH_SHORT).show()
                // Refresh the activity
                   refreshActivity()
            } else {
                // Failed to save the room, you can show an error message to the user
                   Toast.makeText(this@addbooking, "Failed to Add Booking", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private  fun saveBooking(name: String, email: String, phone: String, category: String, roomNo: Int, numberOfDay: Int, date: String, roomPrice: Int): Boolean {
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var updateStatement: PreparedStatement? = null
        return try {
            connection = DBConnection.connect()

            // Parse the date string to java.sql.Date
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            val parsedDate = dateFormat.parse(date)
            val sqlDate = java.sql.Date(parsedDate.time)

            // Calculate the checkout date based on numberOfDays
            val calendar = Calendar.getInstance()
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
            calendar.add(Calendar.DAY_OF_YEAR, numberOfDay)
            val checkoutDate = java.sql.Date(calendar.timeInMillis)
             val checkoutstatus="Ongoing"

            // Insert booking record
            val insertQuery = "INSERT INTO Booking (name, email, phone, room_type, room_no, no_of_days, date_arrival, checkout_date, room_price, booking_status,checkout_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            preparedStatement = connection?.prepareStatement(insertQuery)
            preparedStatement?.setString(1, name)
            preparedStatement?.setString(2, email)
            preparedStatement?.setString(3, phone)
            preparedStatement?.setString(4, category)
            preparedStatement?.setInt(5, roomNo)
            preparedStatement?.setInt(6, numberOfDay)
            preparedStatement?.setDate(7, sqlDate)
            preparedStatement?.setDate(8, checkoutDate)
            preparedStatement?.setInt(9, (roomPrice*numberOfDay))
            preparedStatement?.setString(10, binding.bookingstatus.selectedItem.toString())
            preparedStatement?.setString(11, checkoutstatus)


            val rowsInserted = preparedStatement?.executeUpdate() ?: 0

            if (rowsInserted > 0) {
                // Update room status to 'Unavailable'
                val updateQuery = "UPDATE Rooms SET status = 'Unavailable' WHERE room_no = ?"
                updateStatement = connection?.prepareStatement(updateQuery)
                updateStatement?.setInt(1, roomNo)

                (updateStatement?.executeUpdate() ?: 0) > 0
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Failed:${e.message}", Toast.LENGTH_SHORT).show()
            }
            false
        } finally {
            preparedStatement?.close()
            updateStatement?.close()
            connection?.close()
        }
    }




    private fun fetchCategoriesAndSetupSpinner() {
        CoroutineScope(Dispatchers.Main).launch {
            val categories = withContext(Dispatchers.IO) {
                fetchCategoriesFromDatabase()
            }
            setupSpinner(binding.category, categories)
        }
    }

    private  fun fetchCategoriesFromDatabase(): Array<String> {
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

    private fun fetchAndSetupRoomNumbers() {
        val selectedCategory = binding.category.selectedItem?.toString()
        val selectedFeature = binding.roomfeature.selectedItem?.toString()

        if (selectedCategory != null && selectedFeature != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val roomNumbers = withContext(Dispatchers.IO) {
                    fetchRoomNumbersFromDatabase(selectedCategory, selectedFeature)
                }
                setupSpinner(binding.roomno, roomNumbers)
            }
        }
    }

    private fun fetchRoomNumbersFromDatabase(category: String, feature: String): Array<String> {
        val roomNumbers = mutableListOf<String>()
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            connection = DBConnection.connect()
            val query = "SELECT room_no FROM Rooms WHERE category = ? AND features = ? AND status ='Available'"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setString(1, category)
            preparedStatement?.setString(2, feature)
            resultSet = preparedStatement?.executeQuery()

            while (resultSet?.next() == true) {
                roomNumbers.add(resultSet.getString("room_no"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }

        return roomNumbers.toTypedArray()
    }

    private fun fetchAndSetPrice() {
        val selectedRoomNo = binding.roomno.selectedItem?.toString()

        if (selectedRoomNo != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val priceAndImage = withContext(Dispatchers.IO) {
                    fetchPriceAndImageFromDatabase(selectedRoomNo)
                }
                if (priceAndImage.first.isNotEmpty()) {
                    binding.price.setText(priceAndImage.first)
                } else {
                    binding.price.setText("")
                }

                if (priceAndImage.second != null) {
                    binding.roomimage.setImageBitmap(priceAndImage.second)
                } else {
                    binding.roomimage.setImageResource(R.drawable.demorooom) // Default image if no image is found
                }
            }
        }
    }


    private fun fetchPriceAndImageFromDatabase(roomNo: String): Pair<String, Bitmap?> {
        var price = ""
        var image: Bitmap? = null
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            connection = DBConnection.connect()
            val query = "SELECT room_price, room_image FROM Rooms WHERE room_no = ?"
            preparedStatement = connection?.prepareStatement(query)
            preparedStatement?.setString(1, roomNo)
            resultSet = preparedStatement?.executeQuery()

            if (resultSet?.next() == true) {
                price = resultSet.getString("room_price")
                val blob = resultSet.getBytes("room_image")
                if (blob != null) {
                    image = BitmapFactory.decodeByteArray(blob, 0, blob.size)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        }

        return Pair(price, image)
    }


    // calendar function to select the date
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dateEditText.setText(selectedDate)
            },
            year, month, day
        )

        // Set the minimum date to the current date
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    private fun refreshActivity() {
        finish()
        startActivity(intent)
    }
}
